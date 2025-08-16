# KybersStream ProGuard/R8 Rules
# Configuración de ofuscación y optimización para release builds

# ======================================
# CONFIGURACIÓN BÁSICA
# ======================================

# Mantener información de línea para stack traces útiles
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# Mantener annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# ======================================
# ANDROID CORE
# ======================================

# Mantener clases de Android que se usan via reflection
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Mantener métodos nativos
-keepclasseswithmembernames class * {
    native <methods>;
}

# Mantener enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Mantener Parcelables
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ======================================
# JETPACK COMPOSE
# ======================================

# Mantener clases de Compose
-keep class androidx.compose.** { *; }
-keep class androidx.activity.compose.** { *; }
-keep class androidx.navigation.compose.** { *; }

# ======================================
# HILT / DAGGER
# ======================================

# Mantener clases anotadas con Hilt
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @javax.inject.Singleton class *
-keep @dagger.Module class *
-keep @dagger.hilt.InstallIn class *

# Mantener métodos de inyección
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

-keepclasseswithmembers class * {
    @javax.inject.Inject <fields>;
}

# ======================================
# KOTLINX SERIALIZATION
# ======================================

# Mantener clases serializables
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantener serializers generados
-keep,includedescriptorclasses class com.kybers.stream.**$$serializer { *; }
-keepclassmembers class com.kybers.stream.** {
    *** Companion;
}

# ======================================
# RETROFIT / OKHTTP
# ======================================

# Retrofit interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# OkHttp platform implementations
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Mantener clases de modelo para APIs
-keep class com.kybers.stream.data.remote.dto.** { *; }
-keep class com.kybers.stream.domain.model.** { *; }

# ======================================
# EXOPLAYER / MEDIA3
# ======================================

# Mantener clases de ExoPlayer
-keep class androidx.media3.** { *; }
-keep class com.google.android.exoplayer2.** { *; }

# Mantener decoders y extractors
-keep class androidx.media3.decoder.** { *; }
-keep class androidx.media3.extractor.** { *; }

# ======================================
# DATASTORE
# ======================================

# Mantener clases de DataStore
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }

# ======================================
# APLICACIÓN ESPECÍFICA
# ======================================

# Mantener clases principales de la aplicación
-keep class com.kybers.stream.KybersStreamApplication { *; }
-keep class com.kybers.stream.MainActivity { *; }

# Mantener ViewModels
-keep class com.kybers.stream.presentation.viewmodel.** { *; }

# Mantener casos de uso (pueden ser llamados por reflection)
-keep class com.kybers.stream.domain.usecase.** { *; }

# Mantener repositorios
-keep class com.kybers.stream.domain.repository.** { *; }
-keep class com.kybers.stream.data.repository.**Impl { *; }

# Mantener clases de seguridad (importantes para funcionalidad)
-keep class com.kybers.stream.data.security.** { *; }
-keep class com.kybers.stream.data.logging.** { *; }

# Mantener managers críticos
-keep class com.kybers.stream.data.manager.** { *; }
-keep class com.kybers.stream.data.cache.** { *; }

# ======================================
# OFUSCACIÓN AGRESIVA
# ======================================

# Ofuscar nombres de clases pero mantener funcionalidad
-allowaccessmodification
-repackageclasses 'a'

# Optimizaciones adicionales
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 5

# ======================================
# LOGGING Y DEBUGGING
# ======================================

# Remover logs en release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# ======================================
# KOTLIN COROUTINES
# ======================================

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ======================================
# WARNINGS Y NOTAS
# ======================================

# Suprimir warnings comunes que no afectan la funcionalidad
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn okhttp3.internal.platform.**
-dontwarn retrofit2.Platform$Java8
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**

# Ignorar notas sobre clases de reflection
-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor

# ======================================
# SEGURIDAD ADICIONAL
# ======================================

# Proteger clases de control parental
-keep class com.kybers.stream.domain.model.ParentalControl** { *; }
-keep class com.kybers.stream.data.repository.ParentalControlRepositoryImpl {
    public <methods>;
}