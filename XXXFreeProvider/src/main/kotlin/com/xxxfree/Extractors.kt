package com.xxxfree

import com.cosmix.app.SubtitleFile
import com.cosmix.app.app
import com.cosmix.app.utils.ExtractorApi
import com.cosmix.app.utils.ExtractorLink
import com.cosmix.app.utils.ExtractorLinkType
import com.cosmix.app.utils.Qualities
import com.cosmix.app.utils.newExtractorLink

class WoffXXXExtractor : ExtractorApi() {
    override val name = "WoffXXX"
    override val mainUrl = "https://woffxxx.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val html = app.get(url, referer = referer ?: mainUrl).text
        val regex = Regex("""['"]([^'"]+\.(?:mp4|m3u8)[^'"]*)['"]""")
        
        regex.findAll(html).forEach { match ->
            var link = match.groupValues[1]
            if (link.startsWith("//")) {
                link = "https:$link"
            }
            if (link.contains("woffxxx") || link.contains("cfglobalcdn") || link.contains("secip")) {
                val isM3u8 = link.contains(".m3u8")
                callback.invoke(
                    newExtractorLink(
                        name,
                        name,
                        link,
                        if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                    ) {
                        this.referer = url
                        this.quality = Qualities.P1080.value
                    }
                )
            }
        }
    }
}

class VidloxExtractor : ExtractorApi() {
    override val name = "Vidlox"
    override val mainUrl = "https://vidlox.me"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val html = app.get(url, referer = referer).text
        
        // Try sources array first
        var link = Regex("""sources\s*:\s*\[.*?file\s*:\s*['"](.*?)['"]""").find(html)?.groupValues?.get(1)
        
        if (link == null) {
            // Fallback to simple regex
            link = Regex("""['"](https?://[^'"]+\.mp4[^'"]*)['"]""").find(html)?.groupValues?.get(1)
        }
        
        link?.let {
            callback.invoke(
                newExtractorLink(
                    name,
                    name,
                    it,
                    ExtractorLinkType.VIDEO
                ) {
                    this.referer = url
                    this.quality = Qualities.Unknown.value
                }
            )
        }
    }
}

class FirestreamExtractor : ExtractorApi() {
    override val name = "Firestream"
    override val mainUrl = "https://firestream.to"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val html = app.get(url, referer = referer ?: mainUrl).text
        
        val regex = Regex("""['"]([^'"]+\.(?:mp4|m3u8)[^'"]*)['"]""")
        regex.findAll(html).forEach { match ->
            var link = match.groupValues[1]
            if (link.startsWith("//")) {
                link = "https:$link"
            }
            if (link.contains("firestream") && (link.contains("md5") || link.contains("expires"))) {
                val isM3u8 = link.contains(".m3u8")
                callback.invoke(
                    newExtractorLink(
                        name,
                        name,
                        link,
                        if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                    ) {
                        this.referer = url
                        this.quality = Qualities.Unknown.value
                    }
                )
            }
        }
    }
}

class VSonicExtractor : ExtractorApi() {
    override val name = "VSonic"
    override val mainUrl = "https://vsonic.click"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val html = app.get(url, referer = referer ?: mainUrl).text
        
        val regex = Regex("""['"]([^'"]+\.(?:mp4|m3u8)[^'"]*)['"]""")
        regex.findAll(html).forEach { match ->
            var link = match.groupValues[1]
            if (link.startsWith("//")) {
                link = "https:$link"
            }
            if (link.contains("vidsonic.net") || link.contains("vsonic")) {
                val isM3u8 = link.contains(".m3u8")
                callback.invoke(
                    newExtractorLink(
                        name,
                        name,
                        link,
                        if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                    ) {
                        this.referer = url
                        this.quality = Qualities.Unknown.value
                    }
                )
            }
        }
    }
}
