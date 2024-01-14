package com.mun.bonecci.biometrics.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mun.bonecci.biometrics.commons.isValidEmail

/**
 * Jetpack Compose component for an email text field with validation.
 *
 * @param email The current value of the email text field.
 * @param onEmailChange The callback to be invoked when the email value changes.
 * @param errorColor The color to be used for displaying error messages.
 * @param textFieldLabel The label for the email text field.
 * @param errorText The error message to be displayed when the email is not valid.
 */
@Composable
fun EmailTextField(
    email: TextFieldValue,
    onEmailChange: (TextFieldValue) -> Unit,
    errorColor: Color = MaterialTheme.colorScheme.error,
    textFieldLabel: String = "Enter your Email",
    errorText: String = "Email not valid"
) {
    // State variable to track email validity
    var isEmailError by remember { mutableStateOf(true) }

    // TextField for entering user email
    TextField(
        value = email,
        onValueChange = {
            onEmailChange(it)
            isEmailError = it.text.isValidEmail()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        isError = !isEmailError,
        supportingText = {
            // Display error text if the email is not valid
            if (!isEmailError) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = errorText,
                    color = errorColor
                )
            }
        },
        label = { Text(textFieldLabel) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}