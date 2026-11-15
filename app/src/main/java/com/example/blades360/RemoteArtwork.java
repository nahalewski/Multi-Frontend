package com.example.blades360;

import android.content.Context;
import android.graphics.BitmapFactory;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.*;

/** On-demand boxart from https://github.com/libretro-thumbnails/libretro-thumbnails.
 * Call find only from a worker thread. No fuzzy/substring title matching is used.
 */
final class RemoteArtwork {
  private static final Map<String,List<String>> catalogs=new HashMap<>();
  private static final Map<String,Long> retryAfter=new HashMap<>();
  private static final long DAY=86400000L;
  static synchronized ArrayList<ArtworkManager.Candidate> search(Context context,String system,String query)throws Exception{
    String wanted=normalize(query);ArrayList<ArtworkManager.Candidate> result=new ArrayList<>();if(wanted.length()<2)return result;
    if(system.equals("Nintendo Switch")){
      if(switchCatalog==null)try(InputStream in=context.getAssets().open("switch_art.json")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);switchCatalog=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));}
      HashSet<String> seen=new HashSet<>();Iterator<String> ids=switchCatalog.keys();while(ids.hasNext()){JSONObject e=switchCatalog.getJSONObject(ids.next());String title=e.getString("name"),url=e.getString("url");boolean match=searchMatch(title,wanted);JSONArray aliases=e.optJSONArray("aliases");if(aliases!=null)for(int i=0;i<aliases.length()&&!match;i++)match=searchMatch(aliases.getString(i),wanted);if(match&&seen.add(url))result.add(new ArtworkManager.Candidate(title,system,url));}
    }else{
      String repo=repository(system);if(repo==null)return result;File dir=new File(context.getFilesDir(),"boxart");dir.mkdirs();List<String> files=index(dir,repo);if(files==null)throw new IOException("Catalog unavailable");for(String title:files)if(searchMatch(title,wanted))result.add(new ArtworkManager.Candidate(title.replaceFirst("(?i)\\.png$",""),system,mirror(repo)+URLEncoder.encode(title,"UTF-8").replace("+","%20")));
      if(system.equals("PlayStation 3")&&searchMatch("3D Dot Game Heroes",wanted))result.add(new ArtworkManager.Candidate("3D Dot Game Heroes (North America)",system,"https://images.launchbox-app.com/8f258531-8390-4368-a066-29ebea519445.jpg"));
      if(system.equals("Xbox 360")&&searchMatch("Banjo-Kazooie Nuts and Bolts",wanted))result.add(new ArtworkManager.Candidate("Banjo-Kazooie Nuts & Bolts (North America)",system,"https://images.launchbox-app.com/acae376c-7a04-4bbc-9c24-01c663f978c4.jpg"));
    }
    result.sort((a,b)->{int ar=normalize(a.title).equals(wanted)?0:1,br=normalize(b.title).equals(wanted)?0:1;return ar!=br?ar-br:a.title.compareToIgnoreCase(b.title);});if(result.size()>60)return new ArrayList<>(result.subList(0,60));return result;
  }
  private static boolean searchMatch(String title,String wanted){String normalized=normalize(title);for(String word:wanted.split(" "))if(!normalized.contains(word))return false;return true;}
  static synchronized File downloadChoice(Context context,ArtworkManager.Candidate choice)throws Exception{
    URL parsed=new URL(choice.url);String host=parsed.getHost();if(!parsed.getProtocol().equals("https")||!(host.equals("thumbnails.libretro.com")||host.equals("img-eshop.cdn.nintendo.net")||host.equals("images.launchbox-app.com")))throw new IOException("Unsupported image source");
    File dir=new File(context.getFilesDir(),"boxart");dir.mkdirs();File target=new File(dir,"chosen-"+hash(choice.url)+".png");if(target.isFile())return target;byte[] data=download(choice.url,8*1024*1024);BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,options);if(options.outWidth<=0||options.outHeight<=0||options.outWidth>8192||options.outHeight>8192)throw new IOException("Invalid cover");writeAtomic(target,data);writeAtomic(new File(dir,target.getName()+".source.txt"),(choice.url+"\nManually selected title: "+choice.title+"\n").getBytes(StandardCharsets.UTF_8));return target;
  }
  static synchronized File find(Context context,String system,String gameName){
    if(system.equals("Nintendo Switch"))return switchArt(context,gameName);
    if((system.equals("PS3")||system.equals("PlayStation 3"))&&normalize(gameName).equals("3d dot game heroes"))return curatedArt(context,"ps3-3d-dot-game-heroes","https://images.launchbox-app.com/8f258531-8390-4368-a066-29ebea519445.jpg","https://gamesdb.launchbox-app.com/games/images/6475-3d-dot-game-heroes");
    if((system.equals("Xbox 360")||system.equals("X360"))&&normalize(gameName).equals("banjo kazooie nuts and bolts"))return curatedArt(context,"x360-banjo-kazooie-nuts-bolts","https://images.launchbox-app.com/acae376c-7a04-4bbc-9c24-01c663f978c4.jpg","https://gamesdb.launchbox-app.com/games/images/13700-banjo-kazooie-nuts-bolts");
    String repo=repository(system);if(repo==null||gameName==null)return null;
    String key=repo+"/"+gameName;
    File directory=new File(context.getFilesDir(),"boxart");directory.mkdirs();
    File image=new File(directory,hash(key)+".png");
    if(image.isFile()&&image.length()>0)return image;
    if(retryAfter.getOrDefault(key,0L)>System.currentTimeMillis())return null;
    try{
      List<String> files=index(directory,repo);if(files==null)return null;
      String title=normalize(gameName);if(title.isEmpty())return null;
      ArrayList<String> candidates=new ArrayList<>();
      for(String name:files)if(normalize(name).equals(title))candidates.add(name);
      if(candidates.isEmpty()){
        String tokens=tokens(title);
        for(String name:files)if(tokens(normalize(name)).equals(tokens))candidates.add(name);
      }
      if(candidates.isEmpty()){retryAfter.put(key,System.currentTimeMillis()+DAY);return null;}
      final String wantedRegion=region(gameName);
      candidates.sort((a,b)->{int delta=rank(a,wantedRegion)-rank(b,wantedRegion);return delta!=0?delta:a.compareTo(b);});
      String chosen=candidates.get(0);
      String url=mirror(repo)+URLEncoder.encode(chosen,"UTF-8").replace("+","%20");
      byte[] data;
      try{data=download(url,8*1024*1024);}catch(IOException e){url="https://raw.githubusercontent.com/libretro-thumbnails/"+repo+"/master/Named_Boxarts/"+URLEncoder.encode(chosen,"UTF-8").replace("+","%20");data=download(url,8*1024*1024);}
      BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,options);
      if(options.outWidth<=0||options.outHeight<=0||options.outWidth>8192||options.outHeight>8192)throw new IOException("Invalid artwork");
      writeAtomic(image,data);
      writeAtomic(new File(directory,hash(key)+".source.txt"),(url+"\nMatched title: "+chosen+"\nSource: Libretro thumbnails\n").getBytes(StandardCharsets.UTF_8));
      return image;
    }catch(Exception e){retryAfter.put(key,System.currentTimeMillis()+60000);return null;}
  }
  private static File curatedArt(Context context,String key,String url,String source){
    File dir=new File(context.getFilesDir(),"boxart");dir.mkdirs();File f=new File(dir,key+".png");if(f.isFile())return f;
    try{byte[] data=download(url,8*1024*1024);BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,opts);if(opts.outWidth<=0||opts.outHeight<=0)return null;writeAtomic(f,data);writeAtomic(new File(dir,key+".source.txt"),(url+"\nSource: "+source+"\n").getBytes(StandardCharsets.UTF_8));return f;}catch(Exception e){return null;}
  }
  private static List<String> index(File directory,String repo)throws Exception{
    if(catalogs.containsKey(repo))return catalogs.get(repo);
    if(retryAfter.getOrDefault(repo,0L)>System.currentTimeMillis())return null;
    File cache=new File(directory,repo+".json");JSONArray entries=null;
    if(cache.isFile())try{entries=new JSONArray(new String(read(cache),StandardCharsets.UTF_8));}catch(Exception ignored){}
    if(entries!=null&&entries.length()==0)entries=null;
    if(entries==null){
      try{
        String html=new String(download(mirror(repo),20*1024*1024),StandardCharsets.UTF_8);
        java.util.regex.Matcher links=java.util.regex.Pattern.compile("href=\"([^\"]+)\"",java.util.regex.Pattern.CASE_INSENSITIVE).matcher(html);
        entries=new JSONArray();HashSet<String> unique=new HashSet<>();
        while(links.find()){String path=URLDecoder.decode(links.group(1).replace("&amp;","&").replace("+","%2B"),"UTF-8");if(path.toLowerCase(Locale.ROOT).endsWith(".png")&&!path.contains("/")&&unique.add(path))entries.put(path);}
        if(entries.length()==0)throw new IOException("No boxart catalog");
        writeAtomic(cache,entries.toString().getBytes(StandardCharsets.UTF_8));
      }catch(Exception e){retryAfter.put(repo,System.currentTimeMillis()+60000);return null;}
    }
    ArrayList<String> result=new ArrayList<>();for(int i=0;i<entries.length();i++)result.add(entries.getString(i));catalogs.put(repo,result);return result;
  }
  private static String mirror(String repo){return "https://thumbnails.libretro.com/"+repo.replace("_","%20")+"/Named_Boxarts/";}
  private static JSONObject switchCatalog;
  private static File switchArt(Context context,String name){
    try{
      if(switchCatalog==null)try(InputStream in=context.getAssets().open("switch_art.json")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);switchCatalog=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));}
      java.util.regex.Matcher id=java.util.regex.Pattern.compile("(?i)(?<![0-9a-f])010[0-9a-f]{13}(?![0-9a-f])").matcher(name);
      JSONObject entry=null;String key=null;
      if(id.find()){
        key=id.group().toUpperCase(Locale.ROOT);entry=switchCatalog.optJSONObject(key);
        // The title ID identifies the parent even when a DLC has its own subtitle.
        String parent=PackageArtwork.parentId(key);
        if(!parent.isEmpty()){JSONObject candidate=switchCatalog.optJSONObject(parent);if(candidate!=null){entry=candidate;key=parent;}}
      }
      else{Iterator<String> keys=switchCatalog.keys();while(keys.hasNext()){String k=keys.next();JSONObject e=switchCatalog.getJSONObject(k);if(switchTitleMatches(e,name)){if(entry!=null&&!entry.getString("url").equals(e.getString("url")))return null;entry=e;key=k;}}}
      // These ID-less packages use release filenames rather than catalog names.
      if(entry==null&&PackageArtwork.titleId(name).isEmpty()&&PackageArtwork.badge("Nintendo Switch",name).equals("DLC")){
        String title=normalize(name).replaceFirst("^v ","");
        if(title.matches("fantasy life i the girl who steals time (edition upgrade digital deluxe|extra mount unicorn|pre order bonus) dlc")){key="0100755017EE0000";entry=switchCatalog.optJSONObject(key);}
      }
      if(entry==null)return null;File dir=new File(context.getFilesDir(),"boxart");dir.mkdirs();File f=new File(dir,"switch-"+key+".png");if(f.isFile())return f;
      String url=entry.getString("url");if(!url.startsWith("https://img-eshop.cdn.nintendo.net/"))return null;
      byte[] data=download(url,8*1024*1024);BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,opts);if(opts.outWidth<=0||opts.outHeight<=0)return null;
      writeAtomic(f,data);writeAtomic(new File(dir,"switch-"+key+".source.txt"),(url+"\nTitle ID: "+key+"\nMetadata: blawar/titledb US.en.json\n").getBytes(StandardCharsets.UTF_8));return f;
    }catch(Exception e){return null;}
  }
  private static boolean switchTitleMatches(JSONObject entry,String name)throws JSONException{
    String title=switchTitle(name);if(switchTitle(entry.getString("name")).equals(title))return true;
    JSONArray aliases=entry.optJSONArray("aliases");if(aliases!=null)for(int i=0;i<aliases.length();i++)if(switchTitle(aliases.getString(i)).equals(title))return true;return false;
  }
  private static String switchTitle(String name){return normalize(name.replaceAll("(?i)\\((?:nsw2u\\.(?:com|xyz)|update [0-9.]+)\\)"," ").replaceAll("(?i)__switch-xci\\.com__v[0-9]+_", " ").replaceAll("(?i)(?:\\s+update)?\\s+v?[0-9]+\\.[0-9]+(?:\\.[0-9]+)?(?:\\s+v[0-9]+)?(?=\\.(?:nsp|xci)$)","").replaceAll("(?i)\\s+v[0-9]+\\.[0-9]+(?=\\[)"," ")).replaceAll("\\bplus\\b"," ").replaceAll("\\s+"," ").trim();}
  static String normalize(String value){
    value=value.replaceAll("(?i)\\((?:SGB Enhanced|CGB\\+SGB Enhanced|GB Compatible|Independent|TrashMan|DCS|Mode7|Venom|Eurasia|Mugs)\\)"," ").replace("\u2019", "'").replaceAll("(?i)\\s*#\\s*N64(?=\\.n64$)","");
    String s=value.replaceFirst("(?i)\\.(png|jpg|jpeg|nes|fds|sfc|smc|gb|gbc|gba|nds|3ds|cia|n64|z64|v64|iso|cso|chd|cue|bin|pbp|gdi|cdi|wbfs|rvz|gcm|dol|elf|rom|zip|7z|md|gen|sms|gg|pce|nsp|xci|wud|wux|rpx|m3u)$","");
    s=s.replaceAll("\\[[^\\]]*\\]", " ").replaceAll("(?i)\\((?:usa|europe|japan|world|asia|australia|korea|china|brazil|germany|france|spain|italy|russia|uk|u|e|j|ju|ue|en|fr|de|es|it|ja|rev|disc|disk|v[0-9]|beta|proto|demo|unl)[^)]*\\)"," ");
    s=Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).replace("'", "").replace("’", "").replace("&"," and ").replaceAll("[^a-z0-9]+"," ").trim().replaceAll("\\s+"," ");
    s=s.replace("leaf green","leafgreen").replace("fire red","firered");
    if(s.matches("pokemon (red|blue|yellow|emerald|sapphire|ruby|leafgreen|firered)"))s+=" version";
    if(s.equals("pokemon yellow version special pikachu edition"))s="pokemon yellow version";
    return s;
  }
  private static String tokens(String value){String[] words=value.replaceAll("\\band\\b", " ").trim().split("\\s+");Arrays.sort(words);return String.join(" ",words);}
  private static String region(String value){String s=value.toLowerCase(Locale.ROOT);if(s.contains("(usa")||s.contains("(u)"))return "usa";if(s.contains("(europe")||s.contains("(e)"))return "europe";if(s.contains("(japan")||s.contains("(j)"))return "japan";return "";}
  private static int rank(String value,String wanted){String r=region(value);if(!wanted.isEmpty()&&r.equals(wanted))return 0;return r.equals("usa")?1:r.equals("europe")?2:r.isEmpty()?3:4;}
  private static byte[] download(String url,int max)throws IOException{
    HttpURLConnection connection=(HttpURLConnection)new URL(url).openConnection();connection.setConnectTimeout(8000);connection.setReadTimeout(12000);connection.setRequestProperty("User-Agent","Mozilla/5.0 Blades360/1.0");connection.setRequestProperty("Accept","*/*");if(url.startsWith("https://images.launchbox-app.com/"))connection.setRequestProperty("Referer","https://gamesdb.launchbox-app.com/");
    try{if(connection.getResponseCode()!=200)throw new IOException("HTTP "+connection.getResponseCode());try(InputStream input=connection.getInputStream();ByteArrayOutputStream output=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int count,total=0;while((count=input.read(buffer))!=-1){total+=count;if(total>max)throw new IOException("Download limit");output.write(buffer,0,count);}return output.toByteArray();}}finally{connection.disconnect();}
  }
  private static byte[] read(File f)throws IOException{try(InputStream in=new FileInputStream(f);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);return out.toByteArray();}}
  private static void writeAtomic(File f,byte[] data)throws IOException{File temp=new File(f.getParentFile(),f.getName()+".tmp");try(FileOutputStream out=new FileOutputStream(temp)){out.write(data);}if(!temp.renameTo(f)){temp.delete();throw new IOException("Cannot save cache");}}
  private static String hash(String value){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte v:d)b.append(String.format(Locale.ROOT,"%02x",v&255));return b.toString();}catch(Exception e){throw new IllegalStateException(e);}}
  static String repository(String system){
    String s=system.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]","");
    switch(s){
      case "GB":case "GAMEBOY":return "Nintendo_-_Game_Boy";
      case "GBC":case "GAMEBOYCOLOR":return "Nintendo_-_Game_Boy_Color";
      case "GBA":case "GAMEBOYADVANCE":return "Nintendo_-_Game_Boy_Advance";
      case "NES":return "Nintendo_-_Nintendo_Entertainment_System";
      case "SNES":case "SUPERNINTENDO":return "Nintendo_-_Super_Nintendo_Entertainment_System";
      case "N64":case "NINTENDO64":return "Nintendo_-_Nintendo_64";
      case "NDS":case "DS":case "NINTENDODS":return "Nintendo_-_Nintendo_DS";
      case "3DS":case "NINTENDO3DS":return "Nintendo_-_Nintendo_3DS";
      case "GC":case "GAMECUBE":return "Nintendo_-_GameCube";
      case "WII":return "Nintendo_-_Wii";
      case "WIIU":return "Nintendo_-_Wii_U";
      case "PSX":case "PS1":case "PLAYSTATION":return "Sony_-_PlayStation";
      case "PS2":case "PLAYSTATION2":return "Sony_-_PlayStation_2";
      case "PS3":case "PLAYSTATION3":return "Sony_-_PlayStation_3";
      case "PS4":case "PLAYSTATION4":return "Sony_-_PlayStation_4";
      case "PSP":return "Sony_-_PlayStation_Portable";
      case "PSVITA":case "VITA":return "Sony_-_PlayStation_Vita";
      case "X360":case "XBOX360":return "Microsoft_-_Xbox_360";
      case "GENESIS":case "MEGADRIVE":case "MD":return "Sega_-_Mega_Drive_-_Genesis";
      case "DREAMCAST":case "DC":return "Sega_-_Dreamcast";
      case "MASTERSYSTEM":case "SMS":return "Sega_-_Master_System_-_Mark_III";
      case "GAMEGEAR":case "GG":return "Sega_-_Game_Gear";
      default:return null;
    }
  }
}
