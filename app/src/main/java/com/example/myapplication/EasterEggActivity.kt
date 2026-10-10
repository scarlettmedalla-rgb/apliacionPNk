package com.example.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Window
import androidx.appcompat.app.AppCompatActivity

/** Secret looping pixel-art scene. The custom view keeps the easter egg self-contained. */
class EasterEggActivity : AppCompatActivity() {
    private var player: MediaPlayer? = null
    private var view: EasterEggView? = null
    private lateinit var envelope: EggVoiceEnvelope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility = 5894
        envelope = EggVoiceEnvelope(applicationContext)
        view = EasterEggView(this, ::playAudioIfBundled, ::vibrateOnce, {
            player?.release()
            player = null
        }, {
            player?.let { if (it.isPlaying) envelope.levelAt(it.currentPosition) else 0f } ?: 0f
        }, { player?.isPlaying == true })
        setContentView(view!!)
    }

    private fun playAudioIfBundled() {
        val id = resources.getIdentifier("easter_egg_audio", "raw", packageName)
        if (id == 0) return
        player?.release()
        player = MediaPlayer.create(this, id)?.also { media ->
            media.setOnCompletionListener { it.release(); if (player === it) player = null }
            media.start()
        }
    }

    private fun vibrateOnce() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
        else @Suppress("DEPRECATION") vibrator.vibrate(70)
    }

    override fun onResume() {
        super.onResume()
        view?.resumeScene()
        player?.start()
    }

    override fun onPause() {
        view?.pauseScene()
        player?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        envelope.close()
        view?.release()
        player?.release()
        player = null
        view = null
        super.onDestroy()
    }
}
