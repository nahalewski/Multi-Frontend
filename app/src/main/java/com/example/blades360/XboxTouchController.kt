package com.example.blades360

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.swordfish.libretrodroid.GLRetroView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.*

/** Transparent Xbox 360 control geometry with per-console, normalized touch layouts. */
class XboxTouchController(context: Context, private val console: String, private val listener: Listener) : View(context) {
    interface Listener {
        /** Android key code already mapped to the libretro RetroPad layout. */
        fun onKey(action: Int, keyCode: Int)
        fun onStick(source: Int, x: Float, y: Float)
        fun onGuide()
        fun onEditorClosed()
    }
    private data class Control(val id:String,var x:Float,var y:Float,var size:Float=1f,var enabled:Boolean=true)
    private data class Layout(var name:String,val controls:MutableList<Control>,var opacity:Float=.38f)
    private data class Finger(var id:String,val startX:Float,val startY:Float,var x:Float,var y:Float)
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val font=Typeface.createFromAsset(context.assets,"convection.ttf")
    private val prefs=context.getSharedPreferences("emulator_library",Context.MODE_PRIVATE)
    private val prefKey="touch_layout_"+console.replace(Regex("[^A-Za-z0-9_]"),"_")
    private val fingers=mutableMapOf<Int,Finger>()
    private val emitted=mutableSetOf<Int>()
    private var layouts=mutableListOf<Layout>()
    private var current=0
    private var selected=0
    private var editing=false
    private var original=""
    private var dragPointer=-1
    private var dragDx=0f
    private var dragDy=0f
    private var pendingAction=-1
    private var status=""
    private var initialized=false
    var controlsVisible:Boolean=true
        private set
    val isEditing:Boolean get()=editing
    private val layout:Layout get()=layouts[current]
    private val density get()=resources.displayMetrics.density
    private val unit get()=min(width.toFloat(),height.toFloat())
    private val faceKeys=mapOf("A" to 97,"B" to 96,"X" to 100,"Y" to 99,"LB" to 102,"RB" to 103,"LT" to 104,"RT" to 105,"Back" to 109,"Start" to 108,"L3" to 106,"R3" to 107)
    private val editorLabels=arrayOf("Layout","New layout","Size −","Size +","Opacity −","Opacity +","Hide / show","Reset","Cancel","Save")

    init {setLayerType(LAYER_TYPE_SOFTWARE,null);decode(prefs.getString(prefKey,"")?:"");isFocusable=false;importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO;initialized=true}
    private fun defaults(name:String="Xbox 360",compact:Boolean=false):Layout {
        val inset=if(compact).045f else 0f
        return Layout(name,mutableListOf(
            Control("LS",.14f+inset,.64f),Control("DPAD",.26f+inset,.82f),
            Control("RS",.73f-inset,.82f),Control("A",.86f-inset,.76f),
            Control("B",.93f-inset,.65f),Control("X",.79f-inset,.65f),Control("Y",.86f-inset,.54f),
            Control("LB",.14f+inset,.43f),Control("LT",.14f+inset,.33f),Control("RB",.86f-inset,.43f),Control("RT",.86f-inset,.33f),
            Control("Back",.43f,.72f),Control("Guide",.5f,.7f),Control("Start",.57f,.72f),
            Control("L3",.14f+inset,.87f,.62f),Control("R3",.64f-inset,.87f,.62f)
        ).apply{if(compact)forEach{it.size*=.85f}})
    }
    private fun encode():String {
        val array=JSONArray()
        layouts.forEach{l->val nodes=JSONArray();l.controls.forEach{n->nodes.put(JSONObject().put("id",n.id).put("x",n.x.toDouble()).put("y",n.y.toDouble()).put("size",n.size.toDouble()).put("enabled",n.enabled))};array.put(JSONObject().put("name",l.name).put("opacity",l.opacity.toDouble()).put("controls",nodes))}
        return JSONObject().put("version",1).put("current",current).put("visible",controlsVisible).put("layouts",array).toString()
    }
    private fun bounded(value:Double,fallback:Float,min:Float,max:Float)=if(value.isFinite())value.toFloat().coerceIn(min,max)else fallback
    private fun decode(raw:String){
        layouts=mutableListOf(defaults(),defaults("Compact",true));current=0
        try {if(raw.isNotEmpty()){val root=JSONObject(raw);val source=root.getJSONArray("layouts");val loaded=mutableListOf<Layout>();for(i in 0 until min(source.length(),12)){val j=source.getJSONObject(i);val base=defaults(j.optString("name","Custom ${i+1}").take(36));base.opacity=bounded(j.optDouble("opacity",.38),.38f,.15f,.85f);val nodes=j.optJSONArray("controls");if(nodes!=null)for(k in 0 until nodes.length()){val n=nodes.getJSONObject(k);base.controls.find{it.id==n.optString("id")}?.let{c->c.x=bounded(n.optDouble("x"),c.x,.03f,.97f);c.y=bounded(n.optDouble("y"),c.y,.04f,.96f);c.size=bounded(n.optDouble("size",1.0),1f,.5f,1.8f);c.enabled=c.id=="Guide"||n.optBoolean("enabled",true)}};loaded.add(base)};if(loaded.isNotEmpty())layouts=loaded;current=root.optInt("current",0).coerceIn(0,layouts.lastIndex);controlsVisible=root.optBoolean("visible",true)}}catch(_:Exception){layouts=mutableListOf(defaults(),defaults("Compact",true));current=0}
        selected=selected.coerceIn(0,layout.controls.lastIndex)
    }
    fun setControlsVisible(visible:Boolean){releaseAll();controlsVisible=visible;prefs.edit().putString(prefKey,encode()).apply();invalidate()}
    fun openEditor(){if(editing)return;releaseAll();original=encode();editing=true;selected=0;status="Drag any control. LB/RB select; D-pad moves; LT/RT resize; START saves; B cancels.";invalidate()}
    /** Cancel edits; Save is explicit in the editor. Returns whether an editor was open. */
    fun closeEditor():Boolean {if(!editing)return false;releaseAll();decode(original);editing=false;dragPointer=-1;pendingAction=-1;invalidate();listener.onEditorClosed();return true}
    private fun commit(){releaseAll();prefs.edit().putString(prefKey,encode()).apply();editing=false;dragPointer=-1;pendingAction=-1;invalidate();listener.onEditorClosed()}
    fun releaseAll(){fingers.clear();emitted.toList().forEach{listener.onKey(KeyEvent.ACTION_UP,it)};emitted.clear();listener.onStick(GLRetroView.MOTION_SOURCE_DPAD,0f,0f);listener.onStick(GLRetroView.MOTION_SOURCE_ANALOG_LEFT,0f,0f);listener.onStick(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,0f,0f);invalidate()}
    override fun onDetachedFromWindow(){releaseAll();super.onDetachedFromWindow()}
    override fun onWindowFocusChanged(hasWindowFocus:Boolean){super.onWindowFocusChanged(hasWindowFocus);if(!hasWindowFocus)releaseAll()}
    override fun onVisibilityChanged(changedView:View,visibility:Int){super.onVisibilityChanged(changedView,visibility);if(initialized&&visibility!=VISIBLE)releaseAll()}
    private fun radius(n:Control):Float {
        val raw=when(n.id){"LS","RS"->.074f;"DPAD"->.068f;"Guide"->.035f;"LB","RB","LT","RT"->.035f;"Back","Start"->.025f;else->.039f}
        return (unit*raw).coerceIn(19*density,62*density)*n.size
    }
    private fun center(n:Control):PointF {val r=radius(n);val rx=if(n.id in listOf("LB","RB","LT","RT"))r*1.8f else r;return PointF((width*n.x).coerceIn(rx.coerceAtMost(width/2f),max(rx,width-rx)),(height*n.y).coerceIn(r.coerceAtMost(height/2f),max(r,height-r)))}
    private fun hit(n:Control,x:Float,y:Float):Boolean {val c=center(n);val r=radius(n)*1.1f;return if(n.id in listOf("LB","RB","LT","RT"))abs(x-c.x)<=r*1.8f&&abs(y-c.y)<=r else hypot(x-c.x,y-c.y)<=r}
    private fun find(x:Float,y:Float):Control?=layout.controls.asReversed().firstOrNull{(editing||it.enabled)&&(editing||controlsVisible||it.id=="Guide")&&hit(it,x,y)}
    private fun buttonState(){
        val now=mutableSetOf<Int>()
        fingers.values.forEach{f->faceKeys[f.id]?.let{now.add(it)}}
        val d=fingers.values.firstOrNull{it.id=="DPAD"};var dx=0f;var dy=0f
        if(d!=null){val n=layout.controls.first{it.id=="DPAD"};val c=center(n);val r=radius(n);dx=(d.x-c.x)/r;dy=(d.y-c.y)/r;dx=if(abs(dx)>.24f)sign(dx)else 0f;dy=if(abs(dy)>.24f)sign(dy)else 0f;if(dx<0)now.add(21);if(dx>0)now.add(22);if(dy<0)now.add(19);if(dy>0)now.add(20)}
        emitted.filter{it !in now}.forEach{listener.onKey(KeyEvent.ACTION_UP,it)};now.filter{it !in emitted}.forEach{listener.onKey(KeyEvent.ACTION_DOWN,it)};emitted.clear();emitted.addAll(now)
        listener.onStick(GLRetroView.MOTION_SOURCE_DPAD,dx,dy)
        for(id in arrayOf("LS","RS")){val finger=fingers.values.firstOrNull{it.id==id};var x=0f;var y=0f;if(finger!=null){val n=layout.controls.first{it.id==id};val c=center(n);val travel=radius(n)*.68f;x=(finger.x-c.x)/travel;y=(finger.y-c.y)/travel;val length=hypot(x,y);if(length<.12f){x=0f;y=0f}else if(length>1f){x/=length;y/=length}};listener.onStick(if(id=="LS")GLRetroView.MOTION_SOURCE_ANALOG_LEFT else GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,x,y)}
        invalidate()
    }
    override fun onTouchEvent(e:MotionEvent):Boolean {
        if(editing)return editTouch(e)
        val pointer=e.getPointerId(e.actionIndex)
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_POINTER_DOWN->{val x=e.getX(e.actionIndex);val y=e.getY(e.actionIndex);val n=find(x,y);if(n==null)return fingers.isNotEmpty();if(n.id=="Guide"){releaseAll();listener.onGuide();return true};if(n.id in listOf("LS","RS","DPAD")&&fingers.values.any{it.id==n.id})return true;fingers[pointer]=Finger(n.id,x,y,x,y);buttonState();return true}
            MotionEvent.ACTION_MOVE->{for(i in 0 until e.pointerCount){val f=fingers[e.getPointerId(i)]?:continue;f.x=e.getX(i);f.y=e.getY(i);if(f.id !in listOf("LS","RS","DPAD")){val next=find(f.x,f.y);f.id=if(next!=null&&next.id in faceKeys)next.id else "outside"}};buttonState();return fingers.isNotEmpty()}
            MotionEvent.ACTION_UP,MotionEvent.ACTION_POINTER_UP->{val captured=fingers.remove(pointer)!=null;buttonState();return captured||fingers.isNotEmpty()}
            MotionEvent.ACTION_CANCEL->{releaseAll();return true}
        };return fingers.isNotEmpty()
    }
    private fun editorScale()=min(width/1000f,height/620f)
    private fun actionRect(index:Int):RectF {val s=editorScale();val gap=8*s;val x=20*s+(index%5)*(width-40*s)/5;val y=(76+(index/5)*48)*s;return RectF(x,y,x+(width-40*s)/5-gap,y+40*s)}
    private fun actionAt(x:Float,y:Float)=(0..9).firstOrNull{actionRect(it).contains(x,y)}?:-1
    private fun editTouch(e:MotionEvent):Boolean {
        val id=e.getPointerId(e.actionIndex)
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN->{pendingAction=actionAt(e.x,e.y);if(pendingAction<0){val n=find(e.x,e.y);if(n!=null){selected=layout.controls.indexOf(n);dragPointer=id;val c=center(n);dragDx=e.x-c.x;dragDy=e.y-c.y}};invalidate()}
            MotionEvent.ACTION_MOVE->{val i=e.findPointerIndex(dragPointer);if(i>=0){val n=layout.controls[selected];n.x=((e.getX(i)-dragDx)/width).coerceIn(.03f,.97f);n.y=((e.getY(i)-dragDy)/height).coerceIn(.04f,.96f);invalidate()}}
            MotionEvent.ACTION_UP->{if(pendingAction>=0&&actionAt(e.x,e.y)==pendingAction)editorAction(pendingAction);dragPointer=-1;pendingAction=-1;invalidate()}
            MotionEvent.ACTION_CANCEL->{dragPointer=-1;pendingAction=-1;invalidate()}
        };return true
    }
    private fun editorAction(action:Int){val n=layout.controls[selected];when(action){
        0->{current=(current+1)%layouts.size;selected=0;status="Editing ${layout.name}"}
        1->{if(layouts.size>=12){status="Up to 10 custom layouts are supported"}else{val copy=Layout("Custom ${layouts.size-1}",layout.controls.map{it.copy()}.toMutableList(),layout.opacity);layouts.add(copy);current=layouts.lastIndex;status="New layout copied from the selected layout"}}
        2->n.size=(n.size-.1f).coerceAtLeast(.5f)
        3->n.size=(n.size+.1f).coerceAtMost(1.8f)
        4->layout.opacity=(layout.opacity-.1f).coerceAtLeast(.15f)
        5->layout.opacity=(layout.opacity+.1f).coerceAtMost(.85f)
        6->{if(n.id=="Guide")status="Guide stays visible so game settings remain accessible" else n.enabled=!n.enabled}
        7->{layouts[current]=defaults(layout.name,current==1);status="Layout reset; Save to keep changes"}
        8->closeEditor()
        9->commit()
    };invalidate()}
    fun handleKey(keyCode:Int,event:KeyEvent):Boolean {
        if(!editing)return false
        if(event.action!=KeyEvent.ACTION_DOWN)return true
        val n=layout.controls[selected]
        when(keyCode){
            97,4,111->closeEditor()
            108->commit()
            109->editorAction(1)
            102->{selected=Math.floorMod(selected-1,layout.controls.size)}
            103->{selected=(selected+1)%layout.controls.size}
            104->editorAction(2)
            105->editorAction(3)
            19->n.y=(n.y-.01f).coerceAtLeast(.04f)
            20->n.y=(n.y+.01f).coerceAtMost(.96f)
            21->n.x=(n.x-.01f).coerceAtLeast(.03f)
            22->n.x=(n.x+.01f).coerceAtMost(.97f)
            99->editorAction(if(layout.opacity>=.8f)4 else 5)
            100->editorAction(6)
            96->editorAction(0)
        };invalidate();return true
    }
    private fun color(argb:Int,alpha:Float)=Color.argb((Color.alpha(argb)*alpha).toInt().coerceIn(0,255),Color.red(argb),Color.green(argb),Color.blue(argb))
    private fun fill(c:Canvas,path:Path,argb:Int){paint.style=Paint.Style.FILL;paint.shader=null;paint.color=argb;c.drawPath(path,paint)}
    private fun label(c:Canvas,s:String,x:Float,y:Float,size:Float,argb:Int){paint.shader=null;paint.style=Paint.Style.FILL;paint.color=argb;paint.typeface=font;paint.textSize=size;paint.textAlign=Paint.Align.CENTER;c.drawText(s,x,y-(paint.ascent()+paint.descent())/2,paint)}
    override fun onDraw(c:Canvas){super.onDraw(c);if(width<=0||height<=0)return;val alpha=if(editing).78f else layout.opacity
        // The controls follow the 360's asymmetrical stick and diamond face-button layout.
        for(n in layout.controls){if(!editing&&(!n.enabled||(!controlsVisible&&n.id!="Guide")))continue;val center=center(n);val r=radius(n);val active=fingers.values.any{it.id==n.id};val a=if(!n.enabled)alpha*.3f else if(active)min(.95f,alpha+.35f)else alpha;c.save();c.translate(center.x,center.y)
            paint.shader=null;paint.style=Paint.Style.FILL;paint.color=color(0xff171a18.toInt(),a)
            when(n.id){
                "LS","RS"->{c.drawCircle(0f,0f,r,paint);paint.style=Paint.Style.STROKE;paint.strokeWidth=max(1.5f,density*1.2f);paint.color=color(0xffd6d9d5.toInt(),a);c.drawCircle(0f,0f,r,paint);val f=fingers.values.firstOrNull{it.id==n.id};var x=0f;var y=0f;if(f!=null&&!editing){x=f.x-center.x;y=f.y-center.y;val d=hypot(x,y);if(d>r*.5f){x*=r*.5f/d;y*=r*.5f/d}};paint.style=Paint.Style.FILL;paint.color=color(0xff333733.toInt(),a);c.drawCircle(x,y,r*.61f,paint);paint.style=Paint.Style.STROKE;paint.color=color(0xffc6cac5.toInt(),a);c.drawCircle(x,y,r*.61f,paint);for(tick in 0..23){val angle=tick*PI/12;val x1=x+cos(angle).toFloat()*r*.52f;val y1=y+sin(angle).toFloat()*r*.52f;c.drawLine(x1,y1,x+cos(angle).toFloat()*r*.59f,y+sin(angle).toFloat()*r*.59f,paint)}}
                "DPAD"->{c.drawCircle(0f,0f,r,paint);val t=r*.34f;val p=Path();p.moveTo(-t,-r*.88f);p.lineTo(t,-r*.88f);p.lineTo(t,-t);p.lineTo(r*.88f,-t);p.lineTo(r*.88f,t);p.lineTo(t,t);p.lineTo(t,r*.88f);p.lineTo(-t,r*.88f);p.lineTo(-t,t);p.lineTo(-r*.88f,t);p.lineTo(-r*.88f,-t);p.lineTo(-t,-t);p.close();fill(c,p,color(0xff878c86.toInt(),a));paint.style=Paint.Style.STROKE;paint.strokeWidth=density;paint.color=color(Color.WHITE,a);c.drawPath(p,paint);paint.style=Paint.Style.FILL;paint.color=color(0xff5a5e59.toInt(),a);c.drawCircle(0f,0f,r*.24f,paint)}
                "Guide"->{paint.shader=RadialGradient(-r*.3f,-r*.4f,r*1.8f,intArrayOf(color(0xfff4f6ed.toInt(),a),color(0xff737d71.toInt(),a)),null,Shader.TileMode.CLAMP);c.drawCircle(0f,0f,r,paint);paint.shader=null;paint.color=color(0xff6eb338.toInt(),min(1f,a+.2f));paint.style=Paint.Style.STROKE;paint.strokeWidth=r*.19f;c.drawCircle(0f,0f,r*.86f,paint);paint.strokeWidth=r*.22f;paint.strokeCap=Paint.Cap.ROUND;c.drawLine(-r*.43f,-r*.45f,r*.43f,r*.45f,paint);c.drawLine(r*.43f,-r*.45f,-r*.43f,r*.45f,paint);paint.strokeCap=Paint.Cap.BUTT}
                "LB","RB","LT","RT"->{val rect=RectF(-r*1.8f,-r*.8f,r*1.8f,r*.8f);c.drawRoundRect(rect,r*.6f,r*.6f,paint);paint.style=Paint.Style.STROKE;paint.strokeWidth=density;paint.color=color(0xffe0e4dc.toInt(),a);c.drawRoundRect(rect,r*.6f,r*.6f,paint);label(c,n.id,0f,0f,r*.72f,color(Color.WHITE,a))}
                "Back","Start"->{c.drawOval(RectF(-r*1.35f,-r*.72f,r*1.35f,r*.72f),paint);paint.style=Paint.Style.STROKE;paint.strokeWidth=density;paint.color=color(0xffdfe5dc.toInt(),a);c.drawOval(RectF(-r*1.35f,-r*.72f,r*1.35f,r*.72f),paint);label(c,if(n.id=="Back")"◀" else "▶",0f,0f,r*.8f,color(Color.WHITE,a))}
                else->{val face=when(n.id){"A"->0xff65b343.toInt();"B"->0xffd8493f.toInt();"X"->0xff4d9bd7.toInt();"Y"->0xffe2c445.toInt();else->0xff818b7d.toInt()};paint.shader=RadialGradient(-r*.2f,-r*.3f,r*1.4f,intArrayOf(color(face,a),color(0xff121613.toInt(),a)),null,Shader.TileMode.CLAMP);c.drawCircle(0f,0f,r,paint);paint.shader=null;paint.style=Paint.Style.STROKE;paint.strokeWidth=max(1.5f,density);paint.color=color(0xffe0e5dc.toInt(),a);c.drawCircle(0f,0f,r,paint);label(c,n.id,0f,0f,r*(if(n.id.length>1).68f else 1.12f),color(if(n.id in listOf("A","B","X","Y"))face else Color.WHITE,min(1f,a+.45f)))}
            }
            if(editing&&layout.controls.indexOf(n)==selected){paint.shader=null;paint.style=Paint.Style.STROKE;paint.color=0xffb6e764.toInt();paint.strokeWidth=3*density;c.drawCircle(0f,0f,r+5*density,paint)};c.restore()
        }
        if(editing)drawEditor(c)
    }
    private fun drawEditor(c:Canvas){val s=editorScale();paint.shader=LinearGradient(0f,0f,0f,174*s,0xffdce7cb.toInt(),0xff94ad70.toInt(),Shader.TileMode.CLAMP);paint.style=Paint.Style.FILL;c.drawRect(0f,0f,width.toFloat(),174*s,paint);paint.shader=null;paint.color=0xff4b702b.toInt();c.drawRect(0f,0f,width.toFloat(),49*s,paint);label(c,"Controller layout • ${layout.name}",width/2f,25*s,27*s,Color.WHITE);label(c,"${layout.controls[selected].id}  •  Size ${(layout.controls[selected].size*100).roundToInt()}%  •  Opacity ${(layout.opacity*100).roundToInt()}%",width/2f,61*s,19*s,0xff27391b.toInt());for(i in 0..9){val r=actionRect(i);paint.color=if(pendingAction==i)0xff789d48.toInt()else 0xffeaf0de.toInt();paint.style=Paint.Style.FILL;c.drawRoundRect(r,6*s,6*s,paint);label(c,if(i==0)"Layout ${current+1}/${layouts.size}" else  editorLabels[i],r.centerX(),r.centerY(),20*s,0xff24351a.toInt())};paint.color=0xc0182313.toInt();c.drawRect(0f,height-45*s,width.toFloat(),height.toFloat(),paint);label(c,status,width/2f,height-23*s,17*s,Color.WHITE)}
}
