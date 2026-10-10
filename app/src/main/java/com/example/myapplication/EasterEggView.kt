package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin

private enum class EggState { QUESTION, YES, NO, BUTTON, EXPLOSION, DARK, RETURN }

class EasterEggView(context: Context, private val audio: () -> Unit) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = false }
    private var state = EggState.QUESTION
    private var startedAt = System.currentTimeMillis()
    private var lastTap = 0L

    init { isFocusable = true; postInvalidateOnAnimation() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val elapsed = System.currentTimeMillis() - startedAt
        val w = width.toFloat(); val h = height.toFloat()
        canvas.drawColor(if (state == EggState.DARK) Color.BLACK else Color.rgb(8, 12, 42))
        if (state != EggState.DARK) drawNight(canvas, w, h)
        when (state) {
            EggState.QUESTION -> { drawTeacher(canvas, w * .5f, h * .58f, 0f); text(canvas, "¿Se sacaron un 7?", w*.5f, h*.18f, 30f, Color.WHITE); button(canvas, "SÍ", w*.32f, h*.84f); button(canvas, "NO", w*.68f, h*.84f) }
            EggState.YES -> { drawTeacher(canvas, w*.5f, h*.58f, elapsed/120f); text(canvas, "¡BUENAAA!", w*.5f, h*.18f, 34f, Color.YELLOW); if (elapsed > 1800) { state=EggState.BUTTON; startedAt=System.currentTimeMillis() } }
            EggState.NO -> { drawTeacher(canvas, w*.5f, h*.58f, -elapsed/120f); text(canvas, "PENKAAAA", w*.5f, h*.18f, 32f, Color.rgb(255,120,180)); if (elapsed > 2200) reset() }
            EggState.BUTTON -> { drawTeacher(canvas, w*.5f, h*.58f, 180f); text(canvas, "Okey, entonces sí hay animación", w*.5f, h*.18f, 24f, Color.WHITE); button(canvas, "¡¡APRETAR!!", w*.5f, h*.82f, Color.RED) }
            EggState.EXPLOSION -> { drawTeacher(canvas, w*.5f, h*.58f, 180f); val r=elapsed/3f; paint.color=Color.YELLOW; paint.style=Paint.Style.STROKE; paint.strokeWidth=8f; canvas.drawCircle(w*.76f,h*.28f,r,paint); paint.style=Paint.Style.FILL; text(canvas,"¡BOOM!",w*.5f,h*.18f,38f,Color.WHITE); if(elapsed>1600){state=EggState.DARK;startedAt=System.currentTimeMillis();audio()} }
            EggState.DARK -> { val blink=(sin(elapsed/180.0)>-.2); if(blink){ paint.color=Color.WHITE; canvas.drawRect(w*.44f,h*.48f,w*.47f,h*.52f,paint); canvas.drawRect(w*.53f,h*.48f,w*.56f,h*.52f,paint) }; if(elapsed>2600) reset() }
            EggState.RETURN -> reset()
        }
        postInvalidateOnAnimation()
    }

    private fun drawNight(c:Canvas,w:Float,h:Float){ paint.color=Color.rgb(245,228,150); c.drawCircle(w*.78f,h*.22f,42f,paint); paint.color=Color.rgb(20,25,65); c.drawCircle(w*.80f,h*.20f,42f,paint); paint.color=Color.rgb(18,55,48); c.drawRect(0f,h*.72f,w,h,paint); }
    private fun drawTeacher(c:Canvas,x:Float,y:Float,turn:Float){ paint.color=Color.rgb(255,205,160); c.drawRect(x-26,y-76,x+26,y-24,paint); paint.color=Color.DKGRAY; c.drawRect(x-30,y-92,x+30,y-76,paint); paint.color=Color.rgb(55,90,170); c.drawRect(x-34,y-24,x+34,y+65,paint); paint.color=Color.BLACK; c.drawRect(x-14,y-58,x-8,y-52,paint); c.drawRect(x+8,y-58,x+14,y-52,paint); paint.color=Color.rgb(40,40,40); c.drawRect(x-28,y+65,x-8,y+88,paint); c.drawRect(x+8,y+65,x+28,y+88,paint); }
    private fun button(c:Canvas,label:String,x:Float,y:Float,color:Int=Color.rgb(35,170,110)){ paint.color=color; c.drawRoundRect(RectF(x-110,y-30,x+110,y+30),12f,12f,paint); text(c,label,x,y+10,22f,Color.WHITE) }
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int){ paint.color=color;paint.textSize=size;paint.textAlign=Paint.Align.CENTER;paint.typeface=android.graphics.Typeface.DEFAULT_BOLD;c.drawText(s,x,y,paint) }
    private fun reset(){state=EggState.QUESTION;startedAt=System.currentTimeMillis()}
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP || System.currentTimeMillis() - lastTap < 250) return true
        lastTap = System.currentTimeMillis()
        val y = e.y / height
        when (state) {
            EggState.QUESTION -> if (y > .7f) {
                state = if (e.x < width / 2) EggState.YES else EggState.NO
                startedAt = System.currentTimeMillis()
                if (state == EggState.NO) audio()
            }
            EggState.BUTTON -> if (y > .65f) {
                state = EggState.EXPLOSION
                startedAt = System.currentTimeMillis()
            }
            else -> Unit
        }
        return true
    }
}
