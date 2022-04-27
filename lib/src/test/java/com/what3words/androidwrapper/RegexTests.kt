package com.what3words.androidwrapper

import com.what3words.androidwrapper.helpers.didYouMean3wa
import com.what3words.androidwrapper.helpers.isPossible3wa
import com.what3words.androidwrapper.helpers.searchPossible3wa
import com.what3words.androidwrapper.voice.Microphone
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import okhttp3.WebSocket
import okio.ByteString
import org.junit.Before
import org.junit.Test

class RegexTests {
    @Test
    fun `match regex invalid delimiter`() {
        val words = "index.home raft"
        assert(!words.isPossible3wa())
    }

    @Test
    fun `match regex valid delimiter`() {
        val words = "index.home.raft"
        assert(words.isPossible3wa())
    }

    @Test
    fun `dym regex invalid words`() {
        val words = "index home"
        assert(!words.didYouMean3wa())
    }

    @Test
    fun `dym regex valid words`() {
        val words = "index home raft"
        assert(words.didYouMean3wa())
    }

    @Test
    fun `search regex in text with 3wa`() {
        val words = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor index.home.raft nunc vitae mauris mattis, et cursus ante posuere. Quisque consequat varius orci, ut auctor ipsum. Integer gravida non eros non posuere. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean consequat interdum lacus, viverra auctor nisi dignissim in. Aenean sed tempor ro.do.so tellus, eget vestibulum dolor. Donec vel mi maximus, commodo diam sit amet, dictum purus. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc volutpat eu ligula ultricies feugiat. Cras nisi justo, varius vitae augue at, eleifend porttitor velit.\n" +
                "\n" +
                "Aenean id lacus ipsum. Integer ut dolor a enim efficitur aliquam. filled.count.soap. Aliquam vitae mattis diam, eget tincidunt nibh. Suspendisse mattis leo eu arcu finibus lobortis. Aenean bibendum, turpis id posuere aliquet, orci ante euismod ipsum, nec imperdiet ex felis nec nulla. Nulla facilisi. Nam auctor dapibus nunc, sed maximus quam varius a. Quisque eu lacinia dui. Sed at consectetur magna."
        assert(words.searchPossible3wa().count() == 3)
        assert(words.searchPossible3wa().contains("index.home.raft"))
        assert(words.searchPossible3wa().contains("filled.count.soap"))
        assert(words.searchPossible3wa().contains("ro.do.so"))
    }

    @Test
    fun `search regex in text without 3wa`() {
        val words = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor nunc vitae mauris mattis, et cursus ante posuere. Quisque consequat varius orci, ut auctor ipsum. Integer gravida non eros non posuere. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean consequat interdum lacus, viverra auctor nisi dignissim in. Aenean sed tempor tellus, eget vestibulum dolor. Donec vel mi maximus, commodo diam sit amet, dictum purus. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc volutpat eu ligula ultricies feugiat. Cras nisi justo, varius vitae augue at, eleifend porttitor velit.\n" +
                "\n" +
                "Aenean id lacus ipsum. Integer ut dolor a enim efficitur aliquam. Aliquam vitae mattis diam, eget tincidunt nibh. Suspendisse mattis leo eu arcu finibus lobortis. Aenean bibendum, turpis id posuere aliquet, orci ante euismod ipsum, nec imperdiet ex felis nec nulla. Nulla facilisi. Nam auctor dapibus nunc, sed maximus quam varius a. Quisque eu lacinia dui. Sed at consectetur magna."
        assert(words.searchPossible3wa().count() == 0)
    }
}
