package com.ctf.mobile.deepdive

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Storefront login screen — the launcher activity and the app's visible face.
 *
 * This is a static UI only: the "Log In" button performs no authentication, it
 * just opens the product catalogue so the storefront can be browsed. It exists
 * as a distraction; the real challenge path is the hidden, exported
 * [AdminDashboardActivity] deep link found through static analysis.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = UiKit.dp(this, 28)

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(UiKit.BG)
            setPadding(pad, pad, pad, pad)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val brand = TextView(this).apply {
            text = "NovaCart"
            textSize = 34f
            setTextColor(UiKit.PRIMARY)
            gravity = Gravity.CENTER
        }

        val welcome = UiKit.heading(this, "Welcome back").apply {
            gravity = Gravity.CENTER
            layoutParams = UiKit.rowParams(this@MainActivity, bottomDp = 4).apply {
                topMargin = UiKit.dp(this@MainActivity, 24)
            }
        }
        val prompt = UiKit.subheading(this, "Sign in to continue shopping").apply {
            gravity = Gravity.CENTER
            layoutParams = UiKit.rowParams(this@MainActivity, bottomDp = 28)
        }

        val email = UiKit.field(this, "Email address").apply {
            layoutParams = UiKit.rowParams(this@MainActivity, bottomDp = 14)
        }
        val password = UiKit.field(this, "Password", password = true).apply {
            layoutParams = UiKit.rowParams(this@MainActivity, bottomDp = 8)
        }

        val forgot = TextView(this).apply {
            text = "Forgot password?"
            textSize = 13f
            setTextColor(UiKit.MUTED)
            gravity = Gravity.END
            layoutParams = UiKit.rowParams(this@MainActivity, bottomDp = 20)
        }

        // Static UI: navigates to the catalogue, performs no real authentication.
        val loginButton = UiKit.primaryButton(this, "Log In").apply {
            layoutParams = UiKit.rowParams(this@MainActivity)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, ProductsActivity::class.java))
            }
        }

        val signupLink = UiKit.link(this, "New here?  Create an account").apply {
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SignupActivity::class.java))
            }
        }

        column.addView(brand)
        column.addView(welcome)
        column.addView(prompt)
        column.addView(email)
        column.addView(password)
        column.addView(forgot)
        column.addView(loginButton)
        column.addView(signupLink)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(UiKit.BG)
            addView(column)
        }
        setContentView(scroll)
    }
}
