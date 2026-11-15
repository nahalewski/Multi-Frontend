package com.example.blades360

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import java.security.MessageDigest

/** Android normalizes Xbox/PlayStation/standard HID pads; overrides are per device and console. */
class ControllerMappings(context:Context, private val system:String) {
    private val prefs=context.getSharedPreferences("controller_mappings",0)
    companion object {
        val buttons=listOf(19,20,21,22,96,97,99,100,102,103,104,105,106,107,108,109)
        fun label(code:Int)=when(code){19->"D-pad up";20->"D-pad down";21->"D-pad left";22->"D-pad right";96->"A / Cross (south)";97->"B / Circle (east)";99->"X / Square (west)";100->"Y / Triangle (north)";102->"LB / L1";103->"RB / R1";104->"LT / L2";105->"RT / R2";106->"Left stick click";107->"Right stick click";108->"Start / Options";109->"Back / Share";else->KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_")}
        fun isPad(d:InputDevice?)=d!=null&&!d.isVirtual&&((d.sources and InputDevice.SOURCE_GAMEPAD)==InputDevice.SOURCE_GAMEPAD||(d.sources and InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK)
        fun devices()=InputDevice.getDeviceIds().toList().mapNotNull{InputDevice.getDevice(it)}.filter{isPad(it)}
        fun retro(code:Int)=when(code){96->97;97->96;99->100;100->99;else->code}
    }
    private fun prefix(device:String):String=MessageDigest.getInstance("SHA-256").digest((system+"|"+device).toByteArray()).joinToString(""){"%02x".format(it)}+"_"
    fun map(device:String,source:Int)=prefs.getInt(prefix(device)+source,source)
    fun bind(device:String,source:Int,target:Int){
        // Swap the displaced assignment, keeping the normal button set usable.
        val previous=map(device,source);val editor=prefs.edit()
        (buttons+prefs.all.keys.filter{it.startsWith(prefix(device))}.mapNotNull{it.substringAfterLast('_').toIntOrNull()}).distinct().filter{it!=source&&map(device,it)==target}.forEach{editor.putInt(prefix(device)+it,previous)}
        editor.putInt(prefix(device)+source,target).apply()
    }
    fun sources(device:String,target:Int)=(buttons+prefs.all.keys.filter{it.startsWith(prefix(device))}.mapNotNull{it.substringAfterLast('_').toIntOrNull()}).distinct().filter{map(device,it)==target}.joinToString(" / "){label(it)}
    fun reset(device:String){val edit=prefs.edit();prefs.all.keys.filter{it.startsWith(prefix(device))}.forEach{edit.remove(it)};edit.apply()}
}
