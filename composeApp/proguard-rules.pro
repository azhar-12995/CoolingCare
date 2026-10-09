# kotlinx.serialization — keep generated serializers (stored data + navigation routes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.coolingcare.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.coolingcare.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.coolingcare.app.**$$serializer { *; }
