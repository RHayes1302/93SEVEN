package com.hayesenterprise.the93seven

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL



private const val SUPABASE_URL = "https://puyumqmmkxpiywtdoamc.supabase.co"
private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB1eXVtcW1ta3hwaXl3dGRvYW1jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTUwNDg3OTksImV4cCI6MjExMDYyNDc5OX0.rfHCARVcBTGwAH5LXkRzvy2s9tjZMFIYBmAJnT7O-e0"

data class AuthUser(val id: String, val email: String?)

/**
 * Email + password login through Supabase Auth (plain HTTPS calls, no extra libraries).
 * The signed-in user is kept in Compose state, so the UI switches between login and the app.
 */
object AuthManager {
    var user by mutableStateOf<AuthUser?>(null)
        private set

    private var appContext: Context? = null
    private fun prefs() = appContext!!.getSharedPreferences("the93seven_auth", Context.MODE_PRIVATE)

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val id = prefs().getString("uid", null)
        if (id != null) user = AuthUser(id, prefs().getString("email", null))
    }

    fun signOut() {
        prefs().edit().clear().apply()
        user = null
    }

    /** Returns null on success, or a friendly error message. */
    suspend fun signIn(email: String, password: String): String? {
        if (notConfigured()) return CONFIG_MSG
        val (code, json) = try {
            post("/auth/v1/token?grant_type=password", JSONObject().put("email", email).put("password", password))
        } catch (e: Exception) { return "No internet connection. Check your connection and try again." }
        if (code !in 200..299) return friendly(json)
        return if (saveSession(json)) null else "Sign-in worked but the response was unexpected. Try again."
    }

    /** Returns null on success (logged in), "CHECK_EMAIL" if the project requires email confirmation, or an error. */
    suspend fun signUp(email: String, password: String): String? {
        if (notConfigured()) return CONFIG_MSG
        val (code, json) = try {
            post("/auth/v1/signup", JSONObject().put("email", email).put("password", password))
        } catch (e: Exception) { return "No internet connection. Check your connection and try again." }
        if (code !in 200..299) return friendly(json)
        // With "Confirm email" OFF Supabase returns a session right away; with it ON there is no token yet.
        return if (saveSession(json)) null else CHECK_EMAIL
    }

    suspend fun resetPassword(email: String): String? {
        if (notConfigured()) return CONFIG_MSG
        val (code, json) = try {
            post("/auth/v1/recover", JSONObject().put("email", email))
        } catch (e: Exception) { return "No internet connection. Check your connection and try again." }
        return if (code in 200..299) null else friendly(json)
    }

    const val CHECK_EMAIL = "CHECK_EMAIL"
    private const val CONFIG_MSG = "Supabase isn't set up yet. Paste your project URL and anon key at the top of AuthManager.kt."

    private fun notConfigured() = SUPABASE_URL.contains("YOUR-") || SUPABASE_ANON_KEY.contains("YOUR-")

    private fun saveSession(json: JSONObject?): Boolean {
        val u = json?.optJSONObject("user") ?: return false
        if (json.optString("access_token").isEmpty()) return false
        val id = u.optString("id")
        if (id.isEmpty()) return false
        val email = u.optString("email").ifEmpty { null }
        prefs().edit().putString("uid", id).putString("email", email).apply()
        user = AuthUser(id, email)
        return true
    }

    private suspend fun post(path: String, body: JSONObject): Pair<Int, JSONObject?> = withContext(Dispatchers.IO) {
        val conn = URL(SUPABASE_URL + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.doOutput = true
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            code to (try { JSONObject(text) } catch (e: Exception) { null })
        } finally {
            conn.disconnect()
        }
    }

    private fun friendly(json: JSONObject?): String {
        val raw = json?.optString("msg").orEmpty().ifEmpty { json?.optString("error_description").orEmpty() }
            .ifEmpty { json?.optString("message").orEmpty() }
            .ifEmpty { json?.optString("error").orEmpty() }
        return when {
            raw.contains("Invalid login", true) -> "Wrong email or password."
            raw.contains("already registered", true) -> "That email already has an account. Try logging in."
            raw.contains("not confirmed", true) -> "Please confirm your email first. Check your inbox."
            raw.contains("rate limit", true) -> "Too many attempts. Wait a minute and try again."
            raw.contains("at least", true) && raw.contains("character", true) -> "Password must be at least 6 characters."
            raw.isEmpty() -> "Something went wrong. Please try again."
            else -> raw
        }
    }
}