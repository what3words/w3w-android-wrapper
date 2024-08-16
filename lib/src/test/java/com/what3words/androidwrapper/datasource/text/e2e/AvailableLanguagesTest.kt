package com.what3words.androidwrapper.datasource.text.e2e

import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_KEY
import com.what3words.androidwrapper.BuildConfig.PRE_PROD_API_URL
import com.what3words.androidwrapper.datasource.text.W3WApiTextDataSource
import com.what3words.core.types.common.W3WResult
import com.what3words.javawrapper.What3WordsV3
import org.junit.Assert
import org.junit.Test

class AvailableLanguagesTest {
    private val api = W3WApiTextDataSource.create(
        apiKey = PRE_PROD_API_KEY, endPoint = PRE_PROD_API_URL
    )

    @Test
    fun validAvailableLanguagesTest() {
        val al = api.availableLanguages()

        Assert.assertTrue(al is W3WResult.Success)
        al as W3WResult.Success

        var found = false
        for (l in al.value) {
            if (l.code.equals(
                    "mn",
                    ignoreCase = true
                ) && l.locale != null && l.locale!!.isNotEmpty()
            ) {
                found = true
            }
            if (l.code.equals(
                    "zh",
                    ignoreCase = true
                ) && l.locale != null && l.locale!!.isNotEmpty()
            ) {
                found = true
            }

            if (l.code.equals(
                    "oo",
                    ignoreCase = true
                ) && l.locale != null && l.locale!!.isNotEmpty()
            ) {
                found = true
            }
        }

        Assert.assertTrue("Failed to find locale list for zh or mn", found)
    }
}
