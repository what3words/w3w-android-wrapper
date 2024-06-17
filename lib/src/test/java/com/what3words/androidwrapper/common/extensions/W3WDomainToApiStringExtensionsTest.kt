package com.what3words.androidwrapper.common.extensions

import android.media.AudioFormat
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toAPIString
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toApiString
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toQueryMap
import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toVoiceApiString
import com.what3words.core.datasource.voice.audiostream.W3WAndroidAudioStreamEncoding
import com.what3words.core.datasource.voice.audiostream.W3WAudioStreamEncoding
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCircle
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WDistance
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.geometry.W3WRectangle
import com.what3words.core.types.language.W3WRFC5646Language
import com.what3words.core.types.options.W3WAutosuggestInputType
import com.what3words.core.types.options.W3WAutosuggestOptions
import kotlinx.coroutines.test.runTest
import org.junit.Test

class W3WDomainToApiStringExtensionsTest {

    @Test
    fun `test List_W3WCountry_toAPIString`() = runTest {
        // Given
        val countries = listOf(
            W3WCountry("GB"),
            W3WCountry("FR"),
            W3WCountry("DE")
        )

        // When
        val result = countries.toAPIString()

        // Then
        assert(result == "GB,FR,DE")
    }

    @Test
    fun `test empty List_W3WCountry_toAPIString should return empty string`() = runTest {
        // Given
        val countries = emptyList<W3WCountry>()

        // When
        val result = countries.toAPIString()

        // Then
        assert(result == "")
    }

    @Test
    fun `test W3WCoordinates_toAPIString`() = runTest {
        // Given
        val coordinates = W3WCoordinates(51.520847, -0.195521)

        // When
        val result = coordinates.toAPIString()

        // Then
        assert(result == "51.520847,-0.195521")
    }

    @Test
    fun `test W3WW3WPolygon_toAPIString`() = runTest {
        // Given
        val polygon = W3WPolygon(
            listOf(
                W3WCoordinates(51.521, -0.343),
                W3WCoordinates(52.6, 2.3324),
                W3WCoordinates(54.234, 8.343),
                W3WCoordinates(51.521, -0.343),
            )
        )

        // When
        val result = polygon.toAPIString()

        // Then
        assert(result == "51.521,-0.343,52.6,2.3324,54.234,8.343,51.521,-0.343")
    }

    @Test
    fun `test W3WCircle_toAPIString`() = runTest {
        // Given
        val circle = W3WCircle(
            W3WCoordinates(51.4243877, -0.3474524),
            W3WDistance(10.0)
        )

        // When
        val result = circle.toAPIString()

        // Then
        assert(result == "51.4243877,-0.3474524,10.0")
    }

    @Test
    fun `test W3WRectangle_toAPIString`() = runTest {
        // Given
        val rectangle = W3WRectangle(
            W3WCoordinates(52.207988, 0.116126),
            W3WCoordinates(52.208867, 0.11754)
        )

        // When
        val result = rectangle.toAPIString()

        // Then
        assert(result == "52.207988,0.116126,52.208867,0.11754")
    }

    @Test
    fun `test W3WAutosuggestOptions_toQueryMap`() = runTest {
        // Arrange
        val options = W3WAutosuggestOptions.Builder()
            .focus(W3WCoordinates(51.4243877, -0.3474524))
            .language(W3WRFC5646Language.EN_GB)
            .nResults(4)
            .nFocusResults(4)
            .clipToCountry(W3WCountry("GB"))
            .clipToCircle(W3WCircle(W3WCoordinates(51.4243877, -0.3474524), W3WDistance(10.0)))
            .clipToBoundingBox(
                W3WRectangle(
                    W3WCoordinates(52.207988, 0.116126),
                    W3WCoordinates(52.208867, 0.11754)
                )
            )
            .clipToPolygon(
                W3WPolygon(
                    listOf(
                        W3WCoordinates(51.521, -0.343),
                        W3WCoordinates(52.6, 2.3324),
                        W3WCoordinates(54.234, 8.343),
                        W3WCoordinates(51.521, -0.343)
                    )
                )
            )
            .inputType(W3WAutosuggestInputType.GENERIC_VOICE)
            .preferLand(true)
            .includeCoordinates(true)
            .build()

        // Act
        val result = options.toQueryMap()

        // Assert
        assert(result["focus"] == "51.4243877,-0.3474524")
        assert(result["language"] == "en")
        assert(result["locale"] == null)
        assert(result["n-results"] == "4")
        assert(result["n-focus-results"] == "4")
        assert(result["clip-to-country"] == "GB")
        assert(result["clip-to-circle"] == "51.4243877,-0.3474524,10.0")
        assert(result["clip-to-bounding-box"] == "52.207988,0.116126,52.208867,0.11754")
        assert(result["clip-to-polygon"] == "51.521,-0.343,52.6,2.3324,54.234,8.343,51.521,-0.343")
        assert(result["input-type"] == "GENERIC_VOICE")
        assert(result["prefer-land"] == "true")
    }

    @Test
    fun `W3WLanguage toVoiceApiString returns cmn when w3wCode is zh`() = runTest {
        // Arrange
        val language = W3WRFC5646Language.ZH_HANS

        // Act
        val result = language.toVoiceApiString()

        // Assert
        assert(result == "cmn")
    }

    @Test
    fun `W3WLanguage toVoiceApiString returns w3wCode when it is not zh`() = runTest {
        // Given
        val language = W3WRFC5646Language.EN_GB

        // When
        val result = language.toVoiceApiString()

        // Then
        assert(result == "en")
    }

    @Test
    fun `W3WAudioStreamEncoding toApiString returns pcm_s16le when encoding is PCM_16BIT`() = runTest {
        // Arrange
        val encoding = W3WAndroidAudioStreamEncoding.PCM_16BIT

        // Act
        val result = encoding.toApiString()

        // Assert
        assert(result == "pcm_s16le")
    }

    @Test
    fun `W3WAudioStreamEncoding toApiString returns pcm_f32le when encoding is PCM_8BIT`() = runTest {
        // Arrange
        val encoding = W3WAndroidAudioStreamEncoding.PCM_8BIT

        // Act
        val result = encoding.toApiString()

        // Assert
        assert(result == "mulaw")
    }

    @Test
    fun `W3WAudioStreamEncoding toApiString returns pcm_f32le when encoding is PCM_FLOAT`() = runTest {
        // Arrange
        val encoding = object : W3WAudioStreamEncoding {
            override val value: Any
                get() = AudioFormat.ENCODING_PCM_FLOAT

        }

        // Act
        val result = encoding.toApiString()

        // Assert
        assert(result == "pcm_f32le")
    }

    @Test
    fun `W3WAudioStreamEncoding toApiString returns pcm_s16le when encoding is not PCM_16BIT, PCM_FLOAT or PCM_8BIT`() = runTest {
        // Given
        val encoding = object: W3WAudioStreamEncoding {
            override val value: Any
                get() = 999 // Some random value

        }

        // When
        val result = encoding.toApiString()

        // Then
        assert(result == "pcm_s16le")
    }


}