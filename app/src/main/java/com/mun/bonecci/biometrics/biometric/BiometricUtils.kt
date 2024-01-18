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
     * Check whether the Device is Capable of the Biometric
     */
    private fun hasBiometricCapability(context: Context): Int {
        return BiometricManager.from(context)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK)
    }

    fun isBiometricReady(context: Context) =
        hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Initiate the Biometric Prompt
     */
    @Composable
    fun initBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricAuthListener
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticateError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                listener.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticateSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Display the Biometric Prompt
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

    //setting up a biometric
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