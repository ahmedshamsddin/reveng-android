package com.ctf.mobile.deepdive

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Tiny programmatic styling helper shared by the storefront screens.
 *
 * The project intentionally ships no resource files (no XML layouts, no
 * drawables), so every screen is built in Kotlin. These helpers keep the look
 * consistent — a light, flat e-commerce theme — without pulling in any
 * dependencies.
 */
object UiKit {

    val BG = Color.parseColor("#F4F5FA")
    val CARD = Color.parseColor("#FFFFFF")
    val PRIMARY = Color.parseColor("#4A6CF7")
    val TEXT = Color.parseColor("#1A1A2E")
    val MUTED = Color.parseColor("#8A8FA3")
    val BORDER = Color.parseColor("#E2E4ED")

    /** Convert dp to px for the given context. */
    fun dp(ctx: Context, value: Int): Int =
        (value * ctx.resources.displayMetrics.density).toInt()

    /** A solid, optionally-outlined rounded rectangle drawable. */
    fun rounded(
        ctx: Context,
        fill: Int,
        radiusDp: Float,
        strokeColor: Int? = null,
        strokeDp: Int = 1
    ): GradientDrawable = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = radiusDp * ctx.resources.displayMetrics.density
        if (strokeColor != null) setStroke(dp(ctx, strokeDp), strokeColor)
    }

    /** Full-width vertical container with a bottom margin. */
    fun rowParams(ctx: Context, bottomDp: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(ctx, bottomDp) }

    /** A styled text input. */
    fun field(ctx: Context, hintText: String, password: Boolean = false): EditText =
        EditText(ctx).apply {
            hint = hintText
            setHintTextColor(MUTED)
            setTextColor(TEXT)
            textSize = 15f
            background = rounded(ctx, CARD, 12f, strokeColor = BORDER)
            val p = dp(ctx, 14)
            setPadding(p, p, p, p)
            if (password) {
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

    /** A filled primary action button. */
    fun primaryButton(ctx: Context, label: String): Button =
        Button(ctx).apply {
            text = label
            setAllCaps(false)
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, dp(ctx, 14), 0, dp(ctx, 14))
            background = rounded(ctx, PRIMARY, 12f)
            stateListAnimator = null
        }

    /** A centered, tappable text link (primary colour). */
    fun link(ctx: Context, label: String): TextView =
        TextView(ctx).apply {
            text = label
            textSize = 14f
            setTextColor(PRIMARY)
            gravity = Gravity.CENTER
            setPadding(0, dp(ctx, 16), 0, 0)
            isClickable = true
        }

    /** A heading label. */
    fun heading(ctx: Context, label: String): TextView =
        TextView(ctx).apply {
            text = label
            textSize = 26f
            setTextColor(TEXT)
            typeface = Typeface.DEFAULT_BOLD
        }

    /** A muted sub-heading label. */
    fun subheading(ctx: Context, label: String): TextView =
        TextView(ctx).apply {
            text = label
            textSize = 15f
            setTextColor(MUTED)
        }
}
