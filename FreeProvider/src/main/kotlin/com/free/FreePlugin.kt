package com.free
import com.cosmix.app.plugins.CsxPlugin
import com.cosmix.app.plugins.CsxPluginAnnotation

@CsxPluginAnnotation
class FreePlugin : CsxPlugin() {
    override fun load() {
        // Register the provider here
        registerCsxApi(FreeProvider())
        
        // Register extractors
        registerExtractorApi(Luluvdo())
        registerExtractorApi(LuluStream())
        registerExtractorApi(Playmogo())
        registerExtractorApi(Filemoon())
        registerExtractorApi(Filelions())
        registerExtractorApi(Voe())
        registerExtractorApi(Doodstream())
        registerExtractorApi(Streamtape())
    }
}
