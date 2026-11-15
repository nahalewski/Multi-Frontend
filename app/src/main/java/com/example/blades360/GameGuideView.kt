package com.example.blades360

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.*

/** Canvas-only game Guide: menus, status, console options and the rewind timeline. */
class GameGuideView(context:Context):View(context) {
    data class Row(val label:String,val action:()->Unit)
    var heading="Game Guide"
    var description=""
    var rows:List<Row> = emptyList()
    var back:()->Unit = {}
    var timeline:List<RewindHistory.Entry> = emptyList()
    var timelineIndex=0
    var onScrub:(Int)->Unit = {}
    var selection=0
    var blocked=false
    private var offset=0
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val font=Typeface.createFromAsset(context.assets,"convection.ttf")
    private var sx=1f;private var sy=1f;private var tx=0f;private var ty=0f
    private var downY=0f
    private var lastStick=0L
    private val hasTimeline get()=timeline.isNotEmpty()
    private val rowTop get()=if(hasTimeline)460f else 230f
    private val slots get()=max(2,min(8,((logicalHeight-130-rowTop)/49).toInt()))
    fun show(title:String,sub:String,items:List<Row>,onBack:()->Unit){heading=title;description=sub;rows=items;back=onBack;timeline=emptyList();selection=0;offset=0;invalidate()}
    fun move(delta:Int){selection=(selection+delta).coerceIn(0,max(0,rows.size-1));if(selection<offset)offset=selection;if(selection>=offset+slots)offset=selection-slots+1;invalidate()}
    fun scrub(delta:Int){if(!hasTimeline)return;timelineIndex=(timelineIndex+delta).coerceIn(0,timeline.lastIndex);onScrub(timelineIndex);invalidate()}
    fun handleKey(code:Int,event:KeyEvent):Boolean {if(blocked||event.action!=KeyEvent.ACTION_DOWN)return true;if(event.repeatCount>0&&code in listOf(96,97,66))return true;when(code){19->move(-1);20->move(1);21->scrub(-1);22->scrub(1);102->scrub(-5);103->scrub(5);96,23,66->rows.getOrNull(selection)?.action?.invoke();97,4,82->back()};return true}
    fun handleMotion(event:MotionEvent):Boolean {if(blocked)return true;var x=event.getAxisValue(MotionEvent.AXIS_HAT_X);var y=event.getAxisValue(MotionEvent.AXIS_HAT_Y);if(abs(x)<.5f)x=event.getAxisValue(MotionEvent.AXIS_X);if(abs(y)<.5f)y=event.getAxisValue(MotionEvent.AXIS_Y);val now=android.os.SystemClock.uptimeMillis();if(max(abs(x),abs(y))<.5f){lastStick=0;return true};if(now-lastStick>180){if(abs(x)>abs(y))scrub(if(x>0)1 else -1)else move(if(y>0)1 else -1);lastStick=now};return true}
    private fun txt(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=0xff26381d.toInt()){paint.color=color;paint.shader=null;paint.typeface=font;paint.textSize=size;c.drawText(s,x,y,paint)}
    private fun fit(s:String,width:Float,size:Float):String{paint.typeface=font;paint.textSize=size;if(paint.measureText(s)<=width)return s;var n=s.length;while(n>0&&paint.measureText(s.substring(0,n)+"…")>width)n--;return s.substring(0,n)+"…"}
    private fun rect(c:Canvas,r:RectF,color:Int,radius:Float=7f){paint.shader=null;paint.color=color;c.drawRoundRect(r,radius,radius,paint)}
    private var logicalHeight=720f
    private fun gradient(c:Canvas,l:Float,t:Float,r:Float,b:Float,a:Int,z:Int,vertical:Boolean=true,radius:Float=0f){paint.shader=LinearGradient(l,t,if(vertical)l else r,if(vertical)b else t,a,z,Shader.TileMode.CLAMP);c.drawRoundRect(RectF(l,t,r,b),radius,radius,paint);paint.shader=null}
    private fun button(c:Canvas,x:Float,y:Float,label:String,color:Int){paint.shader=RadialGradient(x-4,y-5,17f,intArrayOf(0xffeef5da.toInt(),color,0xff40532c.toInt()),floatArrayOf(0f,.5f,1f),Shader.TileMode.CLAMP);c.drawCircle(x,y,15f,paint);txt(c,label,x-8,y+8,23f,Color.WHITE)}
    override fun onDraw(c:Canvas){
        sx=min(width/1280f,height/720f);sy=sx;tx=(width-1280*sx)/2;ty=0f;logicalHeight=height/sx;val h=logicalHeight
        c.drawColor(0xa6000000.toInt());c.save();c.translate(tx,0f);c.scale(sx,sy)
        val edge=572f;val panel=Path().apply{moveTo(-tx/sx,0f);lineTo(edge-13,0f);cubicTo(edge-22,45f,edge-34,82f,edge-29,96f);cubicTo(edge-25,108f,edge+14,93f,edge+13,119f);cubicTo(edge+10,h*.38f,edge+5,h*.60f,edge-2,h*.68f);cubicTo(edge-18,h*.78f,edge-32,h*.82f,edge-20,h);lineTo(-tx/sx,h);close()}
        c.save();c.clipPath(panel)
        gradient(c,-40f,0f,edge+20,h,0xffdce9cc.toInt(),0xff839d66.toInt())
        gradient(c,edge-52,0f,edge+20,h,0xffbac6b2.toInt(),0xff4a5f42.toInt(),false)
        gradient(c,-40f,-25f,edge+2,91f,0xff415d28.toInt(),0xff91b950.toInt(),true,24f)
        txt(c,fit(heading,480f,32f),45f,56f,32f,0xfff6f8e9.toInt())
        c.save();c.clipRect(40f,105f,520f,if(hasTimeline)180f else 215f)
        var line="";var y=135f;description.replace('\n',' ').split(' ').forEach{word->paint.textSize=22f;if(paint.measureText("$line $word")>460&&line.isNotEmpty()){txt(c,line,48f,y,22f);y+=27;line=word}else line=if(line.isEmpty())word else "$line $word"};txt(c,line,48f,y,22f);c.restore()
        if(hasTimeline){val entry=timeline[timelineIndex.coerceIn(0,timeline.lastIndex)];rect(c,RectF(45f,185f,515f,375f),0xff203019.toInt());entry.preview?.takeUnless{it.isRecycled}?.let{val ratio=it.width.toFloat()/it.height;val w=min(470f,190f*ratio);val hh=w/ratio;c.drawBitmap(it,null,RectF(280-w/2,280-hh/2,280+w/2,280+hh/2),paint)}
            txt(c,"${RewindHistory.format(entry.timeMs)} • ${timelineIndex+1} / ${timeline.size}",48f,401f,22f)
            rect(c,RectF(52f,420f,508f,427f),0xff51683a.toInt());val marker=52+456*(if(timeline.size>1)timelineIndex.toFloat()/timeline.lastIndex else 1f);rect(c,RectF(marker-6,412f,marker+6,436f),0xffeff8d0.toInt())
        }
        rows.drop(offset).take(slots).forEachIndexed{n,item->val top=rowTop+n*49;if(n+offset==selection)gradient(c,40f,top-3,518f,top+43,0xfff8fbe8.toInt(),0xffa9c983.toInt(),true,9f);paint.color=0x666a7e53;paint.strokeWidth=1.4f;c.drawLine(45f,top+44,514f,top+44,paint);txt(c,fit(item.label,445f,25f),58f,top+31,25f)}
        if(offset>0)txt(c,"▲",490f,rowTop-10,20f);if(offset+slots<rows.size)txt(c,"▼",490f,rowTop+slots*49+18,20f)
        gradient(c,-40f,h-100,edge+2,h+25,0xff5b783d.toInt(),0xffb8ce99.toInt(),true,24f)
        button(c,64f,h-45,"A",0xff82b83c.toInt());txt(c,"Select",88f,h-37,24f,0xfff4f6e7.toInt());button(c,376f,h-45,"B",0xffbd3a30.toInt());txt(c,"Back",400f,h-37,24f,0xfff4f6e7.toInt());c.restore();c.restore()
    }
    override fun onTouchEvent(e:MotionEvent):Boolean {if(blocked)return true;val x=(e.x-tx)/sx;val y=e.y/sy;if(hasTimeline&&y in 390f..445f&&x in 38f..520f){timelineIndex=((x-52)/456*timeline.lastIndex).roundToInt().coerceIn(0,timeline.lastIndex);onScrub(timelineIndex);invalidate();return true};if(e.action==0){downY=y;return true};if(e.action!=1)return true;if(abs(y-downY)>50){move(if(y<downY)slots else -slots);return true};if(x>590){back();return true};if(y>logicalHeight-100){if(x<280)rows.getOrNull(selection)?.action?.invoke()else back();return true};val index=offset+((y-rowTop)/49).toInt();if(y>=rowTop&&index in rows.indices&&index<offset+slots){if(index==selection)rows[index].action()else{selection=index;invalidate()}};return true}
}
