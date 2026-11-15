package com.example.blades360;

import android.graphics.*;
import org.json.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Jellyfin browsing and artwork; secrets share the app's encrypted credential mechanism. */
class JellyfinClient {
  final MainActivity a; final RommClient vault;
  final ExecutorService worker=Executors.newSingleThreadExecutor();
  String status="Connect your Jellyfin server";boolean closed;int generation;
  Runnable changed=()->{};
  final android.util.LruCache<String,Bitmap> images=new android.util.LruCache<String,Bitmap>(16*1024*1024){protected int sizeOf(String k,Bitmap b){return b.getByteCount();}};
  final Set<String> pending=new HashSet<>();
  JellyfinClient(MainActivity a){this(a,"jellyfin");}
  JellyfinClient(MainActivity a,String storage){this.a=a;vault=new RommClient(a,storage);}
  String name(){return "Jellyfin";}
  byte[] fetch(String address,int limit)throws Exception{return MediaHttp.get(address,headers(),limit);}
  String server(){return vault.server();} String user(){return vault.prefs.getString("user_id","");}
  String userName(){return vault.prefs.getString("user_name","");}
  boolean connected(){return !server().isEmpty()&&vault.prefs.contains("credential");}
  void server(String url){generation++;vault.server(url);vault.prefs.edit().remove("user_id").remove("user_name").apply();images.evictAll();pending.clear();status=vault.status;changed.run();}
  void key(String key){generation++;vault.credential(key.trim());images.evictAll();pending.clear();status=vault.status;changed.run();}
  void user(JSONObject u){generation++;vault.prefs.edit().putString("user_id",u.optString("Id")).putString("user_name",u.optString("Name")).apply();}
  String authorization()throws Exception{String bearer=vault.authorization();if(!bearer.startsWith("Bearer "))throw new Exception();return "MediaBrowser Client=\"Blades360\", Device=\"Android\", DeviceId=\"blades360-tablet\", Version=\"1.0\", Token=\""+bearer.substring(7)+"\"";}
  Map<String,String> headers()throws Exception{return Collections.singletonMap("Authorization",authorization());}
  static String encode(String s){try{return URLEncoder.encode(s,"UTF-8");}catch(Exception e){return "";}}
  void get(String path,Consumer<Object> done){if(closed)return;if(!connected()){status="Set server URL and API key in X Connection";changed.run();return;}int ticket=++generation;String base=server();final String auth;try{auth=authorization();}catch(Exception e){status="Re-enter the Jellyfin API key";changed.run();return;}status="Loading Jellyfin…";changed.run();worker.execute(()->{try{Object result=new JSONTokener(new String(RommClient.get(base+path,auth,8*1024*1024),"UTF-8")).nextValue();a.runOnUiThread(()->{if(closed||ticket!=generation)return;status="Connected to Jellyfin";done.accept(result);changed.run();});}catch(Exception e){a.runOnUiThread(()->{if(closed||ticket!=generation)return;status="Could not load Jellyfin. Check server, API key and connection.";changed.run();});}});}
  void cancel(){generation++;}
  String art(JSONObject item){JSONObject tags=item.optJSONObject("ImageTags");if(tags!=null&&tags.has("Primary"))return "/Items/"+item.optString("Id")+"/Images/Primary?maxWidth=500&quality=90&tag="+encode(tags.optString("Primary"));String album=item.optString("AlbumId");return album.isEmpty()?"":"/Items/"+album+"/Images/Primary?maxWidth=500&quality=90";}
  Bitmap image(String path){if(path.isEmpty()||closed||!connected())return null;Bitmap cached=images.get(path);if(cached!=null||pending.contains(path))return cached;final String base=server(),auth;try{auth=authorization();}catch(Exception e){return null;}pending.add(path);worker.execute(()->{Bitmap result=null;try{byte[] data=fetch(base+path,5*1024*1024);BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,o);o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>512)o.inSampleSize*=2;o.inJustDecodeBounds=false;result=BitmapFactory.decodeByteArray(data,0,data.length,o);}catch(Exception ignored){}Bitmap bitmap=result;a.runOnUiThread(()->{if(closed||!base.equals(server()))return;if(bitmap!=null)images.put(path,bitmap);changed.run();});});return null;}
  void stopEncoding(String session){if(closed)return;final String base=server(),auth;try{auth=authorization();}catch(Exception e){return;}worker.execute(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(base+"/Videos/ActiveEncodings?deviceId=blades360-tablet&playSessionId="+encode(session)).openConnection();c.setRequestMethod("DELETE");c.setRequestProperty("Authorization",auth);c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setInstanceFollowRedirects(false);c.getResponseCode();c.disconnect();}catch(Exception ignored){}});}
  void close(){closed=true;generation++;worker.shutdown();vault.close();images.evictAll();}
}
