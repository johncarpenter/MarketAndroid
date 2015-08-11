# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Tools\android/tools/proguard/proguard-android.txt
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

##
## Not working yet. Don't implement
##
-dontobfuscate
-dontpreverify

-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Dagger
#Keep the annotated things annotated
-keepattributes *Annotation*

#Keep the dagger annotation classes themselves
-keep @interface dagger.*,javax.inject.*

#Keep the Modules intact
-keep @dagger.Module class *

#-Keep the fields annotated with @Inject of any class that is not deleted.
-keepclassmembers class  javax.inject.*


#-Keep the names of classes that have fields annotated with @Inject and the fields themselves.
-keepclasseswithmembernames class javax.inject.*

# Keep the generated classes by dagger-compile
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection

-keep class com.twolinessoftware.smarterlist.RootModule
-keep class com.twolinessoftware.smarterlist.CoreModule
-keep class com.twolinessoftware.smarterlist.AndroidModule

#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}


# For Guava:
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.invoke.**
-dontwarn dagger.internal.**
-dontwarn java.nio.file.**

-dontwarn rx.**
-dontwarn org.apache.lang.**

-keepattributes Signature

# We use Gson's @SerializedName annotation which won't work without this:
-keepattributes *Annotation*

# Remove logging calls
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-dontwarn com.squareup.okhttp.internal.huc.**
-dontwarn org.codehaus.mojo.**

-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }
-dontwarn android.support.design.**
-dontwarn **CompatHoneycomb
-keep public class * extends android.support.v4.app.Fragment
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }