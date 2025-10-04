package com.ssfdre38.cpcli.android.client.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * Modern UI Manager that creates consistent, universal UI elements without relying on XML themes
 * This provides a Material Design 3 inspired look that works across all devices and Android versions
 */
object ModernUIManager {

    // Color scheme that adapts to system dark mode
    object Colors {
        // Primary colors
        const val PRIMARY_LIGHT = 0xFF1976D2.toInt()
        const val PRIMARY_DARK = 0xFF64B5F6.toInt()
        const val PRIMARY_CONTAINER_LIGHT = 0xFFE3F2FD.toInt()
        const val PRIMARY_CONTAINER_DARK = 0xFF1565C0.toInt()
        
        // Surface colors
        const val SURFACE_LIGHT = 0xFFFFFFFF.toInt()
        const val SURFACE_DARK = 0xFF121212.toInt()
        const val SURFACE_VARIANT_LIGHT = 0xFFF5F5F5.toInt()
        const val SURFACE_VARIANT_DARK = 0xFF2A2A2A.toInt()
        
        // Text colors
        const val ON_SURFACE_LIGHT = 0xFF212121.toInt()
        const val ON_SURFACE_DARK = 0xFFE1E1E1.toInt()
        const val ON_PRIMARY = 0xFFFFFFFF.toInt()
        
        // Status colors
        const val SUCCESS = 0xFF4CAF50.toInt()
        const val ERROR = 0xFFF44336.toInt()
        const val WARNING = 0xFFFF9800.toInt()
        
        // Accent colors
        const val ACCENT = 0xFF9C27B0.toInt()
        const val SECONDARY = 0xFF4CAF50.toInt()
    }

    // Spacing constants in dp
    object Spacing {
        const val EXTRA_SMALL = 4
        const val SMALL = 8
        const val MEDIUM = 16
        const val LARGE = 24
        const val EXTRA_LARGE = 32
        const val BUTTON_HEIGHT = 48
        const val MINIMUM_TOUCH_TARGET = 48
    }

    // Typography constants in sp
    object Typography {
        const val HEADLINE_LARGE = 32f
        const val HEADLINE_MEDIUM = 28f
        const val HEADLINE_SMALL = 24f
        const val TITLE_LARGE = 22f
        const val TITLE_MEDIUM = 18f
        const val TITLE_SMALL = 16f
        const val BODY_LARGE = 16f
        const val BODY_MEDIUM = 14f
        const val BODY_SMALL = 12f
        const val LABEL_LARGE = 14f
        const val LABEL_MEDIUM = 12f
        const val LABEL_SMALL = 10f
    }

    /**
     * Checks if the device is in dark mode
     */
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Gets the appropriate color based on current theme
     */
    fun getColor(context: Context, lightColor: Int, darkColor: Int): Int {
        return if (isDarkMode(context)) darkColor else lightColor
    }

    /**
     * Converts dp to pixels
     */
    fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    /**
     * Creates a modern rounded rectangle drawable
     */
    fun createRoundedRectDrawable(
        color: Int,
        cornerRadius: Float = 12f,
        strokeWidth: Int = 0,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            this.cornerRadius = cornerRadius
            if (strokeWidth > 0) {
                setStroke(strokeWidth, strokeColor)
            }
        }
    }

    /**
     * Creates a state list drawable for buttons with pressed and normal states
     */
    fun createButtonStateDrawable(
        context: Context,
        normalColor: Int,
        pressedColor: Int,
        cornerRadius: Float = 12f
    ): StateListDrawable {
        val stateList = StateListDrawable()
        
        // Pressed state
        stateList.addState(
            intArrayOf(android.R.attr.state_pressed),
            createRoundedRectDrawable(pressedColor, cornerRadius)
        )
        
        // Normal state
        stateList.addState(
            intArrayOf(),
            createRoundedRectDrawable(normalColor, cornerRadius)
        )
        
        return stateList
    }

    /**
     * Applies modern button styling
     */
    fun styleButton(
        context: Context,
        button: Button,
        buttonType: ButtonType = ButtonType.PRIMARY
    ) {
        val isDark = isDarkMode(context)
        
        when (buttonType) {
            ButtonType.PRIMARY -> {
                val normalColor = getColor(context, Colors.PRIMARY_LIGHT, Colors.PRIMARY_DARK)
                val pressedColor = Color.argb(
                    200,
                    Color.red(normalColor),
                    Color.green(normalColor),
                    Color.blue(normalColor)
                )
                button.background = createButtonStateDrawable(context, normalColor, pressedColor)
                button.setTextColor(Colors.ON_PRIMARY)
            }
            ButtonType.SECONDARY -> {
                val normalColor = getColor(context, Colors.SURFACE_VARIANT_LIGHT, Colors.SURFACE_VARIANT_DARK)
                val pressedColor = Color.argb(
                    150,
                    Color.red(normalColor),
                    Color.green(normalColor),
                    Color.blue(normalColor)
                )
                val strokeColor = getColor(context, Colors.PRIMARY_LIGHT, Colors.PRIMARY_DARK)
                val background = createRoundedRectDrawable(normalColor, 12f, dpToPx(context, 1), strokeColor)
                button.background = background
                button.setTextColor(getColor(context, Colors.ON_SURFACE_LIGHT, Colors.ON_SURFACE_DARK))
            }
            ButtonType.TEXT -> {
                button.background = null
                button.setTextColor(getColor(context, Colors.PRIMARY_LIGHT, Colors.PRIMARY_DARK))
            }
        }
        
        // Common button styling
        button.textSize = Typography.LABEL_LARGE
        button.isAllCaps = false
        button.setPadding(
            dpToPx(context, Spacing.LARGE),
            dpToPx(context, Spacing.MEDIUM),
            dpToPx(context, Spacing.LARGE),
            dpToPx(context, Spacing.MEDIUM)
        )
        button.minHeight = dpToPx(context, Spacing.BUTTON_HEIGHT)
        button.elevation = if (buttonType == ButtonType.PRIMARY) dpToPx(context, 2).toFloat() else 0f
    }

    /**
     * Applies modern text input styling
     */
    fun styleEditText(context: Context, editText: EditText) {
        val isDark = isDarkMode(context)
        
        // Background
        val backgroundColor = getColor(context, Colors.SURFACE_VARIANT_LIGHT, Colors.SURFACE_VARIANT_DARK)
        val strokeColor = getColor(context, Colors.PRIMARY_LIGHT, Colors.PRIMARY_DARK)
        editText.background = createRoundedRectDrawable(backgroundColor, 8f, dpToPx(context, 1), strokeColor)
        
        // Text styling
        editText.setTextColor(getColor(context, Colors.ON_SURFACE_LIGHT, Colors.ON_SURFACE_DARK))
        editText.setHintTextColor(Color.argb(
            120,
            Color.red(getColor(context, Colors.ON_SURFACE_LIGHT, Colors.ON_SURFACE_DARK)),
            Color.green(getColor(context, Colors.ON_SURFACE_LIGHT, Colors.ON_SURFACE_DARK)),
            Color.blue(getColor(context, Colors.ON_SURFACE_LIGHT, Colors.ON_SURFACE_DARK))
        ))
        editText.textSize = Typography.BODY_LARGE
        
        // Padding
        editText.setPadding(
            dpToPx(context, Spacing.MEDIUM),
            dpToPx(context, Spacing.MEDIUM),
            dpToPx(context, Spacing.MEDIUM),
            dpToPx(context, Spacing.MEDIUM)
        )
    }

    /**
     * Applies modern text view styling
     */
    fun styleTextView(
        context: Context,
        textView: TextView,
        textType: TextType = TextType.BODY_LARGE
    ) {
        val textColor = getColor(context, Colors.ON_SURFACE_LIGHT, Colors.ON_SURFACE_DARK)
        textView.setTextColor(textColor)
        
        when (textType) {
            TextType.HEADLINE_LARGE -> {
                textView.textSize = Typography.HEADLINE_LARGE
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.HEADLINE_MEDIUM -> {
                textView.textSize = Typography.HEADLINE_MEDIUM
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.HEADLINE_SMALL -> {
                textView.textSize = Typography.HEADLINE_SMALL
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.TITLE_LARGE -> {
                textView.textSize = Typography.TITLE_LARGE
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.TITLE_MEDIUM -> {
                textView.textSize = Typography.TITLE_MEDIUM
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.TITLE_SMALL -> {
                textView.textSize = Typography.TITLE_SMALL
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.BODY_LARGE -> {
                textView.textSize = Typography.BODY_LARGE
            }
            TextType.BODY_MEDIUM -> {
                textView.textSize = Typography.BODY_MEDIUM
            }
            TextType.BODY_SMALL -> {
                textView.textSize = Typography.BODY_SMALL
            }
            TextType.LABEL_LARGE -> {
                textView.textSize = Typography.LABEL_LARGE
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.LABEL_MEDIUM -> {
                textView.textSize = Typography.LABEL_MEDIUM
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            TextType.LABEL_SMALL -> {
                textView.textSize = Typography.LABEL_SMALL
                textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
        }
    }

    /**
     * Creates a modern card-like container
     */
    fun createCard(context: Context): GradientDrawable {
        val cardColor = getColor(context, Colors.SURFACE_LIGHT, Colors.SURFACE_DARK)
        return createRoundedRectDrawable(cardColor, 16f).apply {
            // Add subtle elevation effect
            if (!isDarkMode(context)) {
                setStroke(dpToPx(context, 1), Color.argb(20, 0, 0, 0))
            }
        }
    }

    /**
     * Applies modern container styling
     */
    fun styleContainer(context: Context, container: ViewGroup) {
        container.background = createCard(context)
        container.setPadding(
            dpToPx(context, Spacing.MEDIUM),
            dpToPx(context, Spacing.MEDIUM),
            dpToPx(context, Spacing.MEDIUM),
            dpToPx(context, Spacing.MEDIUM)
        )
        container.elevation = dpToPx(context, 1).toFloat()
    }

    /**
     * Sets the window background to match the current theme
     */
    fun setWindowBackground(context: Context, view: View) {
        val backgroundColor = getColor(context, Colors.SURFACE_LIGHT, Colors.SURFACE_DARK)
        view.setBackgroundColor(backgroundColor)
    }

    /**
     * Creates status indicator drawable
     */
    fun createStatusIndicator(context: Context, status: StatusType): GradientDrawable {
        val color = when (status) {
            StatusType.SUCCESS -> Colors.SUCCESS
            StatusType.ERROR -> Colors.ERROR
            StatusType.WARNING -> Colors.WARNING
            StatusType.CONNECTING -> Colors.WARNING
        }
        return createRoundedRectDrawable(color, 50f) // Circular indicator
    }

    enum class ButtonType {
        PRIMARY, SECONDARY, TEXT
    }

    enum class TextType {
        HEADLINE_LARGE, HEADLINE_MEDIUM, HEADLINE_SMALL,
        TITLE_LARGE, TITLE_MEDIUM, TITLE_SMALL,
        BODY_LARGE, BODY_MEDIUM, BODY_SMALL,
        LABEL_LARGE, LABEL_MEDIUM, LABEL_SMALL
    }

    enum class StatusType {
        SUCCESS, ERROR, WARNING, CONNECTING
    }
}