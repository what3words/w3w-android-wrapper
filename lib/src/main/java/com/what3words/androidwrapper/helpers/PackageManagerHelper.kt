package com.what3words.androidwrapper.helpers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

internal object PackageManagerHelper {

    /**
     * Retrieves the SHA1 signature of the package associated with this [Context].
     *
     * @return The SHA1 signature of the package, or null if an error occurs during retrieval.
     */
    internal fun Context.getPackageSignature(): String? {
        return try {
            val packageManager: PackageManager = packageManager
            val packageName: String = packageName
            val packageInfo: PackageInfo? =
                packageManager.getPackageInfoCompat(
                    packageName,
                    getSigningFlagsCompat()
                )
            val packageSignatures: Array<Signature?>? = packageInfo?.getSignaturesCompat()

            if (packageSignatures.isNullOrEmpty() || packageSignatures[0] == null) {
                null
            } else {
                signatureDigest(packageSignatures[0])
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null // Package not found
        } catch (e: Exception) {
            null // Other exceptions
        }
    }

    /**
     * Computes the SHA1 digest of the given [Signature].
     *
     * @param sig The signature whose digest is to be computed.
     * @return The SHA1 digest of the signature, or null if the signature is null or if an error occurs during computation.
     */
    internal fun signatureDigest(sig: Signature?): String? {
        sig ?: return null

        return try {
            val md = MessageDigest.getInstance("SHA1")
            val sha1Hash = md.digest(sig.toByteArray())

            val result = StringBuilder()
            for ((index, byte) in sha1Hash.withIndex()) {
                val formatResult = if (index != sha1Hash.lastIndex) {
                    String.format("%02X:", byte)
                } else {
                    String.format("%02X", byte) // Don't append : to the last entry
                }
                result.append(formatResult)
            }
            result.toString()
        } catch (e: NoSuchAlgorithmException) {
            null // SHA1 algorithm not available
        } catch (e: Exception) {
            null // Other exceptions
        }
    }

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