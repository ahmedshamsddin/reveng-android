package com.ctf.mobile.deepdive

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Storefront product catalogue.
 *
 * Static UI only: a scrollable two-column grid of product cards. The "Add"
 * buttons are decorative and perform no action — there is no cart or checkout.
 */
class ProductsActivity : Activity() {

    private data class Product(val name: String, val price: String, val swatch: String)

    private val catalogue = listOf(
        Product("Aura Wireless Headphones", "$129", "#FFE0E9"),
        Product("Nimbus Running Shoes", "$89", "#E0F0FF"),
        Product("Pulse Smart Watch", "$199", "#E6E0FF"),
        Product("Trail Canvas Backpack", "$64", "#E0FFE9"),
        Product("Solstice Sunglasses", "$45", "#FFF3D6"),
        Product("Lumen 4K Camera", "$349", "#FFE0D6"),
        Product("Echo Bluetooth Speaker", "$59", "#D6FFF6"),
        Product("Glide Mechanical Keyboard", "$110", "#EDE0FF")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(UiKit.BG)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        screen.addView(buildTopBar())

        val pad = UiKit.dp(this, 16)
        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        // Section heading.
        grid.addView(TextView(this).apply {
            text = "Featured"
            textSize = 20f
            setTextColor(UiKit.TEXT)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = UiKit.rowParams(this@ProductsActivity, bottomDp = 12)
        })

        // Two cards per row.
        catalogue.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = UiKit.rowParams(this@ProductsActivity, bottomDp = 12)
            }
            pair.forEach { product -> row.addView(buildCard(product)) }
            // If the row has a single item, add a spacer so it stays half-width.
            if (pair.size == 1) row.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
            })
            grid.addView(row)
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(UiKit.BG)
            addView(grid)
        }
        screen.addView(scroll)

        setContentView(screen)
    }

    /** Top app bar with the store name and a decorative cart glyph. */
    private fun buildTopBar(): View {
        val pad = UiKit.dp(this, 16)
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(UiKit.CARD)
            setPadding(pad, pad, pad, pad)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(TextView(this@ProductsActivity).apply {
                text = "NovaCart"
                textSize = 22f
                setTextColor(UiKit.PRIMARY)
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(this@ProductsActivity).apply {
                text = "🛒"   // shopping cart emoji (decorative)
                textSize = 20f
            })
        }
    }

    /** A single half-width product card. */
    private fun buildCard(product: Product): View {
        val ctx = this
        val margin = UiKit.dp(ctx, 6)

        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.rounded(ctx, UiKit.CARD, 16f, strokeColor = UiKit.BORDER)
            val p = UiKit.dp(ctx, 12)
            setPadding(p, p, p, p)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = margin
                rightMargin = margin
            }
        }

        // Coloured image placeholder.
        card.addView(View(ctx).apply {
            background = UiKit.rounded(ctx, Color.parseColor(product.swatch), 12f)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiKit.dp(ctx, 110)
            ).apply { bottomMargin = UiKit.dp(ctx, 10) }
        })

        card.addView(TextView(ctx).apply {
            text = product.name
            textSize = 14f
            setTextColor(UiKit.TEXT)
            maxLines = 2
        })

        val priceRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = UiKit.dp(ctx, 8) }
        }
        priceRow.addView(TextView(ctx).apply {
            text = product.price
            textSize = 16f
            setTextColor(UiKit.PRIMARY)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        // Decorative "Add" pill — intentionally does nothing.
        priceRow.addView(TextView(ctx).apply {
            text = "Add"
            textSize = 13f
            setTextColor(Color.WHITE)
            background = UiKit.rounded(ctx, UiKit.PRIMARY, 10f)
            val hp = UiKit.dp(ctx, 12)
            val vp = UiKit.dp(ctx, 6)
            setPadding(hp, vp, hp, vp)
        })
        card.addView(priceRow)

        return card
    }
}
