package com.example.blades360;

import android.app.Activity;
import android.content.*;
import android.graphics.*;
import java.io.*;
import java.util.*;

/** Local first-run/profile flow, rendered through the existing Blades menu and keyboard. */
final class ProfileSetup {
  static final int PICK=8621;
  final MainActivity a;
  final SharedPreferences prefs;
  String tag,console,zone,picture;
  int boot;boolean sound;
  Bitmap custom,savedCustom;
  boolean editing;
  static final String[] PICTURES={"Green rings","Orange rings","Blue rings","Purple rings","Robot","Smiley","Star","Controller"};
  ProfileSetup(MainActivity a){this.a=a;prefs=a.getSharedPreferences("blades_profile",0);load();}
  String name(){return prefs.getString("gamertag","Player 1");}
  void load(){tag=name();console=prefs.getString("console_name","Blades 360");zone=prefs.getString("zone","Recreation");picture=prefs.getString("picture","0");boot=Math.max(0,Math.min(3,prefs.getInt("boot_blade",1)));sound=a.library.prefs.getBoolean("sound_enabled",true);custom=BitmapFactory.decodeFile(new File(a.getFilesDir(),"gamer-picture.png").getPath());savedCustom=custom;}
  void start(){load();editing=true;a.dashboard.optionMode=0;welcome();}
  FrontendTools.Action row(String label,Runnable run){return a.tools.action(label,run);}
  void show(String title,String sub,Runnable back,FrontendTools.Action... rows){a.tools.show(title,sub,back,rows);}
  void leave(){editing=false;a.tools.close();a.dashboard.invalidate();}
  void welcome(){show("Initial Setup","Welcome to Blades 360. Set up this frontend and create your local gamer profile.",this::leave,
    row("Continue",this::consolePage),row("Set up later",this::leave));}
  void consolePage(){show("Console Setup","Step 1 of 4 • Settings for this frontend",this::welcome,
    row("Console name: "+console,()->edit("Console name",console,32,s->{console=s;consolePage();})),
    row("Startup blade: "+a.dashboard.names[boot],()->{boot=(boot+1)%4;consolePage();}),
    row("Menu sounds: "+(sound?"On":"Off"),()->{sound=!sound;consolePage();}),row("Continue",this::profilePage));}
  void edit(String heading,String value,int limit,java.util.function.Consumer<String> done){a.tools.closeEditor();a.tools.editor=new BladesKeyboard(a,heading,value,limit,true,done,()->a.tools.editor=null);}
  void profilePage(){show("Create Profile","Step 2 of 4 • Choose a local gamertag. This does not create or rename an Xbox Live account.",this::consolePage,
    row("Gamertag: "+tag,()->edit("Enter Gamertag",tag,15,s->{tag=s;profilePage();})),row("Continue",this::pictures));}
  void pictures(){ArrayList<FrontendTools.Action> rows=new ArrayList<>();for(int i=0;i<PICTURES.length;i++){final int n=i;rows.add(row((picture.equals(""+i)?"✓ ":"")+PICTURES[i],()->{picture=""+n;pictures();}));}rows.add(row("Choose image from device",this::pick));if(custom!=null)rows.add(row((picture.equals("custom")?"✓ ":"")+"My picture",()->{picture="custom";pictures();}));rows.add(row("Continue",this::zonePage));a.tools.showList("Gamer Picture","Step 3 of 4 • Select a picture, then Continue. Your selection appears in the preview.",this::profilePage,rows);}
  void zonePage(){show("Gamer Zone","Step 4 of 4 • Choose the play style shown on your local profile. Current: "+zone,this::pictures,
    row("Recreation",()->{zone="Recreation";review();}),row("Pro",()->{zone="Pro";review();}),row("Family",()->{zone="Family";review();}),row("Underground",()->{zone="Underground";review();}));}
  void review(){show("Setup Complete",tag+" • "+zone+"\n"+console+" • Startup: "+a.dashboard.names[boot],this::zonePage,
    row("Finish",this::finish),row("Review settings",this::consolePage));}
  void finish(){try{if(picture.equals("custom")&&custom!=null){File target=new File(a.getFilesDir(),"gamer-picture.png"),temp=new File(a.getFilesDir(),"gamer-picture.tmp");try(OutputStream out=new FileOutputStream(temp)){if(!custom.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("Could not encode picture");}if(!temp.renameTo(target))throw new IOException("Could not save picture");}
    prefs.edit().putString("gamertag",tag).putString("console_name",console).putString("zone",zone).putString("picture",picture).putInt("boot_blade",boot).putBoolean("complete",true).apply();a.library.prefs.edit().putBoolean("sound_enabled",sound).apply();savedCustom=custom;savedCustom=custom;a.dashboard.blade=boot;a.dashboard.row=0;leave();
  }catch(Exception e){show("Picture could not be saved","Choose another picture or try again.",this::pictures,row("Choose picture",this::pictures));}}
  void openProfile(){load();editing=true;a.dashboard.optionMode=0;show("Gamer Profile",tag+" • "+zone+"\n"+console+" • Local profile",this::leave,row("Edit profile and picture",this::profilePage),row("Run Initial Setup",this::welcome),row("Back",this::leave));}
  void pick(){try{a.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE),PICK);}catch(Exception e){show("Gamer Picture","No image picker available.",this::pictures,row("Back",this::pictures));}}
  boolean result(int request,int result,Intent data){if(request!=PICK)return false;if(result!=Activity.RESULT_OK||data==null||data.getData()==null){pictures();return true;}try{BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;try(InputStream in=a.getContentResolver().openInputStream(data.getData())){BitmapFactory.decodeStream(in,null,options);}if(options.outWidth<=0||options.outHeight<=0)throw new IOException();options.inJustDecodeBounds=false;options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>1024)options.inSampleSize*=2;Bitmap decoded;try(InputStream in=a.getContentResolver().openInputStream(data.getData())){decoded=BitmapFactory.decodeStream(in,null,options);}if(decoded==null)throw new IOException();int side=Math.min(decoded.getWidth(),decoded.getHeight());Bitmap square=Bitmap.createBitmap(decoded,(decoded.getWidth()-side)/2,(decoded.getHeight()-side)/2,side,side);custom=Bitmap.createScaledBitmap(square,256,256,true);picture="custom";pictures();}catch(Exception e){show("Gamer Picture","That image could not be opened. Choose a PNG or JPEG image.",this::pictures,row("Choose another image",this::pick),row("Back",this::pictures));}return true;}
  void draw(Canvas c,RectF box,boolean draft){String id=draft?picture:prefs.getString("picture","0");if(id.equals("custom")&&(draft?custom:savedCustom)!=null){c.drawBitmap(draft?custom:savedCustom,null,box,new Paint(3));return;}int n;try{n=Integer.parseInt(id);}catch(Exception e){n=0;}n=Math.max(0,Math.min(7,n));int[] colors={0xff75ac3c,0xffce873a,0xff428bb8,0xff8d58a9,0xff61768a,0xffdda944,0xff285887,0xff4e873d};Paint p=new Paint(3);c.save();c.clipRect(box);c.translate(box.left,box.top);c.scale(box.width()/100,box.height()/100);p.setShader(new LinearGradient(0,0,100,100,colors[n],0xff172b23,Shader.TileMode.CLAMP));c.drawRect(0,0,100,100,p);p.setShader(null);p.setColor(0xffe5efc9);p.setStrokeWidth(3);
    if(n<4){p.setStyle(Paint.Style.STROKE);for(int i=0;i<8;i++)c.drawCircle(40,24,9+i*10,p);}
    else if(n==4){c.drawRoundRect(20,30,80,78,8,8,p);p.setColor(0xff24352e);c.drawRect(30,43,41,55,p);c.drawRect(59,43,70,55,p);c.drawRect(36,65,64,69,p);p.setColor(0xffe5efc9);c.drawLine(50,30,50,17,p);c.drawCircle(50,15,5,p);}
    else if(n==5){c.drawCircle(50,50,32,p);p.setColor(0xff57452c);c.drawCircle(38,42,4,p);c.drawCircle(62,42,4,p);p.setStyle(Paint.Style.STROKE);c.drawArc(32,42,68,70,0,180,false,p);}
    else if(n==6){Path star=new Path();for(int i=0;i<10;i++){double angle=-Math.PI/2+i*Math.PI/5;float r=i%2==0?37:16,x=50+(float)Math.cos(angle)*r,y=50+(float)Math.sin(angle)*r;if(i==0)star.moveTo(x,y);else star.lineTo(x,y);}star.close();c.drawPath(star,p);}
    else{c.drawRoundRect(15,30,85,72,17,17,p);p.setColor(0xff243c2a);c.drawRect(28,40,35,62,p);c.drawRect(21,47,42,54,p);c.drawCircle(66,45,5,p);c.drawCircle(76,55,5,p);}
    c.restore();}
  void preview(Canvas c){if(!editing||!a.tools.active||!Arrays.asList("Initial Setup","Console Setup","Create Profile","Gamer Picture","Gamer Zone","Setup Complete","Gamer Profile").contains(a.tools.title))return;MainActivity.Dashboard d=a.dashboard;float x=700,y=145;d.round(c,x-12,y-12,x+240,y+335,12,0xdd273922);draw(c,new RectF(x,y,x+228,y+228),true);d.text(c,d.fit(tag,228,28),x,y+270,28,0xffeff4df,false,true);d.text(c,zone,x,y+307,22,0xffd2e0bc,false,false);}
}
