package com.example.blades360;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.view.KeyEvent;
import org.json.*;

/** In-memory fixture validation; never substitutes test totals for the player's saved account. */
public final class RetroAchievementsInstrumentation extends Instrumentation {
 int checks;void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){Bundle result=new Bundle();final Throwable[] error={null};MainActivity a=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));runOnMainSync(()->{
   RetroAchievements ra=a.retroAchievements;JSONObject originalProfile=ra.profile;JSONArray originalGames=ra.games;long originalUpdated=ra.updated;String originalStatus=ra.status;boolean hadKey=ra.prefs.contains("key");String originalKey=ra.prefs.getString("key","");
   try{if(a.bootAnimation!=null)a.bootAnimation.finish();a.tools.close();
     ra.profile=new JSONObject("{\"User\":\"TEST FIXTURE\",\"TotalPoints\":100,\"TotalSoftcorePoints\":25}");ra.games=new JSONArray("[{\"GameID\":1,\"Title\":\"Fixture Game\",\"NumAwarded\":6,\"NumAwardedHardcore\":4,\"MaxPossible\":6},{\"GameID\":2,\"Title\":\"Fixture Game 2\",\"NumAwarded\":3,\"NumAwardedHardcore\":1,\"MaxPossible\":10}]");ra.updated=1;
     check(RetroAchievements.count(ra.games,false)==9,"Unique achievement total");check(RetroAchievements.count(ra.games,true)==5,"Hardcore total");
     JSONArray repeated=new JSONArray(ra.games.toString());repeated.put(ra.games.getJSONObject(0));check(RetroAchievements.count(repeated,false)==9,"Repeated game is not counted twice");
     check(ra.achievements().equals("9"),"Dashboard displays achievements");
     check(ra.setKey("0123456789abcdef0123456789abcdef"),"Keystore encryption works");check(!ra.prefs.getString("key","").contains("0123456789abcdef"),"Stored key is not plaintext");check(!ra.setKey("invalid"),"Malformed key rejected");
     RetroMarketplace view=new RetroMarketplace(a);view.layout(0,0,2960,1848);Bitmap image=Bitmap.createBitmap(2960,1848,Bitmap.Config.RGB_565);view.draw(new Canvas(image));check(view.screen==2&&view.rows.size()==2,"Achievements opens directly to game progress");view.page(0);check(view.rows.size()==5,"Achievement options");view.page(2);check(view.rows.size()==2,"Account game list");view.page(3);check(view.rows.size()==1,"Completed filter");view.page(4);view.move(99);check(view.index==5,"Settings scroll clamped");view.back();check(view.screen==2,"Back returns to Achievements");
     view.detail=new JSONObject("{\"Title\":\"Fixture\",\"Achievements\":{\"1\":{\"Title\":\"First\",\"Points\":5,\"Description\":\"Test achievement\",\"DateEarned\":\"2026-01-01\"},\"2\":{\"Title\":\"Second\",\"Points\":10}}}");view.page(5);check(view.rows.get(0).sub.contains("Unlocked"),"Earned achievement status");check(view.rows.get(1).sub.contains("Locked"),"Locked achievement status");view.draw(new Canvas(image));image.recycle();view.close();
     RommClient originalRomm=a.romm;RommClient fixture=new RommClient(a,"romm_instrumentation");a.romm=fixture;
     try{fixture.prefs.edit().clear().commit();fixture.server("https://example.invalid/romm/");check(fixture.server().equals("https://example.invalid/romm"),"RomM base path normalization");fixture.credential("test-only-token");check(fixture.authorization().equals("Bearer test-only-token"),"Token authorization");check(!fixture.prefs.getString("credential","").contains("test-only-token"),"RomM token encrypted");fixture.server("https://other.invalid");check(!fixture.prefs.contains("credential"),"Server change clears credentials");fixture.toggleAuth();fixture.user("fixture");fixture.credential("test-password");check(fixture.authorization().startsWith("Basic "),"Basic authorization");
     check(!RommClient.sameOrigin(new java.net.URL("https://example.invalid"),new java.net.URL("https://other.invalid")),"Cross-origin cover cannot receive credentials");boolean rejected=false;try{RommClient.normalize("http://public.example.com");}catch(Exception expected){rejected=true;}check(rejected,"Public cleartext rejected");check(RommClient.normalize("http://192.168.0.56:8080").contains("8080"),"Private LAN supported");
     fixture.games=new JSONArray("[{\"id\":1,\"name\":\"Alpha\",\"platform_display_name\":\"Nintendo 64\"},{\"id\":2,\"name\":\"Beta\",\"platform_display_name\":\"Game Boy Advance\"}]");RommMarketplace catalog=new RommMarketplace(a);catalog.layout(0,0,2960,1848);Bitmap canvas=Bitmap.createBitmap(2960,1848,Bitmap.Config.RGB_565);catalog.draw(new Canvas(canvas));check(catalog.rows.size()==5,"RomM marketplace categories");catalog.page(1);check(catalog.rows.size()==2,"Catalog grouped by console");catalog.filter="Nintendo 64";catalog.page(2);check(catalog.rows.size()==1&&catalog.rows.get(0).title.equals("Alpha"),"Console filter");catalog.filter="";catalog.query="beta";catalog.page(2);check(catalog.rows.size()==1&&catalog.rows.get(0).title.equals("Beta"),"Case-insensitive title search");catalog.select();check(catalog.screen==5,"Game detail page");catalog.back();check(catalog.screen==2,"Game detail back navigation");catalog.draw(new Canvas(canvas));canvas.recycle();catalog.close();
     }finally{fixture.close();fixture.prefs.edit().clear().commit();a.romm=originalRomm;}
     ra.updated=0;check(ra.score().equals("—")&&ra.achievements().equals("—"),"Missing data does not invent zero totals");
   }catch(Throwable t){error[0]=t;}finally{ra.generation++;ra.profile=originalProfile;ra.games=originalGames;ra.updated=originalUpdated;ra.status=originalStatus;if(hadKey)ra.prefs.edit().putString("key",originalKey).commit();else ra.prefs.edit().remove("key").commit();ra.changed=()->{};a.dashboard.invalidate();}
 });result.putString("stream",error[0]==null?"PASS: "+checks+" RetroAchievements checks":"FAIL: "+error[0]);finish(error[0]==null?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);}
}
