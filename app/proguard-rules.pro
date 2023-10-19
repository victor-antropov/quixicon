##---------- Begin: App ----------

-keep interface * extends java.io.Serializable { *; }

##---------- End: App ----------


##---------- Begin: Crashlytics ----------
## https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android

# Keep file names and line numbers.
-keepattributes SourceFile, LineNumberTable

# Optional: Keep custom exceptions.
-keep public class * extends java.lang.Exception

##---------- End: Crashlytics ----------


##---------- Begin: Log ----------
## https://stackoverflow.com/questions/54301532/proguard-not-stripping-down-timber-logs

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
    public static int e(...);
}
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** w(...);
    public static *** v(...);
    public static *** i(...);
    public static *** e(...);
}

##---------- End: Log ----------


##---------- Begin: Retrofit ----------
## https://github.com/square/retrofit/blob/master/retrofit/src/main/resources/META-INF/proguard/retrofit2.pro

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

##---------- End: Retrofit ----------


##---------- Begin: OkHttp ----------
## https://github.com/square/okhttp/blob/master/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

##---------- End: OkHttp ----------


##---------- Begin: Okio ----------
## https://github.com/square/okio/blob/master/okio/src/jvmMain/resources/META-INF/proguard/okio.pro

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

##---------- End: Okio ----------


##---------- Begin: Gson ----------
## https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation.
-keepattributes *Annotation*

# Gson specific classes.
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson.
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter).
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

##---------- End: Gson ----------


##---------- Begin: Guava ----------
# https://github.com/google/guava/wiki/UsingProGuardWithGuava

-dontwarn javax.lang.model.element.Modifier

# Note: We intentionally don't add the flags we'd need to make Enums work.
# That's because the Proguard configuration required to make it work on
# optimized code would preclude lots of optimization, like converting enums
# into ints.

# Throwables uses internal APIs for lazy stack trace resolution.
-dontnote sun.misc.SharedSecrets
-keep class sun.misc.SharedSecrets {
    *** getJavaLangAccess(...);
}
-dontnote sun.misc.JavaLangAccess
-keep class sun.misc.JavaLangAccess {
    *** getStackTraceElement(...);
    *** getStackTraceDepth(...);
}

# FinalizableReferenceQueue calls this reflectively.
# Proguard is intelligent enough to spot the use of reflection onto this, so we
# only need to keep the names, and allow it to be stripped out if
# FinalizableReferenceQueue is unused.
-keepnames class com.google.common.base.internal.Finalizer {
    *** startFinalizer(...);
}
# However, it cannot "spot" that this method needs to be kept IF the class is.
-keepclassmembers class com.google.common.base.internal.Finalizer {
    *** startFinalizer(...);
}
-keepnames class com.google.common.base.FinalizableReference {
    void finalizeReferent();
}
-keepclassmembers class com.google.common.base.FinalizableReference {
    void finalizeReferent();
}

# Striped64, LittleEndianByteArray, UnsignedBytes, AbstractFuture.
-dontwarn sun.misc.Unsafe

# Striped64 appears to make some assumptions about object layout that
# really might not be safe. This should be investigated.
-keepclassmembers class com.google.common.cache.Striped64 {
    *** base;
    *** busy;
}
-keepclassmembers class com.google.common.cache.Striped64$Cell {
    <fields>;
}

-dontwarn java.lang.SafeVarargs

-keep class java.lang.Throwable {
    *** addSuppressed(...);
}

# Futures.getChecked, in both of its variants, is incompatible with proguard.

# Used by AtomicReferenceFieldUpdater and sun.misc.Unsafe.
-keepclassmembers class com.google.common.util.concurrent.AbstractFuture** {
    *** waiters;
    *** value;
    *** listeners;
    *** thread;
    *** next;
}
-keepclassmembers class com.google.common.util.concurrent.AtomicDouble {
    *** value;
}
-keepclassmembers class com.google.common.util.concurrent.AggregateFutureState {
    *** remaining;
    *** seenExceptions;
}

# Since Unsafe is using the field offsets of these inner classes, we don't want
# to have class merging or similar tricks applied to these classes and their
# fields. It's safe to allow obfuscation, since the by-name references are
# already preserved in the -keep statement above.
-keep,allowshrinking,allowobfuscation class com.google.common.util.concurrent.AbstractFuture** {
    <fields>;
}

# Futures.getChecked (which often won't work with Proguard anyway) uses this. It
# has a fallback, but again, don't use Futures.getChecked on Android regardless.
-dontwarn java.lang.ClassValue

# MoreExecutors references AppEngine.
-dontnote com.google.appengine.api.ThreadManager
-keep class com.google.appengine.api.ThreadManager {
    static *** currentRequestThreadFactory(...);
}
-dontnote com.google.apphosting.api.ApiProxy
-keep class com.google.apphosting.api.ApiProxy {
    static *** getCurrentEnvironment (...);
}

##---------- End: Guava ----------


##---------- Begin: Glide ----------
## https://github.com/bumptech/glide/blob/master/library/proguard-rules.txt

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Uncomment for DexGuard only.
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

##---------- End: Glide ----------


##---------- Begin: Paper ----------
## https://proguard-rules.blogspot.com/2017/06/paper-proguard-rules.html

# Keep data classes
-keep class my.package.data.model.** { *; }

# Keep all your data classes that implement Serializable.
-keep class * implements java.io.Serializable { *; }

##---------- End: Paper ----------


##---------- Begin: RxPaper ----------
## https://github.com/cesarferreira/RxPaper

# Keep data classes
-keep class my.package.data.model.** { *; }

# Keep all your data classes that implement Serializable.
-keep class * implements java.io.Serializable { *; }

# Keep library classes and its dependencies.
-keep class io.paperdb.** { *; }
-keep class com.esotericsoftware.** { *; }
-dontwarn com.esotericsoftware.**
-keep class de.javakaffee.kryoserializers.** { *; }
-dontwarn de.javakaffee.kryoserializers.**

##---------- End: RxPaper ----------


##---------- Begin: Room ----------
## https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/room/runtime/proguard-rules.pro

-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

##---------- End: Room ----------


##---------- Begin: Data-binding ----------
## https://android.googlesource.com/platform/frameworks/data-binding/+/master/proguard.cfg

-keep class android.databinding.** { *; }
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepattributes *Annotation*
-keepattributes javax.xml.bind.annotation.*
-keepattributes javax.annotation.processing.*
-keepclassmembers class * extends java.lang.Enum { *; }
-keepclasseswithmembernames class android.**
-keepclasseswithmembernames interface android.**
-dontobfuscate
-dontwarn

##---------- End: Data-binding ----------

# For Amplitude.io
-keep class com.google.android.gms.ads.** { *; }
-dontwarn okio.**