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
 * Storefront sign-up screen.
 *
 * Static UI only: "Create Account" performs no registration, it simply opens
 * the product catalogue. The "Log In" link returns to [MainActivity].
 */
class SignupActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = UiKit.dp(this, 28)

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiKit.BG)
            setPadding(pad, UiKit.dp(this@SignupActivity, 48), pad, pad)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = UiKit.heading(this, "Create account").apply {
            layoutParams = UiKit.rowParams(this@SignupActivity, bottomDp = 4)
        }
        val subtitle = UiKit.subheading(this, "Join NovaCart in a few seconds").apply {
            layoutParams = UiKit.rowParams(this@SignupActivity, bottomDp = 28)
        }

        val name = UiKit.field(this, "Full name").apply {
            layoutParams = UiKit.rowParams(this@SignupActivity, bottomDp = 14)
        }
        val email = UiKit.field(this, "Email address").apply {
            layoutParams = UiKit.rowParams(this@SignupActivity, bottomDp = 14)
        }
        val password = UiKit.field(this, "Password", password = true).apply {
            layoutParams = UiKit.rowParams(this@SignupActivity, bottomDp = 14)
        }
        val confirm = UiKit.field(this, "Confirm password", password = true).apply {
            layoutParams = UiKit.rowParams(this@SignupActivity, bottomDp = 24)
        }

        // Static UI: navigates to the catalogue, performs no real registration.
        val createButton = UiKit.primaryButton(this, "Create Account").apply {
            layoutParams = UiKit.rowParams(this@SignupActivity)
            setOnClickListener {
                startActivity(Intent(this@SignupActivity, ProductsActivity::class.java))
            }
        }

        val loginLink = UiKit.link(this, "Already have an account?  Log in").apply {
            setOnClickListener { finish() }
        }

        column.addView(title)
        column.addView(subtitle)
        column.addView(name)
        column.addView(email)
        column.addView(password)
        column.addView(confirm)
        column.addView(createButton)
        column.addView(loginLink)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(UiKit.BG)
            addView(column)
        }
        setContentView(scroll)
    }
}
