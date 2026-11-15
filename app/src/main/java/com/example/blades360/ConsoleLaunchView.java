package com.example.blades360;

import android.app.Activity;
import android.app.Application;
import android.graphics.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.*;
import java.util.WeakHashMap;

/** Attributed console logo assets, rendered before either an embedded or external launch. */
final class ConsoleLaunchView extends View implements Application.ActivityLifecycleCallbacks {
    private static final WeakHashMap<Activity,ConsoleLaunchView> OPEN=new WeakHashMap<>();
    private final Activity activity;
    private final String system,title;
    private final Runnable launch;
    private final View previousFocus;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface font;
    private final Bitmap consoleLogo;
    private final long started=SystemClock.uptimeMillis();
    private final Runnable complete=()->finish(true);
    private boolean finished;
    private float scale,ox,oy;

    static ConsoleLaunchView show(Activity activity,String system,String title,Runnable launch){
        cancel(activity);
        ConsoleLaunchView view=new ConsoleLaunchView(activity,system,title,launch);
        OPEN.put(activity,view);
        activity.addContentView(view,new ViewGroup.LayoutParams(-1,-1));
        activity.getApplication().registerActivityLifecycleCallbacks(view);
        view.requestFocus();
        view.postDelayed(view.complete,900);
        return view;
    }
    static boolean cancel(Activity activity){ConsoleLaunchView view=OPEN.get(activity);if(view==null)return false;view.finish(false);return true;}
    private ConsoleLaunchView(Activity a,String system,String title,Runnable launch){
        super(a);this.activity=a;this.previousFocus=a.getCurrentFocus();this.system=system==null?"Game console":system;this.title=title==null?"Game":title;this.launch=launch;
        Typeface face;try{face=Typeface.createFromAsset(a.getAssets(),"convection.ttf");}catch(Exception ignored){face=Typeface.SANS_SERIF;}font=face;
        Bitmap image=null;String asset=assetName(this.system);if(asset!=null)try(java.io.InputStream in=a.getAssets().open("console_logos/"+asset+".png")){image=BitmapFactory.decodeStream(in);}catch(Exception ignored){}consoleLogo=image;
        setFocusable(true);setFocusableInTouchMode(true);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);setContentDescription("Launching "+this.title+" on "+this.system+". Back cancels.");
    }
    private void finish(boolean play){
        if(finished)return;finished=true;removeCallbacks(complete);OPEN.remove(activity);
        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        boolean attached=isAttachedToWindow();
        if(getParent() instanceof ViewGroup)((ViewGroup)getParent()).removeView(this);
        if(play&&attached&&!activity.isFinishing()&&!activity.isDestroyed())launch.run();
        else if(previousFocus!=null&&previousFocus.isAttachedToWindow())previousFocus.requestFocus();
    }
    @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();if(!finished)finish(false);}
    @Override public boolean onKeyDown(int key,KeyEvent event){if(key==KeyEvent.KEYCODE_BACK||key==KeyEvent.KEYCODE_BUTTON_B||key==KeyEvent.KEYCODE_ESCAPE)finish(false);return true;}
    @Override public boolean onKeyUp(int key,KeyEvent event){return true;}
    @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP&&(e.getY()-oy)/scale>480)finish(false);return true;}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);c.drawColor(0xff10150e);scale=Math.min(getWidth()/960f,getHeight()/560f);ox=(getWidth()-960*scale)/2;oy=(getHeight()-560*scale)/2;
        c.save();c.translate(ox,oy);c.scale(scale,scale);
        paint.setShader(new RadialGradient(480,240,470,new int[]{0xff34432a,0xff10150e},null,Shader.TileMode.CLAMP));c.drawRect(0,0,960,560,paint);paint.setShader(null);
        logo(c);
        text(c,system,480,348,34,0xfff6f8ed,Typeface.create(font,Typeface.BOLD));
        text(c,fit(title,790,23),480,390,23,0xffc6d5ba,font);
        text(c,"Preparing game",480,430,20,0xffa3b68f,font);
        paint.setColor(0xff33432b);c.drawRoundRect(325,452,635,456,2,2,paint);
        float progress=Math.min(1,(SystemClock.uptimeMillis()-started)/900f);paint.setColor(0xff9dce54);c.drawRoundRect(325,452,325+310*progress,456,2,2,paint);
        paint.setColor(0xffae3830);c.drawCircle(433,518,15,paint);text(c,"B",433,525,21,Color.WHITE,font);text(c,"Cancel",492,526,22,0xffe1e9d7,font);
        c.restore();if(!finished)postInvalidateOnAnimation();
    }
    private String fit(String value,float width,float size){paint.setTypeface(font);paint.setTextSize(size);if(paint.measureText(value)<=width)return value;int n=paint.breakText(value,true,width-paint.measureText("..."),null);return value.substring(0,n)+"...";}
    private void text(Canvas c,String value,float x,float y,float size,int color,Typeface face){paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setTypeface(face);paint.setTextSize(size);paint.setColor(color);paint.setTextAlign(Paint.Align.CENTER);c.drawText(value,x,y,paint);}
    private void stroke(Canvas c,Path path,int color,float width){paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(width);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(color);c.drawPath(path,paint);paint.setStyle(Paint.Style.FILL);}
    private void logo(Canvas c){
        if(consoleLogo!=null){float ratio=consoleLogo.getWidth()/(float)consoleLogo.getHeight();float width=Math.min(700,210*ratio),height=width/ratio;paint.setShader(null);paint.setColor(Color.WHITE);paint.setFilterBitmap(true);c.drawBitmap(consoleLogo,null,new RectF(480-width/2,205-height/2,480+width/2,205+height/2),paint);return;}
        String s=system.toLowerCase(java.util.Locale.ROOT);
        if(s.contains("xbox")){paint.setColor(0xffe9f0e3);c.drawCircle(480,205,83,paint);Path x=new Path();x.moveTo(425,151);x.cubicTo(454,164,503,213,537,263);x.moveTo(536,151);x.cubicTo(507,164,458,213,423,263);stroke(c,x,0xff76b82a,22);return;}
        if(s.contains("switch")){paint.setColor(0xffe82c37);c.drawRoundRect(379,117,579,291,24,24,paint);paint.setColor(Color.WHITE);c.drawRoundRect(413,145,470,264,24,24,paint);paint.setColor(0xffe82c37);c.drawRoundRect(420,152,463,257,19,19,paint);paint.setColor(Color.WHITE);c.drawCircle(441,177,12,paint);c.drawRoundRect(483,145,545,264,24,24,paint);paint.setColor(0xffe82c37);c.drawCircle(514,231,13,paint);return;}
        if(s.contains("playstation")||s.equals("psp")||s.contains("vita")){
            Path ps=new Path();ps.moveTo(460,279);ps.lineTo(460,135);ps.cubicTo(535,133,553,199,494,217);stroke(c,ps,0xffe83939,24);
            Path base=new Path();base.moveTo(433,214);base.cubicTo(357,231,390,267,442,251);base.moveTo(496,233);base.cubicTo(566,210,593,250,508,273);stroke(c,base,0xff25a0c7,15);
            text(c,s.equals("psp")?"PSP":s.contains("vita")?"PS VITA":"PlayStation",480,310,25,Color.WHITE,Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL));return;
        }
        if(s.contains("64")){Path n=new Path();n.moveTo(415,281);n.lineTo(415,134);n.lineTo(543,279);n.lineTo(543,132);stroke(c,n,0xff23ab56,39);Path blue=new Path();blue.moveTo(393,266);blue.lineTo(393,120);blue.lineTo(522,264);blue.lineTo(522,118);stroke(c,blue,0xff3560d7,28);paint.setColor(0xfff2bf2c);c.drawRect(380,111,409,131,paint);paint.setColor(0xffe03c35);c.drawRect(509,107,537,127,paint);return;}
        if(s.contains("gamecube")){Path hex=new Path();hex.moveTo(480,117);hex.lineTo(558,160);hex.lineTo(558,248);hex.lineTo(480,292);hex.lineTo(402,248);hex.lineTo(402,160);hex.close();stroke(c,hex,0xffa899e5,17);Path cube=new Path();cube.moveTo(480,149);cube.lineTo(529,177);cube.lineTo(480,205);cube.lineTo(431,177);cube.close();cube.moveTo(480,205);cube.lineTo(480,263);cube.moveTo(431,177);cube.lineTo(431,235);cube.lineTo(480,263);cube.lineTo(529,235);cube.lineTo(529,177);stroke(c,cube,0xffc6bcf5,8);return;}
        if(s.contains("dreamcast")){Path spiral=new Path();for(int i=0;i<180;i++){double angle=i*.095;double r=4+i*.40;float x=480+(float)(Math.cos(angle)*r),y=198+(float)(Math.sin(angle)*r);if(i==0)spiral.moveTo(x,y);else spiral.lineTo(x,y);}stroke(c,spiral,0xffef7135,9);return;}
        if(s.contains("mega drive")||s.contains("master system")||s.contains("game gear")||s.contains("sega")){text(c,"SEGA",480,227,90,0xff4a8be6,Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD));paint.setColor(0xffd4e5ff);c.drawRect(340,235,620,239,paint);return;}
        if(s.contains("wii")){text(c,s.contains("u")?"Wii U":"Wii",480,244,112,0xffe3edf0,Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL));return;}
        if(s.contains("game boy")){paint.setColor(0xffd5dbcb);c.drawRoundRect(413,102,547,294,15,15,paint);paint.setColor(0xff647d50);c.drawRoundRect(430,124,531,202,6,6,paint);paint.setColor(0xff313c2b);c.drawRect(430,231,466,244,paint);c.drawRect(442,219,454,256,paint);paint.setColor(0xff9e3b69);c.drawCircle(506,247,10,paint);c.drawCircle(529,232,10,paint);return;}
        if(s.contains("3ds")||s.contains("ds")){paint.setColor(0xffd5dce0);c.drawRoundRect(390,112,570,197,12,12,paint);c.drawRoundRect(390,206,570,291,12,12,paint);paint.setColor(0xff2c4653);c.drawRect(423,127,537,184,paint);c.drawRect(423,219,537,278,paint);paint.setColor(0xffdb343c);c.drawCircle(555,229,5,paint);return;}
        if(s.contains("nintendo")||s.equals("nes")){paint.setColor(0xffe43639);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(9);c.drawRoundRect(290,158,670,245,43,43,paint);paint.setStyle(Paint.Style.FILL);text(c,"Nintendo",480,221,58,0xfff0f0e7,Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD));return;}
        paint.setColor(0xffa5c871);c.drawRoundRect(380,150,580,254,35,35,paint);paint.setColor(0xff354629);c.drawRect(402,195,454,211,paint);c.drawRect(420,177,436,229,paint);c.drawCircle(535,184,13,paint);c.drawCircle(558,215,13,paint);
    }
    private static String assetName(String system){switch(system){
        case "Game Boy Advance":return "gba";case "Game Boy":return "gb";case "Game Boy Color":return "gbc";
        case "Nintendo 64":return "n64";case "NES":return "nes";case "Super Nintendo":return "snes";
        case "Nintendo DS":return "nds";case "Nintendo 3DS":return "3ds";case "Nintendo Switch":return "switch";
        case "GameCube":case "GameCube / Wii":return "gc";case "Wii":return "wii";case "Wii U":return "wiiu";
        case "PlayStation":return "psx";case "PlayStation 2":return "ps2";case "PlayStation 3":return "ps3";case "PlayStation 4":return "ps4";
        case "PSP":return "psp";case "PS Vita":return "psvita";case "Mega Drive":return "megadrive";case "Master System":return "mastersystem";
        case "Game Gear":return "gamegear";case "Dreamcast":return "dreamcast";case "Xbox":case "Original Xbox":return "xbox";case "Xbox 360":return "xbox360";case "PC":return "windows";
        default:return null;
    }}
    @Override public void onActivityPaused(Activity a){if(a==activity)finish(false);}
    @Override public void onActivityDestroyed(Activity a){if(a==activity)finish(false);}
    @Override public void onActivityCreated(Activity a,Bundle b){}
    @Override public void onActivityStarted(Activity a){}
    @Override public void onActivityResumed(Activity a){}
    @Override public void onActivityStopped(Activity a){}
    @Override public void onActivitySaveInstanceState(Activity a,Bundle b){}
}
