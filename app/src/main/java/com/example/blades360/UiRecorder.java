package com.example.blades360;

import android.graphics.*;
import android.media.*;
import android.os.*;
import android.view.View;
import java.io.File;
import java.nio.ByteBuffer;

/** Records only this View; never captures other apps or microphone audio. */
final class UiRecorder {
  interface Done {void complete(boolean saved,String error);}
  final View view;final File file;final Done done;
  final Handler main=new Handler(Looper.getMainLooper());
  final HandlerThread thread=new HandlerThread("Blades-UI-recorder");
  Handler worker; MediaCodec codec; MediaMuxer muxer;
  volatile boolean stopping,finished;boolean muxStarted;int track=-1,frames,width,height;
  long start;String error="";
  UiRecorder(View v,File f,Done d){view=v;file=f;done=d;}
  void start(){width=Math.min(960,view.getWidth())&~1;height=((int)((long)view.getHeight()*width/view.getWidth()))&~1;thread.start();worker=new Handler(thread.getLooper());worker.post(()->{try{MediaFormat format=MediaFormat.createVideoFormat("video/avc",width,height);format.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);format.setInteger(MediaFormat.KEY_BIT_RATE,2500000);format.setInteger(MediaFormat.KEY_FRAME_RATE,8);format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);codec=MediaCodec.createEncoderByType("video/avc");codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);codec.start();muxer=new MediaMuxer(file.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);start=System.nanoTime();main.post(capture);}catch(Exception e){fail(e);}});}
  final Runnable capture=new Runnable(){public void run(){if(stopping||finished){if(worker!=null)worker.post(()->finish());return;}if(System.nanoTime()-start>120000000000L){stop();return;}try{Bitmap b=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.scale((float)width/view.getWidth(),(float)height/view.getHeight());view.draw(c);worker.post(()->encode(b));}catch(Exception e){worker.post(()->fail(e));}}};
  void encode(Bitmap b){try{if(stopping){b.recycle();finish();return;}int slot=codec.dequeueInputBuffer(100000);if(slot>=0){Image image=codec.getInputImage(slot);if(image==null)throw new IllegalStateException("Encoder does not accept image frames");int[] rgb=new int[width*height];b.getPixels(rgb,0,width,0,0,width,height);Image.Plane[] p=image.getPlanes();for(int y=0;y<height;y++)for(int x=0;x<width;x++){int pixel=rgb[y*width+x],r=(pixel>>16)&255,g=(pixel>>8)&255,blue=pixel&255;put(p[0],x,y,((66*r+129*g+25*blue+128)>>8)+16);if((x&1)==0&&(y&1)==0){put(p[1],x/2,y/2,((-38*r-74*g+112*blue+128)>>8)+128);put(p[2],x/2,y/2,((112*r-94*g-18*blue+128)>>8)+128);}}image.close();codec.queueInputBuffer(slot,0,width*height*3/2,(System.nanoTime()-start)/1000,0);frames++;}b.recycle();drain(false);if(stopping)finish();else main.postDelayed(capture,125);}catch(Exception e){if(!b.isRecycled())b.recycle();fail(e);}}
  void put(Image.Plane plane,int x,int y,int value){plane.getBuffer().put(y*plane.getRowStride()+x*plane.getPixelStride(),(byte)Math.max(0,Math.min(255,value)));}
  void drain(boolean end)throws Exception{MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();int empty=0;while(true){int out=codec.dequeueOutputBuffer(info,end?10000:0);if(out==MediaCodec.INFO_TRY_AGAIN_LATER){if(!end||++empty>200)break;}else if(out==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){track=muxer.addTrack(codec.getOutputFormat());muxer.start();muxStarted=true;}else if(out>=0){ByteBuffer buf=codec.getOutputBuffer(out);if(info.size>0&&(info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)==0&&muxStarted){buf.position(info.offset);buf.limit(info.offset+info.size);muxer.writeSampleData(track,buf,info);}boolean eos=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;codec.releaseOutputBuffer(out,false);if(eos)break;}}}
  void stop(){stopping=true;main.removeCallbacks(capture);if(worker!=null)worker.post(()->finish());}
  void fail(Exception e){error=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();finish();}
  void finish(){if(finished)return;finished=true;stopping=true;main.removeCallbacks(capture);try{if(codec!=null){int in=codec.dequeueInputBuffer(100000);if(in>=0){codec.queueInputBuffer(in,0,0,(System.nanoTime()-start)/1000,MediaCodec.BUFFER_FLAG_END_OF_STREAM);drain(true);}}}catch(Exception e){error="Video encoder could not finish";}finally{try{if(codec!=null){codec.stop();codec.release();}}catch(Exception ignored){}try{if(muxer!=null){if(muxStarted)muxer.stop();muxer.release();}}catch(Exception e){error="Video could not be saved";}boolean ok=error.isEmpty()&&muxStarted&&frames>0&&file.length()>0;if(!ok)file.delete();main.post(()->done.complete(ok,error.isEmpty()?"No frames captured":error));thread.quitSafely();}}
}
