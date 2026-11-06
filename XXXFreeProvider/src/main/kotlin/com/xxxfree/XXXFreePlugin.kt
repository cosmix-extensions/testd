package com.xxxfree

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.BasePlugin

@CloudstreamPlugin
class XXXFreePlugin: BasePlugin() {
    override fun load() {
        // All providers should be added in this manner. Please don't edit the providers list directly.
        registerMainAPI(XXXFreeProvider())
        
        registerExtractorAPI(WoffXXXExtractor())
        registerExtractorAPI(VidloxExtractor())
        registerExtractorAPI(FirestreamExtractor())
        registerExtractorAPI(VSonicExtractor())
    }
}
