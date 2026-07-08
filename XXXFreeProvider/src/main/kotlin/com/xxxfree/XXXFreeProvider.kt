package com.xxxfree

import com.cosmix.app.*
import com.cosmix.app.utils.*
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class XXXFreeProvider : CsxApi() {
    override var mainUrl = "https://xxxfree.watch"
    override var name = "XXXFree"
    override var lang = "en"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie)

    private val ua = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "Accept-Language" to "en-US,en;q=0.9",
        "Cache-Control" to "max-age=0",
        "Sec-Ch-Ua" to "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"",
        "Sec-Ch-Ua-Mobile" to "?0",
        "Sec-Ch-Ua-Platform" to "\"Windows\"",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "none",
        "Sec-Fetch-User" to "?1",
        "Upgrade-Insecure-Requests" to "1"
    )

    override val mainPage = mainPageOf(
        "$mainUrl/latest-updates/" to "Latest Updates",
        "$mainUrl/top-rated/" to "Top Rated",
        "$mainUrl/most-popular/" to "Most Popular"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page == 1) request.data else "${request.data}$page/"
        val doc = app.get(url, headers = ua, timeout = 60).document
        
        val items = doc.select("article.loop-video").mapNotNull { item ->
            val a = item.selectFirst("a[href*=/videos/], a[href*=/movie/], a.link") ?: item.selectFirst("a") ?: return@mapNotNull null
            var href = a.attr("href")
            if (href.startsWith("/")) href = "$mainUrl$href"

            val title = a.attr("title").trim().ifEmpty { 
                item.selectFirst(".title")?.text()?.trim() ?: "Unknown"
            }
            
            var poster = item.selectFirst("img")?.let { img ->
                img.attr("data-src").ifEmpty { img.attr("src") }
            }
            
            if (poster?.startsWith("//") == true) poster = "https:$poster"
            if (poster?.startsWith("/") == true) poster = "$mainUrl$poster"
            
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
            }
        }
        
        return newHomePageResponse(request.name, items, items.isNotEmpty())
    }

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val q = query.replace(" ", "+")
        val url = if (page == 1) "$mainUrl/search/$q/relevance/" else "$mainUrl/search/$q/relevance/$page/"
        val document = app.get(url, headers = ua, timeout = 60).document
        
        val items = document.select("article.loop-video").mapNotNull { item ->
            val a = item.selectFirst("a[href*=/videos/], a[href*=/movie/], a.link") ?: item.selectFirst("a") ?: return@mapNotNull null
            var href = a.attr("href")
            if (href.startsWith("/")) href = "$mainUrl$href"

            val title = a.attr("title").trim().ifEmpty { 
                item.selectFirst(".title")?.text()?.trim() ?: "Unknown"
            }
            
            var poster = item.selectFirst("img")?.let { img ->
                img.attr("data-src").ifEmpty { img.attr("src") }
            }
            
            if (poster?.startsWith("//") == true) poster = "https:$poster"
            if (poster?.startsWith("/") == true) poster = "$mainUrl$poster"
            
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
            }
        }
        
        return newSearchResponseList(items, items.isNotEmpty())
    }

    override suspend fun quickSearch(query: String): List<SearchResponse>? {
        return search(query, 1)?.items?.take(5)
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url, headers = ua, timeout = 60).document
        val title = doc.selectFirst("h1.post-title, h1")?.text()?.trim() ?: doc.title().trim()
        
        var poster = doc.selectFirst("meta[property=og:image]")?.attr("content")
        if (poster == null) {
            poster = doc.selectFirst(".player-container img")?.attr("src")
        }
        
        val plotText = doc.selectFirst("meta[name=description]")?.attr("content")
        val tags = doc.select("div.tags a").map { it.text() }
        val actors = doc.select("div.models a").map { it.text() }
        
        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = plotText
            this.tags = tags
            this.actors = actors.map { ActorData(Actor(it)) }
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        if (data.isBlank()) return false
        
        try {
            val html = app.get(data, headers = ua, timeout = 60).text
            val doc = org.jsoup.Jsoup.parse(html)
            
            var found = false
            
            // 1. Try to find iframes directly and pass to Extractors
            doc.select("iframe").forEach { iframe ->
                val src = iframe.attr("src")
                if (src.isNotBlank() && !src.contains("crwdcntrl") && !src.contains("javascript:false")) {
                    Log.d("XXXFree", "Found iframe: $src")
                    if (src.contains("firestream.to") || src.contains("vsonic") || src.contains("vidsonic") || src.contains("woffxxx") || src.contains("vidlox")) {
                        loadExtractor(src, subtitleCallback, callback)
                        found = true
                    } else {
                        // Attempt fallback default extractor
                        loadExtractor(src, subtitleCallback, callback)
                        found = true
                    }
                }
            }
            
            // 2. Fallback: Parse direct .mp4 links in the main HTML just in case
            val matcher = Pattern.compile("src=['\"]([^'\"]*\\.mp4[^'\"]*)['\"]").matcher(html)
            while (matcher.find()) {
                var streamUrl = matcher.group(1) ?: continue
                if (streamUrl.startsWith("//")) streamUrl = "https:$streamUrl"
                
                val qualityMatch = Regex("(\\d{3,4})[mp]?\\.mp4").find(streamUrl)
                var qualityValue = Qualities.Unknown.value
                var qualityName = "Direct Stream"
                
                if (qualityMatch != null) {
                    val q = qualityMatch.groupValues[1].toIntOrNull() ?: 0
                    qualityName = "${q}p"
                    qualityValue = when (q) {
                        1080 -> Qualities.P1080.value
                        720 -> Qualities.P720.value
                        480 -> Qualities.P480.value
                        360 -> Qualities.P360.value
                        else -> Qualities.Unknown.value
                    }
                }

                callback.invoke(
                    newExtractorLink(
                        this.name,
                        qualityName,
                        streamUrl,
                        ExtractorLinkType.VIDEO
                    ) {
                        quality = qualityValue
                    }
                )
                found = true
            }
            return found
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return false
    }
}
