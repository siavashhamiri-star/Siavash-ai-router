# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line numbers and source file names for debugging crash logs in production
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Data Models & Entities (Tavana City AI Router)
-keep class com.example.tavanacity.domain.model.** { *; }
-keep class com.example.tavanacity.data.local.** { *; }

# Room Database ProGuard Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# BuildConfig & Generated API Keys (Secrets Gradle Plugin)
-keep class com.example.BuildConfig { *; }

# Moshi & JSON Parsing
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

# OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin Coroutines
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory {
    java.lang.String FAST_SERVICE_KEY;
}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {
    public <init>();
}
