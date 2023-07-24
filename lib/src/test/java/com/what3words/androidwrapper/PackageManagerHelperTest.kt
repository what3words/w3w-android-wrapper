package com.what3words.androidwrapper

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.test.platform.app.InstrumentationRegistry
import com.what3words.androidwrapper.helpers.PackageManagerHelper
import com.what3words.androidwrapper.helpers.PackageManagerHelper.getPackageInfoCompat
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
}