package com.mun.bonecci.biometrics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Result Screen is a Composable function that displays a card with a welcome message
 *
 *
 * @param email The user's email received as a String.
 * @param password The user's password received as a String.
 */
@Composable
fun ResultScreen(email: String?, password: String?) {
    // Create a Card to provide visual styling
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Create a Box to center the content within the Card
        Box(
            modifier = Modifier
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Display a Text with a welcome message containing the user's email and password
            Text(
                text = "Welcome, $email! with Password: $password",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}