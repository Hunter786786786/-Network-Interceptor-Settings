package com.example.interceptor

import android.app.Application
import android.util.Log
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
                if (eventType == XmlPullParser.START_TAG && parser.name == "string") {
                    key = parser.getAttributeValue(null, "name") ?: ""
                } else if (eventType == XmlPullParser.TEXT && key.isNotEmpty()) {
                    result[key] = parser.text
                    key = ""
                } else if (eventType == XmlPullParser.START_TAG && parser.name == "boolean") {
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

    private fun getFloat(key: String, defaultValue: Float): Float {
        return readPrefsFromXml()[key]?.toFloat() ?: defaultValue
    }

    override fun onHook() = encase {
        ApplicationClass.hook {
            injectMember {
                method {
                    name = "onCreate"
                }
                afterHook {
                    val app = instance<Application>()
                    Log.i("XposedHook", "Hooked: ${app.packageName}")
                    
                    val master = getBoolean("master_switch", false)
                    val pickup = getFloat("max_pickup_distance", 5.0f)
                    Log.i("XposedHook", "Prefs loaded - Master: $master, Pickup: $pickup")
                }
            }
        }
    }
}
