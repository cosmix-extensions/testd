apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

cloudstream {
    language = "en"
    authors = listOf("cosmix-extensions")
    description = "XXXFree Movies and Videos"
    status = 1
    tvTypes = listOf("Movie")
    // isCrossPlatform is specific to Cosmix, usually not needed for Cloudstream, but keeping it if custom plugin supports it. 
    // iconUrl is fully supported by Cloudstream
    iconUrl = "https://t3.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://xxxfree.watch/&size=128"
}

android {
    namespace = "com.cosmix.xxxfree"
}