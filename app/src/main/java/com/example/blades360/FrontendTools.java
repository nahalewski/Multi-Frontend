package com.example.blades360;

import android.graphics.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;
import java.util.function.Consumer;

/** Functional library panels using the existing Guide-style appearance. */
final class FrontendTools {
  final MainActivity a;final ArtworkManager art;final SetupBackup backup;FullSettingsBackup fullBackup;
  boolean active;String title="",subtitle="";int index,offset,slots;
  ArrayList<Action> actions=new ArrayList<>();Runnable parent;
  BladesKeyboard editor;
  static class Action {String label;Runnable run;Action(String l,Runnable r){label=l;run=r;}}
  FrontendTools(MainActivity a){this.a=a;art=new ArtworkManager(a,a.library);backup=new SetupBackup(a,a.library);backup.changed=()->{if(active&&title.equals("Backup & restore"))openBackup();a.dashboard.invalidate();};}
  void show(String t,String sub,Runnable back,Action... rows){title=t;subtitle=sub;parent=back;actions=new ArrayList<>(Arrays.asList(rows));active=true;index=offset=0;a.dashboard.stopStick();a.dashboard.invalidate();}
  void showList(String t,String sub,Runnable back,ArrayList<Action> rows){show(t,sub,back,rows.toArray(new Action[0]));}
  Action action(String label,Runnable r){return new Action(label,r);}
  void close(){active=false;art.cancel();closeEditor();a.dashboard.requestFocus();a.dashboard.invalidate();}
  void openHome(){show("Library tools","Search, organize and manage your collection",null,
    action("Search games",()->textEntry("Search games",a.library.searchQuery,s->{a.library.setSearch(s);browse();})),
    action("Clear search",()->{a.library.setSearch("");browse();}),
    action("Sort games",this::openSort),action("Filter games",this::openFilters),
    action("Unmatched artwork",()->{a.library.filter="Unmatched artwork";a.library.setSearch("");browse();}),
    action("Console emulator defaults",this::openDefaults),action("Backup & restore",this::openBackup),
    action("Now playing",()->{close();a.media.openNowPlaying();}),
    action("Emulation setup",this::openEmulation),action("Gamer Profile",()->a.profile.openProfile()));}
  void openEmulation(){show("Emulation setup","Automatic cores run inside Blades. Standalone emulators open their own app. Import your BIOS files if a core requires them.",this::openHome,
    action("Import BIOS files",()->EmulationSetup.pickBios(a)),action("Console emulator defaults",this::openDefaults),
    action("Core and platform support",()->show("Supported engines","In-app: NES, SNES, GB/GBC, GBA, Sega 8/16-bit, N64, DS, PS1, PSP and Dreamcast. Installed apps: Azahar, Eden, Dolphin, PS2, Vita, Cemu and X360 Mobile. xemu has no official Android port.",this::openEmulation,action("Back",this::openEmulation))));}
  void browse(){close();a.dashboard.gameIndex=a.dashboard.gameOffset=0;a.dashboard.openLibrary();}
  void openSort(){ArrayList<Action> rows=new ArrayList<>();for(String sort:a.library.sortOptions())rows.add(action(sort,()->{a.library.setSort(sort);browse();}));showList("Sort games","Current: "+a.library.sortOrder,this::openHome,rows);}
  void openFilters(){ArrayList<Action> rows=new ArrayList<>();for(String filter:a.library.filters())rows.add(action(filter,()->{a.library.filter=filter;browse();}));showList("Filter games","Choose a console or collection",this::openHome,rows);}
  void openDefaults(){TreeSet<String> systems=new TreeSet<>();for(EmulatorLibrary.Game g:a.library.games)systems.add(g.system);ArrayList<Action> rows=new ArrayList<>();for(String system:systems)rows.add(action(system,()->openDefault(system)));showList("Console defaults","Per-game choices override these defaults",this::openHome,rows);}
  void openDefault(String system){ArrayList<Action> rows=new ArrayList<>();rows.add(action("Clear console default",()->{a.library.setConsoleDefault(system,"");openDefaults();}));for(EmulatorLibrary.App app:a.library.apps(false))rows.add(action(app.name,()->{a.library.setConsoleDefault(system,app.pkg);openDefaults();}));showList("Choose console emulator",system,this::openDefaults,rows);}
  void openExtras(EmulatorLibrary.Game game){ArrayList<Action> rows=new ArrayList<>();for(EmulatorLibrary.Game extra:a.library.extras(game))rows.add(action(PackageArtwork.badge(extra.system,extra.name)+"  "+EmulatorLibrary.cleanTitle(extra.name),()->show("Game extra",EmulatorLibrary.cleanTitle(extra.name),()->openExtras(game),action("Find or change artwork",()->openArtwork(extra)),action("Install using your emulator",()->show("Install extra","Updates and DLC must be installed through the emulator's own install function.",()->openExtras(game),action("Back to extras",()->openExtras(game)))))));if(rows.isEmpty())rows.add(action("No updates or DLC found",()->close()));showList("Updates & DLC",EmulatorLibrary.cleanTitle(game.name),this::close,rows);}
  void openArtwork(EmulatorLibrary.Game game){show("Artwork",EmulatorLibrary.cleanTitle(game.name),this::close,
    action("Search by title",()->textEntry("Find artwork",EmulatorLibrary.cleanTitle(game.name),s->searchArt(game,s))),
    action("Choose local image",()->{a.library.artGame=game;try{a.startActivityForResult(new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(android.content.Intent.CATEGORY_OPENABLE).addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION|android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),EmulatorLibrary.ART);close();}catch(Exception e){a.library.toast("No image picker is available.");}}),
    action("Browse unmatched games",()->{a.library.filter="Unmatched artwork";a.library.setSearch("");browse();}));}
  void searchArt(EmulatorLibrary.Game game,String query){show("Artwork search","Searching...",()->openArtwork(game),action("Searching...",()->{}));art.search(game,query,()->{if(!active||!title.equals("Artwork search"))return;ArrayList<Action> rows=new ArrayList<>();for(ArtworkManager.Candidate candidate:art.candidates)rows.add(action(candidate.title,()->show("Use this artwork?",candidate.title,()->searchArt(game,query),action("Use cover",()->{subtitle="Saving artwork...";art.choose(game,candidate,()->{subtitle=art.status;a.dashboard.invalidate();});}),action("Back to results",()->searchArt(game,query)))));if(rows.isEmpty())rows.add(action("Try another title",()->textEntry("Find artwork",query,s->searchArt(game,s))));showList("Artwork search",art.status,()->openArtwork(game),rows);});}
  void openBackup(){show("Backup & restore",backup.busy?backup.status:backup.status.isEmpty()?"Settings, favorites, assignments and artwork. No ROMs.":backup.status,this::openHome,
    action("Export all settings (encrypted)",()->{if(fullBackup==null)fullBackup=new FullSettingsBackup(a);fullBackup.start(false);}),
    action("Restore all settings (encrypted)",()->{if(fullBackup==null)fullBackup=new FullSettingsBackup(a);fullBackup.start(true);}),
    action("Export library and artwork",()->{if(!backup.busy)backup.exportSetup();}),
    action("Import and merge setup",()->{if(!backup.busy)backup.importSetup();}),
    action("About restoring",()->show("Restore setup","Imports merge with your library. On another device, reselect ROM and music folders to grant access.",this::openBackup,action("Back",this::openBackup))));}
  void launchIssue(EmulatorLibrary.LaunchIssue issue){ArrayList<Action> rows=new ArrayList<>();if(issue.canOpenEmulator)rows.add(action("Open assigned emulator",()->{close();a.library.openAssignedEmulator(issue.game);}));rows.add(action("Choose another emulator",()->{close();a.dashboard.openEmulators(issue.game,false,false);}));rows.add(action("Console emulator default",()->openDefault(issue.game.system)));rows.add(action("Back",this::close));showList(issue.title,issue.message,this::close,rows);}
  void textEntry(String heading,String initial,Consumer<String> done){closeEditor();editor=new BladesKeyboard(a,heading,initial,200,false,done,()->editor=null);}
  void closeEditor(){if(editor!=null)editor.close();}
  boolean back(){if(editor!=null){closeEditor();return true;}if(!active)return false;if(parent!=null)parent.run();else close();return true;}
  void move(int delta){index=Math.max(0,Math.min(actions.size()-1,index+delta));if(index<offset)offset=index;if(index>=offset+slots)offset=index-slots+1;a.dashboard.invalidate();}
  void select(){if(index>=0&&index<actions.size())actions.get(index).run.run();a.dashboard.invalidate();}
  boolean key(int key,KeyEvent e){if(editor!=null)return false;if(key==108){if(!active)openHome();else close();return true;}if(!active)return false;if(e.getRepeatCount()>0&&(key==96||key==97))return true;if(key==19)move(-1);else if(key==20)move(1);else if(key==96||key==23||key==66)select();else if(key==97||key==4)back();return true;}
  float top=230,step=49,downY;
  boolean touch(MotionEvent e,float x,float y){if(!active)return false;if(e.getAction()==MotionEvent.ACTION_DOWN){downY=y;return true;}if(e.getAction()!=MotionEvent.ACTION_UP)return true;if(Math.abs(y-downY)>50){move(y<downY?slots:-slots);return true;}if(x>590){back();return true;}if(y>a.dashboard.H-100){if(x<280)select();else back();return true;}int n=offset+(int)((y-top)/step);if(y>=top&&n>=offset&&n<Math.min(actions.size(),offset+slots)){if(n==index)select();else{index=n;a.dashboard.invalidate();}}return true;}
  void draw(Canvas c){if(!active)return;MainActivity.Dashboard d=a.dashboard;float H=d.H,edge=572;d.rect(c,-d.offsetX/d.scale,0,d.W+d.offsetX/d.scale,H,0xa6000000);Path panel=new Path();panel.moveTo(-d.offsetX/d.scale,0);panel.lineTo(edge-13,0);panel.cubicTo(edge-22,45,edge-34,82,edge-29,96);panel.cubicTo(edge-25,108,edge+14,93,edge+13,119);panel.cubicTo(edge+10,H*.38f,edge+5,H*.60f,edge-2,H*.68f);panel.cubicTo(edge-18,H*.78f,edge-32,H*.82f,edge-20,H);panel.lineTo(-d.offsetX/d.scale,H);panel.close();c.save();c.clipPath(panel);d.gradient(c,-40,0,edge+20,H,0xffdce9cc,0xff839d66,true,0);d.gradient(c,edge-52,0,edge+20,H,0xffbac6b2,0xff4a5f42,false,0);d.gradient(c,-40,-25,edge+2,91,0xff415d28,0xff91b950,true,24);d.text(c,d.fit(title,480,32),45,56,32,0xfff6f8e9,false,true);c.save();c.clipRect(40,105,520,215);d.wrapped(c,subtitle,48,135,460,22,0xff20331b);c.restore();slots=Math.max(2,Math.min(8,(int)((H-130-top)/step)));for(int i=offset;i<Math.min(actions.size(),offset+slots);i++){float y=top+(i-offset)*step;if(i==index)d.gradient(c,40,y-3,518,y+43,0xfff8fbe8,0xffa9c983,true,9);d.line(c,45,y+44,514,y+44,0x666a7e53,1.4f);d.text(c,d.fit(actions.get(i).label,445,25),58,y+31,25,0xff20331b,false,false);}if(offset>0)d.text(c,"▲",490,top-10,20,0xff3d572c,false,false);if(offset+slots<actions.size())d.text(c,"▼",490,top+slots*step+18,20,0xff3d572c,false,false);d.gradient(c,-40,H-100,edge+2,H+25,0xff5b783d,0xffb8ce99,true,24);d.button(c,64,H-45,"A",0xff82b83c);d.text(c,"Select",88,H-37,24,0xfff4f6e7,false,true);d.button(c,376,H-45,"B",0xffbd3a30);d.text(c,"Back",400,H-37,24,0xfff4f6e7,false,true);c.restore();}
}
