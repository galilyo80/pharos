# Keep Shizuku API
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.pharos.evlink.** { *; }
