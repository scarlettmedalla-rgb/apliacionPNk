package com.example.myapplication.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.example.myapplication.R

object ThemeHelper {

    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences("smarttemp_settings", Context.MODE_PRIVATE)
        return prefs.getString("app_theme_palette", "pink") ?: "pink"
    }

    fun getPrimaryColor(context: Context): Int {
        return when (getTheme(context)) {
            "blue" -> Color.parseColor("#38BDF8")
            "purple" -> Color.parseColor("#C084FC")
            "green" -> Color.parseColor("#10B981")
            else -> ContextCompat.getColor(context, R.color.pink_primary)
        }
    }

    fun getDarkColor(context: Context): Int {
        return when (getTheme(context)) {
            "blue" -> Color.parseColor("#0284C7")
            "purple" -> Color.parseColor("#9333EA")
            "green" -> Color.parseColor("#047857")
            else -> ContextCompat.getColor(context, R.color.pink_primary_dark)
        }
    }

    fun getPastelColor(context: Context): Int {
        return when (getTheme(context)) {
            "blue" -> Color.parseColor("#E0F2FE")
            "purple" -> Color.parseColor("#F3E8FF")
            "green" -> Color.parseColor("#D1FAE5")
            else -> ContextCompat.getColor(context, R.color.pink_pastel_soft)
        }
    }

    fun createGradientDrawable(context: Context, angle: Int = 135): GradientDrawable {
        val primary = getPrimaryColor(context)
        val dark = getDarkColor(context)
        val pastel = getPastelColor(context)
        val orientation = when (angle) {
            90 -> GradientDrawable.Orientation.TOP_BOTTOM
            else -> GradientDrawable.Orientation.TL_BR
        }
        return GradientDrawable(orientation, intArrayOf(dark, primary, pastel)).apply {
            cornerRadius = 36f * context.resources.displayMetrics.density
        }
    }

    fun createHeroGradientDrawable(context: Context): GradientDrawable {
        val primary = getPrimaryColor(context)
        val dark = getDarkColor(context)
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(dark, primary)).apply {
            cornerRadius = 24f * context.resources.displayMetrics.density
        }
    }

    fun createTagDrawable(context: Context): GradientDrawable {
        val pastel = getPastelColor(context)
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * context.resources.displayMetrics.density
            setColor(pastel)
        }
    }
}
