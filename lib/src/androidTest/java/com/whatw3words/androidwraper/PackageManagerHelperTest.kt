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
    fun testSignatureDigest() {
        // Read the byte array from the package_signature_bytes.txt file in the assets folder
        val byteArray = context.assets.open("package_signature_bytes.txt")
            .bufferedReader()
            .readText()
            .replace(" ", "")
            .split(",")
            .map { it.toByte() }
            .toByteArray()

        // Create a Signature object from the byte array
        val signature = Signature(byteArray)

        // Expected SHA1 signature
        val expectedSHA1 = "79:57:BC:E7:48:D6:25:BC:8B:50:99:91:51:A3:8B:61:0F:F0:AF:23"

        // Assert that the PackageManagerHelper.signatureDigest returns an SHA1 signature that matches the expected signature
        assertEquals(expectedSHA1, PackageManagerHelper.signatureDigest(signature))
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