package com.example.blades360;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Download/cache integration uses a local fixture and separate encrypted files. */
public final class IptvImportInstrumentation extends Instrumentation {
 int checks;void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){Bundle output=new Bundle();MainActivity a=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));IptvClient[] clients=new IptvClient[2];
  try(ServerSocket socket=new ServerSocket(0,5,InetAddress.getByName("127.0.0.1"))){socket.setSoTimeout(15000);long now=System.currentTimeMillis();java.text.SimpleDateFormat format=new java.text.SimpleDateFormat("yyyyMMddHHmmss Z",Locale.ROOT);format.setTimeZone(TimeZone.getTimeZone("UTC"));String epg="<tv><programme channel=\"fixture\" start=\""+format.format(new Date(now-10000))+"\" stop=\""+format.format(new Date(now+600000))+"\"><title>Current fixture show</title></programme></tv>";Throwable[] failure={null};Thread server=new Thread(()->{try{for(int i=0;i<3;i++)try(Socket c=socket.accept()){BufferedReader in=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"));String request=in.readLine();for(String line;(line=in.readLine())!=null&&!line.isEmpty();){}String body=i==0?"#EXTM3U x-tvg-url=\"guide.xml\"\n#EXTINF:-1 tvg-id=\"fixture\" group-title=\"Test\",Fixture channel\nhttp://127.0.0.1/stream.ts\n":i==1?epg:"Unavailable";if(i==1&&!request.contains("/guide.xml"))throw new AssertionError("Guide URL discovery");byte[] data=body.getBytes("UTF-8");OutputStream out=c.getOutputStream();out.write(("HTTP/1.1 "+(i==2?"503 Service Unavailable":"200 OK")+"\r\nContent-Length: "+data.length+"\r\nConnection: close\r\n\r\n").getBytes("UTF-8"));out.write(data);out.flush();}}catch(Throwable t){failure[0]=t;}});server.start();
   CountDownLatch ready=new CountDownLatch(1);runOnMainSync(()->{if(a.bootAnimation!=null)a.bootAnimation.finish();clients[0]=new IptvClient(a,"iptv-import-test");clients[0].changed=()->{if(!clients[0].busy&&clients[0].programmes.size()==1)ready.countDown();};clients[0].set("playlist","http://127.0.0.1:"+socket.getLocalPort()+"/list.m3u");});check(ready.await(15,TimeUnit.SECONDS),"M3U and discovered XMLTV load");check(clients[0].channels.size()==1,"Imported channel retained");check(clients[0].now(clients[0].channels.get(0))!=null,"Now-playing guide match");check(clients[0].guideSource().endsWith("/guide.xml"),"Discovered guide saved");
   CountDownLatch failed=new CountDownLatch(1);runOnMainSync(()->{clients[0].changed=()->{if(!clients[0].busy)failed.countDown();};clients[0].refresh();});check(failed.await(15,TimeUnit.SECONDS),"Network failure completes");check(clients[0].channels.size()==1&&clients[0].status.contains("retained"),"Failed refresh preserves saved channels");
   CountDownLatch cached=new CountDownLatch(1);runOnMainSync(()->{clients[1]=new IptvClient(a,"iptv-import-test");clients[1].changed=()->cached.countDown();});check(cached.await(10,TimeUnit.SECONDS),"Encrypted cache reload");check(clients[1].channels.size()==1&&clients[1].programmes.size()==1,"Playlist and EPG restore offline");check(failure[0]==null,"Fixture request sequence");server.join(1000);output.putString("stream","PASS: "+checks+" IPTV import checks");
  }catch(Throwable e){output.putString("stream","FAIL: "+e.getClass().getSimpleName()+": "+e.getMessage());}finally{runOnMainSync(()->{for(IptvClient c:clients)if(c!=null)c.close();a.deleteFile("iptv-import-test-config.bin");a.deleteFile("iptv-import-test-cache.bin");a.deleteFile("iptv-import-test-guide.bin");a.getSharedPreferences("iptv-import-test_options",0).edit().clear().commit();});}
  finish(output.getString("stream").startsWith("PASS")?Activity.RESULT_OK:Activity.RESULT_CANCELED,output);
 }
}
