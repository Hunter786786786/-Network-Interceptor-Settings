package com.example.interceptor

import android.app.Application
import android.util.Log
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.android.ApplicationClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import org.json.JSONObject
import java.io.File

@InjectYukiHookWithXposed
class XposedHook : IYukiHookXposedInit {

    // FIX: File দিয়ে preference পড়ুন
    private fun getPrefsFile(packageName: String): File? {
        val possiblePaths = listOf(
            "/data/data/$packageName/shared_prefs/interceptor_prefs.xml",
            "/data/user/0/$packageName/shared_prefs/interceptor_prefs.xml"
        )
        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                return file
            }
        }
        return null
    }

    // FIX: XML থেকে value পড়ুন
    private fun getBooleanFromPrefs(key: String, defaultValue: Boolean): Boolean {
        return try {
            val file = getPrefsFile("com.example.interceptor")
            if (file != null) {
                val content = file.readText()
                // Simple XML parsing for boolean
                val pattern = "<$key[^>]*>([^<]+)</$key>".toRegex()
                val match = pattern.find(content)
                match?.groupValues?.get(1)?.toBoolean() ?: defaultValue
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun getFloatFromPrefs(key: String, defaultValue: Float): Float {
        return try {
            val file = getPrefsFile("com.example.interceptor")
            if (file != null) {
                val content = file.readText()
                val pattern = "<$key[^>]*>([^<]+)</$key>".toRegex()
                val match = pattern.find(content)
                match?.groupValues?.get(1)?.toFloat() ?: defaultValue
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
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
                    Log.i("XposedHook", "Initialized in: ${app.packageName}")
                    
                    // Test: Preference পড়ুন
                    val masterSwitch = getBooleanFromPrefs("master_switch", false)
                    val maxPickup = getFloatFromPrefs("max_pickup_distance", 5.0f)
                    Log.i("XposedHook", "Master: $masterSwitch, MaxPickup: $maxPickup")
                }
            }
        }
    }
}
