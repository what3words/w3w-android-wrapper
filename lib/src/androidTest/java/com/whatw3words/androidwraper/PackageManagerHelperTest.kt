package com.whatw3words.androidwraper

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.what3words.androidwrapper.helpers.PackageManagerHelper
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageInfoCompat
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getSignaturesCompat
import org.junit.Test

import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackageManagerHelperTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun testGetSignaturesCompatNotNull() {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = packageManager.getPackageInfoCompat(
            packageName = packageName,
            flags = PackageManagerHelper.getSigningFlagsCompat()
        )
        val packageSignatures = packageInfo?.getSignaturesCompat()

        assert(packageSignatures != null)
        assert(packageSignatures?.isNotEmpty() == true)
        assert(packageSignatures?.first() != null)
    }

}