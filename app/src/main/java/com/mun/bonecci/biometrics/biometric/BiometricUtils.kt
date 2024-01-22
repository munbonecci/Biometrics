package com.mun.bonecci.biometrics.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricUtils {

    /**
     * Checks whether the device is capable of biometric authentication.
     *
     * @param context The context to retrieve biometric capability information.
     * @return A constant indicating the biometric capability status. Possible values:
     *         [BiometricManager.BIOMETRIC_SUCCESS] - The device supports biometric authentication.
     *         [BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE] - The device does not have biometric hardware.
     *         [BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE] - Biometric hardware is currently unavailable.
     *         [BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED] - The device does not support the required features.
     */
    private fun hasBiometricCapability(context: Context): Int {
        return BiometricManager.from(context)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK)
    }

    /**
     * Checks whether the device is ready for biometric authentication.
     *
     * @param context The context to check biometric readiness.
     * @return `true` if the device is ready for biometric authentication, otherwise `false`.
     */
    fun isBiometricReady(context: Context): Boolean =
        hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS


    /**
     * Initializes a [BiometricPrompt] instance for biometric authentication.
     *
     * @param activity The [FragmentActivity] context.
     * @param listener The listener for biometric authentication events.
     * @return Configured [BiometricPrompt] instance.
     */
    @Composable
    fun initBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricAuthListener
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            /**
             * Called when an error occurs during authentication.
             *
             * @param errorCode The error code representing the type of error.
             * @param errString A human-readable error message.
             */
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticateError(errorCode, errString.toString())
            }

            /**
             * Called when authentication fails.
             */
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                listener.onAuthenticationFailed()
            }

            /**
             * Called when authentication is successful.
             *
             * @param result The authentication result containing additional information.
             */
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticateSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }


    /**
     * Creates a BiometricPrompt.PromptInfo for displaying the biometric prompt.
     *
     * @param title The title to be displayed in the biometric prompt.
     * @param description The description to be displayed in the biometric prompt.
     * @param negativeText The text for the negative button in the biometric prompt.
     * @return Configured [BiometricPrompt.PromptInfo] instance.
     */
    fun createPromptInfo(
        title: String,
        description: String,
        negativeText: String,
    ): BiometricPrompt.PromptInfo =
        setBiometricPromptInfo(
            title,
            description,
            negativeText
        )

    /**
     * Sets up a BiometricPrompt.PromptInfo with the specified parameters.
     *
     * @param title The title to be displayed in the biometric prompt.
     * @param description The description to be displayed in the biometric prompt.
     * @param negativeText The text for the negative button in the biometric prompt.
     * @return Configured [BiometricPrompt.PromptInfo] instance.
     */
    private fun setBiometricPromptInfo(
        title: String,
        description: String,
        negativeText: String
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(negativeText)
            .setConfirmationRequired(false)
            .build()
    }


    /**
     * Encrypt and store credentials in preferences with cipher
     */
    fun encryptAndStoreServerToken(
        token: String,
        context: Context,
        authResult: BiometricPrompt.AuthenticationResult
    ) {
        authResult.cryptoObject?.cipher?.apply {
            val cryptographyManagerForEncryption = CryptographyManager()
            val encryptedServerTokenWrapper =
                cryptographyManagerForEncryption.encryptData(token, this)
            cryptographyManagerForEncryption.persistCiphertextWrapperToSharedPrefs(
                encryptedServerTokenWrapper,
                context,
                BiometricConstants.SHARED_PREFS_FILENAME,
                Context.MODE_PRIVATE,
                BiometricConstants.CIPHERTEXT_WRAPPER
            )
        }
    }

    /**
     * Decrypt credentials stored in preferences
     */
    fun decryptServerToken(
        context: Context,
        result: BiometricPrompt.AuthenticationResult
    ): Pair<String, String> {
        val cryptographyManager = CryptographyManager()
        val ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            context,
            BiometricConstants.SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            BiometricConstants.CIPHERTEXT_WRAPPER
        )
        var email = ""
        var password = ""

        ciphertextWrapper?.let { textWrapper ->
            result.cryptoObject?.cipher?.let {
                val plaintext =
                    cryptographyManager.decryptData(textWrapper.ciphertext, it)
                val userCredentials = plaintext.split(" ")
                email = userCredentials[0]
                password = userCredentials[1]
            }
        }
        return email to password
    }

}