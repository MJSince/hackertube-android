# HackerTube ProGuard rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class org.hackertube.android.data.model.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
