package com.mun.bonecci.biometrics

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.mun.bonecci.biometrics.biometric.BiometricAuthListener
import com.mun.bonecci.biometrics.biometric.BiometricConstants.BIOMETRIC_ENCRYPTION_KEY
import com.mun.bonecci.biometrics.biometric.BiometricConstants.CIPHERTEXT_WRAPPER
import com.mun.bonecci.biometrics.biometric.BiometricConstants.SHARED_PREFS_FILENAME
import com.mun.bonecci.biometrics.biometric.BiometricUtils
import com.mun.bonecci.biometrics.biometric.CiphertextWrapper
import com.mun.bonecci.biometrics.biometric.CryptographyManager
import com.mun.bonecci.biometrics.commons.isValidEmail
import com.mun.bonecci.biometrics.commons.isValidPassword
import com.mun.bonecci.biometrics.components.EmailTextField
import com.mun.bonecci.biometrics.components.PasswordTextField
import com.mun.bonecci.biometrics.navigation.NavigationItem

private lateinit var biometricPrompt: BiometricPrompt
private val cryptographyManager = CryptographyManager()
private lateinit var promptInfo: BiometricPrompt.PromptInfo
private var ciphertextWrapper: CiphertextWrapper? = null


/**
 * [LoginScreen] is a composable that represents the first screen of the app.
 * It allows the user to input their email and password through text fields.
 * Upon clicking the "Login" button, it navigates to Result Screen passing
 * the entered user email and password as arguments.
 *
 * @param navController The navigation controller to handle navigation actions.
 */
@Composable
fun LoginScreen(navController: NavHostController, initCallback: (BiometricAuthListener) -> Unit) {

    // State variables to store user input for name and age
    var email by remember { mutableStateOf(TextFieldValue()) }
    var isEmailValid by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var isPasswordValid by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isBiometricReady = BiometricUtils.isBiometricReady(context)
    var authenticationResult by remember {
        mutableStateOf<BiometricPrompt.AuthenticationResult?>(null)
    }
    var authenticationError by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var authenticationFailed by remember { mutableStateOf(false) }

    // Initialize the callback using the higher-order function
    val callback = remember {
        object : BiometricAuthListener {
            override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
                authenticationError = Pair(error, errMsg)
            }

            override fun onAuthenticationFailed() {
                authenticationFailed = true
            }

            override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
                authenticationResult = result
            }
        }
    }

    // Execute the provided initialization function
    initCallback(callback)
    InitBiometrics(context = context, callback = callback)
    useBiometrics()

    // Card composable for visual styling
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Column to arrange UI components vertically
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text field for entering user email
            EmailTextField(email = email, onEmailChange = {
                email = it
                isEmailValid = it.text.isValidEmail()
            })

            // Text field for entering user password
            PasswordTextField(
                password = password,
                onPasswordChange = {
                    password = it
                    isPasswordValid = it.isValidPassword()
                },
            )

            // Button to navigate to Result Screen with entered user data
            Button(
                enabled = isEmailValid && isPasswordValid && isBiometricReady,
                onClick = {
                    if (isEmailValid && isPasswordValid) {
                        showBiometricPromptForEncryption(context = context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Login")
            }
        }
    }

    // Display authentication result or error
    authenticationResult?.let { result ->
        val token = "${email.text} ${password.text}"
        BiometricUtils.encryptAndStoreServerToken(token, context, result)
        navController.navigate("${NavigationItem.ResultScreen.route}/${email.text}/${password.text}")
    }

    authenticationError?.let { (errorCode: Int, errorMessage: String) ->
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED -> {}
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {}
            BiometricPrompt.ERROR_NO_BIOMETRICS -> {}
        }
    }

    if (authenticationFailed) {
        Text("Authentication Failed")
    }
}

@Composable
private fun InitBiometrics(context: Context, callback: BiometricAuthListener) {
    biometricPrompt =
        BiometricUtils.initBiometricPrompt(context as FragmentActivity, callback)

    promptInfo = BiometricUtils.createPromptInfo(
        title = "Biometric Example",
        description = "Touch your Fingerprint sensor",
        negativeText = "Cancel"
    )

    ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
        context,
        SHARED_PREFS_FILENAME,
        Context.MODE_PRIVATE,
        CIPHERTEXT_WRAPPER
    )
}

private fun useBiometrics() {
    if (ciphertextWrapper != null) showBiometricPromptForDecryption()
}

private fun showBiometricPromptForEncryption(
    context: Context, secretKeyName: String = BIOMETRIC_ENCRYPTION_KEY
) {
    if (BiometricUtils.isBiometricReady(context)) {
        val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}

private fun showBiometricPromptForDecryption(secretKeyName: String = BIOMETRIC_ENCRYPTION_KEY) {
    ciphertextWrapper?.let { textWrapper ->
        val cipher = cryptographyManager.getInitializedCipherForDecryption(
            secretKeyName, textWrapper.initializationVector
        )
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}