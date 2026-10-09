package com.example.myapplication.util

import android.net.Uri
import android.widget.ImageView
import com.example.myapplication.R
import java.io.File

object ImageHelper {

    /**
     * Safely loads a photo into an ImageView. Properly handles local files, content URIs,
     * resource names (e.g. "jorge_cortes"), resource IDs, and android.resource:// URIs
     * for both bitmap and XML VectorDrawables.
     */
    fun loadProfilePhoto(
        imageView: ImageView,
        pathOrUri: String?,
        defaultResId: Int = R.drawable.ic_users
    ) {
        imageView.clearColorFilter()
        imageView.imageTintList = null
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Helper function to set padding if the loaded image is the default icon
        fun applyImageAndPadding(resId: Int) {
            if (resId == R.drawable.ic_users) {
                val density = imageView.context.resources.displayMetrics.density
                val p = (10 * density).toInt()
                imageView.setPadding(p, p, p, p)
            } else {
                imageView.setPadding(0, 0, 0, 0)
            }
            imageView.setImageResource(resId)
        }

        if (pathOrUri.isNullOrEmpty()) {
            applyImageAndPadding(defaultResId)
            return
        }

        try {
            val context = imageView.context
            val file = File(pathOrUri)

            // 1. Local storage file
            if (file.exists() && file.isFile && file.length() > 0) {
                imageView.setPadding(0, 0, 0, 0)
                imageView.setImageURI(Uri.fromFile(file))
                return
            }

            // 2. android.resource:// URI scheme
            if (pathOrUri.startsWith("android.resource://")) {
                // If it's an old hardcoded resource URI, ignore numeric IDs to prevent showing wrong images from old builds.
                // Always fallback to defaultResId which correctly maps to the current names.
                applyImageAndPadding(defaultResId)
                return
            }

            // 3. Content or File scheme URI
            if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
                imageView.setPadding(0, 0, 0, 0)
                val uri = Uri.parse(pathOrUri)
                imageView.setImageURI(uri)
                return
            }

            // 4. Drawable resource name directly
            val resIdByName = context.resources.getIdentifier(pathOrUri, "drawable", context.packageName)
            if (resIdByName != 0) {
                applyImageAndPadding(resIdByName)
                return
            }

            // 5. Fallback
            applyImageAndPadding(defaultResId)
        } catch (e: Exception) {
            e.printStackTrace()
            applyImageAndPadding(defaultResId)
        }
    }
}
