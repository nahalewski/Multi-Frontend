package com.example.blades360;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.swordfish.libretrodroid.GLRetroView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Dependency-free device checks: exercises actual View dispatch, not a copied routing model. */
public final class ControllerInstrumentation extends Instrumentation {
    private static final String CONSOLE="Instrumentation Test";
    private static final String PREF="touch_layout_Instrumentation_Test";
    private static final int WIDTH=2960,HEIGHT=1848;
    private int assertions;
    private long clock;
    private long downTime;

    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }

    @Override public void onStart() {
        Bundle result=new Bundle();
        final Throwable[] failure={null};
        runOnMainSync(()->{
            Context context=getTargetContext();
            SharedPreferences prefs=context.getSharedPreferences("emulator_library",Context.MODE_PRIVATE);
            boolean existed=prefs.contains(PREF);
            String original=prefs.getString(PREF,"");
            try {
                prefs.edit().remove(PREF).commit();
                checkMultitouch(context);
                checkLayouts(context,prefs);
                checkVisibility(context);
                checkMappings(context);
            } catch(Throwable t) { failure[0]=t; }
            finally {
                SharedPreferences.Editor editor=prefs.edit();
                if(existed)editor.putString(PREF,original);else editor.remove(PREF);
                editor.commit();
            }
        });
        if(failure[0]==null){
            result.putString("stream","\nPASS: "+assertions+" assertions; multitouch, cancellation, custom layout persistence, edit cancellation, and hidden controls.\n");
            result.putInt("assertions",assertions);
            finish(Activity.RESULT_OK,result);
        } else {
            StringWriter trace=new StringWriter();failure[0].printStackTrace(new PrintWriter(trace));
            result.putString("stream","\nFAIL after "+assertions+" assertions\n"+trace);
            result.putString("shortMsg",failure[0].toString());
            finish(Activity.RESULT_CANCELED,result);
        }
    }

    private static final class Listener implements XboxTouchController.Listener {
        final Set<Integer> pressed=new HashSet<>();
        final Map<Integer,float[]> axes=new HashMap<>();
        final Map<Integer,Integer> ups=new HashMap<>();
        int guide,closed;
        @Override public void onKey(int action,int key){
            if(action==KeyEvent.ACTION_DOWN)pressed.add(key);
            else {pressed.remove(key);ups.put(key,ups.getOrDefault(key,0)+1);}
        }
        @Override public void onStick(int source,float x,float y){axes.put(source,new float[]{x,y});}
        @Override public void onGuide(){guide++;}
        @Override public void onEditorClosed(){closed++;}
    }
    private XboxTouchController view(Context context,Listener listener){
        XboxTouchController v=new XboxTouchController(context,CONSOLE,listener);
        v.layout(0,0,WIDTH,HEIGHT);
        return v;
    }
    private void check(boolean condition,String message){assertions++;if(!condition)throw new AssertionError(message);}
    private void checkMappings(Context context){
        ControllerMappings map=new ControllerMappings(context,CONSOLE);
        String pad="instrumentation-pad";
        try {
            map.reset(pad);
            check(map.map(pad,96)==96,"Automatic south button mapping");
            map.bind(pad,97,96);
            check(map.map(pad,97)==96&&map.map(pad,96)==97,"Remapping swaps displaced button");
            check(new ControllerMappings(context,CONSOLE).map(pad,97)==96,"Mapping survives new instance");
            check(map.map("other-pad",97)==97,"Device mappings are isolated");
            check(new ControllerMappings(context,"Other Console").map(pad,97)==97,"Console mappings are isolated");
            check(ControllerMappings.Companion.retro(map.map(pad,97))==97,"Remapped south button reaches libretro B");
            map.bind(pad,188,108);
            check(map.map(pad,188)==108,"Nonstandard pad button is assignable");
            map.reset(pad);check(map.map(pad,97)==97&&map.map(pad,188)==188,"Reset restores automatic mapping");
            GameGuideView guide=new GameGuideView(context);guide.layout(0,0,WIDTH,HEIGHT);
            final int[] chosen={0};
            java.util.List<GameGuideView.Row> rows=new java.util.ArrayList<>();
            for(int i=0;i<16;i++){final int n=i;rows.add(new GameGuideView.Row("Button "+i,()->{chosen[0]=n;return kotlin.Unit.INSTANCE;}));}
            guide.show("Controller mapping","Test",rows,()->kotlin.Unit.INSTANCE);
            guide.draw(new android.graphics.Canvas(android.graphics.Bitmap.createBitmap(WIDTH,HEIGHT,android.graphics.Bitmap.Config.RGB_565)));
            guide.move(15);guide.handleKey(96,new KeyEvent(0,96));check(chosen[0]==15,"Guide scroll reaches last mapping");
        } finally {map.reset(pad);}
    }
    private boolean near(float actual,float expected){return Math.abs(actual-expected)<.0001f;}
    private void key(XboxTouchController v,int code){
        v.handleKey(code,new KeyEvent(KeyEvent.ACTION_DOWN,code));
        v.handleKey(code,new KeyEvent(KeyEvent.ACTION_UP,code));
    }
    private boolean touch(XboxTouchController view,int action,int[] ids,float[] xs,float[] ys){
        MotionEvent.PointerProperties[] props=new MotionEvent.PointerProperties[ids.length];
        MotionEvent.PointerCoords[] coords=new MotionEvent.PointerCoords[ids.length];
        for(int i=0;i<ids.length;i++){
            props[i]=new MotionEvent.PointerProperties();props[i].id=ids[i];props[i].toolType=MotionEvent.TOOL_TYPE_FINGER;
            coords[i]=new MotionEvent.PointerCoords();coords[i].x=xs[i];coords[i].y=ys[i];coords[i].pressure=1;coords[i].size=1;
        }
        if(action==MotionEvent.ACTION_DOWN){clock=SystemClock.uptimeMillis();downTime=clock;}
        MotionEvent event=MotionEvent.obtain(downTime,++clock,action,ids.length,props,coords,0,0,1,1,0,0,InputDevice.SOURCE_TOUCHSCREEN,0);
        try{return view.dispatchTouchEvent(event);}finally{event.recycle();}
    }
    private void checkMultitouch(Context context){
        Listener l=new Listener();XboxTouchController v=view(context,l);
        float lx=WIDTH*.14f,ly=HEIGHT*.64f,ax=WIDTH*.86f,ay=HEIGHT*.76f;
        check(touch(v,MotionEvent.ACTION_DOWN,new int[]{3},new float[]{lx},new float[]{ly}),"Left-stick DOWN must be captured");
        touch(v,MotionEvent.ACTION_MOVE,new int[]{3},new float[]{lx+140},new float[]{ly});
        float[] stick=l.axes.get(GLRetroView.MOTION_SOURCE_ANALOG_LEFT);
        check(stick!=null&&stick[0]>.5f&&near(stick[1],0),"Left stick must move independently to the right");
        touch(v,MotionEvent.ACTION_POINTER_DOWN|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),new int[]{3,8},new float[]{lx+140,ax},new float[]{ly,ay});
        check(l.pressed.contains(97),"Xbox A must send mapped RetroPad key 97 while stick is held");
        check(l.axes.get(GLRetroView.MOTION_SOURCE_ANALOG_LEFT)[0]>.5f,"Second pointer must not release left stick");
        touch(v,MotionEvent.ACTION_MOVE,new int[]{3,8},new float[]{lx+100,ax},new float[]{ly+100,ay});
        stick=l.axes.get(GLRetroView.MOTION_SOURCE_ANALOG_LEFT);
        check(stick[0]>.3f&&stick[1]>.3f&&l.pressed.contains(97),"Diagonal stick and face button must work together");
        touch(v,MotionEvent.ACTION_POINTER_UP|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),new int[]{3,8},new float[]{lx+100,ax},new float[]{ly+100,ay});
        check(!l.pressed.contains(97)&&l.ups.getOrDefault(97,0)==1,"Face-button UP must emit exactly once");
        check(l.axes.get(GLRetroView.MOTION_SOURCE_ANALOG_LEFT)[0]>.3f,"Releasing A must not release the other pointer");
        touch(v,MotionEvent.ACTION_CANCEL,new int[]{3},new float[]{lx+100},new float[]{ly+100});
        stick=l.axes.get(GLRetroView.MOTION_SOURCE_ANALOG_LEFT);
        check(near(stick[0],0)&&near(stick[1],0)&&l.pressed.isEmpty(),"CANCEL must neutralize all inputs");
        touch(v,MotionEvent.ACTION_DOWN,new int[]{12},new float[]{ax},new float[]{ay});
        check(l.pressed.contains(97),"A should remain usable after cancellation");
        v.openEditor();
        check(l.pressed.isEmpty()&&l.ups.getOrDefault(97,0)==2,"Entering editor must release an active face button");
        v.closeEditor();v.releaseAll();
    }
    private JSONObject control(JSONObject root,String id)throws Exception{
        JSONArray controls=root.getJSONArray("layouts").getJSONObject(root.getInt("current")).getJSONArray("controls");
        for(int i=0;i<controls.length();i++){JSONObject item=controls.getJSONObject(i);if(item.getString("id").equals(id))return item;}
        throw new AssertionError("Missing control "+id);
    }
    private void checkLayouts(Context context,SharedPreferences prefs)throws Exception{
        Listener l=new Listener();XboxTouchController v=view(context,l);
        v.openEditor();check(v.isEditing(),"Editor must open");
        key(v,109); // SELECT creates a custom copy.
        // Rearrange the control through actual drag events, rather than inspecting private state.
        touch(v,MotionEvent.ACTION_DOWN,new int[]{5},new float[]{WIDTH*.14f},new float[]{HEIGHT*.64f});
        touch(v,MotionEvent.ACTION_MOVE,new int[]{5},new float[]{WIDTH*.15f},new float[]{HEIGHT*.66f});
        touch(v,MotionEvent.ACTION_UP,new int[]{5},new float[]{WIDTH*.15f},new float[]{HEIGHT*.66f});
        key(v,105); // Increase selected control size.
        key(v,108); // START saves.
        check(!v.isEditing()&&l.closed==1,"Saving closes editor and invokes callback once");
        String saved=prefs.getString(PREF,"");JSONObject json=new JSONObject(saved);
        check(json.getJSONArray("layouts").length()==3&&json.getInt("current")==2,"Custom preset must be stored and selected");
        JSONObject ls=control(json,"LS");
        check(Math.abs(ls.getDouble("x")-.15)<.0001&&Math.abs(ls.getDouble("y")-.66)<.0001&&Math.abs(ls.getDouble("size")-1.1)<.0001,"Dragged position and resized control must persist");
        Listener fresh=new Listener();XboxTouchController restored=view(context,fresh);
        restored.openEditor();key(restored,108);
        JSONObject restoredJson=new JSONObject(prefs.getString(PREF,""));
        check(restoredJson.getInt("current")==2&&Math.abs(control(restoredJson,"LS").getDouble("x")-.15)<.0001,"A fresh View must restore the selected custom layout");
        String baseline=prefs.getString(PREF,"");
        restored.openEditor();key(restored,22);key(restored,109);key(restored,97);
        check(!restored.isEditing()&&prefs.getString(PREF,"").equals(baseline),"Cancel must not persist a moved control or new preset");
        restored.openEditor();key(restored,108);
        JSONObject afterCancel=new JSONObject(prefs.getString(PREF,""));
        check(afterCancel.getJSONArray("layouts").length()==3&&Math.abs(control(afterCancel,"LS").getDouble("x")-.15)<.0001,"Cancel must also restore in-memory layout before the next Save");
        restored.releaseAll();v.releaseAll();
    }
    private void checkVisibility(Context context){
        Listener l=new Listener();XboxTouchController v=view(context,l);
        v.setControlsVisible(false);
        check(!touch(v,MotionEvent.ACTION_DOWN,new int[]{1},new float[]{WIDTH*.86f},new float[]{HEIGHT*.76f}),"Hidden A must not capture game touches");
        touch(v,MotionEvent.ACTION_CANCEL,new int[]{1},new float[]{WIDTH*.86f},new float[]{HEIGHT*.76f});
        check(l.pressed.isEmpty(),"Hidden controls must not emit button presses");
        check(touch(v,MotionEvent.ACTION_DOWN,new int[]{2},new float[]{WIDTH*.5f},new float[]{HEIGHT*.7f})&&l.guide==1,"Guide must remain available when controls are hidden");
        touch(v,MotionEvent.ACTION_UP,new int[]{2},new float[]{WIDTH*.5f},new float[]{HEIGHT*.7f});
        v.setControlsVisible(true);v.releaseAll();
    }
}
