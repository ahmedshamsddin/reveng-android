package com.ctf.mobile.deepdive

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.app.Activity
import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Hidden admin dashboard.
 *
 * This activity is exported and bound to the `ctf://deep-dive/unlock` deep link.
 * When launched with the correct administrative token it contacts the
 * player-supplied `backend` URL and renders the flag returned by that backend.
 *
 * The administrative token is not stored as a plaintext constant: it is held
 * XOR-obfuscated and Base64-encoded, and reconstructed in memory at runtime by
 * [resolveAdminToken]. A reverse engineer must read and replay that routine
 * (or hook it dynamically) rather than simply grepping the decompiled sources.
 *
 * The intended exploit:
 *
 *   adb shell am start -W -a android.intent.action.VIEW \
 *     -d "ctf://deep-dive/unlock?token=<ADMIN_TOKEN>&backend=http://10.0.2.2:5000"
 */
class AdminDashboardActivity : Activity() {

    companion object {
        // Administrative token, stored obfuscated so it is not a grep-able
        // plaintext constant in the decompiled APK. The value is the token bytes
        // XOR'd with SEED and then Base64-encoded; resolveAdminToken() reverses
        // this in memory at runtime.
        private const val DATA = "CQEUAjMCMUVeK0cGRwAAGARUVihtQgEC"
        private const val SEED = "d1v3_1nt0_th3_sh4d0w_r34lm"

        /** Reconstruct the plaintext administrative token at runtime. */
        private fun resolveAdminToken(): String {
            val raw = Base64.decode(DATA, Base64.DEFAULT)
            val key = SEED.toByteArray(Charsets.US_ASCII)
            val out = ByteArray(raw.size)
            for (i in raw.indices) {
                out[i] = (raw[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            return String(out, Charsets.US_ASCII)
        }
    }

    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var flagView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Build a simple scrollable dashboard layout in code.
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val header = TextView(this).apply {
            text = "Admin Dashboard"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        flagView = TextView(this).apply {
            text = "Awaiting authorization…"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextIsSelectable(true)
        }

        root.addView(header)
        root.addView(flagView)

        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)

        handleIntentData()
    }

    /**
     * Extract the `token` and `backend` query parameters from the launching
     * intent's data URI, validate the token, and — if valid — fetch the flag.
     */
    private fun handleIntentData() {
        val data = intent?.data
        if (data == null) {
            flagView.text = "No deep link data provided."
            return
        }

        val token = data.getQueryParameter("token")
        val backend = data.getQueryParameter("backend")

        if (token != resolveAdminToken()) {
            flagView.text = "Access denied: invalid administrative token."
            return
        }

        if (backend.isNullOrBlank()) {
            flagView.text = "Authorized, but no backend URL was supplied."
            return
        }

        flagView.text = "Token accepted. Contacting backend…"
        fetchFlag(backend)
    }

    /**
     * Asynchronously issue a GET request to `<backend>/verify-intent?token=...`
     * and, on an HTTP 200, parse the JSON body and render the `flag` field.
     */
    private fun fetchFlag(backend: String) {
        backgroundExecutor.execute {
            val result = runCatching {
                val base = backend.trimEnd('/')
                val url = URL(base + "/verify-intent?token=" + resolveAdminToken())

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                try {
                    val code = connection.responseCode
                    if (code == HttpURLConnection.HTTP_OK) {
                        val body = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(body)
                        json.optString("flag", "Flag field missing in response.")
                    } else {
                        "Backend rejected the request (HTTP $code)."
                    }
                } finally {
                    connection.disconnect()
                }
            }.getOrElse { error ->
                "Failed to reach backend: ${error.message}"
            }

            mainHandler.post { flagView.text = result }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundExecutor.shutdownNow()
    }
}
