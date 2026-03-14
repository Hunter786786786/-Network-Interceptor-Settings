package com.example.interceptor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.config
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import org.json.JSONObject
import java.io.File

@InjectYukiHookWithXposed
class XposedHook : IYukiHookXposedInit {

    override fun onInit() = YukiHookAPI.config {
        isDebug = true
    }

    private fun getPrefsFile(): File? {
        val paths = listOf(
            "/data/user/0/com.example.interceptor/shared_prefs/interceptor_prefs.xml",
            "/data/data/com.example.interceptor/shared_prefs/interceptor_prefs.xml"
        )
        for (path in paths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                Log.i("XposedHook", "Prefs found: $path")
                return file
            }
        }
        Log.w("XposedHook", "Prefs not found")
        return null
    }

    private fun readPrefs(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val file = getPrefsFile() ?: return result
        
        try {
            val content = file.readText()
            // boolean
            "<boolean name=\"([^\"]+)\" value=\"([^\"]+)\" />".toRegex()
                .findAll(content).forEach { 
                    result[it.groupValues[1]] = it.groupValues[2] 
                }
            // float
            "<float name=\"([^\"]+)\" value=\"([^\"]+)\" />".toRegex()
                .findAll(content).forEach { 
                    result[it.groupValues[1]] = it.groupValues[2] 
                }
        } catch (e: Exception) {
            Log.e("XposedHook", "Read error: ${e.message}")
        }
        return result
    }

    private fun showNotification(context: Context, title: String, message: String) {
        try {
            val channelId = "interceptor"
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nm.createNotificationChannel(
                    NotificationChannel(channelId, "Interceptor", NotificationManager.IMPORTANCE_HIGH)
                )
            }
            
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            
            nm.notify(System.currentTimeMillis().toInt(), notification)
            Log.i("XposedHook", "Notification shown: $title")
        } catch (e: Exception) {
            Log.e("XposedHook", "Notification error: ${e.message}")
        }
    }

    override fun onHook() = encase {
        // শুধু Samurai অ্যাপে হুক
        loadApp("delivery.samurai.android") {
            Log.i("XposedHook", "Samurai app loaded")
            
            // OkHttp Response হুক করুন
            findClass("okhttp3.Response").hook {
                injectMember {
                    method {
                        name = "body"
                    }
                    afterHook {
                        val response = instance
                        Log.i("XposedHook", "Response intercepted")
                        
                        // বডি পড়ুন
                        try {
                            val body = response?.javaClass?.getMethod("body")?.invoke(response)
                            val source = body?.javaClass?.getMethod("source")?.invoke(body)
                            val buffer = source?.javaClass?.getMethod("buffer")?.invoke(source)
                            val clone = buffer?.javaClass?.getMethod("clone")?.invoke(buffer)
                            val content = clone?.javaClass?.getMethod("readUtf8")?.invoke(clone) as? String
                            
                            content?.let { parseOrder(it, appContext) }
                        } catch (e: Exception) {
                            Log.e("XposedHook", "Body read error: ${e.message}")
                        }
                    }
                }
            }
        }
    }
    
    private fun parseOrder(json: String, context: Context) {
        try {
            val obj = JSONObject(json)
            
            // অর্ডার ডেটা খুঁজুন
            val payload = obj.optJSONObject("payload") ?: obj
            val id = payload.optString("id", "")
            val pickupKm = payload.optDouble("pickupDistanceInKm", 999.0)
            val deliveryKm = payload.optDouble("deliveryDistanceInKm", 999.0)
            
            if (id.isEmpty()) return
            
            Log.i("XposedHook", "Order: $id, Pickup: $pickupKm, Delivery: $deliveryKm")
            
            // Preference পড়ুন
            val prefs = readPrefs()
            val masterOn = prefs["master_switch"]?.toBoolean() ?: false
            val maxPickup = prefs["max_pickup_distance"]?.toFloat() ?: 5.0f
            val maxDelivery = prefs["max_delivery_distance"]?.toFloat() ?: 10.0f
            
            if (!masterOn) {
                Log.i("XposedHook", "Master OFF")
                return
            }
            
            // চেক করুন
            if (pickupKm <= maxPickup && deliveryKm <= maxDelivery) {
                Log.i("XposedHook", "✅ MATCH! Auto-accepting...")
                
                // নোটিফিকেশন দেখান
                showNotification(
                    context,
                    "Order Accepted!",
                    "ID: $id | Pickup: ${pickupKm}km | Delivery: ${deliveryKm}km"
                )
                
                // TODO: এখানে অটো এক্সেপ্ট API কল করুন
                // autoAcceptOrder(id)
            } else {
                Log.i("XposedHook", "❌ Too far")
            }
            
        } catch (e: Exception) {
            Log.e("XposedHook", "Parse error: ${e.message}")
        }
    }
}
