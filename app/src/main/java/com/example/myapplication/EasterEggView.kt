package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin

private enum class EggState { ENTER, QUESTION, YES, NO, BUTTON, EXPLOSION, DARK, RETURN }

class EasterEggView(context: Context, private val audio: () -> Unit, private val vibrate: () -> Unit) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = false }
    private var state = EggState.ENTER
    private var started = System.currentTimeMillis()
    private var lastTap = 0L
    private val stars = (0 until 42).map { Pair((it * 83 % 100) / 100f, (it * 47 % 58) / 100f) }
    init { isFocusable = true; postInvalidateOnAnimation() }
    private fun advance(next: EggState) { state = next; started = System.currentTimeMillis() }

    override fun onDraw(c: Canvas) {
        val t = System.currentTimeMillis() - started; val w = width.toFloat(); val h = height.toFloat()
        c.drawColor(if (state == EggState.DARK) Color.BLACK else Color.rgb(8, 12, 42))
        if (state != EggState.DARK) drawNight(c, w, h, t)
        when (state) {
            EggState.ENTER -> { drawTeacher(c, w * (.12f + .38f * (t.coerceAtMost(1800) / 1800f)), h*.63f, t); if (t > 1900) advance(EggState.QUESTION) }
            EggState.QUESTION -> { drawTeacher(c,w*.5f,h*.63f,t); dialog(c,"¿Se sacaron un 7?",w*.5f,h*.16f,Color.WHITE); button(c,"SÍ",w*.32f,h*.86f,Color.rgb(38,170,108)); button(c,"NO",w*.68f,h*.86f,Color.rgb(38,170,108)) }
            EggState.YES -> { drawTeacher(c,w*.5f,h*.63f,t); dialog(c,"¡BUENAAA!",w*.5f,h*.16f,Color.YELLOW); confetti(c,w,h,t); if(t>2200) advance(EggState.BUTTON) }
            EggState.NO -> { drawTeacher(c,w*.5f,h*.63f,t); dialog(c,"PENKAAAA",w*.5f,h*.16f,Color.rgb(255,120,180)); dialog(c,"Entonces no hay animación",w*.5f,h*.27f,Color.WHITE); if(t>3300) advance(EggState.RETURN) }
            EggState.BUTTON -> { drawTeacher(c,w*.5f,h*.63f,t); dialog(c,"Okey, entonces sí hay animación",w*.5f,h*.16f,Color.WHITE); button(c,"¡¡APRETAR!!",w*.5f,h*.84f,Color.rgb(190,35,45)) }
            EggState.EXPLOSION -> { drawTeacher(c,w*.46f,h*.63f,t); drawBazooka(c,w*.55f,h*.63f); drawBlast(c,w*.78f,h*.27f,t); if(t>1800){ vibrate(); audio(); advance(EggState.DARK) } }
            EggState.DARK -> { if (sin(t/130.0) > -.25) { p.color=Color.WHITE; c.drawRect(w*.44f,h*.49f,w*.47f,h*.535f,p); c.drawRect(w*.53f,h*.49f,w*.56f,h*.535f,p) }; if(t>3800) advance(EggState.RETURN) }
            EggState.RETURN -> { drawTeacher(c,w*(.86f-.36f*(t.coerceAtMost(1500)/1500f)),h*.63f,t); if(t>1800) advance(EggState.ENTER) }
        }
        postInvalidateOnAnimation()
    }

    private fun drawNight(c:Canvas,w:Float,h:Float,t:Long) { p.style=Paint.Style.FILL; p.color=Color.rgb(245,228,150); c.drawCircle(w*.78f,h*.23f,46f,p); p.color=Color.rgb(13,18,55); c.drawCircle(w*.81f,h*.20f,43f,p); p.color=Color.WHITE; stars.forEach { c.drawRect(w*it.first,h*(.08f+it.second*.35f),w*it.first+4,h*(.08f+it.second*.35f)+4,p) }; drawCity(c,w,h,0f,Color.rgb(24,35,69)); drawCity(c,w,h,(sin(t/900.0)*10).toFloat(),Color.rgb(17,74,70)) }
    private fun drawCity(c:Canvas,w:Float,h:Float,offset:Float,color:Int) { p.color=color; for(i in 0..13){val x=i*w/13+offset; val bh=(35+(i*17%75)).toFloat(); c.drawRect(x,h*.78f-bh,x+w/18,h*.78f,p)} }
    private fun drawTeacher(c:Canvas,x:Float,y:Float,t:Long) { val bob=sin(t/180.0).toFloat()*3; p.color=Color.rgb(255,205,160); c.drawRect(x-27,y-86+bob,x+27,y-30+bob,p); p.color=Color.rgb(28,31,45); c.drawRect(x-33,y-101+bob,x+33,y-86+bob,p); p.color=Color.rgb(55,90,170); c.drawRect(x-36,y-30+bob,x+36,y+66+bob,p); p.color=Color.BLACK; c.drawRect(x-15,y-66+bob,x-8,y-59+bob,p); c.drawRect(x+8,y-66+bob,x+15,y-59+bob,p); if(state==EggState.NO || state==EggState.EXPLOSION) c.drawRect(x-10,y-47+bob,x+12,y-42+bob,p); p.color=Color.rgb(40,40,40); c.drawRect(x-30,y+66+bob,x-8,y+90+bob,p); c.drawRect(x+8,y+66+bob,x+30,y+90+bob,p) }
    private fun drawBazooka(c:Canvas,x:Float,y:Float) { p.color=Color.DKGRAY; c.rotate(-12f,x,y); c.drawRect(x-10,y-20,x+120,y+5,p); c.drawRect(x+70,y-35,x+105,y-20,p); c.rotate(12f,x,y) }
    private fun drawBlast(c:Canvas,x:Float,y:Float,t:Long) { val r=(t/3f).coerceAtMost(150f); p.color=Color.YELLOW; c.drawCircle(x,y,r,p); p.color=Color.WHITE; c.drawCircle(x,y,r*.45f,p); p.color=Color.argb(150,180,180,180); c.drawCircle(x-50,y+35,r*.7f,p); c.drawCircle(x+55,y+28,r*.55f,p) }
    private fun confetti(c:Canvas,w:Float,h:Float,t:Long) { p.color=Color.YELLOW; for(i in 0..12){val y=h*.35f+sin((t+i*50)/100.0).toFloat()*30; c.drawRect(w*.25f+i*w*.04f,y,w*.25f+i*w*.04f+7,y+14,p)} }
    private fun dialog(c:Canvas,s:String,x:Float,y:Float,color:Int) { p.color=Color.argb(210,12,17,42); c.drawRoundRect(RectF(x-250,y-36,x+250,y+20),8f,8f,p); p.textAlign=Paint.Align.CENTER; p.textSize=24f; p.typeface=android.graphics.Typeface.DEFAULT_BOLD; p.color=color; c.drawText(s,x,y,p) }
    private fun button(c:Canvas,s:String,x:Float,y:Float,color:Int) { p.color=Color.rgb(10,10,18); c.drawRect(x-128,y-35,x+128,y+35,p); p.color=color; c.drawRect(x-120,y-28,x+120,y+28,p); p.textAlign=Paint.Align.CENTER;p.textSize=22f;p.color=Color.WHITE;c.drawText(s,x,y+8,p) }
    override fun onTouchEvent(e:MotionEvent):Boolean { if(e.action!=MotionEvent.ACTION_UP || System.currentTimeMillis()-lastTap<220)return true; lastTap=System.currentTimeMillis(); if(state==EggState.QUESTION && e.y>height*.72f){ if(e.x<width/2){audio();advance(EggState.YES)}else{audio();advance(EggState.NO)} } else if(state==EggState.BUTTON && e.y>height*.65f) advance(EggState.EXPLOSION); return true }
}
