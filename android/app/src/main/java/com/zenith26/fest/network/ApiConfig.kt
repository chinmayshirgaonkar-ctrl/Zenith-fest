package com.zenith26.fest.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Configuration for backend connection.
 * Default is http://10.0.2.2:8000/ which targets host machine's localhost from the Android emulator.
 * Can be changed at runtime if testing on a physical Android device on the same local Wi-Fi.
 */
object ApiConfig {
    // Live Cloudflare Tunnel URL - works universally across all physical phones and emulators
    private const val DEFAULT_EMULATOR_URL = "https://stars-authorization-cake-attachment.trycloudflare.com/"

    var baseUrl by mutableStateOf(DEFAULT_EMULATOR_URL)
        private set

    fun updateBaseUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        val formatted = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        baseUrl = formatted
        ApiClient.resetService()
    }
}
