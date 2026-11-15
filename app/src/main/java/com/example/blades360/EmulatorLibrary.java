package com.example.blades360;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Local library of user-selected documents; no filesystem-wide access or downloads. */
final class EmulatorLibrary {
  static final int IMPORT=3601,FOLDER=3602,ART=3603;
  final Activity activity;
  final android.content.SharedPreferences prefs;
  final Typeface font;
  final ArrayList<Game> games=new ArrayList<>();
  String filter="All games",searchQuery="",sortOrder="Title";
  private final Map<String,Game> extraParents=new HashMap<>();
  private final Map<String,ArrayList<Game>> groupedExtras=new HashMap<>();
  private JSONObject switchNames;
  private int indexedSize=-1;
  private boolean indexDirty=true;
  static class Game {
    String uri,name,emulator="",artUri="",system; boolean favorite; long played;
    Game(String u,String n){uri=u;name=n;system=detectSystem(u,n);}
  }
  static class App {String pkg,name;App(String p,String n){pkg=p;name=n;}}
  EmulatorLibrary(Activity a){activity=a;prefs=a.getSharedPreferences("emulator_library",0);font=Typeface.createFromAsset(a.getAssets(),"convection.ttf");sortOrder=prefs.getString("library_sort","Title");load();}
  void reloadSettings(){String saved=prefs.getString("library_sort","Title");sortOrder=Arrays.asList(sortOptions()).contains(saved)?saved:"Title";searchQuery="";indexDirty=true;}
  void load(){games.clear();reloadSettings();try{JSONArray list=new JSONArray(prefs.getString("games","[]"));for(int i=0;i<list.length();i++){JSONObject j=list.getJSONObject(i);Game g=new Game(j.getString("uri"),j.getString("name"));g.emulator=j.optString("emulator");g.favorite=j.optBoolean("favorite");g.played=j.optLong("played");g.artUri=j.optString("artUri");games.add(g);}}catch(JSONException e){toast("Saved library could not be read.");}}
  void save(){JSONArray list=new JSONArray();try{for(Game g:games){JSONObject j=new JSONObject();j.put("uri",g.uri).put("name",g.name).put("emulator",g.emulator).put("favorite",g.favorite).put("played",g.played).put("artUri",g.artUri);list.put(j);}prefs.edit().putString("games",list.toString()).apply();}catch(JSONException e){toast("Could not save library.");}}
  void toast(String s){Toast.makeText(activity,s,Toast.LENGTH_LONG).show();}
  void style(View v){if(v instanceof TextView){((TextView)v).setTypeface(font);((TextView)v).setTextColor(0xff18371b);}if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)style(((ViewGroup)v).getChildAt(i));}
  AlertDialog show(AlertDialog.Builder b){AlertDialog d=b.create();d.setOnShowListener(x->{d.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xffdeead2));style(d.getWindow().getDecorView());});d.setOnKeyListener((dialog,key,e)->{if(key==KeyEvent.KEYCODE_BUTTON_B){if(e.getAction()==KeyEvent.ACTION_UP)d.dismiss();return true;}if(key==KeyEvent.KEYCODE_BUTTON_A){if(e.getAction()==KeyEvent.ACTION_UP){ListView list=d.getListView();if(list!=null&&list.hasFocus()&&list.getSelectedItemPosition()>=0){int pos=list.getSelectedItemPosition();list.performItemClick(list.getSelectedView(),pos,list.getItemIdAtPosition(pos));}else if(d.getCurrentFocus()!=null)d.getCurrentFocus().performClick();}return true;}return false;});d.show();return d;}
  Runnable openView;
  void setSearch(String query){searchQuery=query==null?"":query.trim();}
  void setSort(String order){if(Arrays.asList(sortOptions()).contains(order)){sortOrder=order;prefs.edit().putString("library_sort",order).apply();}}
  String[] sortOptions(){return new String[]{"Title","Recently played","Console"};}
  boolean isExtra(Game game){return !PackageArtwork.badge(game.system,game.name).isEmpty();}
  int playableCount;
  int playableCount(){indexGroups();return playableCount;}
  private String titleKey(Game game){return game.system+"/"+PackageArtwork.baseTitle(game.name);}
  private void indexGroups(){
    if(!indexDirty&&indexedSize==games.size())return;indexDirty=false;indexedSize=games.size();extraParents.clear();groupedExtras.clear();
    Map<String,Game> ids=new HashMap<>(),titles=new HashMap<>();Set<String> ambiguous=new HashSet<>();
    playableCount=0;
    for(Game game:games)if(!isExtra(game)){
      playableCount++;
      String id=PackageArtwork.titleId(game.name);if(!id.isEmpty())ids.putIfAbsent(game.system+"/"+id,game);
      String key=titleKey(game);if(titles.containsKey(key))ambiguous.add(key);else titles.put(key,game);
    }
    boolean needsCatalog=false;for(Game g:games)if(isExtra(g)&&g.system.equals("Nintendo Switch")){needsCatalog=true;break;}
    if(needsCatalog&&switchNames==null)try(java.io.InputStream in=activity.getAssets().open("switch_art.json");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){
      byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);switchNames=new JSONObject(new String(out.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));
    }catch(Exception ignored){switchNames=new JSONObject();}
    // Resolve catalog IDs for base files without IDs using unique exact names/aliases.
    if(switchNames!=null){Iterator<String> keys=switchNames.keys();while(keys.hasNext()){String id=keys.next();JSONObject item=switchNames.optJSONObject(id);if(item==null)continue;ArrayList<String> names=new ArrayList<>();names.add(item.optString("name"));JSONArray aliases=item.optJSONArray("aliases");if(aliases!=null)for(int i=0;i<aliases.length();i++)names.add(aliases.optString(i));Game found=null;boolean conflict=false;for(String name:names){String key="Nintendo Switch/"+PackageArtwork.baseTitle(name);Game g=titles.get(key);if(g!=null&&!ambiguous.contains(key)){String explicit=PackageArtwork.titleId(g.name);if(!explicit.isEmpty()&&!explicit.equals(id))continue;if(found!=null&&found!=g)conflict=true;found=g;}}if(found!=null&&!conflict)ids.putIfAbsent("Nintendo Switch/"+id,found);}}
    for(Game game:games)if(isExtra(game)){
      String id=PackageArtwork.titleId(game.name),parentId=game.system.equals("Nintendo Switch")?PackageArtwork.parentId(id):"";
      Game parent=parentId.isEmpty()?null:ids.get(game.system+"/"+parentId);
      // An explicit package ID must not attach to a differently numbered base.
      if(parent==null){String key=titleKey(game);Game candidate=ambiguous.contains(key)?null:titles.get(key);if(candidate!=null){String baseId=PackageArtwork.titleId(candidate.name);if(parentId.isEmpty()||baseId.isEmpty()||baseId.equals(parentId))parent=candidate;}}
      if(parent!=null){extraParents.put(game.uri,parent);groupedExtras.computeIfAbsent(parent.uri,k->new ArrayList<>()).add(game);}
    }
    for(ArrayList<Game> extras:groupedExtras.values())extras.sort((a,b)->{int delta=PackageArtwork.badge(a.system,a.name).compareTo(PackageArtwork.badge(b.system,b.name));return delta!=0?delta:a.name.compareToIgnoreCase(b.name);});
  }
  Game parentOf(Game game){indexGroups();return extraParents.get(game.uri);}
  ArrayList<Game> extras(Game game){indexGroups();Game parent=extraParents.get(game.uri);ArrayList<Game> found=groupedExtras.get(parent==null?game.uri:parent.uri);return found==null?new ArrayList<>():new ArrayList<>(found);}
  int groupedCount(){indexGroups();return games.size()-extraParents.size();}
  String displayTitle(Game game){String title=cleanTitle(game.name);return isExtra(game)&&parentOf(game)==null?title+" (base not linked)":title;}
  boolean unmatched(Game game){return game.artUri.isEmpty()||artFailed.contains(game.artUri);}
  ArrayList<Game> visibleGames(){
    indexGroups();ArrayList<Game> visible=new ArrayList<>();String query=PackageArtwork.baseTitle(searchQuery);
    for(Game g:games){boolean grouped=extraParents.containsKey(g.uri);if(grouped&&!filter.equals("Updates / DLC")&&!filter.equals("Unmatched artwork"))continue;
      if(filter.equals("Favorites")&&!g.favorite)continue;if(filter.equals("Recently played")&&g.played<=0)continue;
      if(filter.equals("Updates / DLC")&&!isExtra(g))continue;if(filter.equals("Unmatched artwork")&&!unmatched(g))continue;
      if(!Arrays.asList("All games","Favorites","Recently played","Updates / DLC","Unmatched artwork").contains(filter)&&!g.system.equals(filter))continue;
      if(!query.isEmpty()&&!PackageArtwork.baseTitle(g.name).contains(query)&&!PackageArtwork.baseTitle(g.system).contains(query)){boolean extraMatch=false;ArrayList<Game> attached=groupedExtras.get(g.uri);if(attached!=null)for(Game extra:attached)if(PackageArtwork.baseTitle(extra.name).contains(query)||cleanTitle(extra.name).toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)))extraMatch=true;if(!extraMatch)continue;}visible.add(g);
    }
    visible.sort((a,b)->{int delta=0;if(filter.equals("Recently played")||sortOrder.equals("Recently played"))delta=Long.compare(b.played,a.played);else if(sortOrder.equals("Console"))delta=a.system.compareToIgnoreCase(b.system);if(delta==0)delta=cleanTitle(a.name).compareToIgnoreCase(cleanTitle(b.name));return delta!=0?delta:a.uri.compareTo(b.uri);});return visible;
  }
  void open(){if(openView!=null)openView.run();if(!artScanned)scanArtwork();}
  void chooseFilter(){String[] names={"All games","Favorites","Recently played","Installed emulators"};show(new AlertDialog.Builder(activity).setTitle("Games").setItems(names,(d,w)->{if(w==3)emulators();else{filter=names[w];open();}}));}
  void importGames(){show(new AlertDialog.Builder(activity).setTitle("Add games").setItems(new String[]{"Choose game files","Scan a game folder"},(d,w)->{Intent i;if(w==0)i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);else i=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);try{activity.startActivityForResult(i,w==0?IMPORT:FOLDER);}catch(ActivityNotFoundException e){toast("No document picker is installed.");}}));}
  boolean folderResult(int result,Intent data){if(result!=Activity.RESULT_OK||data==null||data.getData()==null)return true;Uri tree=data.getData();try{activity.getContentResolver().takePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(SecurityException e){toast("Permanent folder access was not granted.");return true;}
    ProgressDialog progress=new ProgressDialog(activity);progress.setTitle("Scanning game folder");progress.setMessage("Finding gamesÃ¢â‚¬Â¦");progress.setCancelable(false);progress.show();
    new Thread(()->{ArrayList<Game> found=new ArrayList<>();String error=null;try{scan(tree,android.provider.DocumentsContract.getTreeDocumentId(tree),found,new HashSet<String>(),0);}catch(Exception e){error="Some files could not be scanned. Check folder access.";}final String issue=error;activity.runOnUiThread(()->{if(activity.isFinishing()||activity.isDestroyed())return;progress.dismiss();int added=0;HashSet<String> existing=new HashSet<>();for(Game g:games)existing.add(g.uri);for(Game g:found)if(existing.add(g.uri)){games.add(g);added++;}save();toast("Added "+added+" game(s)"+(found.size()>=5000?" (5,000-file scan limit reached)":"")+(issue==null?"":". "+issue));open();});},"ROM-folder-scan").start();return true;
  }
  void scan(Uri tree,String parent,ArrayList<Game> found,Set<String> visited,int depth){if(depth>30||found.size()>=5000||!visited.add(parent))return;Uri children=android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(tree,parent);String[] columns={android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID,android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME,android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE};try(Cursor c=activity.getContentResolver().query(children,columns,null,null,null)){if(c==null)return;while(c.moveToNext()&&found.size()<5000){String id=c.getString(0),name=c.getString(1),mime=c.getString(2);if(android.provider.DocumentsContract.Document.MIME_TYPE_DIR.equals(mime))scan(tree,id,found,visited,depth+1);else if(name!=null&&name.toLowerCase(Locale.ROOT).matches(".*\\.(nes|fds|sfc|smc|gb|gbc|gba|nds|3ds|cia|n64|z64|v64|iso|cso|chd|cue|bin|pbp|gdi|cdi|wbfs|rvz|gcm|dol|elf|rom|zip|7z|md|gen|sms|gg|pce|nsp|xci|wud|wux|rpx|m3u)$"))found.add(new Game(android.provider.DocumentsContract.buildDocumentUriUsingTree(tree,id).toString(),name));}}}
  boolean result(int request,int result,Intent data){if(request==ART){if(result==Activity.RESULT_OK&&data!=null&&data.getData()!=null&&artGame!=null){try{activity.getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}artGame.artUri=data.getData().toString();save();open();}return true;}if(request==FOLDER)return folderResult(result,data);if(request!=IMPORT)return false;if(result!=Activity.RESULT_OK||data==null)return true;LinkedHashSet<Uri> uris=new LinkedHashSet<>();if(data.getData()!=null)uris.add(data.getData());if(data.getClipData()!=null)for(int i=0;i<data.getClipData().getItemCount();i++)uris.add(data.getClipData().getItemAt(i).getUri());int added=0;for(Uri uri:uris){try{activity.getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(SecurityException e){toast("Permanent file access was not granted. Please select this file again if access expires.");}boolean exists=false;for(Game g:games)if(g.uri.equals(uri.toString()))exists=true;if(exists)continue;String name=uri.getLastPathSegment();try(Cursor c=activity.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst())name=c.getString(0);}catch(Exception ignored){}games.add(new Game(uri.toString(),name==null?"Game":name));added++;}save();toast("Added "+added+" game(s)");open();return true;}
  java.util.function.Consumer<Game> detailsView;
  java.util.function.BiConsumer<Game,Boolean> emulatorView;
  void details(Game game){if(detailsView!=null){detailsView.accept(game);return;}String[] actions={"Play",game.emulator.isEmpty()?"Choose emulator":"Change emulator",game.favorite?"Remove favorite":"Add favorite","Choose artwork","Refresh local artwork","Remove from library"};show(new AlertDialog.Builder(activity).setTitle(game.name).setItems(actions,(d,w)->{if(w==0){if(effectiveEmulator(game).isEmpty())chooseEmulator(game,true);else launch(game);}else if(w==1)chooseEmulator(game,false);else if(w==2){game.favorite=!game.favorite;save();details(game);}else if(w==3){artGame=game;activity.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),ART);}else if(w==4){artScanned=false;scanArtwork();}else show(new AlertDialog.Builder(activity).setTitle("Remove "+game.name+"?").setMessage("The original game file will remain on your device.").setPositiveButton("Remove",(x,y)->{games.remove(game);save();open();}).setNegativeButton("Cancel",(x,y)->details(game)));}).setNegativeButton("Back",(d,w)->open()));}
  ArrayList<App> apps(boolean all){PackageManager pm=activity.getPackageManager();Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);TreeMap<String,App> found=new TreeMap<>();for(ResolveInfo r:pm.queryIntentActivities(i,0)){String pkg=r.activityInfo.packageName;String label=r.loadLabel(pm).toString();String match=(pkg+" "+label).toLowerCase(Locale.ROOT);if(!pkg.equals(activity.getPackageName())&&(all||match.matches(".*(azahar|x360.mobile|ax360e|xendroid|xeo|retroarch|ppsspp|duckstation|melonds|lime3ds|citra|dolphin|vita3k|cemu|aethersx2|nethersx2|armsx2|mupen|redream|flycast|drastic|pizza|snes9x|nes.emu|md.emu|eden|yuzu|sudachi|rpcsx|xenia).*")))found.put(pkg,new App(pkg,label));}ArrayList<App> result=new ArrayList<>(found.values());result.sort((a,b)->a.name.compareToIgnoreCase(b.name));return result;}
  void chooseEmulator(Game game,boolean play){if(emulatorView!=null){emulatorView.accept(game,play);return;}pickApp(false,app->{game.emulator=app.pkg;save();if(play)launch(game);else details(game);});}
  interface AppChoice{void select(App app);}
  void pickApp(boolean all,AppChoice choice){ArrayList<App> list=apps(all);String[] labels=new String[list.size()];for(int i=0;i<labels.length;i++)labels[i]=list.get(i).name;AlertDialog.Builder b=new AlertDialog.Builder(activity).setTitle(all?"Choose application":"Choose emulator").setNegativeButton("Cancel",null);if(list.isEmpty())b.setMessage("No recognized emulator is installed. Use All apps to choose a different app.");else b.setItems(labels,(d,w)->choice.select(list.get(w)));if(!all)b.setNeutralButton("All apps",(d,w)->pickApp(true,choice));show(b);}
  void emulators(){pickApp(false,app->{Intent i=activity.getPackageManager().getLaunchIntentForPackage(app.pkg);try{if(i==null)throw new ActivityNotFoundException();activity.startActivity(i);}catch(Exception e){toast("This emulator is unavailable.");}});}
  java.util.function.Consumer<Game> launchConfirmation;
  String consoleDefault(String system){return prefs.getString("default_emulator_"+system,"");}
  void setConsoleDefault(String system,String pkg){prefs.edit().putString("default_emulator_"+system,pkg==null?"":pkg).apply();}
  long autoChecked;ArrayList<App> autoApps;
  String automaticEmulator(String system){String core=AutoEmulator.CORES.get(system);if(core!=null)return AutoEmulator.PREFIX+core;if(autoApps==null||android.os.SystemClock.uptimeMillis()-autoChecked>5000){autoApps=apps(false);autoChecked=android.os.SystemClock.uptimeMillis();}return AutoEmulator.choose(system,autoApps);}
  String effectiveEmulator(Game game){if(!game.emulator.isEmpty())return game.emulator;if(isExtra(game)){Game parent=parentOf(game);if(parent!=null&&!parent.emulator.isEmpty())return parent.emulator;}String configured=consoleDefault(game.system);return configured.isEmpty()?automaticEmulator(game.system):configured; }
  void useConsoleDefault(Game game){game.emulator="";save();}
  String emulatorLabel(Game game){String pkg=effectiveEmulator(game);if(pkg.startsWith(AutoEmulator.PREFIX))return AutoEmulator.label(pkg);if(pkg.isEmpty())return "Choose emulator";try{return activity.getPackageManager().getApplicationLabel(activity.getPackageManager().getApplicationInfo(pkg,0)).toString()+(game.emulator.isEmpty()?(consoleDefault(game.system).isEmpty()?" (automatic)":" (console default)"):"");}catch(Exception ignored){return "Emulator unavailable";}}
  static class LaunchIssue {
    final Game game;final String title,message;final boolean canOpenEmulator;
    LaunchIssue(Game g,String t,String m,boolean open){game=g;title=t;message=m;canOpenEmulator=open;}
  }
  java.util.function.Consumer<LaunchIssue> launchIssue;
  void issue(Game game,String title,String message,boolean canOpen){if(launchIssue!=null){launchIssue.accept(new LaunchIssue(game,title,message,canOpen));return;}AlertDialog.Builder b=new AlertDialog.Builder(activity).setTitle(title).setMessage(message).setNegativeButton("Back",null);if(canOpen)b.setPositiveButton("Open emulator",(d,w)->openAssignedEmulator(game));b.setNeutralButton("Choose emulator",(d,w)->chooseEmulator(game,false));show(b);}
  void openAssignedEmulator(Game game){String pkg=effectiveEmulator(game);try{Intent i=activity.getPackageManager().getLaunchIntentForPackage(pkg);if(i==null)throw new ActivityNotFoundException();activity.startActivity(i);}catch(Exception ignored){issue(game,"Emulator unavailable","Install this emulator or choose another emulator for "+game.system+".",false);}}
  String launchGuidance(Game game){String pkg=effectiveEmulator(game).toLowerCase(Locale.ROOT);if(isExtra(game))return "This is an update or DLC package. Install it from the parent game's emulator, then launch the base game. Grouping does not install packages automatically.";if(pkg.contains("retroarch"))return "RetroArch needs the correct core and any required BIOS. Open RetroArch and load the game with its core; this frontend does not guess a core.";return "If the emulator opens but the game does not run, check its BIOS, game compatibility and folder permissions inside the emulator.";}
  void launch(Game game){if(isExtra(game)){issue(game,"Update / DLC package",launchGuidance(game),!effectiveEmulator(game).isEmpty());return;}if(effectiveEmulator(game).isEmpty()){chooseEmulator(game,true);return;}if(prefs.getBoolean("confirm_launch",false)&&launchConfirmation!=null){launchConfirmation.accept(game);return;}launchConfirmed(game);}
  Game loadedGame(){
    String saved=prefs.getString("loaded_game_uri",null);
    if(saved!=null){for(Game g:games)if(g.uri.equals(saved))return g;return null;}
    Game latest=null;for(Game g:games)if(g.played>0&&(latest==null||g.played>latest.played))latest=g;
    if(latest!=null)prefs.edit().putString("loaded_game_uri",latest.uri).apply();
    return latest;
  }
  void launchConfirmed(Game game){
    if(isExtra(game)){issue(game,"Update / DLC package",launchGuidance(game),!effectiveEmulator(game).isEmpty());return;}
    if(effectiveEmulator(game).isEmpty()){chooseEmulator(game,true);return;}
    ConsoleLaunchView.show(activity,game.system,cleanTitle(game.name),()->launchAfterSplash(game));
  }
  void launchAfterSplash(Game game){
    if(isExtra(game)){issue(game,"Update / DLC package",launchGuidance(game),!effectiveEmulator(game).isEmpty());return;}
    String pkg=effectiveEmulator(game);if(pkg.isEmpty()){chooseEmulator(game,true);return;}
    if(pkg.startsWith(AutoEmulator.PREFIX)){activity.startActivityForResult(new Intent(activity,EmbeddedGameActivity.class).putExtra("game_uri",game.uri).putExtra("game_name",game.name).putExtra("game_system",game.system).putExtra("core",pkg.substring(AutoEmulator.PREFIX.length())),EmulationSetup.PLAY);return;}
    try{activity.getPackageManager().getApplicationInfo(pkg,0);}catch(Exception e){issue(game,"Emulator not installed","The saved emulator is no longer installed. Choose a per-game emulator or update the "+game.system+" console default.",false);return;}
    Uri uri=Uri.parse(game.uri);try(android.content.res.AssetFileDescriptor fd=activity.getContentResolver().openAssetFileDescriptor(uri,"r")){if(fd==null)throw new java.io.IOException();}catch(Exception e){issue(game,"Game file unavailable","The game file was moved, its storage is disconnected, or folder access expired. Reconnect the storage and add the game folder again. No game file has been deleted.",false);return;}
    if(pkg.toLowerCase(Locale.ROOT).contains("retroarch")){issue(game,"RetroArch core setup",launchGuidance(game),true);return;}
    Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/octet-stream").setPackage(pkg).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);intent.setClipData(ClipData.newRawUri(game.name,uri));
    if(activity.getPackageManager().resolveActivity(intent,0)==null){String mime=activity.getContentResolver().getType(uri);if(mime!=null)intent.setDataAndType(uri,mime);}
    if(pkg.equals("com.github.stenzek.duckstation")){intent.setComponent(new ComponentName(pkg,pkg+".EmulationActivity"));intent.putExtra("bootPath",uri.toString());intent.putExtra("resumeState",false);}
    else if(pkg.equals("me.magnum.melonds")||pkg.equals("me.magnum.melonds.nightly")){intent.setAction("me.magnum.melonds.LAUNCH_ROM");intent.setComponent(new ComponentName(pkg,"me.magnum.melonds.ui.emulator.EmulatorActivity"));intent.setData(uri);intent.putExtra("uri",uri.toString());}
    else if(pkg.equals("emu.x360.mobile")){intent.setComponent(new ComponentName(pkg,"emu.x360.mobile.X360MobileGameLaunchActivity"));}
    try{if(activity.getPackageManager().resolveActivity(intent,0)==null)throw new ActivityNotFoundException();activity.startActivity(intent);game.played=System.currentTimeMillis();prefs.edit().putString("loaded_game_uri",game.uri).apply();save();}catch(Exception e){issue(game,"Direct launch unavailable","This emulator did not accept the selected game through its file-opening interface. Open the emulator and choose the game there, or select a different emulator. "+launchGuidance(game),true);}
  }
  static String detectSystem(String uri,String name){
    String path=Uri.decode(uri).replace('\\','/').toUpperCase(Locale.ROOT);
    String[] folders={"3DS","NDS","GBA","GBC","GB","GC","GAMECUBE","WII","WIIU","NES","SNES","N64","PS1","PSX","PS2","PS3","PS4","PSP","PSVITA","VITA","SWITCH","DREAMCAST","GENESIS","MEGADRIVE","SMS","GAMEGEAR","X360","GAMENATIVE-PC"};
    String[] labels={"Nintendo 3DS","Nintendo DS","Game Boy Advance","Game Boy Color","Game Boy","GameCube","GameCube","Wii","Wii U","NES","Super Nintendo","Nintendo 64","PlayStation","PlayStation","PlayStation 2","PlayStation 3","PlayStation 4","PSP","PS Vita","PS Vita","Nintendo Switch","Dreamcast","Mega Drive","Mega Drive","Master System","Game Gear","Xbox 360","PC"};
    for(int i=0;i<folders.length;i++)if(path.contains("/"+folders[i]+"/"))return labels[i];
    String ext=name.toLowerCase(Locale.ROOT);String[] ends={"nes","sfc","smc","gba","gbc","gb","nds","3ds","cia","n64","z64","v64","cso","pbp","gcm","rvz","wbfs","nsp","xci","gen","md","sms","gg"};String[] systems={"NES","Super Nintendo","Super Nintendo","Game Boy Advance","Game Boy Color","Game Boy","Nintendo DS","Nintendo 3DS","Nintendo 3DS","Nintendo 64","Nintendo 64","Nintendo 64","PSP","PSP","GameCube","GameCube / Wii","Wii","Nintendo Switch","Nintendo Switch","Mega Drive","Mega Drive","Master System","Game Gear"};
    for(int i=0;i<ends.length;i++)if(ext.endsWith("."+ends[i]))return systems[i];return "Other systems";
  }
  static String cleanTitle(String name){return name.replaceFirst("(?i)\\.[a-z0-9]{1,5}$","").replaceAll("\\[[^]]*\\]","").replaceAll("\\([^)]*\\)","").replace('_',' ').replaceAll("\\s+"," ").trim();}
  static String artKey(String name){return cleanTitle(name).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");}
  ArrayList<String> filters(){ArrayList<String> result=new ArrayList<>(Arrays.asList("All games","Favorites","Recently played","Updates / DLC","Unmatched artwork"));TreeSet<String> systems=new TreeSet<>();for(Game g:games)systems.add(g.system);result.addAll(systems);return result;}
  Game artGame;boolean artScanned=false,artScanning=false;
  Runnable artChanged;
  final java.util.concurrent.ExecutorService artWorker=java.util.concurrent.Executors.newSingleThreadExecutor();
  final android.util.LruCache<String,android.graphics.Bitmap> artCache=new android.util.LruCache<String,android.graphics.Bitmap>(16*1024*1024){protected int sizeOf(String k,android.graphics.Bitmap b){return b.getByteCount();}};
  final Set<String> artPending=new HashSet<>(),artFailed=new HashSet<>();
  android.graphics.Bitmap thumbnail(Game game){String uri=game.artUri;if(uri.isEmpty()){requestRemoteArtwork(game);return null;}android.graphics.Bitmap cached=artCache.get(uri);if(cached!=null)return cached;if(artPending.contains(uri)||artFailed.contains(uri))return null;artPending.add(uri);artWorker.execute(()->{android.graphics.Bitmap bitmap=null;try{android.graphics.BitmapFactory.Options opts=new android.graphics.BitmapFactory.Options();opts.inJustDecodeBounds=true;try(java.io.InputStream in=activity.getContentResolver().openInputStream(Uri.parse(uri))){android.graphics.BitmapFactory.decodeStream(in,null,opts);}opts.inSampleSize=1;while(opts.outWidth/opts.inSampleSize>600||opts.outHeight/opts.inSampleSize>800)opts.inSampleSize*=2;opts.inJustDecodeBounds=false;try(java.io.InputStream in=activity.getContentResolver().openInputStream(Uri.parse(uri))){bitmap=android.graphics.BitmapFactory.decodeStream(in,null,opts);}}catch(Exception ignored){}final android.graphics.Bitmap result=bitmap;activity.runOnUiThread(()->{artPending.remove(uri);if(result!=null)artCache.put(uri,result);else artFailed.add(uri);if(artChanged!=null)artChanged.run();});});return null;}
  void scanArtwork(){if(artScanning)return;artScanning=true;artScanned=true;artFailed.clear();ArrayList<android.content.UriPermission> permissions=new ArrayList<>(activity.getContentResolver().getPersistedUriPermissions());artWorker.execute(()->{HashMap<String,ArrayList<String>> found=new HashMap<>();for(android.content.UriPermission permission:permissions){Uri root=permission.getUri();if(!android.provider.DocumentsContract.isTreeUri(root))continue;try{scanArtFolder(root,android.provider.DocumentsContract.getTreeDocumentId(root),false,found,new HashSet<>(),0);}catch(Exception ignored){}}activity.runOnUiThread(()->{int count=0;for(Game g:games){if(!g.artUri.isEmpty())continue;ArrayList<String> candidates=found.get(artKey(g.name));if(candidates==null)continue;String best=null;for(String candidate:candidates)if(detectSystem(candidate,"").equals(g.system)){best=candidate;break;}if(best==null&&candidates.size()==1)best=candidates.get(0);if(best!=null){g.artUri=best;count++;}}artScanning=false;save();if(artChanged!=null)artChanged.run();if(count>0)toast("Matched "+count+" local covers");});});}
  void scanArtFolder(Uri tree,String id,boolean inside,Map<String,ArrayList<String>> found,Set<String> visited,int depth){if(depth>10||visited.size()>10000||!visited.add(id))return;String lower=id.toLowerCase(Locale.ROOT);inside=inside||lower.contains("cover")||lower.contains("artwork")||lower.contains("boxart");Uri children=android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(tree,id);try(Cursor c=activity.getContentResolver().query(children,new String[]{"document_id","_display_name","mime_type"},null,null,null)){if(c==null)return;while(c.moveToNext()){String child=c.getString(0),name=c.getString(1),mime=c.getString(2);if(android.provider.DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)){if(inside||depth<2)scanArtFolder(tree,child,inside,found,visited,depth+1);}else if(inside&&name!=null&&name.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpe?g|webp)$")){String key=artKey(name);found.computeIfAbsent(key,k->new ArrayList<>()).add(android.provider.DocumentsContract.buildDocumentUriUsingTree(tree,child).toString());}}}}

  final Set<String> remoteTried=new HashSet<>();
  final Map<String,Long> remoteFailures=new HashMap<>();
  String artworkStatus(Game game){if(artScanning||!artScanned)return "Finding artwork";if(!game.artUri.isEmpty())return artFailed.contains(game.artUri)?"Artwork unavailable":"Loading artwork";if(!prefs.getBoolean("auto_art",true))return "Automatic artwork off";if(remoteTried.contains(game.uri))return "Downloading artwork";if(remoteFailures.containsKey(game.uri))return "Artwork unavailable Ã‚Â· retrying";return "Finding artwork";}

  void clearCoverCache(){artWorker.execute(()->{java.io.File directory=new java.io.File(activity.getFilesDir(),"boxart");java.io.File[] files=directory.listFiles();if(files!=null)for(java.io.File f:files)if(f.isFile())f.delete();activity.runOnUiThread(()->{for(Game g:games)if(g.artUri.startsWith(Uri.fromFile(directory).toString()+"/"))g.artUri="";artCache.evictAll();artFailed.clear();remoteTried.clear();save();toast("Downloaded cover cache cleared. ROMs and chosen artwork are unchanged.");if(artChanged!=null)artChanged.run();});});}

  void requestRemoteArtwork(Game game){if(!prefs.getBoolean("auto_art",true))return;if(artScanning||!artScanned||remoteTried.contains(game.uri)||remoteFailures.getOrDefault(game.uri,0L)>System.currentTimeMillis())return;remoteTried.add(game.uri);artWorker.execute(()->{java.io.File file=null;try{file=RemoteArtwork.find(activity,game.system,game.name);}catch(Exception ignored){}final java.io.File result=file;activity.runOnUiThread(()->{if(result==null){remoteTried.remove(game.uri);remoteFailures.put(game.uri,System.currentTimeMillis()+60000);}if(result!=null&&game.artUri.isEmpty()){game.artUri=Uri.fromFile(result).toString();save();}if(artChanged!=null)artChanged.run();});});}

}
