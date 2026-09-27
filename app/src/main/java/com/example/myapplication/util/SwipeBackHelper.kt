package com.example.myapplication.util

import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import kotlin.math.abs

object SwipeBackHelper {

    private var startX = 0f
    private var startY = 0f
    private var isEdgeSwipe = false

    fun processDispatchTouchEvent(activity: AppCompatActivity, ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y
                // Touch initiated near the left edge of the screen (first 300px)
                isEdgeSwipe = startX < 300f
            }
            MotionEvent.ACTION_MOVE -> {
                if (isEdgeSwipe) {
                    val diffX = ev.x - startX
                    val diffY = ev.y - startY
                    // iOS Left-to-Right Edge Swipe Gesture
                    if (diffX > 130f && abs(diffX) > abs(diffY) * 1.2f) {
                        isEdgeSwipe = false
                        activity.finish()
                        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isEdgeSwipe = false
            }
        }
        return false
    }
}
