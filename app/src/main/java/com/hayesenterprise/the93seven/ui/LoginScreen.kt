@file:OptIn(ExperimentalMaterial3Api::class)

package com.hayesenterprise.the93seven

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    val scope = rememberCoroutineScope()

    var creating by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var showPw by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    fun submit() {
        error = null; info = null
        val e = email.trim()
        if (!e.contains("@") || !e.contains(".")) { error = "Enter a valid email address."; return }
        if (password.length < 6) { error = "Password must be at least 6 characters."; return }
        if (creating && password != confirm) { error = "Passwords don't match."; return }
        busy = true
        scope.launch {
            val result = if (creating) AuthManager.signUp(e, password) else AuthManager.signIn(e, password)
            busy = false
            when (result) {
                null -> {}  // success: AuthManager.user changed, the app switches screens by itself
                AuthManager.CHECK_EMAIL -> {
                    info = "Account created. Check your email to confirm it, then log in."
                    creating = false
                }
                else -> error = result
            }
        }
    }

    fun resetPassword() {
        error = null; info = null
        val e = email.trim()
        if (!e.contains("@")) { error = "Type your email above first, then tap Forgot password."; return }
        scope.launch {
            val result = AuthManager.resetPassword(e)
            if (result == null) info = "Password reset email sent. Check your inbox." else error = result
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Image(
            painterResource(R.drawable.logo_93seven), contentDescription = "93SEVEN Lifestyle",
            modifier = Modifier.fillMaxWidth().height(230.dp), contentScale = ContentScale.Fit
        )
        Text("Clock in to your lifestyle.", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Gold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))

        Text(if (creating) "Create your account" else "Log in", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Password") }, singleLine = true,
            visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth()
        )
        if (creating) {
            OutlinedTextField(
                value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm password") }, singleLine = true,
                visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth()
            )
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showPw, onCheckedChange = { showPw = it })
            Text("Show password", fontSize = 14.sp)
        }

        error?.let { Text(it, color = Over, fontSize = 13.sp, modifier = Modifier.fillMaxWidth()) }
        info?.let { Text(it, color = Gold, fontSize = 13.sp, modifier = Modifier.fillMaxWidth()) }

        Button(onClick = { submit() }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            else Text(if (creating) "CREATE ACCOUNT" else "LOG IN", fontWeight = FontWeight.Bold)
        }

        if (!creating) TextButton(onClick = { resetPassword() }) { Text("Forgot password?") }

        TextButton(onClick = { creating = !creating; error = null; info = null }) {
            Text(if (creating) "Already have an account? Log in" else "New here? Create an account")
        }
        Spacer(Modifier.height(16.dp))
    }
}