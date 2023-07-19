package com.what3words.androidwrapper.helpers

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build

internal object PackageManagerHelper {
    /**
     * Retrieves package information for the specified package name in a backward-compatible manner.
     *
     * This method fetches package information such as signatures, version codes, and other essential details related to the given package name.
     * It takes into account the changes in Android versions, utilizing appropriate flags to maintain compatibility across different SDK levels.
     *
     * @param packageName The package name for which to retrieve package information.
     * @param flags Additional option flags to modify the data returned.
     * @return A PackageInfo object containing details about the specified package or null if the package information is not available.
     */
    internal fun PackageManager.getPackageInfoCompat(
        packageName: String,
        flags: Int
    ): PackageInfo? {
        val list = listOf<String>()
        list.isEmpty()
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
        }

        return packageInfo
    }

    /**
     * Retrieves the package signatures in a backward-compatible manner.
     *
     * This method fetches the digital signatures associated with the package information, supporting compatibility across different Android SDK versions.
     * On devices running Android P (API level 28) and above, it leverages the modern signingInfo APIs to obtain signatures.
     * For devices running earlier versions of Android, it falls back to the deprecated signatures field for signature retrieval.
     *
     * @return An array of Signature objects or null if not available.
     * */

    internal fun PackageInfo.getSignaturesCompat(): Array<Signature?>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (signingInfo?.hasMultipleSigners() == true) {
                signingInfo?.apkContentsSigners
            } else {
                signingInfo?.signingCertificateHistory
            }
        } else {
            @Suppress("DEPRECATION") signatures
        }
    }

    /**
     * Retrieves the appropriate package signing flags in a backward-compatible manner.
     *
     * @return An integer value representing the package signing flags based on the device's Android version.
     * **/
    internal fun getSigningFlagsCompat(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
        }
    }
}