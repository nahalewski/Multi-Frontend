package com.example.blades360;

import android.graphics.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import java.util.function.Consumer;

/** App-owned keyboard: no editable Android widget or IME connection. */
final class BladesKeyboard extends View {
  final MainActivity a;
  final String heading;
  final int limit;
  final boolean name;
  final Consumer<String> done;
  final Runnable dismissed;
  final Paint p=new Paint(3);
  String value,error="";
  int cursor,col,row;
  boolean upper, symbols,replace,closed;
  boolean secret;
  float scale,left,top;
  long lastMove;
  static final String[] LETTERS={"1234567890","qwertyuiop","asdfghjkl'","zxcvbnm,.-"};
  static final String[] SYMBOLS={"!@#$%^&*()","[]{}<>+=_/","\\|:;\"?~`'-","1234567890"};
  static final String[] ACTIONS={"Shift","Symbols","Space","Delete","Clear","◀","▶","Done","Cancel"};
  BladesKeyboard(MainActivity a,String heading,String initial,int limit,boolean name,Consumer<String> done,Runnable dismissed){
    super(a);this.a=a;this.heading=heading;this.value=initial;this.limit=limit;this.name=name;this.done=done;this.dismissed=dismissed;cursor=value.length();replace=!value.isEmpty();
    setFocusable(true);setFocusableInTouchMode(true);a.dashboard.stopStick();
    ((InputMethodManager)a.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(a.dashboard.getWindowToken(),0);
    a.addContentView(this,new ViewGroup.LayoutParams(-1,-1));requestFocus();
  }
  void close(){if(closed)return;closed=true;if(getParent()!=null)((ViewGroup)getParent()).removeView(this);dismissed.run();a.dashboard.requestFocus();a.dashboard.invalidate();}
  void insert(String s){if(replace){value="";cursor=0;replace=false;}if(value.length()+s.length()>limit){error="Maximum "+limit+" characters";invalidate();return;}value=value.substring(0,cursor)+s+value.substring(cursor);cursor+=s.length();error="";invalidate();}
  void erase(){if(replace){value="";cursor=0;replace=false;}else if(cursor>0){int previous=value.offsetByCodePoints(cursor,-1);value=value.substring(0,previous)+value.substring(cursor);cursor=previous;}error="";invalidate();}
  void submit(){String result=value.trim();if(name&&!MediaQueue.validName(result)){error="Enter a name without slashes";invalidate();return;}close();done.accept(result);}
  void move(int x,int y){row=Math.floorMod(row+y,5);col=Math.floorMod(col+x,row==4?9:10);invalidate();}
  void activate(){if(row<4){String s=String.valueOf((symbols?SYMBOLS:LETTERS)[row].charAt(col));insert(upper?s.toUpperCase(java.util.Locale.ROOT):s);return;}switch(col){case 0:upper=!upper;break;case 1:symbols=!symbols;break;case 2:insert(" ");break;case 3:erase();break;case 4:value="";cursor=0;replace=false;break;case 5:replace=false;if(cursor>0)cursor=value.offsetByCodePoints(cursor,-1);break;case 6:replace=false;if(cursor<value.length())cursor=value.offsetByCodePoints(cursor,1);break;case 7:submit();break;case 8:close();break;}invalidate();}
  @Override public boolean onKeyDown(int key,KeyEvent event){
    if(event.getRepeatCount()>0&&(key==96||key==97||key==108))return true;
    switch(key){case 19:move(0,-1);break;case 20:move(0,1);break;case 21:move(-1,0);break;case 22:move(1,0);break;case 96:case 23:activate();break;case 97:case 4:case 111:close();break;case 99:case 67:erase();break;case 100:insert(" ");break;case 102:upper=!upper;invalidate();break;case 103:symbols=!symbols;invalidate();break;case 108:case 66:submit();break;default:int ch=event.getUnicodeChar();if(ch>=32&&!Character.isISOControl(ch))insert(new String(Character.toChars(ch)));}return true;
  }
  @Override public boolean onKeyUp(int key,KeyEvent e){return true;}
  @Override public boolean onGenericMotionEvent(MotionEvent e){float x=e.getAxisValue(MotionEvent.AXIS_HAT_X),y=e.getAxisValue(MotionEvent.AXIS_HAT_Y);if(Math.abs(x)<.5f)x=e.getAxisValue(MotionEvent.AXIS_X);if(Math.abs(y)<.5f)y=e.getAxisValue(MotionEvent.AXIS_Y);long now=android.os.SystemClock.uptimeMillis();if(Math.max(Math.abs(x),Math.abs(y))<.5f){lastMove=0;return true;}if(now-lastMove>180){if(Math.abs(x)>Math.abs(y))move(x>0?1:-1,0);else move(0,y>0?1:-1);lastMove=now;}return true;}
  void box(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);p.setShader(null);c.drawRoundRect(x,y,x+w,y+h,7,7,p);}
  void text(Canvas c,String s,float x,float y,float size,int color){p.setColor(color);p.setShader(null);p.setTypeface(a.dashboard.regular);p.setTextSize(size);c.drawText(s,x,y,p);}
  @Override protected void onDraw(Canvas c){super.onDraw(c);String value=secret?new String(new char[this.value.length()]).replace('\0','•'):this.value;c.drawColor(0xb8000000);scale=Math.min(getWidth()/1000f,getHeight()/620f);left=(getWidth()-960*scale)/2;top=(getHeight()-560*scale)/2;c.save();c.translate(left,top);c.scale(scale,scale);
    p.setShader(new LinearGradient(0,0,0,560,new int[]{0xffe4e8d6,0xffa5b58b},null,Shader.TileMode.CLAMP));c.drawRoundRect(0,0,960,560,24,24,p);p.setShader(null);
    box(c,0,0,960,65,0xff52782f);text(c,heading,30,44,30,0xfff7f8ef);
    box(c,28,82,904,65,0xfff2f3e7);c.save();c.clipRect(40,85,920,145);p.setTypeface(a.dashboard.regular);p.setTextSize(28);float shift=Math.max(0,p.measureText(value.substring(0,cursor))-850);if(replace)box(c,42-shift,94,Math.max(5,p.measureText(value)),38,0xffbfd88f);text(c,value,42-shift,124,28,0xff26321e);float cx=42-shift+p.measureText(value.substring(0,cursor));box(c,cx,96,2,32,0xff385a23);c.restore();
    for(int r=0;r<5;r++){int count=r==4?9:10;float width=900f/count;for(int k=0;k<count;k++){float x=30+k*width,y=170+r*59;boolean selected=r==row&&k==col;box(c,x,y,width-6,51,selected?0xff709b38:0xffe4e8d8);String label=r==4?ACTIONS[k]:String.valueOf((symbols?SYMBOLS:LETTERS)[r].charAt(k));if(r<4&&upper)label=label.toUpperCase(java.util.Locale.ROOT);p.setTypeface(a.dashboard.regular);p.setTextSize(r==4?20:28);float tx=x+(width-6-p.measureText(label))/2;text(c,label,tx,y+34,r==4?20:28,selected?Color.WHITE:0xff293524);}}
    text(c,error.isEmpty()?"A Select   B Cancel   X Delete   Y Space   START Done":error,30,502,23,error.isEmpty()?0xff293524:0xff99372a);text(c,"LB Shift   RB Symbols   •   Touch any key to type",30,538,20,0xff43583a);c.restore();
  }
  @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){float x=(e.getX()-left)/scale,y=(e.getY()-top)/scale;if(y>=170&&y<465&&x>=30&&x<930){int r=(int)((y-170)/59);if(r<5){row=r;col=Math.min(row==4?8:9,(int)((x-30)/(900f/(row==4?9:10))));activate();}}}return true;}
}
