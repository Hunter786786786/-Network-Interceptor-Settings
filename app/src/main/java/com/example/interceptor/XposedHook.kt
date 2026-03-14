package com.example.interceptor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONObject
import java.io.File

class XposedHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // শুধু Samurai অ্যাপে কাজ করুন
        if (lpparam.packageName != "delivery.samurai.android") return
        
        Log.i("XposedHook", "Samurai app loaded")
        
        // Application onCreate হুক
        XposedHelpers.findAndHookMethod(
            Application::class.java,
            "onCreate",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val app = param.thisObject as Application
                    Log.i("XposedHook", "Samurai started")
                    
                    // OkHttp হুক করুন
                    hookOkHttp(app, lpparam.classLoader)
                }
            }
        )
    }
    
    private fun hookOkHttp(app: Application, classLoader: ClassLoader) {
        try {
            // ResponseBody.string() হুক
            XposedHelpers.findAndHookMethod(
                "okhttp3.ResponseBody",
                classLoader,
                "string",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val body = param.result as? String ?: return
                        
                        // অর্ডার ডেটা চেক
                        if (body.contains("pickupDistanceInKm") || body.contains("order")) {
                            Log.i("XposedHook", "Order response detected")
                            parseAndNotify(body, app.applicationContext)
                        }
                    }
                }
            )
            Log.i("XposedHook", "OkHttp hooked successfully")
        } catch (e: Exception) {
            Log.e("XposedHook", "OkHttp hook failed: ${e.message}")
        }
    }
    
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
            Log.i("XposedHook", "Notification shown: $title")
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
            
            if (id.isEmpty()) {
                Log.w("XposedHook", "Empty order ID")
                return
            }
            
            Log.i("XposedHook", "Order: $id, Pickup: $pickupKm, Delivery: $deliveryKm")
            
            val prefs = readPrefs()
            val masterOn = prefs["master_switch"]?.toBoolean() ?: false
            val maxPickup = prefs["max_pickup_distance"]?.toFloat() ?: 5.0f
            val maxDelivery = prefs["max_delivery_distance"]?.toFloat() ?: 10.0f
            
            Log.i("XposedHook", "Settings: Master=$masterOn, MaxPickup=$maxPickup, MaxDelivery=$maxDelivery")
            
            if (!masterOn) {
                Log.i("XposedHook", "Master switch OFF")
                return
            }
            
            if (pickupKm <= maxPickup && deliveryKm <= maxDelivery) {
                Log.i("XposedHook", "✅ MATCH! Auto-accepting order")
                showNotification(context, "Order Accepted!", "ID:$id | P:${pickupKm}km | D:${deliveryKm}km")
                // TODO: এখানে অটো এক্সেপ্ট API কল করুন
            } else {
                Log.i("XposedHook", "❌ Too far - Pickup:$pickupKm > $maxPickup or Delivery:$deliveryKm > $maxDelivery")
            }
        } catch (e: Exception) {
            Log.e("XposedHook", "Parse error: ${e.message}")
        }
    }
}
