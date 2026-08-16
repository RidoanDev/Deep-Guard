# ===========================================================================
# DeepGuard Security & Obfuscation ProGuard Rules (R8)
# ===========================================================================

# Aggressive Optimization & Code Shrinking
-repackageclasses 'com.deepguard.internal'
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Strip debugging and source file references
-renamesourcefileattribute ""
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# Keep essential Android / Jetpack Compose entry points
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Accessibility Service & Device Admin entry points
-keep class com.example.service.** { *; }

# Keep Room Database and Entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase {
    public abstract *;
}

# Keep Moshi / Serialization models if applicable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

