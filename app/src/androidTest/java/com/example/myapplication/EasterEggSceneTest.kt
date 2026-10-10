package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import org.junit.Assert.*

/** Exercises the actual activity and captures review frames on the emulator. */
@RunWith(AndroidJUnit4::class)
class EasterEggSceneTest {
    @Test fun bothBranchesAndLifecycle() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext, EasterEggActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        fun shot(name:String) {
            instrumentation.runOnMainSync {
                val root=activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                val bitmap=Bitmap.createBitmap(root.width,root.height,Bitmap.Config.ARGB_8888)
                root.draw(Canvas(bitmap))
                File(activity.getExternalFilesDir(null), "egg-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                bitmap.recycle()
            }
        }
        fun tap(x:Float, bottom:Float) {
            instrumentation.runOnMainSync {
                val root=activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                val scale=root.width/360f
                val now=SystemClock.uptimeMillis()
                for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) {
                    val event=MotionEvent.obtain(now,now,action,x*scale,root.height-bottom*scale,0)
                    root.dispatchTouchEvent(event); event.recycle()
                }
            }
        }
        fun sceneView()=activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as EasterEggView
        fun state():String { var value=""; instrumentation.runOnMainSync { value=sceneView().sceneName }; return value }
        fun awaitState(expected:String,timeout:Long=18000) {
            val end=SystemClock.uptimeMillis()+timeout
            while(state()!=expected && SystemClock.uptimeMillis()<end) SystemClock.sleep(30)
            assertEquals(expected,state())
        }
        try {
            awaitState("QUESTION"); shot("question")
            tap(95f,77f)
            SystemClock.sleep(650); shot("celebrate")
            awaitState("WAIT"); shot("wait")
            // Waiting must remain interactive even after the animation timers elapse.
            SystemClock.sleep(2300); shot("wait-later")
            assertEquals("WAIT",state())
            tap(180f,80f)
            assertEquals("TURN",state())
            awaitState("DRAW"); SystemClock.sleep(500); shot("back")
            awaitState("ROCKET"); SystemClock.sleep(350); shot("rocket")
            awaitState("BOOM"); SystemClock.sleep(650); shot("explosion")
            awaitState("DARK")
            instrumentation.runOnMainSync { assertFalse("Voice must not precede the black frame",sceneView().isVoiceStarted) }
            SystemClock.sleep(550)
            instrumentation.runOnMainSync { assertTrue(sceneView().isVoiceStarted) }
            shot("dark")
            awaitState("QUESTION")
            tap(260f,77f)
            assertEquals("ANGRY",state())
            var sawOpen=false
            var sawClosed=false
            val end=SystemClock.uptimeMillis()+15000
            while(state()=="ANGRY" && SystemClock.uptimeMillis()<end) {
                instrumentation.runOnMainSync { if(sceneView().isMouthOpen) sawOpen=true else sawClosed=true }
                SystemClock.sleep(20)
            }
            assertTrue("Decoded audio must animate the mouth",sawOpen)
            assertTrue("Mouth must close in pauses",sawClosed)
            awaitState("REFUSE"); shot("no")
            awaitState("EXIT"); shot("exit")
            awaitState("QUESTION"); shot("returned")
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
