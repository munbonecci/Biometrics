package com.mun.bonecci.biometrics

import android.annotation.SuppressLint
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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.launch

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
@SuppressLint("RestrictedApi")
@Composable
fun LoginScreen(navController: NavHostController) {

    // State variables to store user input for name and age
    var email by remember { mutableStateOf(TextFieldValue()) }
    var isEmailValid by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var isPasswordValid by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isBiometricReady = BiometricUtils.isBiometricReady(context)
    var isLoginFromModal by remember { mutableStateOf(false) }

    // Execute the provided initialization function
    InitBiometrics(
        context = context as FragmentActivity,
        callback = object : BiometricAuthListener {
            override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
                when (error) {
                    BiometricPrompt.ERROR_USER_CANCELED -> {}
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {}
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {}
                }
            }

            override fun onAuthenticationFailed() {}

            override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
                if (isLoginFromModal) {
                    val token = "${email.text} ${password.text}"
                    BiometricUtils.encryptAndStoreServerToken(token, context, result)
                    goToResultScreen(navController, email.text, password.text)
                } else {
                    BiometricUtils.decryptServerToken(context, result).run {
                        goToResultScreen(navController, first, second)
                    }
                }
            }
        })

    LaunchedEffect(Unit) {
        launch { useBiometrics() }
    }

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
                isEmailValid = it.text.trim().isValidEmail()
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
                        isLoginFromModal = true
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
}

private fun goToResultScreen(navController: NavHostController, email: String, password: String) {
    navController.navigate("${NavigationItem.ResultScreen.route}/${email}/${password}")
}

@Composable
private fun InitBiometrics(context: FragmentActivity, callback: BiometricAuthListener) {
    biometricPrompt =
        BiometricUtils.initBiometricPrompt(context, callback)

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