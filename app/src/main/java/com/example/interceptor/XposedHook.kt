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
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.android.ApplicationClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import org.json.JSONObject
import java.io.File

@InjectYukiHookWithXposed
class XposedHook : IYukiHookXposedInit {

    private fun getPrefsFile(): File? {
        val paths = listOf(
            "/data/user/0/com.example.interceptor/shared_prefs/interceptor_prefs.xml",
            "/data/data/com.example.interceptor/shared_prefs/interceptor_prefs.xml"
        )
        for (path in paths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                return file
            }
        }
        return null
    }

    private fun readPrefs(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val file = getPrefsFile() ?: return result
        try {
            val content = file.readText()
            "<boolean name=\"([^\"]+)\" value=\"([^\"]+)\" />".toRegex()
                .findAll(content).forEach { result[it.groupValues[1]] = it.groupValues[2] }
            "<float name=\"([^\"]+)\" value=\"([^\"]+)\" />".toRegex()
                .findAll(content).forEach { result[it.groupValues[1]] = it.groupValues[2] }
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
                nm.createNotificationChannel(NotificationChannel(channelId, "Interceptor", NotificationManager.IMPORTANCE_HIGH))
            }
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            nm.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e("XposedHook", "Notify error: ${e.message}")
        }
    }

    private fun parseAndNotify(json: String, context: Context) {
        try {
            val obj = JSONObject(json)
            val payload = obj.optJSONObject("payload") ?: obj
            val id = payload.optString("id", "")
            val pickupKm = payload.optDouble("pickupDistanceInKm", 999.0)
            val deliveryKm = payload.optDouble("deliveryDistanceInKm", 999.0)
            
            if (id.isEmpty()) return
            
            val prefs = readPrefs()
            val masterOn = prefs["master_switch"]?.toBoolean() ?: false
            val maxPickup = prefs["max_pickup_distance"]?.toFloat() ?: 5.0f
            val maxDelivery = prefs["max_delivery_distance"]?.toFloat() ?: 10.0f
            
            if (!masterOn) return
            
            if (pickupKm <= maxPickup && deliveryKm <= maxDelivery) {
                showNotification(context, "Order Accepted!", "ID:$id P:${pickupKm}km D:${deliveryKm}km")
            }
        } catch (e: Exception) {
            Log.e("XposedHook", "Parse error: ${e.message}")
        }
    }

    override fun onHook() = encase {
        loadApp("delivery.samurai.android") {
            
            // Application onCreate হুক
            ApplicationClass.hook {
                injectMember {
                    method { name = "onCreate" }
                    afterHook {
                        val app = instance<Application>()
                        Log.i("XposedHook", "Samurai started")
                    }
                }
            }
            
            // OkHttp ResponseBody.string() হুক
            findClass("okhttp3.ResponseBody") {
                injectMember {
                    method { name = "string" }
                    afterHook {
                        val body = result as? String ?: return@afterHook
                        if (body.contains("pickupDistanceInKm")) {
                            parseAndNotify(body, appContext)
                        }
                    }
                }
            }
        }
    }
}
