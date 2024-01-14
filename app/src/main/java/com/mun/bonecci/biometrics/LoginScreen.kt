package com.mun.bonecci.biometrics

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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mun.bonecci.biometrics.commons.isValidEmail
import com.mun.bonecci.biometrics.commons.isValidPassword
import com.mun.bonecci.biometrics.components.EmailTextField
import com.mun.bonecci.biometrics.components.PasswordTextField
import java.util.regex.Pattern

/**
 * [LoginScreen] is a composable that represents the first screen of the app.
 * It allows the user to input their email and password through text fields.
 * Upon clicking the "Login" button, it navigates to Result Screen passing
 * the entered user email and password as arguments.
 *
 * @param navController The navigation controller to handle navigation actions.
 */
@Composable
fun LoginScreen(navController: NavHostController) {

    // State variables to store user input for name and age
    var email by remember { mutableStateOf(TextFieldValue()) }
    var isEmailValid by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var isPasswordValid by remember { mutableStateOf(false) }

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
                enabled = isEmailValid && isPasswordValid,
                onClick = {

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