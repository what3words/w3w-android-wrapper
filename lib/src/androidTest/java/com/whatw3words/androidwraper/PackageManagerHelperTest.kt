package com.whatw3words.androidwraper

import android.content.pm.Signature
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.what3words.androidwrapper.helpers.PackageManagerHelper
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageInfoCompat
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageSignature
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getSignaturesCompat
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackageManagerHelperTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun testGetPackageSignature() {
        // Get the package signature
        val signature = context.getPackageSignature()

        // Assert that the signature is not null
        assertNotNull("Package signature should not be null", signature)
    }

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