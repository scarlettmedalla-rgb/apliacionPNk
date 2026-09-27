package com.example.myapplication.util

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

object ThemeHelper {

    fun getThemePalette(context: Context): String {
        val prefs = context.getSharedPreferences("smarttemp_settings", Context.MODE_PRIVATE)
        return prefs.getString("app_theme_palette", "pink") ?: "pink"
    }

    fun setThemePalette(context: Context, palette: String) {
        val prefs = context.getSharedPreferences("smarttemp_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_theme_palette", palette).apply()
    }

    fun getPrimaryColor(context: Context): Int {
        return when (getThemePalette(context)) {
            "blue" -> Color.parseColor("#0EA5E9")
            "purple" -> Color.parseColor("#9333EA")
            "green" -> Color.parseColor("#10B981")
            else -> ContextCompat.getColor(context, R.color.pink_primary)
        }
    }

    fun getDarkColor(context: Context): Int {
        return when (getThemePalette(context)) {
            "blue" -> Color.parseColor("#0284C7")
            "purple" -> Color.parseColor("#7E22CE")
            "green" -> Color.parseColor("#047857")
            else -> Color.parseColor("#DB2777")
        }
    }

    fun getLightColor(context: Context): Int {
        return when (getThemePalette(context)) {
            "blue" -> Color.parseColor("#38BDF8")
            "purple" -> Color.parseColor("#C084FC")
            "green" -> Color.parseColor("#34D399")
            else -> Color.parseColor("#F472B6")
        }
    }

    fun getPastelColor(context: Context): Int {
        return when (getThemePalette(context)) {
            "blue" -> Color.parseColor("#E0F2FE")
            "purple" -> Color.parseColor("#F3E8FF")
            "green" -> Color.parseColor("#D1FAE5")
            else -> Color.parseColor("#FCE7F3")
        }
    }

    fun getBottomCardDrawable(context: Context): GradientDrawable {
        val dark = getDarkColor(context)
        val primary = getPrimaryColor(context)
        val light = getLightColor(context)
        val density = context.resources.displayMetrics.density
        return GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(dark, primary, light)
        ).apply {
            cornerRadii = floatArrayOf(
                36f * density, 36f * density, // top-left
                36f * density, 36f * density, // top-right
                0f, 0f,                       // bottom-right
                0f, 0f                        // bottom-left
            )
        }
    }

    fun getHeroCardDrawable(context: Context): GradientDrawable {
        val dark = getDarkColor(context)
        val primary = getPrimaryColor(context)
        val density = context.resources.displayMetrics.density
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(dark, primary)
        ).apply {
            cornerRadius = 24f * density
        }
    }

    fun getTagDrawable(context: Context): GradientDrawable {
        val pastel = getPastelColor(context)
        val density = context.resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * density
            setColor(pastel)
        }
    }

    fun applyThemeToActivity(activity: Activity) {
        val primaryColor = getPrimaryColor(activity)

        // Dynamically apply active palette background drawables
        activity.findViewById<View>(R.id.card_pink_register)?.background = getBottomCardDrawable(activity)
        activity.findViewById<View>(R.id.card_pink_login)?.background = getBottomCardDrawable(activity)
        activity.findViewById<View>(R.id.card_hero_pink)?.background = getHeroCardDrawable(activity)
        activity.findViewById<View>(R.id.card_pink_hero)?.background = getHeroCardDrawable(activity)

        // Tint FABs & Buttons
        activity.findViewById<FloatingActionButton>(R.id.fab_add_new)?.backgroundTintList = ColorStateList.valueOf(primaryColor)
        activity.findViewById<FloatingActionButton>(R.id.fab_add_sensor)?.backgroundTintList = ColorStateList.valueOf(primaryColor)

        activity.findViewById<MaterialButton>(R.id.btn_hero_sensors)?.setTextColor(primaryColor)

        val btnOptionRegister = activity.findViewById<MaterialButton>(R.id.btn_option_register)
        btnOptionRegister?.backgroundTintList = ColorStateList.valueOf(primaryColor)

        activity.findViewById<TextView>(R.id.tv_initial_title)?.setTextColor(primaryColor)
        activity.findViewById<TextView>(R.id.tv_welcome_title)?.setTextColor(primaryColor)
        activity.findViewById<TextView>(R.id.tv_register_title)?.setTextColor(primaryColor)

        activity.findViewById<ImageView>(R.id.iv_icon_active_user)?.imageTintList = ColorStateList.valueOf(primaryColor)
        activity.findViewById<ImageView>(R.id.iv_icon_active_sensor)?.imageTintList = ColorStateList.valueOf(primaryColor)
        activity.findViewById<ImageView>(R.id.iv_icon_dev)?.imageTintList = ColorStateList.valueOf(primaryColor)
    }
}
