-keep class com.shopping.pricecompare.model.** { *; }
-keep class com.shopping.pricecompare.api.model.** { *; }
-keep class com.shopping.pricecompare.data.MainCategoryItem { *; }
-keep class com.shopping.pricecompare.data.MidCategoryItem { *; }
-keep class com.shopping.pricecompare.data.SubCategoryItem { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ViewBinding
-keep class com.shopping.pricecompare.databinding.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }
