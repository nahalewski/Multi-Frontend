package com.example.blades360;

import android.content.SharedPreferences;
import android.graphics.*;
import android.security.keystore.*;
import android.util.Base64;
import org.json.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.net.*;
import java.io.*;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.*;

/** Read-only personal RomM catalog. Credentials never appear in URLs or error messages. */
final class RommClient {
  final MainActivity a; final SharedPreferences prefs;
  final ExecutorService worker=Executors.newSingleThreadExecutor();
  JSONArray games=new JSONArray(); String status="Set up your personal RomM server";
  Runnable changed=()->{}; boolean busy,closed; volatile int generation;
  final Map<String,Bitmap> images=new HashMap<>(); final Set<String> pending=new HashSet<>();
  RommClient(MainActivity a){this(a,"romm");}
  RommClient(MainActivity a,String storage){this.a=a;prefs=a.getSharedPreferences(storage,0);try{games=new JSONArray(prefs.getString("catalog","[]"));if(prefs.contains("catalog"))status="Saved catalog · Y to refresh";}catch(Exception ignored){}}
  String server(){return prefs.getString("server","");}
  String username(){return prefs.getString("username","");}
  boolean basic(){return prefs.getBoolean("basic",false);}
  void notifyChanged(){if(!closed)changed.run();}
  static String normalize(String value)throws Exception{URI u=new URI(value.trim());String scheme=u.getScheme(),host=u.getHost();if(host==null||u.getRawUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null)throw new IOException();boolean lan=host.equals("localhost")||host.equals("127.0.0.1")||host.matches("10\\.\\d+\\.\\d+\\.\\d+")||host.matches("192\\.168\\.\\d+\\.\\d+")||host.matches("172\\.(1[6-9]|2\\d|3[01])\\.\\d+\\.\\d+")||host.endsWith(".local");if(!"https".equals(scheme)&&!("http".equals(scheme)&&lan))throw new IOException();return u.toString().replaceAll("/+$","");}
  void reset(){generation++;busy=false;games=new JSONArray();images.clear();pending.clear();prefs.edit().remove("catalog").apply();}
  void server(String s){try{String normalized=normalize(s);if(!normalized.equals(server())){reset();prefs.edit().putString("server",normalized).remove("credential").apply();}status="Server saved. Enter credentials, then refresh.";}catch(Exception e){status="Use HTTPS, or HTTP with a private LAN IP / .local host.";}notifyChanged();}
  void user(String s){if(!s.trim().equals(username())){reset();prefs.edit().putString("username",s.trim()).remove("credential").apply();}notifyChanged();}
  void toggleAuth(){reset();prefs.edit().putBoolean("basic",!basic()).remove("credential").apply();status="Enter credentials for the selected sign-in method";notifyChanged();}
  private SecretKey secret()throws Exception{KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);if(ks.containsAlias("blades-romm-key"))return (SecretKey)ks.getKey("blades-romm-key",null);KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder("blades-romm-key",KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());return g.generateKey();}
  void credential(String s){try{if(s.isEmpty()||s.contains("\r")||s.contains("\n"))throw new IOException();Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,secret());String encrypted=Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+":"+Base64.encodeToString(c.doFinal(s.getBytes("UTF-8")),Base64.NO_WRAP);reset();prefs.edit().putString("credential",encrypted).apply();status="Credentials saved securely";}catch(Exception e){status="Could not save credentials. Please try again.";}notifyChanged();}
  String authorization()throws Exception{if(!prefs.contains("credential"))return "";String[] parts=prefs.getString("credential","").split(":");Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,secret(),new GCMParameterSpec(128,Base64.decode(parts[0],Base64.NO_WRAP)));String s=new String(c.doFinal(Base64.decode(parts[1],Base64.NO_WRAP)),"UTF-8");return basic()?"Basic "+Base64.encodeToString((username()+":"+s).getBytes("UTF-8"),Base64.NO_WRAP):"Bearer "+s;}
  static boolean sameOrigin(URL a,URL b){return a.getProtocol().equals(b.getProtocol())&&a.getHost().equalsIgnoreCase(b.getHost())&&(a.getPort()<0?a.getDefaultPort():a.getPort())==(b.getPort()<0?b.getDefaultPort():b.getPort());}
  static byte[] get(String address,String auth,int max)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(12000);c.setReadTimeout(20000);if(!auth.isEmpty())c.setRequestProperty("Authorization",auth);try{int code=c.getResponseCode();if(code!=200)throw new RetroAchievements.StatusException(code==401||code==403?"Check RomM sign-in and catalog read permission.":"RomM returned HTTP "+code+". Check server URL.");try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>max)throw new IOException();out.write(buffer,0,n);}return out.toByteArray();}}finally{c.disconnect();}}
  void refresh(){if(busy||closed)return;if(server().isEmpty()){status="Set your RomM server in X Connection";notifyChanged();return;}final String base=server(),auth;try{auth=authorization();}catch(Exception e){status="Re-enter RomM credentials";notifyChanged();return;}final int ticket=++generation;busy=true;status="Loading personal catalog…";notifyChanged();worker.execute(()->{try{JSONArray all=new JSONArray();Set<Long> ids=new HashSet<>();int offset=0;while(true){JSONObject page=new JSONObject(new String(get(base+"/api/roms?limit=100&offset="+offset+"&order_by=id&order_dir=asc&with_char_index=false&with_filter_values=false&with_rom_id_index=false&with_files=false",auth,12*1024*1024),"UTF-8"));JSONArray items=page.getJSONArray("items");int before=ids.size();for(int i=0;i<items.length();i++){JSONObject g=items.getJSONObject(i);if(ids.add(g.getLong("id")))all.put(compact(g));}offset+=items.length();if(offset>=page.getInt("total"))break;if(items.length()==0||ids.size()==before||offset>100000)throw new IOException();if(ticket!=generation)return;final int loaded=all.length(),total=page.getInt("total");a.runOnUiThread(()->{if(!closed&&ticket==generation){status="Loading catalog: "+loaded+" / "+total;notifyChanged();}});}final String snapshot=all.toString();a.runOnUiThread(()->{if(closed||ticket!=generation)return;games=all;busy=false;prefs.edit().putString("catalog",snapshot).apply();status=all.length()+" games · Catalog updated";notifyChanged();});}catch(Exception e){a.runOnUiThread(()->{if(closed||ticket!=generation)return;busy=false;status=(games.length()>0?"Saved catalog retained. ":"")+(e instanceof RetroAchievements.StatusException?e.getMessage():"Unable to load catalog. Check server and connection.");notifyChanged();});}});}
  /** Drop provider payloads, hashes and file trees before retaining a page in memory. */
  static JSONObject compact(JSONObject g)throws JSONException{JSONObject row=new JSONObject();row.put("id",g.getLong("id"));row.put("name",title(g));row.put("platform_display_name",platform(g));row.put("path_cover_large",cover(g));row.put("fs_name",g.optString("fs_name",""));String summary=g.optString("summary","");row.put("summary",summary.length()>4000?summary.substring(0,4000)+"…":summary);return row;}
  static String title(JSONObject g){String name=g.optString("name","");return name.isEmpty()||name.equals("null")?g.optString("fs_name","Untitled"):name;}
  static String platform(JSONObject g){return g.optString("platform_display_name",g.optString("platform_name",g.optString("platform_slug","Unknown console")));}
  static String cover(JSONObject g){return g.optString("path_cover_large",g.optString("path_cover_l",g.optString("path_cover_small",g.optString("path_cover_s",""))));}
  Bitmap image(String path){if(path==null||path.isEmpty()||path.equals("null")||closed)return null;Bitmap cached=images.get(path);if(cached!=null||pending.contains(path))return cached;final String address,auth;try{URL base=new URL(server()+"/"),target=new URL(base,path);if(!sameOrigin(base,target)||target.getUserInfo()!=null)return null;address=target.toString();auth=authorization();}catch(Exception e){return null;}pending.add(path);int ticket=generation;worker.execute(()->{Bitmap result=null;try{byte[] bytes=get(address,auth,5*1024*1024);BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>512)o.inSampleSize*=2;o.inJustDecodeBounds=false;result=BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);}catch(Exception ignored){}Bitmap bitmap=result;a.runOnUiThread(()->{if(closed||ticket!=generation)return;if(bitmap!=null){if(images.size()>80)images.clear();images.put(path,bitmap);}notifyChanged();});});return null;}
  void disconnect(){reset();prefs.edit().clear().apply();status="Disconnected from RomM";notifyChanged();}
  void close(){closed=true;generation++;worker.shutdownNow();}
}
