package com.example.interceptor

import android.app.Application
import android.util.Log
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.android.ApplicationClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XSharedPreferences

@InjectYukiHookWithXposed
class XposedHook : IYukiHookXposedInit {

    override fun onHook() = encase {
        ApplicationClass.hook {
            injectMember {
                method {
                    name = "onCreate"
                }
                afterHook {
                    val app = instance<Application>()
                    Log.i("XposedHook", "Hooked: ${app.packageName}")
                    
                    // Use YukiHookAPI's built-in prefs
                    val prefs = YukiHookAPI.modulePrefs
                    val master = prefs.getBoolean("master_switch", false)
                    
                    Log.i("XposedHook", "Master switch: $master")
                }
            }
        }
    }
}
