package com.what3words.androidwrapper

import android.content.pm.Signature
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.test.platform.app.InstrumentationRegistry
import com.what3words.androidwrapper.helpers.PackageManagerHelper
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageInfoCompat
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageSignature
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [VERSION_CODES.P, VERSION_CODES.Q, VERSION_CODES.N, VERSION_CODES.TIRAMISU, VERSION_CODES.S])
class PackageManagerHelperTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun testGetPackageInfoNotNull() {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = packageManager.getPackageInfoCompat(
            packageName = packageName,
            flags = PackageManagerHelper.getSigningFlagsCompat()
        )
        assert(packageInfo != null)
    }

    @Test
    fun testGetSigningFlagsCompat() {
        val signingFlags = PackageManagerHelper.getSigningFlagsCompat()
        if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            assertEquals(0x08000000, signingFlags)
        } else {
            assertEquals(0x00000040, signingFlags)
        }
    }

    @Test
    fun `test SignatureDigest with valid signature`() {
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

        // Invoke the signatureDigest function with the valid Signature object
        val actualSHA1 = PackageManagerHelper.signatureDigest(signature)

        // Assert that the PackageManagerHelper.signatureDigest returns an SHA1 signature that matches the expected signature
        assertEquals(expectedSHA1, actualSHA1)
    }

    @Test
    fun `test SignatureDigest with empty signature`() {
        // Create an empty Signature object
        val signature = Signature(byteArrayOf())

        // Expected SHA1 signature for an empty byte array
        val expectedSHA1 = "DA:39:A3:EE:5E:6B:4B:0D:32:55:BF:EF:95:60:18:90:AF:D8:07:09"

        // Invoke the signatureDigest function with the empty Signature object
        val actualSHA1 = PackageManagerHelper.signatureDigest(signature)

        // Assert that the actual SHA1 signature matches the expected SHA1 signature
        assertEquals(expectedSHA1, actualSHA1)
    }

    @Test
    fun `test SignatureDigest with null signature`() {
        // Set the Signature object to null
        val signature: Signature? = null

        // Expected SHA1 signature for a null signature
        val expectedSHA1: String? = null

        // Invoke the signatureDigest function with the null Signature object
        val actualSHA1 = PackageManagerHelper.signatureDigest(signature)

        // Assert that the actual SHA1 signature matches the expected SHA1 signature
        assertEquals(expectedSHA1, actualSHA1)
    }
}