package com.example.blades360;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import org.json.*;
import java.util.concurrent.*;

/** Live read-only integration against the configured server; never prints credentials or changes server media. */
public final class JellyfinInstrumentation extends Instrumentation {
 int checks;MainActivity a;JellyfinBrowser browser;
 void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 Object request(String path)throws Exception{Object[] value={null};CountDownLatch done=new CountDownLatch(1);runOnMainSync(()->a.jellyfin.get(path,result->{value[0]=result;done.countDown();}));check(done.await(25,TimeUnit.SECONDS),"Jellyfin request completed");return value[0];}
 void ui(Runnable r){runOnMainSync(r);}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){Bundle result=new Bundle();a=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  try{ui(()->{if(a.bootAnimation!=null)a.bootAnimation.finish();a.openJellyfin();browser=a.jellyfinBrowser;});check(a.jellyfin.connected(),"Configured connection available");check(!a.jellyfin.user().isEmpty(),"Profile selected");
   JSONArray users=(JSONArray)request("/Users");check(users.length()>0,"Authenticated profile listing");JSONObject libs=(JSONObject)request("/Users/"+a.jellyfin.user()+"/Views");check(libs.getJSONArray("Items").length()>0,"Libraries available");
   String path="/Items?UserId="+a.jellyfin.user()+"&Recursive=true&IncludeItemTypes=Movie&Fields=Overview,MediaSources&SortBy=SortName&Limit=2";JSONObject movies=(JSONObject)request(path+"&StartIndex=0");JSONArray items=movies.getJSONArray("Items");check(items.length()==2,"Media page size respected");JSONObject next=(JSONObject)request(path+"&StartIndex=2");check(!next.getJSONArray("Items").getJSONObject(0).getString("Id").equals(items.getJSONObject(0).getString("Id")),"Next page returns different media");
   ui(()->{browser.entries=items;browser.total=movies.optInt("TotalRecordCount");browser.listTitle="Movies";browser.page(2);check(browser.rows.size()==3,"Next-page action present");browser.move(999);check(browser.index==2,"Gamepad index clamped");browser.index=0;browser.select();check(browser.screen==3,"Media detail opens");Bitmap image=Bitmap.createBitmap(2960,1848,Bitmap.Config.RGB_565);browser.layout(0,0,2960,1848);browser.draw(new Canvas(image));image.recycle();browser.back();check(browser.screen==2,"Back returns to media list");browser.index=0;browser.select();browser.play();});
   boolean[] ready={false};for(int i=0;i<50;i++){ui(()->ready[0]=browser.player!=null&&browser.player.prepared);if(ready[0])break;Thread.sleep(500);}check(ready[0],"Live video prepares successfully");ui(()->{check(browser.player.video.isPlaying(),"Live video starts");browser.player.toggle();check(!browser.player.video.isPlaying(),"Blades pause works");browser.player.seek(10000);browser.player.close();check(browser.player==null,"Player closes and releases overlay");check(a.jellyfinBrowser==browser,"Browser survives playback");});
   result.putString("stream","PASS: "+checks+" Jellyfin integration checks");
  }catch(Throwable e){result.putString("stream","FAIL: "+e.getClass().getSimpleName()+": "+e.getMessage());}
  finally{ui(()->{if(browser!=null)browser.close();});}
  finish(result.getString("stream").startsWith("PASS")?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
 }
}
