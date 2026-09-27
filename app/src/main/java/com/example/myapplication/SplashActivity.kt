package com.example.myapplication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.myapplication.util.EdgeToEdgeHelper

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var heartbeatAnimatorSet: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_splash)

        val mainView = findViewById<View>(R.id.splash_main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }

        val cardLogo = findViewById<View>(R.id.card_splash_logo)
        val pulseRing1 = findViewById<View>(R.id.pulse_ring_1)
        val pulseRing2 = findViewById<View>(R.id.pulse_ring_2)
        val tvSubtitle = findViewById<TextView>(R.id.tv_splash_subtitle)
        val lottieView = findViewById<LottieAnimationView>(R.id.lottie_splash)

        if (cardLogo != null && pulseRing1 != null && pulseRing2 != null) {
            startHeartbeatAndExplosionAnimation(cardLogo, pulseRing1, pulseRing2)
        }

        lottieView?.playAnimation()

        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                tvSubtitle?.setText(R.string.splash_status_2)
            }
        }, 1750)

        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                tvSubtitle?.setText(R.string.splash_status_3)
            }
        }, 3500)

        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                tvSubtitle?.setText(R.string.splash_status_4)
            }
        }, 5250)

        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 7000)
    }

    private fun startHeartbeatAndExplosionAnimation(
        cardLogo: View,
        ring1: View,
        ring2: View
    ) {
        val logoScaleX = ObjectAnimator.ofFloat(cardLogo, View.SCALE_X, 1.0f, 1.06f, 1.0f, 1.07f, 1.0f)
        val logoScaleY = ObjectAnimator.ofFloat(cardLogo, View.SCALE_Y, 1.0f, 1.06f, 1.0f, 1.07f, 1.0f)
        logoScaleX.duration = 1400
        logoScaleY.duration = 1400
        logoScaleX.repeatCount = ObjectAnimator.INFINITE
        logoScaleY.repeatCount = ObjectAnimator.INFINITE
        logoScaleX.interpolator = AccelerateDecelerateInterpolator()
        logoScaleY.interpolator = AccelerateDecelerateInterpolator()

        val ring1ScaleX = ObjectAnimator.ofFloat(ring1, View.SCALE_X, 1.0f, 2.0f)
        val ring1ScaleY = ObjectAnimator.ofFloat(ring1, View.SCALE_Y, 1.0f, 2.0f)
        val ring1Alpha = ObjectAnimator.ofFloat(ring1, View.ALPHA, 0.8f, 0.0f)
        ring1ScaleX.duration = 1400
        ring1ScaleY.duration = 1400
        ring1Alpha.duration = 1400
        ring1ScaleX.repeatCount = ObjectAnimator.INFINITE
        ring1ScaleY.repeatCount = ObjectAnimator.INFINITE
        ring1Alpha.repeatCount = ObjectAnimator.INFINITE
        ring1ScaleX.interpolator = DecelerateInterpolator()
        ring1ScaleY.interpolator = DecelerateInterpolator()
        ring1Alpha.interpolator = DecelerateInterpolator()

        val ring2ScaleX = ObjectAnimator.ofFloat(ring2, View.SCALE_X, 1.0f, 2.2f)
        val ring2ScaleY = ObjectAnimator.ofFloat(ring2, View.SCALE_Y, 1.0f, 2.2f)
        val ring2Alpha = ObjectAnimator.ofFloat(ring2, View.ALPHA, 0.7f, 0.0f)
        ring2ScaleX.duration = 1400
        ring2ScaleY.duration = 1400
        ring2Alpha.duration = 1400
        ring2ScaleX.startDelay = 350
        ring2ScaleY.startDelay = 350
        ring2Alpha.startDelay = 350
        ring2ScaleX.repeatCount = ObjectAnimator.INFINITE
        ring2ScaleY.repeatCount = ObjectAnimator.INFINITE
        ring2Alpha.repeatCount = ObjectAnimator.INFINITE
        ring2ScaleX.interpolator = DecelerateInterpolator()
        ring2ScaleY.interpolator = DecelerateInterpolator()
        ring2Alpha.interpolator = DecelerateInterpolator()

        heartbeatAnimatorSet = AnimatorSet().apply {
            playTogether(
                logoScaleX, logoScaleY,
                ring1ScaleX, ring1ScaleY, ring1Alpha,
                ring2ScaleX, ring2ScaleY, ring2Alpha
            )
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        heartbeatAnimatorSet?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}
