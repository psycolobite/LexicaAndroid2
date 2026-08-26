# ==========================================
# Lexica ProGuard / R8 Configuration
# ==========================================

# 1. Obfuscation & Line Numbers (utile pour les stacktraces Crashlytics / Google Play Console)
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 2. Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void clearAllTables();
    <methods>;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# 3. Gson & Data Models
-keepclassmembers enum * { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
# Préserver tous les modèles de données de l'application
-keep class com.example.lexicaandroid2.data.model.** { *; }
-keep class com.example.lexicaandroid2.features.gamification.domain.** { *; }
-keep class com.example.lexicaandroid2.features.gamification.data.** { *; }

# 4. Retrofit & OkHttp
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes *Annotation*
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# 5. Firebase & Google Play Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# 6. TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# 7. Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# 8. Jsoup
-keep public class org.jsoup.** { public *; }
-dontwarn org.jsoup.**