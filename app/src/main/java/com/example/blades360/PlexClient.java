package com.example.blades360;
import org.json.*;
import android.net.Uri;
import java.util.*;
import java.util.function.Consumer;

/** Normalizes Plex library records for the shared Blades media browser. */
final class PlexClient extends JellyfinClient {
 PlexClient(MainActivity a){super(a,"plex");}
 @Override String name(){return "Plex";}
 @Override Map<String,String> headers()throws Exception{Map<String,String> h=new HashMap<>();String value=vault.authorization();if(!value.startsWith("Bearer "))throw new Exception();h.put("X-Plex-Token",value.substring(7));h.put("Accept","application/json");h.put("X-Plex-Client-Identifier","blades360-tablet");h.put("X-Plex-Product","Blades360");h.put("X-Plex-Version","1.0");h.put("X-Plex-Platform","Android");return h;}
 @Override String authorization()throws Exception{return vault.authorization();}
 @Override String art(JSONObject item){return item.optString("_plex_thumb","");}
 @Override void get(String path,Consumer<Object> done){if(closed)return;if(!connected()){status="Set Plex server URL and token in Connection";changed.run();return;}final int ticket=++generation;final String base=server();status="Loading Plex…";changed.run();worker.execute(()->{try{Object result=read(path,base);a.runOnUiThread(()->{if(closed||ticket!=generation)return;status="Connected to Plex";done.accept(result);changed.run();});}catch(Exception e){a.runOnUiThread(()->{if(closed||ticket!=generation)return;status="Could not load Plex. Check server, token and connection.";changed.run();});}});}
 Object read(String request,String base)throws Exception{
  if(request.equals("/Users")){JSONObject root=new JSONObject(new String(fetch(base+"/",1024*1024),"UTF-8")).getJSONObject("MediaContainer");return new JSONArray().put(new JSONObject().put("Id","server").put("Name",root.optString("friendlyName","Plex Library")));}
  boolean sections=request.contains("/Views");Uri query=Uri.parse(server()+request);String parent=query.getQueryParameter("ParentId"),search=query.getQueryParameter("SearchTerm");int start=number(query.getQueryParameter("StartIndex"),0),limit=number(query.getQueryParameter("Limit"),100);String path;
  if(sections)path="/library/sections";
  else if(search!=null&&!search.isEmpty())path="/hubs/search?query="+encode(search)+"&limit="+limit;
  else if(parent!=null&&parent.startsWith("section:"))path="/library/sections/"+encode(parent.substring(8))+"/all";
  else if(parent!=null)path="/library/metadata/"+encode(parent)+"/children";
  else path="/library/all";
  path+=(path.contains("?")?"&":"?")+"X-Plex-Container-Start="+start+"&X-Plex-Container-Size="+limit;
  JSONObject container=new JSONObject(new String(fetch(base+path,8*1024*1024),"UTF-8")).getJSONObject("MediaContainer");JSONArray source=container.optJSONArray(sections?"Directory":"Metadata"),rows=new JSONArray();
  if(search!=null){source=new JSONArray();JSONArray hubs=container.optJSONArray("Hub");if(hubs!=null)for(int i=0;i<hubs.length();i++){JSONArray entries=hubs.getJSONObject(i).optJSONArray("Metadata");if(entries!=null)for(int j=0;j<entries.length();j++)source.put(entries.get(j));}}
  if(source!=null)for(int i=0;i<source.length();i++)rows.put(normalize(source.getJSONObject(i),sections));
  return new JSONObject().put("Items",rows).put("TotalRecordCount",search!=null?rows.length():container.optInt("totalSize",container.optInt("size",rows.length())));
 }
 static int number(String s,int fallback){try{return Integer.parseInt(s);}catch(Exception e){return fallback;}}
 static JSONObject normalize(JSONObject p,boolean section)throws Exception{
  String t=p.optString("type"),type=section?"CollectionFolder":t.equals("show")?"Series":t.equals("season")?"Season":t.equals("artist")?"MusicArtist":t.equals("album")?"MusicAlbum":t.equals("track")?"Audio":t.equals("episode")?"Episode":"Movie";
  JSONObject out=new JSONObject().put("Id",section?"section:"+p.optString("key"):p.optString("ratingKey")).put("Name",p.optString("title")).put("Type",type).put("IsFolder",section||Arrays.asList("show","season","artist","album").contains(t)).put("Overview",p.optString("summary","")).put("RunTimeTicks",p.optLong("duration")*10000L).put("_plex_thumb",p.optString("thumb",p.optString("parentThumb","")));
  if(p.has("year"))out.put("ProductionYear",p.optInt("year"));out.put("MediaType",type.equals("Audio")?"Audio":"Video");JSONArray media=p.optJSONArray("Media");if(media!=null&&media.length()>0){JSONObject m=media.getJSONObject(0);JSONArray parts=m.optJSONArray("Part");if(parts!=null&&parts.length()>0)out.put("_plex_part",parts.getJSONObject(0).optString("key"));}
  return out;
 }
 String stream(JSONObject item,String session,boolean direct){if(direct&&!item.optString("_plex_part").isEmpty())return server()+item.optString("_plex_part");return server()+"/video/:/transcode/universal/start.m3u8?path="+encode("/library/metadata/"+item.optString("Id"))+"&mediaIndex=0&partIndex=0&protocol=hls&directPlay=0&directStream=1&fastSeek=1&videoQuality=100&maxVideoBitrate=8000&videoResolution=1920x1080&session="+session+"&X-Plex-Client-Identifier=blades360-tablet&X-Plex-Platform=Android";}
 @Override void stopEncoding(String session){if(closed)return;final String url=server()+"/video/:/transcode/universal/stop?session="+encode(session);worker.execute(()->{try{fetch(url,4096);}catch(Exception ignored){}});}
}
