# keep titanium class / package names
-keepnames class org.appcelerator.** 
-keepnames class com.appcelerator.**
-keepnames class ti.**
-keepnames class android.widget.*
-keepnames class kankan.wheel.widget.*-
-keepnames class org.mozilla.javascript.**

# TODO: generate app ID here -keepnames com.company.id.**
-keepnames class com.arcaner.proguard.test1.**
-keeppackagenames

# preverification does nothing for dex
-dontpreverify

# allows further optimization of getter/setter -> directly to field
-allowaccessmodification

# dalvik specific optimization flags
-optimizations !code/simplification/arithmetic

# app classes
-injars /Users/username/Code/test/titanium/proguardTest1/build/android/bin/classes
 
# module jars
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-accelerometer.jar(!**/accelerometer.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-analytics.jar(!**/analytics.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-android.jar(!**/android.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-api.jar(!**/api.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-app.jar(!**/app.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-bump.jar(!**/bump.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-calendar.jar(!**/calendar.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-contacts.jar(!**/contacts.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-database.jar(!**/database.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-facebook.jar(!**/facebook.json,!META-INF/MANIFEST.MF,!**/LICENSE)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-filesystem.jar(!**/filesystem.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-geolocation.jar(!**/geolocation.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-gesture.jar(!**/gesture.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-json.jar(!**/json.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-locale.jar(!**/locale.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-map.jar(!**/map.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-media.jar(!**/media.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-network.jar(!**/network.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-platform.jar(!**/platform.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-ui.jar(!**/ui.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-utils.jar(!**/utils.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-xml.jar(!**/xml.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium-yahoo.jar(!**/yahoo.json,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/support/android/lib/titanium-verify.jar(!**/.gitignore,!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/dist/android/titanium.jar(!**/titanium.json,!META-INF/MANIFEST.MF,!**/*-NOTICE.txt,!**/.gitignore)

# bundled libs
-injars /Users/username/Code/titanium_mobile/android/titanium/lib/ti-commons-codec-1.3.jar(!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/android/titanium/lib/commons-logging-1.1.1.jar(!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/android/titanium/lib/smalljs.jar(!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/android/modules/xml/lib/jaxen-1.1.1.jar(!META-INF/MANIFEST.MF)
-injars /Users/username/Code/titanium_mobile/android/modules/bump/lib/bump-api.jar(!META-INF/MANIFEST.MF)

# library jars
#-libraryjars /Users/username/Code/titanium_mobile/android/kroll-apt/lib/freemarker.jar
-libraryjars /Users/username/Apps/android-sdk-mac_86/platforms/android-4/android.jar
-libraryjars /Users/username/Apps/android-sdk-mac_86/add-ons/google_apis-4_r02/libs/maps.jar

# misc options

# commons codec/logging use a lot of reflection don't warn
-dontwarn org.apache.commons.codec.binary.Base64
-dontwarn org.apache.commons.codec.binary.Hex
-dontwarn org.apache.commons.logging.impl.ServletContextCleaner
-dontwarn org.apache.commons.logging.impl.LogKitLogger
-dontwarn org.apache.commons.logging.impl.AvalonLogger
-dontnote org.apache.commons.logging.Log
-dontnote org.apache.commons.logging.LogSource
-dontnote org.apache.commons.logging.impl.Log4JLogger
-dontnote org.apache.james.mime4j.message.storage.TempStorage

# bytecode compiled JS uses reflection to bootstrap
-dontnote org.appcelerator.titanium.TiScriptRunner
 
# we use reflection for contacts api support post android r4
-dontnote ti.modules.titanium.contacts.CommonContactsApi
-dontnote ti.modules.titanium.contacts.ContactsApiLevel5
 
# reflection used to get the internal "applicationScale" member in TiPlatformHelper
-dontnote org.appcelerator.titanium.util.TiPlatformHelper
 
# thirdparty classes
-keep class org.mozilla.javascript.jdk13.VMBridge_jdk13
 
# app classes
-keep class com.arcaner.proguard.test1.*

# module classes / methods
-keep class ti.modules.titanium.TitaniumModuleBindingGen
-keep class ti.modules.titanium.ui.UIModule
-keep class ti.modules.titanium.ui.UIModuleBindingGen
-keep class ti.modules.titanium.filesystem.FilesystemModule
-keep class ti.modules.titanium.filesystem.FilesystemModuleBindingGen
-keep class ti.modules.titanium.json.JSONModule
-keep class ti.modules.titanium.json.JSONModuleBindingGen
-keep class ti.modules.titanium.locale.LocaleModuleBindingGen
-keep class ti.modules.titanium.android.AndroidModuleBindingGen
-keep class ti.modules.titanium.app.AppModuleBindingGen
-keep class ti.modules.titanium.api.APIModuleBindingGen
-keep class ti.modules.titanium.media.MediaModuleBindingGen
-keep class ti.modules.titanium.analytics.AnalyticsModuleBindingGen
 
# proxy binding classes
-keep class org.appcelerator.titanium.**BindingGen
-keep class ti.modules.titanium.ui.WindowProxyBindingGen
-keep class ti.modules.titanium.ui.ButtonProxyBindingGen
-keep class ti.modules.titanium.ui.LabelProxyBindingGen
-keep class ti.modules.titanium.filesystem.FileProxyBindingGen
