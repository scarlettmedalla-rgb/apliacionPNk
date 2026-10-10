package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

/** Art is stored in drawable-nodpi; all choreography uses a resolution independent stage. */
class EasterEggView(context: Context, private val audio: () -> Unit, private val vibrate: () -> Unit, private val stopAudio: () -> Unit = {}, private val voiceLevel: () -> Float = { 0f }, private val voicePlaying: () -> Boolean = { false }) : View(context) {
    private enum class Scene { ENTER, QUESTION, CHEER, TEACH, TURN, WAIT, DRAW, ROCKET, BOOM, DARK, ANGRY, REFUSE, EXIT, PAUSE }
    private val paint = Paint().apply { isFilterBitmap = false }
    private val background = BitmapFactory.decodeResource(resources, R.drawable.egg_night)
    private val atlas = BitmapFactory.decodeResource(resources, R.drawable.egg_professor)
    private val motion = BitmapFactory.decodeResource(resources, R.drawable.egg_motion)
    private val back = BitmapFactory.decodeResource(resources, R.drawable.egg_back)
    private val launcher = BitmapFactory.decodeResource(resources, R.drawable.egg_launcher)
    private val blast = BitmapFactory.decodeResource(resources, R.drawable.egg_blast)
    private val src = Rect()
    private val dst = RectF()
    private var scene = Scene.ENTER
    private var time = 0f
    private var world = 0f
    private var previous = 0L
    private var active = false
    private var stageHeight = 640f
    private var scale = 1f
    private var down = -1
    private var sound = true
    private var haptics = false
    private var blinkAt = 2.7f
    private var voiceStarted = false
    private var mouthOpen = false
    private var mouthHold = 0f
    private var targetMoonX = 286f
    internal val sceneName: String get() = scene.name
    internal val isVoiceStarted: Boolean get() = voiceStarted
    internal val isMouthOpen: Boolean get() = mouthOpen
    private val random = Random(71)
    private val stars = List(50) { floatArrayOf(random.nextFloat()*360, 40+random.nextFloat()*260, random.nextFloat()*6) }
    private val yes = RectF()
    private val no = RectF()
    private val press = RectF()
    private val tones = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 45)

    fun resumeScene() { active = true; previous = 0; invalidate() }
    fun pauseScene() { active = false; previous = 0; tones.stopTone() }
    fun release() { active = false; tones.release() }
    private fun enter(next: Scene) {
        scene = next; time = 0f; voiceStarted = false; mouthOpen = false
        when (next) {
            // Voice starts only from a subsequent frame after the pose/black frame is visible.
            Scene.CHEER -> if (sound) tones.startTone(android.media.ToneGenerator.TONE_DTMF_3, 240)
            Scene.ROCKET -> if (sound) tones.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180)
            Scene.BOOM -> if (haptics) vibrate()
            else -> Unit
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        val dt = if (previous == 0L || !active) 0f else ((now-previous)/1000f).coerceAtMost(.05f)
        previous = now; time += dt; world += dt
        if(scene==Scene.ANGRY && !voiceStarted && time>.12f) {
            voiceStarted=true
            if(sound) audio()
        }
        val level=if(sound && voicePlaying()) voiceLevel() else 0f
        if(level>.035f) mouthHold=.045f else mouthHold=max(0f,mouthHold-dt)
        mouthOpen=level>.018f || mouthHold>0
        scale = width / 360f
        stageHeight = height / scale
        val floor = stageHeight*.79f
        yes.set(27f, stageHeight-105, 170f, stageHeight-49)
        no.set(190f, stageHeight-105, 333f, stageHeight-49)
        press.set(36f, stageHeight-119, 324f, stageHeight-47)
        canvas.save(); canvas.scale(scale,scale)
        canvas.drawColor(Color.BLACK)
        val shake = if (scene == Scene.BOOM) max(0f, 1-time/1.9f)*6 else 0f
        canvas.save(); canvas.translate(sin(world*93)*shake, cos(world*77)*shake)
        if (scene != Scene.DARK) {
            // A slightly oversized painted environment drifts behind the independent sky effects and actors.
            val drift = sin(world*.15f)*4
            dst.set(-8+drift,-8f,368+drift,stageHeight+8)
            canvas.drawBitmap(background,null,dst,paint)
            stars.forEach { s -> rect(canvas,s[0],s[1],1.5f,1.5f,Color.argb((100+120*(.5+.5*sin(world+s[2]))).toInt(),225,238,255)) }
            if(scene==Scene.TURN) targetMoonX=286+sin(world*.09f)*2
            if (scene != Scene.BOOM) moon(canvas,if(scene in listOf(Scene.DRAW,Scene.ROCKET)) targetMoonX else 286+sin(world*.09f)*2,stageHeight*.25f)
            // Animated warm light in front of the painted lamp.
            paint.shader = RadialGradient(43f,stageHeight*.29f,70f,intArrayOf(Color.argb(27,255,181,53),Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
            canvas.drawCircle(43f,stageHeight*.29f,70f,paint); paint.shader=null
            var x = 180f
            var pose = 0
            var bob = sin(world*2)*1.3f
            when(scene) {
                Scene.ENTER -> { val f=(time/3f).coerceIn(0f,1f); x=-95+275*(f*f*(3-2*f)); pose=8+(time*8).toInt()%4; bob=-sin(time*16)*1.4f }
                Scene.CHEER -> { pose=12+(time*2.8f).toInt()%2; x=180+sin(time*5.6f)*14; bob=-(1-cos(time*11.2f))*3.5f }
                Scene.TEACH -> pose=0
                Scene.TURN -> pose=if(time<.22f) 0 else if(time<.5f) 2 else 16
                Scene.WAIT -> pose=0
                Scene.DRAW,Scene.ROCKET,Scene.BOOM -> { pose=16; x=180f; bob=0f }
                Scene.ANGRY -> { pose=if(mouthOpen) 15 else 14; bob=sin(world*2)*.5f }
                Scene.REFUSE -> pose=0
                Scene.EXIT -> { pose=8+(time*8).toInt()%4; x=180+280*(time/2.8f); bob=-sin(time*16)*1.4f }
                Scene.PAUSE -> x=500f
                else -> Unit
            }
            canvas.save()
            if(scene==Scene.CHEER) {
                canvas.rotate(sin(time*5.6f)*4,x,floor-90)
                canvas.scale(1f,1+sin(time*11.2f)*.012f,x,floor)
            }
            teacher(canvas,pose,x,floor+bob)
            canvas.restore()
            val originX=221f
            val originY=floor-156
            val aim=atan2(stageHeight*.25f-originY,targetMoonX-originX)
            val muzzleX=originX+cos(aim)*83
            val muzzleY=originY+sin(aim)*83
            if(scene in listOf(Scene.DRAW,Scene.ROCKET,Scene.BOOM)) {
                canvas.save(); canvas.translate(originX,originY)
                val lift=if(scene==Scene.DRAW) (time/.85f).coerceIn(0f,1f) else 1f
                canvas.rotate(aim*180f/PI.toFloat()+35*(1-lift))
                paint.alpha=(255*lift).toInt()
                dst.set(-49f,-37f,88f,32f)
                canvas.drawBitmap(launcher,null,dst,paint); paint.alpha=255
                canvas.restore()
            }
            if(scene==Scene.CHEER) particles(canvas,180f,floor-100,time,true)
            if(scene==Scene.REFUSE) {
                for(i in 0..8) { paint.color=Color.argb(130,167,158,199); canvas.drawCircle(145+i*9f,floor-220+sin(i.toFloat())*5,13f,paint) }
            }
            if(scene==Scene.ROCKET) {
                val f=(time/1.2f).coerceIn(0f,1f)
                val rocketX=muzzleX+(targetMoonX-muzzleX)*f
                val rocketY=muzzleY+(stageHeight*.25f-muzzleY)*f
                for(i in 0..15) {
                    val a=f-i*.022f
                    if(a>=0) { paint.color=Color.argb(125-i*7,210,206,193); canvas.drawCircle(muzzleX+(targetMoonX-muzzleX)*a,muzzleY+(stageHeight*.25f-muzzleY)*a,2+i*.35f,paint) }
                }
                canvas.save(); canvas.translate(rocketX,rocketY); canvas.rotate(aim*180f/PI.toFloat())
                rect(canvas,-10f,-3f,17f,6f,Color.LTGRAY)
                val nose=Path().apply { moveTo(7f,-3f); lineTo(13f,0f); lineTo(7f,3f); close() }
                paint.color=0xffcf4542.toInt(); canvas.drawPath(nose,paint)
                rect(canvas,-19f,-2f,9f,4f,Color.YELLOW)
                canvas.restore()
            }
            if(scene==Scene.BOOM) {
                val frame=(time/.45f).toInt().coerceAtMost(3)
                val edges=floatArrayOf(0f,.187f,.437f,.741f,1f)
                src.set((edges[frame]*blast.width).toInt(),0,(edges[frame+1]*blast.width).toInt(),blast.height)
                val radius=70+min(time,1f)*125
                dst.set(targetMoonX-radius,stageHeight*.25f-radius,targetMoonX+radius,stageHeight*.25f+radius)
                paint.color=Color.WHITE
                cDrawBlast(canvas)
                particles(canvas,286f,stageHeight*.25f,time,false)
                if(time<.15f) { paint.color=Color.argb((210*(1-time/.15f)).toInt(),255,246,213); canvas.drawRect(0f,0f,360f,stageHeight,paint) }
            }
        } else {
            val ey = stageHeight*.57f
            val closed = time%2.6f>2.43f
            for(x in listOf(148f,190f)) {
                rect(canvas,x-3,ey-3,31f,if(closed) 7f else 24f,0xff713e33.toInt())
                rect(canvas,x,ey,25f,if(closed) 2f else 18f,Color.WHITE)
                if(!closed) rect(canvas,x+8+sin(time*2)*4,ey+2,9f,13f,0xff111321.toInt())
            }
        }
        canvas.restore()
        when(scene) {
            Scene.QUESTION -> { bubble(canvas,"¿Se sacaron un 7?"); button(canvas,yes,"SÍ",0xff16ae42.toInt(),down==0); button(canvas,no,"NO",0xffed303b.toInt(),down==1) }
            Scene.CHEER -> bubble(canvas,"¡BUENAAA!",0xffbd281c.toInt())
            Scene.TEACH -> bubble(canvas,"Okey, entonces\nsí hay animación")
            Scene.WAIT -> { bubble(canvas,"Tú decides cuándo…"); button(canvas,press,"¡¡APRETAR!!",0xffed222d.toInt(),down==2) }
            Scene.ANGRY -> bubble(canvas,"¡PENKAAAA!",0xffbc2532.toInt())
            Scene.REFUSE,Scene.EXIT -> bubble(canvas,"Entonces no\nhay animación")
            else -> Unit
        }
        if(scene!=Scene.DARK) {
            label(canvas,"‹ SALIR",32f,27f,11f,Color.WHITE)
            label(canvas,if(sound) "SONIDO: SÍ" else "SONIDO: NO",160f,27f,10f,Color.WHITE)
            label(canvas,if(haptics) "VIBRA: SÍ" else "VIBRA: NO",294f,27f,10f,Color.WHITE)
        }
        canvas.restore()
        when(scene) {
            Scene.ENTER -> if(time>3f) enter(Scene.QUESTION)
            Scene.CHEER -> if(time>3.4f) enter(Scene.TEACH)
            Scene.TEACH -> if(time>2.3f) enter(Scene.WAIT)
            Scene.TURN -> if(time>.8f) enter(Scene.DRAW)
            Scene.DRAW -> if(time>.9f) enter(Scene.ROCKET)
            Scene.ROCKET -> if(time>1.2f) enter(Scene.BOOM)
            Scene.BOOM -> if(time>1.9f) enter(Scene.DARK)
            Scene.DARK -> if(time>6f && !voicePlaying()) enter(Scene.ENTER)
            Scene.ANGRY -> if(time>1f && voiceStarted && !voicePlaying()) enter(Scene.REFUSE)
            Scene.REFUSE -> if(time>2.2f) enter(Scene.EXIT)
            Scene.EXIT -> if(time>2.8f) enter(Scene.PAUSE)
            Scene.PAUSE -> if(time>1.5f) enter(Scene.ENTER)
            else -> Unit
        }
        if(active) postInvalidateOnAnimation()
    }

    private fun cDrawBlast(c:Canvas) { c.drawBitmap(blast,src,dst,paint) }

    private fun teacher(c:Canvas,pose:Int,x:Float,y:Float) {
        val sheet=if(pose==16) back else if(pose>=8) motion else atlas
        val cell=if(pose>=8) pose-8 else pose
        val cw=sheet.width/4; val ch=sheet.height/2
        if(pose==16) src.set(0,0,sheet.width,sheet.height)
        else src.set((cell%4)*cw,(cell/4)*ch,(cell%4+1)*cw,(cell/4+1)*ch)
        paint.color=Color.argb(85,0,0,0); c.drawOval(x-57,y-9,x+57,y+5,paint)
        paint.color=Color.WHITE; dst.set(x-116,y-232,x+116,y)
        c.drawBitmap(sheet,src,dst,paint)
        // Brief eyelid overlay on the frontal idle pose, at randomized intervals.
        if(world>blinkAt+.12f) blinkAt=world+2+random.nextFloat()*3
        if(pose==0 && world>blinkAt) {
            rect(c,x-23,y-179,13f,3f,0xffbd8053.toInt())
            rect(c,x+5,y-179,13f,3f,0xffbd8053.toInt())
        }
    }
    private fun moon(c:Canvas,x:Float,y:Float) {
        paint.shader=RadialGradient(x,y,43f,intArrayOf(0x66ffd96b,Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        c.drawCircle(x,y,43f,paint); paint.shader=null
        for(iy in -8..8) for(ix in -8..8) if(ix*ix+iy*iy<66) {
            val crater=(ix*17+iy*31).mod(13)<3
            rect(c,x+ix*3,y+iy*3,3f,3f,if(crater) 0xffe0b965.toInt() else 0xffffe8a0.toInt())
        }
    }
    private fun particles(c:Canvas,x:Float,y:Float,t:Float,confetti:Boolean) {
        for(i in 0..100) {
            val angle=i*2.39996f
            val speed=18+(i*37%110)
            val r=if(confetti) (t*speed)%170 else t*speed
            val px=x+cos(angle)*r; val py=y+sin(angle)*r+if(confetti) 0f else t*t*20
            paint.color=if(confetti) intArrayOf(0xffffdc55.toInt(),0xff6df3ef.toInt(),0xffff798e.toInt())[i%3]
                else intArrayOf(0xfffff0a5.toInt(),0xffffbe32.toInt(),0xfff85b16.toInt(),0xff994355.toInt())[i%4]
            val size=if(confetti) 3f else max(1f,3-t)*(1+i%3*.3f)
            c.drawRect(px,py,px+size,py+size,paint)
        }
    }
    private fun bubble(c:Canvas,text:String,color:Int=0xff131834.toInt()) {
        val top=stageHeight*.10f
        rect(c,23f,top+4,318f,92f,0xff090d21.toInt())
        rect(c,19f,top,318f,88f,0xff191527.toInt())
        rect(c,23f,top+4,310f,80f,0xfffff0bc.toInt())
        rect(c,27f,top+8,302f,4f,0xffffffe5.toInt())
        val path=Path().apply { moveTo(230f,top+83); lineTo(222f,top+108); lineTo(254f,top+83); close() }
        paint.color=0xfffff0bc.toInt(); c.drawPath(path,paint)
        val lines=text.split('\n')
        lines.forEachIndexed { i,s -> label(c,s,178f,top+if(lines.size==1) 49f else 35+i*25f,if(s.length>21) 16f else 20f,color) }
    }
    private fun button(c:Canvas,r:RectF,text:String,color:Int,pressed:Boolean) {
        val dy=if(pressed) 4f else 0f
        rect(c,r.left-3,r.top+6,r.width()+6,r.height(),0xff0a1020.toInt())
        rect(c,r.left,r.top+dy,r.width(),r.height()-4,color)
        rect(c,r.left+4,r.top+4+dy,r.width()-8,3f,0xffffcf9c.toInt())
        rect(c,r.left+4,r.bottom-12+dy,r.width()-8,5f,0x66000000)
        label(c,text,r.centerX(),r.centerY()+7+dy,if(text.length>4) 23f else 26f,Color.WHITE)
    }
    private fun label(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int) {
        paint.typeface=Typeface.create("monospace",Typeface.BOLD); paint.textAlign=Paint.Align.CENTER; paint.textSize=size
        paint.color=0xff11152a.toInt(); c.drawText(s,x+1,y+2,paint)
        paint.color=color; c.drawText(s,x,y,paint)
    }
    private fun rect(c:Canvas,x:Float,y:Float,w:Float,h:Float,color:Int) { paint.color=color; c.drawRect(x,y,x+w,y+h,paint) }
    private fun hit(x:Float,y:Float):Int = when {
        y<42 && x<78 -> 3
        y<42 && x<234 -> 4
        y<42 -> 5
        scene==Scene.QUESTION && yes.contains(x,y) -> 0
        scene==Scene.QUESTION && no.contains(x,y) -> 1
        scene==Scene.WAIT && press.contains(x,y) -> 2
        else -> -1
    }
    override fun onTouchEvent(e:MotionEvent):Boolean {
        val target=hit(e.x/scale,e.y/scale)
        when(e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { down=target; invalidate(); return true }
            MotionEvent.ACTION_CANCEL -> down=-1
            MotionEvent.ACTION_UP -> {
                if(target==down) when(target) {
                    0 -> enter(Scene.CHEER)
                    1 -> enter(Scene.ANGRY)
                    2 -> enter(Scene.TURN)
                    3 -> (context as? android.app.Activity)?.finish()
                    4 -> { sound=!sound; if(!sound) { tones.stopTone(); stopAudio() } }
                    5 -> haptics=!haptics
                }
                down=-1; performClick(); invalidate()
            }
        }
        return true
    }
    override fun performClick():Boolean { super.performClick(); return true }
}
