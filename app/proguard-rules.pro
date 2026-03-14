# ProGuard rules for Xposed Module
# Keep Xposed related classes
-keep class de.robv.android.xposed.** { *; }
-keep class com.highcapable.yukihookapi.** { *; }

# Keep our hook class
-keep class com.example.interceptor.XposedHook { *; }

# Keep for reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
