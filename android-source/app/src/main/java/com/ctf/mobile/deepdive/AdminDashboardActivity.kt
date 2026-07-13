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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Hidden admin dashboard.
 *
 * This activity is exported and bound to the `ctf://deep-dive/unlock` deep link.
 * When launched with a valid administrative [ADMIN_TOKEN] it contacts the
 * player-supplied `backend` URL and renders the flag returned by that backend.
 *
 * The intended exploit:
 *
 *   adb shell am start -W -a android.intent.action.VIEW \
 *     -d "ctf://deep-dive/unlock?token=m0b1l3_1nt3nt_sp00f_2026&backend=http://10.0.2.2:5000"
 */
class AdminDashboardActivity : Activity() {

    companion object {
        // Hardcoded administrative token. Recoverable via static analysis of the
        // decompiled APK — this is the secret the player must find.
        private const val ADMIN_TOKEN = "m0b1l3_1nt3nt_sp00f_2026"

        // Endpoint (with token) that the app hits on the supplied backend.
        private const val VERIFY_PATH = "/verify-intent?token=$ADMIN_TOKEN"
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

        if (token != ADMIN_TOKEN) {
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
                val url = URL("$base$VERIFY_PATH")

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
