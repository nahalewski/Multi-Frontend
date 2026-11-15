package com.example.blades360;

import android.content.Context;
import android.graphics.*;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.*;

/** Procedural recreation of the 2005 sphere/ribbons/wordmark boot, not a video. */
final class BootAnimationView extends View {
  static final long DURATION=5400;
  final Paint p=new Paint(3);final Runnable complete;final Bitmap logo;
  long started=-1,pausedAt=-1;boolean finished;MediaPlayer sound;
  BootAnimationView(Context context,boolean audio,float volume,Runnable complete){super(context);this.complete=complete;Bitmap decoded=null;try{decoded=BitmapFactory.decodeStream(context.getAssets().open("console_logos/xbox360.png"));}catch(Exception ignored){}logo=decoded;setFocusableInTouchMode(true);setContentDescription("Xbox 360 startup animation. Press A or tap to skip.");if(audio){sound=MediaPlayer.create(context,R.raw.boot_recreated);if(sound!=null)sound.setVolume(volume,volume);}}
  static float phase(float t,float from,float to){return Math.max(0,Math.min(1,(t-from)/(to-from)));}
  static float ease(float x){return x*x*(3-2*x);}
  void pause(){if(finished||pausedAt>=0)return;pausedAt=SystemClock.uptimeMillis();if(sound!=null&&sound.isPlaying())sound.pause();}
  void resume(){if(pausedAt>=0){if(started>=0)started+=SystemClock.uptimeMillis()-pausedAt;pausedAt=-1;if(sound!=null&&started>=0)sound.start();invalidate();}}
  void dispose(){finished=true;release();}
  private void release(){if(sound!=null){sound.release();sound=null;}}
  void finish(){if(finished)return;finished=true;release();if(getParent() instanceof ViewGroup)((ViewGroup)getParent()).removeView(this);complete.run();}
  @Override protected void onDetachedFromWindow(){release();super.onDetachedFromWindow();}
  @Override public boolean onKeyDown(int key,KeyEvent e){if(key==96||key==97||key==4||key==66||key==108)finish();return true;}
  @Override public boolean onKeyUp(int key,KeyEvent e){return true;}
  @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP)finish();return true;}
  @Override protected void onDraw(Canvas c){
    if(finished)return;long now=pausedAt>=0?pausedAt:SystemClock.uptimeMillis();if(started<0){started=now;if(sound!=null&&pausedAt<0)sound.start();}float t=(now-started)/1000f;
    if(t>=DURATION/1000f){post(this::finish);return;}
    p.setAlpha(255);float unit=Math.min(getWidth()/1280f,getHeight()/720f);c.drawColor(0xfff4f6ee);c.save();c.translate(getWidth()/2f,getHeight()/2f);c.scale(unit,unit);
    p.setShader(new RadialGradient(0,-100,780,new int[]{0xffffffff,0xfff1f5e7,0xffd5dfc9},null,Shader.TileMode.CLAMP));c.drawRect(-getWidth()/unit,-getHeight()/unit,getWidth()/unit,getHeight()/unit,p);p.setShader(null);
    float pull=ease(phase(t,.15f,2.8f));float radius=720+(108-720)*pull;float cy=640+(-72-640)*pull;
    // Green luminous orbital ribbons contract around the forming sphere.
    float ribbons=phase(t,.45f,1.1f)*(1-phase(t,2.7f,3.4f));
    if(ribbons>0){c.save();c.translate(0,cy);c.rotate(-30+t*100);p.setStyle(Paint.Style.STROKE);for(int i=0;i<7;i++){float r=radius*(1.12f+i*.12f);p.setStrokeWidth(12-i);p.setColor(Color.argb((int)(ribbons*(125-i*13)),104+i*9,178+i*6,18));c.drawArc(-r,-r*.48f,r,r*.48f,i*43+t*110,210-i*13,false,p);}p.setStyle(Paint.Style.FILL);c.restore();}
    // Ripple expands out as the mark locks into place.
    float ripple=phase(t,2.65f,3.8f);if(ripple>0&&ripple<1){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(25*(1-ripple)+2);p.setColor(Color.argb((int)(115*(1-ripple)),141,193,47));c.drawCircle(0,-72,110+ripple*760,p);p.setStyle(Paint.Style.FILL);}
    p.setAlpha(255);c.save();c.translate(0,cy);c.rotate((1-pull)*-48);p.setShader(new RadialGradient(-radius*.35f,-radius*.42f,radius*1.6f,new int[]{0xffffffff,0xffe7e9e7,0xffa9ada9,0xff6e776e},new float[]{0,.38f,.76f,1},Shader.TileMode.CLAMP));c.drawCircle(0,0,radius,p);p.setShader(null);
    Path sphere=new Path();sphere.addCircle(0,0,radius,Path.Direction.CW);c.clipPath(sphere);float reveal=ease(phase(t,.65f,2.6f));c.save();c.scale(reveal,1);
    Path x=new Path();x.moveTo(-radius*.82f,-radius*.68f);x.cubicTo(-radius*.46f,-radius*.64f,-radius*.17f,-radius*.46f,0,-radius*.24f);x.cubicTo(radius*.2f,-radius*.46f,radius*.5f,-radius*.65f,radius*.83f,-radius*.68f);x.cubicTo(radius*.44f,-radius*.3f,radius*.2f,-radius*.02f,radius*.12f,radius*.1f);x.cubicTo(radius*.46f,radius*.45f,radius*.7f,radius*.78f,radius*.8f,radius);x.cubicTo(radius*.4f,radius*.62f,radius*.16f,radius*.37f,0,radius*.22f);x.cubicTo(-radius*.24f,radius*.45f,-radius*.53f,radius*.78f,-radius*.8f,radius);x.cubicTo(-radius*.65f,radius*.56f,-radius*.34f,radius*.27f,-radius*.12f,radius*.1f);x.cubicTo(-radius*.32f,-radius*.18f,-radius*.58f,-radius*.48f,-radius*.82f,-radius*.68f);x.close();
    p.setShader(new RadialGradient(0,-radius*.15f,radius*1.1f,new int[]{0xffeffb92,0xffa7d92c,0xff539900},null,Shader.TileMode.CLAMP));c.drawPath(x,p);p.setShader(null);c.restore();c.restore();
    float textAlpha=ease(phase(t,2.9f,3.6f));if(logo!=null&&textAlpha>0){p.setAlpha((int)(255*textAlpha));c.drawBitmap(logo,new Rect(290,0,logo.getWidth(),logo.getHeight()),new RectF(-230,67,230,193),p);p.setAlpha(255);}
    c.restore();setAlpha(1-ease(phase(t,5.05f,5.4f)));if(pausedAt<0)postInvalidateOnAnimation();
  }
}
