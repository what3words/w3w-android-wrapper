package com.what3words.androidwrapper.datasource.voice.di

import com.what3words.androidwrapper.datasource.voice.di.MapperFactory
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test

class MapperFactoryTest {

    @Test
    fun `provides non null SuggestionWithCoordinatesMapper`() {
        val mapper = MapperFactory.provideSuggestionWithCoordinatesMapper()
        assertNotNull(mapper)
    }

    @Test
    fun `provides same instance on multiple calls`() {
        val mapper1 = MapperFactory.provideSuggestionWithCoordinatesMapper()
        val mapper2 = MapperFactory.provideSuggestionWithCoordinatesMapper()
        assertSame(mapper1, mapper2)
    }
}