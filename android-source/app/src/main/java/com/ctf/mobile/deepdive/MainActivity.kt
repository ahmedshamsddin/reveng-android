package com.ctf.mobile.deepdive

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.app.Activity

/**
 * Decoy login screen.
 *
 * This is the first thing a player sees. It looks like a normal authentication
 * gate, but the credential check is a dead end: even the "correct" local
 * validation leads nowhere useful. The real path forward is the exported
 * [AdminDashboardActivity] deep link, discovered through static analysis.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "Deep Dive — Secure Portal"
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        val usernameField = EditText(this).apply {
            hint = "Username"
        }

        val passwordField = EditText(this).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val loginButton = Button(this).apply {
            text = "Login"
        }

        val status = TextView(this).apply {
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        loginButton.setOnClickListener {
            val user = usernameField.text.toString()
            val pass = passwordField.text.toString()

            if (isValidLogin(user, pass)) {
                // Deliberate dead end. Even a "successful" local login reveals
                // nothing — the flag never lives on this path.
                status.text = "Login accepted. No records available."
                Toast.makeText(this, "Welcome.", Toast.LENGTH_SHORT).show()
            } else {
                status.text = "Invalid Credentials"
            }
        }

        root.addView(title)
        root.addView(usernameField)
        root.addView(passwordField)
        root.addView(loginButton)
        root.addView(status)

        setContentView(root)
    }

    /**
     * A deliberately convoluted local validation that acts as a rabbit hole for
     * reverse engineers. Satisfying it does NOT surface the flag; the credential
     * path is a distraction from the real intent-spoofing vulnerability.
     */
    private fun isValidLogin(username: String, password: String): Boolean {
        if (username != "administrator") return false
        if (password.length != 16) return false

        // XOR-based obfuscated comparison — pure noise to keep analysts busy.
        val key = "deepdive".toByteArray()
        val transformed = StringBuilder()
        for (i in password.indices) {
            val c = password[i].code xor key[i % key.size].toInt()
            transformed.append(c.toString(16).padStart(2, '0'))
        }

        val expected = "5f5a5e5b5a565f5a5f5a5e5b5a565f5a"
        return transformed.toString() == expected
    }
}
