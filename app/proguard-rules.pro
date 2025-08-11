# AppLovin MAX
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**

# Google UMP
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# Gson reflection (model classes)
-keep class com.zafar.escaperush3d.data.model.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# AndroidX Lifecycle / ViewModel
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }

-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }

# (Optional) Keep your game classes if you later use reflection
# -keep class com.zafar.escaperush3d.** { *; }