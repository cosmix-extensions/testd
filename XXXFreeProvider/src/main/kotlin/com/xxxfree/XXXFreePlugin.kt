package com.xxxfree

import com.cosmix.app.plugins.CsxPlugin
import com.cosmix.app.plugins.CsxPluginAnnotation

@CsxPluginAnnotation
class XXXFreePlugin : CsxPlugin() {
    override fun load() {
        registerCsxApi(XXXFreeProvider())
        registerExtractorApi(WoffXXXExtractor())
        registerExtractorApi(VidloxExtractor())
        registerExtractorApi(FirestreamExtractor())
        registerExtractorApi(VSonicExtractor())
    }
}

