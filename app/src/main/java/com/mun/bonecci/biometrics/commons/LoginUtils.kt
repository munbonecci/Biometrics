package com.mun.bonecci.biometrics.commons

import androidx.compose.ui.text.input.TextFieldValue
import java.util.regex.Pattern

/**
 * Extension function to check if the [TextFieldValue] represents a valid password.
 */
fun TextFieldValue.isValidPassword(): Boolean {
    val password = text
    val passwordRegex =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")

    return password.matches((passwordRegex).toRegex())
}

/**
 * Extension function to check if the [CharSequence] represents a valid email address.
 */
fun CharSequence.isValidEmail(): Boolean {
    val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
    return emailRegex.matches(this)
}