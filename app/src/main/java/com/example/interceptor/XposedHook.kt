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
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileReader

@InjectYukiHookWithXposed
class XposedHook : IYukiHookXposedInit {

    private fun readPrefsFromXml(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            val prefsFile = File("/data/data/com.example.interceptor/shared_prefs/interceptor_prefs.xml")
            if (!prefsFile.exists()) return result

            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(FileReader(prefsFile))

            var key = ""
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "boolean") {
                    val name = parser.getAttributeValue(null, "name") ?: ""
                    val value = parser.getAttributeValue(null, "value") ?: "false"
                    result[name] = value
                } else if (eventType == XmlPullParser.START_TAG && parser.name == "float") {
                    val name = parser.getAttributeValue(null, "name") ?: ""
                    val value = parser.getAttributeValue(null, "value") ?: "0.0"
                    result[name] = value
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("XposedHook", "Error reading prefs: ${e.message}")
        }
        return result
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return readPrefsFromXml()[key]?.toBoolean() ?: defaultValue
    }

    private fun showNotification(context: Context, title: String, message: String) {
        try {
            val channelId = "network_interceptor"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Network Interceptor", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e("XposedHook", "Notification error: ${e.message}")
        }
    }

    override fun onHook() = encase {
        ApplicationClass.hook {
            injectMember {
                method {
                    name = "onCreate"
                }
                afterHook {
                    val app = instance<Application>()
                    val context = app.applicationContext
                    
                    Log.i("XposedHook", "Hooked: ${app.packageName}")
                    
                    // Check if master switch is ON
                    val masterOn = getBoolean("master_switch", false)
                    
                    if (masterOn) {
                        val pickup = readPrefsFromXml()["max_pickup_distance"] ?: "5.0"
                        showNotification(context, "Interceptor Active", "Max pickup: ${pickup}km")
                    }
                }
            }
        }
    }
}
