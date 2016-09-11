# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/a12690/android_sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Ignore warnings
-dontwarn com.nomi.artwatch.**
-dontwarn org.mockito.**
-dontwarn okio.**
-dontwarn org.objenesis.**

# Keep support library
-keep class android.support.v4.** {*; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

# Keep RxJava
-dontwarn sun.misc.**
-keep class sun.misc.Unsafe { *; }
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# Keep org.apache.http
-dontwarn org.apache.http.**
-keep class org.apache.http.** { *; }

# Keep tumblr
-keep class com.tumblr.** { *; }

# Keep gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod