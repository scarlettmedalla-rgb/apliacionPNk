package com.example.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity

/** Secret looping pixel-art scene. The custom view keeps the easter egg self-contained. */
class EasterEggActivity : AppCompatActivity() {
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility = 5894
        setContentView(EasterEggView(this) { playAudioIfBundled() })
    }

    private fun playAudioIfBundled() {
        if (player != null) return
        val id = resources.getIdentifier("easter_egg_audio", "raw", packageName)
        if (id == 0) return
        player = MediaPlayer.create(this, id)?.also { it.isLooping = false; it.start() }
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
