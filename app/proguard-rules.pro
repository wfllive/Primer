# ===== Правила R8 для release-сборки =====

# Compose
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Kotlin
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlin.Metadata { public <methods>; }

# Точки входа Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# Убрать логи из релиза
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Понятные стек-трейсы при краше (mapping.txt сохраняется в build/outputs/mapping)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
