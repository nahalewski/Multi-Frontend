package com.example.blades360;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import org.xmlpull.v1.*;

/** M3U and XMLTV parsers: no network, credential logging or media side effects. */
final class IptvData {
 static class Channel {
  String language="",country="";String id="",name="",group="Ungrouped",logo="",tvg="",url="",catchup="";int days;Map<String,String> headers=new HashMap<>();
  JSONObject json()throws Exception{return new JSONObject().put("language",language).put("country",country).put("id",id).put("name",name).put("group",group).put("logo",logo).put("tvg",tvg).put("url",url).put("catchup",catchup).put("days",days).put("headers",new JSONObject(headers));}
  static Channel from(JSONObject j){Channel c=new Channel();c.language=j.optString("language");c.country=j.optString("country");c.id=j.optString("id");c.name=j.optString("name");c.group=j.optString("group","Ungrouped");c.logo=j.optString("logo");c.tvg=j.optString("tvg");c.url=j.optString("url");c.catchup=j.optString("catchup");c.days=j.optInt("days");JSONObject h=j.optJSONObject("headers");if(h!=null)for(Iterator<String> i=h.keys();i.hasNext();){String k=i.next();c.headers.put(k,h.optString(k));}return c;}
 }
 static class Programme {String channel,title,description;long start,end;JSONObject json()throws Exception{return new JSONObject().put("c",channel).put("t",title).put("d",description).put("s",start).put("e",end);}static Programme from(JSONObject j){Programme p=new Programme();p.channel=j.optString("c");p.title=j.optString("t");p.description=j.optString("d");p.start=j.optLong("s");p.end=j.optLong("e");return p;}}
 static class Playlist {ArrayList<Channel> channels=new ArrayList<>();String guide="";}
 static Map<String,String> attributes(String line){Map<String,String> m=new HashMap<>();Matcher match=Pattern.compile("([\\w-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s,]+))").matcher(line);while(match.find())m.put(match.group(1).toLowerCase(Locale.ROOT),match.group(2)!=null?match.group(2):match.group(3)!=null?match.group(3):match.group(4));return m;}
 static String resolve(String base,String path){if(path==null||path.trim().isEmpty())return "";try{URL u=base.startsWith("http")?new URL(new URL(base),path):new URL(path);return Arrays.asList("http","https").contains(u.getProtocol())&&u.getUserInfo()==null?u.toString():"";}catch(Exception e){return "";}}
 static String hash(String url)throws Exception{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b));return out.toString();}
 static int comma(String line){char quote=0;for(int i=0;i<line.length();i++){char c=line.charAt(i);if((c=='\"'||c=='\'')&&(quote==0||quote==c))quote=quote==0?c:0;else if(c==','&&quote==0)return i;}return -1;}
 static Playlist playlist(String text,String base)throws Exception{
  Playlist result=new Playlist();if(text.contains("#EXT-X-")){Channel c=new Channel();c.url=resolve(base,base);if(c.url.isEmpty())throw new IOException("A local HLS manifest needs a network URL");c.id=hash(c.url);c.name="Live stream";result.channels.add(c);return result;}
  Channel current=null;String defaultCatchup="",globalGroup="";int defaultDays=0;Set<String> seen=new HashSet<>();
  for(String raw:text.replace("\ufeff","").split("\\r?\\n")){String line=raw.trim();if(line.isEmpty())continue;
   if(line.startsWith("#EXTM3U")){Map<String,String> a=attributes(line);result.guide=resolve(base,a.getOrDefault("x-tvg-url",a.getOrDefault("url-tvg","")));defaultCatchup=a.getOrDefault("catchup-source","");defaultDays=PlexClient.number(a.get("catchup-days"),0);continue;}
   if(line.startsWith("#EXTINF")){current=new Channel();Map<String,String> a=attributes(line);current.language=a.getOrDefault("tvg-language","");current.country=a.getOrDefault("tvg-country","");int comma=comma(line);current.name=comma>=0?line.substring(comma+1).trim():a.getOrDefault("tvg-name","Channel");current.tvg=a.getOrDefault("tvg-id",a.getOrDefault("tvg-name",current.name));current.group=a.getOrDefault("group-title",globalGroup.isEmpty()?"Ungrouped":globalGroup);current.logo=resolve(base,a.getOrDefault("tvg-logo",""));current.catchup=a.getOrDefault("catchup-source",defaultCatchup);current.days=PlexClient.number(a.get("catchup-days"),defaultDays);continue;}
   if(line.startsWith("#EXTGRP:")){globalGroup=line.substring(8).trim();if(current!=null)current.group=globalGroup;continue;}
   if(line.startsWith("#EXTVLCOPT:")&&current!=null){String option=line.substring(11);int eq=option.indexOf('=');if(eq>0){String k=option.substring(0,eq).toLowerCase(Locale.ROOT),v=option.substring(eq+1);if(k.equals("http-user-agent"))current.headers.put("User-Agent",v);if(k.equals("http-referrer")||k.equals("http-referer"))current.headers.put("Referer",v);}continue;}
   if(line.startsWith("#"))continue;
   if(current==null){current=new Channel();current.name="Channel "+(result.channels.size()+1);}String[] parts=line.split("\\|",2);current.url=resolve(base,parts[0]);if(parts.length>1)for(String h:parts[1].split("&")){String[] kv=h.split("=",2);if(kv.length==2&&Arrays.asList("user-agent","referer","referrer","origin").contains(kv[0].toLowerCase(Locale.ROOT)))current.headers.put(kv[0].equalsIgnoreCase("referrer")?"Referer":kv[0],URLDecoder.decode(kv[1],"UTF-8"));}
   if(!current.url.isEmpty()){current.id=hash(current.url);if(seen.add(current.id))result.channels.add(current);}current=null;if(result.channels.size()>=20000)throw new IOException("Playlist exceeds 20000 channels");
  }if(result.channels.isEmpty())throw new IOException("No HTTP channels in this playlist");return result;
 }
 static long date(String text){try{String s=text.trim();String digits=s.split("\\s+")[0];String zone=s.contains(" ")?s.substring(s.indexOf(' ')+1).trim():"+0000";while(digits.length()<14)digits+="0";SimpleDateFormat f=new SimpleDateFormat("yyyyMMddHHmmss Z",Locale.ROOT);f.setLenient(false);return f.parse(digits.substring(0,14)+" "+zone).getTime();}catch(Exception e){return 0;}}
 static ArrayList<Programme> guide(InputStream input,Set<String> channelIds,long now)throws Exception{XmlPullParser p=XmlPullParserFactory.newInstance().newPullParser();p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);p.setInput(input,null);ArrayList<Programme> out=new ArrayList<>();Programme current=null;long week=7L*86400000;
  for(int event=p.getEventType();event!=XmlPullParser.END_DOCUMENT;event=p.next()){if(event==XmlPullParser.START_TAG){String name=p.getName();if(name.equals("programme")){current=new Programme();current.channel=p.getAttributeValue(null,"channel");current.start=date(p.getAttributeValue(null,"start"));current.end=date(p.getAttributeValue(null,"stop"));current.title="Untitled programme";current.description="";}else if(current!=null&&(name.equals("title")||name.equals("desc"))){String value=p.nextText();if(name.equals("title"))current.title=value;else current.description=value.substring(0,Math.min(value.length(),1500));}}else if(event==XmlPullParser.END_TAG&&p.getName().equals("programme")&&current!=null){if(channelIds.contains(current.channel)&&current.end>current.start&&current.end>now-week&&current.start<now+week)out.add(current);current=null;if(out.size()>60000)throw new IOException("Guide exceeds limit");}}
  out.sort(Comparator.comparingLong(x->x.start));return out;
 }
 static String catchup(Channel c,Programme p,long now){if(c.catchup.isEmpty()||p.start>=now||p.end>now||c.days<=0||now-p.start>c.days*86400000L)return "";String url=c.catchup.replace("{utc}",""+(p.start/1000)).replace("{utcend}",""+(p.end/1000)).replace("{duration}",""+((p.end-p.start)/1000)).replace("{lutc}",""+(now/1000));if(url.contains("{")||url.contains("}"))return "";return resolve(c.url,url);}
}
