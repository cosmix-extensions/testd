package com.free

import com.cosmix.app.extractors.StreamWishExtractor
import com.cosmix.app.extractors.VidStack
import com.cosmix.app.extractors.Filesim

class Luluvdo : StreamWishExtractor() {
    override var mainUrl = "https://luluvdo.com"
}

class LuluStream : StreamWishExtractor() {
    override var mainUrl = "https://lulustream.com"
}

class Playmogo : VidStack() {
    override var mainUrl = "https://playmogo.com"
}

class Filemoon : Filesim() {
    override var mainUrl = "https://filemoon.sx"
}

class Filelions : VidStack() {
    override var mainUrl = "https://filelions.com"
}

class Voe : VidStack() {
    override var mainUrl = "https://voe.sx"
}

class Doodstream : VidStack() {
    override var mainUrl = "https://dood.yt"
}

class Streamtape : VidStack() {
    override var mainUrl = "https://streamtape.com"
}
