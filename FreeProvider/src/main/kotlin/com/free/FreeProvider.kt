package com.free

import com.cosmix.app.*
import com.cosmix.app.utils.*
import org.jsoup.nodes.Element

class FreeProvider : CsxApi() {
    override var mainUrl = "https://xxxfree.watch"
    override var name = "FreeProvider"
    override val hasMainPage = true
    override var lang = "en"
    override val supportedTypes = setOf(TvType.Movie)

    private val ua = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

    override val mainPage = mainPageOf(
        "$mainUrl/?filter=latest" to "Latest",
        "$mainUrl/category/onlyfans/" to "Onlyfans",
        "$mainUrl/category/asian/" to "Asian",
        "$mainUrl/category/big-ass/" to "Big Ass",
        "$mainUrl/category/big-tits/" to "Big Tits",
        "$mainUrl/category/blonde/" to "Blonde",
        "$mainUrl/category/creampie/" to "Creampie"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page == 1) request.data else {
            if (request.data.contains("?filter")) {
                request.data.replace("?filter", "page/$page/?filter")
            } else {
                "${request.data.trimEnd('/')}/page/$page/"
            }
        }
        
        val doc = app.get(url, headers = ua).document
        val items = doc.select("article.post, div.item").mapNotNull { el ->
            val a = el.selectFirst("a[href]") ?: return@mapNotNull null
            val href = a.attr("abs:href").ifBlank { return@mapNotNull null }
            val title = (el.selectFirst("h2, h3, .entry-title")?.text() ?: a.attr("title")).trim().ifBlank { return@mapNotNull null }
            
            // Get Poster from main page article
            var poster = el.selectFirst("img")?.let { it.attr("data-src").ifBlank { it.attr("src") } }
            if (poster == null) {
                // fallback to finding div with background image if it uses that
                val bgElement = el.selectFirst(".post-thumbnail")
                if (bgElement != null && bgElement.attr("style").contains("background-image")) {
                    val bgStyle = bgElement.attr("style")
                    val bgMatch = Regex("url\\('(.*?)'\\)").find(bgStyle)
                    if (bgMatch != null) poster = bgMatch.groupValues[1]
                }
            }
            
            newMovieSearchResponse(title, href, TvType.Movie) { posterUrl = poster }
        }
        return newHomePageResponse(request.name, items, items.isNotEmpty())
    }

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val url = if (page == 1) "$mainUrl/?s=${java.net.URLEncoder.encode(query, "UTF-8")}" else "$mainUrl/page/$page/?s=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val doc = app.get(url, headers = ua).document
        val items = doc.select("article.post, div.item").mapNotNull { el ->
            val a = el.selectFirst("a[href]") ?: return@mapNotNull null
            val href = a.attr("abs:href").ifBlank { return@mapNotNull null }
            val title = (el.selectFirst("h2, h3, .entry-title")?.text() ?: a.attr("title")).trim().ifBlank { return@mapNotNull null }
            var poster = el.selectFirst("img")?.let { it.attr("data-src").ifBlank { it.attr("src") } }
            
            newMovieSearchResponse(title, href, TvType.Movie) { posterUrl = poster }
        }
        return newSearchResponseList(items, items.isNotEmpty())
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url, headers = ua).document
        
        val title = doc.selectFirst("h1.entry-title, h1")?.text()?.trim() 
            ?: doc.selectFirst("meta[property=og:title]")?.attr("content") 
            ?: "Unknown Title"
            
        var poster = doc.selectFirst("meta[property=og:image]")?.attr("content") ?: ""
        if (poster.isBlank() || poster.contains("blank")) {
            // Check for video poster or main image inside the player container
            poster = doc.selectFirst("video")?.attr("poster") ?: 
                     doc.selectFirst(".post-thumbnail img")?.let { it.attr("data-src").ifBlank { it.attr("src") } } ?: ""
        }
        
        val description = doc.selectFirst("meta[property=og:description]")?.attr("content") ?: ""
        
        // Extract all iframes and video sources for the multiple providers
        val iframes = doc.select("iframe").mapNotNull { it.attr("src") }.filter { it.isNotBlank() }
        val videos = doc.select("video source, video").mapNotNull { it.attr("src") }.filter { it.isNotBlank() }
        
        // Sometimes the provider links are hidden in buttons or generic links
        val playerLinks = doc.select("a.player-link, a.server-link").mapNotNull { it.attr("href") }.filter { it.isNotBlank() }
        
        val dataStr = (iframes + videos + playerLinks).distinct().joinToString(",")
        
        return newMovieLoadResponse(title, url, TvType.Movie, dataStr) { 
            this.posterUrl = poster 
            this.plot = description 
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        if (data.isBlank()) return false
        val urls = data.split(",")
        var found = false
        
        urls.forEach { url ->
            if (url.isNotBlank()) {
                try {
                    val loaded = loadExtractor(url, subtitleCallback, callback)
                    if (loaded) found = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return found
    }
}
