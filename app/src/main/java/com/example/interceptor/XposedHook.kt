package com.example.interceptor

import android.app.Application
import android.util.Log
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.android.ApplicationClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

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
                    Log.i("XposedHook", "Initialized in: ${app.packageName}")
                }
            }
        }
    }
}
