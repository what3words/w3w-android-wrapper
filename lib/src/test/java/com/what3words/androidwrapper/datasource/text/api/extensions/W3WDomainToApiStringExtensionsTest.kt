package com.what3words.androidwrapper.datasource.text.api.extensions

import com.what3words.androidwrapper.common.extensions.W3WDomainToApiStringExtensions.toAPIString
import com.what3words.core.types.domain.W3WCountry
import com.what3words.core.types.geometry.W3WCircle
import com.what3words.core.types.geometry.W3WCoordinates
import com.what3words.core.types.geometry.W3WDistance
import com.what3words.core.types.geometry.W3WPolygon
import com.what3words.core.types.geometry.W3WRectangle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
                W3WCoordinates(51.521,-0.343),
                W3WCoordinates(52.6,2.3324),
                W3WCoordinates(54.234,8.343),
                W3WCoordinates(51.521,-0.343),
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
            W3WCoordinates(51.4243877,-0.3474524),
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
            W3WCoordinates(52.207988,0.116126),
            W3WCoordinates(52.208867,0.11754)
        )

        // When
        val result = rectangle.toAPIString()

        // Then
        assert(result == "52.207988,0.116126,52.208867,0.11754")
    }
}