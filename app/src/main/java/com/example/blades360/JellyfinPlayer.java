package com.example.blades360;

import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.UUID;

/** Native playback with a Canvas Blades transport overlay, never Android's MediaController. */
final class JellyfinPlayer extends FrameLayout {
  final JellyfinBrowser browser;final MainActivity a;final JellyfinClient client;final JSONObject item;
  final VideoView video;final Transport controls;final String session=UUID.randomUUID().toString().replace("-","");
  boolean prepared,closed,photo;String status="Loading media…";int elapsed,duration;
  final Runnable tick=new Runnable(){public void run(){if(closed)return;if(prepared){elapsed=video.getCurrentPosition();duration=video.getDuration();}controls.invalidate();postDelayed(this,500);}};
  JellyfinPlayer(JellyfinBrowser browser,JSONObject item){super(browser.a);this.browser=browser;this.a=browser.a;this.client=browser.ra;this.item=item;setBackgroundColor(Color.BLACK);setFocusableInTouchMode(true);setKeepScreenOn(true);photo=item.optString("MediaType",item.optString("Type")).equals("Photo");
    video=new VideoView(a);video.setFocusable(false);FrameLayout.LayoutParams vp=new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER);addView(video,vp);controls=new Transport();addView(controls,new FrameLayout.LayoutParams(-1,-1));
    if(photo){video.setVisibility(GONE);status="Photo";post(tick);return;}
    video.setOnPreparedListener(p->{prepared=true;duration=p.getDuration();status="Playing";video.start();controls.invalidate();});video.setOnCompletionListener(p->{status="Finished";controls.invalidate();});video.setOnErrorListener((p,w,e)->{prepared=false;status="Cannot play this stream. Go back and try the other playback mode.";controls.invalidate();return true;});
    try{String id=item.getString("Id"),media=item.optString("MediaType",item.optString("Type"));JSONArray sources=item.optJSONArray("MediaSources");String source=sources!=null&&sources.length()>0?sources.getJSONObject(0).optString("Id",id):id;boolean audio=media.equals("Audio"),direct=client.vault.prefs.getBoolean("direct",false);String path;
      if(direct)path=(audio?"/Audio/":"/Videos/")+id+"/stream?static=true&MediaSourceId="+JellyfinClient.encode(source);
      else if(audio)path="/Audio/"+id+"/stream.mp3?AudioCodec=mp3&AudioBitrate=192000&MediaSourceId="+JellyfinClient.encode(source);
      else path="/Videos/"+id+"/master.m3u8?MediaSourceId="+JellyfinClient.encode(source)+"&VideoCodec=h264&AudioCodec=aac&VideoBitrate=8000000&AudioBitrate=192000&MaxWidth=1920&TranscodingMaxAudioChannels=2&SegmentContainer=ts&MinSegments=2&BreakOnNonKeyFrames=true";
      path+="&DeviceId=blades360-tablet&PlaySessionId="+session;video.setVideoURI(Uri.parse(client instanceof PlexClient?((PlexClient)client).stream(item,session,direct):client.server()+path),client.headers());post(tick);
    }catch(Exception e){status="Unable to start playback. Check your connection.";controls.invalidate();}
  }
  void toggle(){if(!prepared)return;if(video.isPlaying()){video.pause();status="Paused";}else{video.start();status="Playing";}controls.invalidate();}
  void seek(int delta){if(prepared)video.seekTo(Math.max(0,Math.min(video.getDuration(),video.getCurrentPosition()+delta)));}
  void pause(){if(prepared&&video.isPlaying()){video.pause();status="Paused";}}
  void close(){if(closed)return;closed=true;removeCallbacks(tick);video.stopPlayback();setKeepScreenOn(false);client.stopEncoding(session);if(getParent() instanceof ViewGroup)((ViewGroup)getParent()).removeView(this);browser.player=null;browser.requestFocus();browser.invalidate();}
  @Override public boolean onKeyDown(int key,KeyEvent e){switch(key){case 4:case 97:close();break;case 96:case 66:case 23:if(e.getRepeatCount()==0)toggle();break;case 21:seek(-10000);break;case 22:seek(10000);break;}return true;}
  @Override public boolean onKeyUp(int key,KeyEvent e){return true;}
  String time(int ms){return String.format(java.util.Locale.ROOT,"%d:%02d",Math.max(0,ms)/60000,(Math.max(0,ms)/1000)%60);}
  final class Transport extends View {
    final Paint p=new Paint(3);Transport(){super(a);setFocusable(false);}
    void text(Canvas c,String s,float x,float y,float size){p.setColor(Color.WHITE);p.setTypeface(a.dashboard.regular);p.setTextSize(size);c.drawText(browser.fit(s,getWidth()-x-28,size),x,y,p);}
    @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight(),scale=w/1280f;float bottom=105*scale;
      if(photo||item.optString("MediaType").equals("Audio")){Bitmap b=client.image(client.art(item));if(b!=null){float factor=Math.min(w*.75f/b.getWidth(),(h-bottom-100*scale)/b.getHeight());float bw=b.getWidth()*factor,bh=b.getHeight()*factor;p.setColor(Color.WHITE);c.drawBitmap(b,null,new RectF((w-bw)/2,(h-bottom-bh)/2,(w+bw)/2,(h-bottom+bh)/2),p);}}
      p.setColor(0xbb17395d);c.drawRect(0,0,w,65*scale,p);text(c,item.optString("Name"),26*scale,43*scale,27*scale);
      p.setShader(new LinearGradient(0,h-bottom,0,h,0xee84b8d4,0xee234e7a,Shader.TileMode.CLAMP));p.setAlpha(255);c.drawRoundRect(new RectF(8*scale,h-bottom,w-8*scale,h+18*scale),20*scale,20*scale,p);p.setShader(null);
      text(c,status,28*scale,h-77*scale,20*scale);if(prepared){p.setColor(0xff355c7a);c.drawRect(28*scale,h-63*scale,w-28*scale,h-57*scale,p);p.setColor(0xffd9eeb4);c.drawRect(28*scale,h-63*scale,28*scale+(w-56*scale)*Math.min(1f,elapsed/(float)Math.max(1,duration)),h-57*scale,p);}
      text(c,"B  Back",30*scale,h-23*scale,24*scale);if(!photo){text(c,"◀  10 sec",245*scale,h-23*scale,24*scale);text(c,prepared&&video.isPlaying()?"A  Pause":"A  Play",480*scale,h-23*scale,24*scale);text(c,"10 sec  ▶",710*scale,h-23*scale,24*scale);text(c,time(elapsed)+" / "+time(duration),960*scale,h-23*scale,21*scale);}
    }
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()/getWidth()*1280,y=e.getY();if(y>getHeight()-105*getWidth()/1280f){if(x<210)close();else if(x<440)seek(-10000);else if(x<680)toggle();else if(x<940)seek(10000);}else toggle();return true;}
  }
}
