# Keep enum names because LEX stores enum names in local JSON state.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    *;
}

# Native methods (for example androidx.graphics.path) must keep their JNI names.
-keepclasseswithmembernames class * {
    native <methods>;
}

# Framework callbacks are invoked by the Android runtime.
-keep class * extends android.print.PrintDocumentAdapter { *; }
-keepclassmembers class * extends android.webkit.WebViewClient { *; }
-keepclassmembers class * implements android.net.nsd.NsdManager$DiscoveryListener { *; }
-keepclassmembers class * implements android.net.nsd.NsdManager$ResolveListener { *; }

-dontwarn org.jetbrains.annotations.**
