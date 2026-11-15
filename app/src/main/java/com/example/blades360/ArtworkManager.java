package com.example.blades360;

import android.app.Activity;
import android.net.Uri;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/** User-directed search never silently applies a guessed title. */
final class ArtworkManager {
  static final class Candidate {
    final String title,system,url;
    Candidate(String title,String system,String url){this.title=title;this.system=system;this.url=url;}
  }
  final Activity activity;final EmulatorLibrary library;
  final ExecutorService worker=Executors.newSingleThreadExecutor();
  final ArrayList<Candidate> candidates=new ArrayList<>();
  boolean busy,closed;String status="";int generation;
  ArtworkManager(Activity a,EmulatorLibrary l){activity=a;library=l;}
  ArrayList<EmulatorLibrary.Game> unmatched(){ArrayList<EmulatorLibrary.Game> out=new ArrayList<>();for(EmulatorLibrary.Game g:library.games){boolean missing=g.artUri.isEmpty();if(!missing&&g.artUri.startsWith("file:"))missing=!new File(Uri.parse(g.artUri).getPath()).isFile();if(missing)out.add(g);}out.sort((a,b)->a.name.compareToIgnoreCase(b.name));return out;}
  void search(EmulatorLibrary.Game game,String query,Runnable changed){final int token=++generation;busy=true;status="Searching "+game.system+" artwork...";candidates.clear();changed.run();worker.execute(()->{ArrayList<Candidate> found=new ArrayList<>();String error="";try{found=RemoteArtwork.search(activity,game.system,query);}catch(Exception e){error="Artwork catalog unavailable. Try again or choose a local image.";}ArrayList<Candidate> result=found;String issue=error;activity.runOnUiThread(()->{if(closed||token!=generation)return;candidates.addAll(result);busy=false;status=issue.isEmpty()?(result.isEmpty()?"No results. Try a shorter title or choose a local image.":result.size()+" results. Select a title to use its cover."):issue;changed.run();});});}
  void choose(EmulatorLibrary.Game game,Candidate candidate,Runnable changed){if(busy)return;final int token=++generation;busy=true;status="Downloading selected artwork...";changed.run();worker.execute(()->{File file=null;try{file=RemoteArtwork.downloadChoice(activity,candidate);}catch(Exception ignored){}File result=file;activity.runOnUiThread(()->{if(closed||token!=generation)return;busy=false;if(result!=null&&library.games.contains(game)){game.artUri=Uri.fromFile(result).toString();library.artCache.evictAll();library.artFailed.clear();library.save();status="Artwork saved for "+EmulatorLibrary.cleanTitle(game.name);}else status="Artwork could not be downloaded. Try another result.";changed.run();});});}
  void cancel(){generation++;busy=false;}
  void close(){closed=true;cancel();worker.shutdownNow();}
}
