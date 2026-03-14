package com.example.interceptor

import android.app.Application
import android.util.Log
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.config
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.android.ApplicationClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XSharedPreferences
import org.json.JSONObject

@InjectYukiHookWithXposed
class XposedHook : IYukiHookXposedInit {

    private lateinit var prefs: XSharedPreferences

    override fun onInit() = YukiHookAPI.config {
        isDebug = false
    }

    override fun onHook() = encase {
        prefs = XSharedPreferences("com.example.interceptor", "interceptor_prefs")
        
        ApplicationClass.hook {
            injectMember {
                method {
                    name = "onCreate"
                }
                afterHook {
                    val app = instance<Application>()
                    Log.i("XposedHook", "Initialized in: ${app.packageName}")
                }
            }
        }
    }
}
