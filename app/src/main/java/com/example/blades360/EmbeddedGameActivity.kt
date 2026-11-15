package com.example.blades360

import android.app.Activity
import android.os.*
import android.net.Uri
import android.view.*
import android.widget.FrameLayout
import android.graphics.*
import androidx.lifecycle.*
import com.swordfish.libretrodroid.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.io.*
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import kotlin.math.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Embedded core session with one Canvas Guide for every game setting. */
class EmbeddedGameActivity : Activity(), LifecycleOwner {
    companion object {private val PREPARE_LOCK=Any()}
    private val registry=LifecycleRegistry(this)
    override fun getLifecycle():Lifecycle=registry
    private val work=CoroutineScope(SupervisorJob()+Dispatchers.Main)
    private val prefs by lazy {getSharedPreferences("emulator_library",0)}
    private var retro:GLRetroView?=null
    private lateinit var root:FrameLayout
    private lateinit var saveDir:File
    private var controller:XboxTouchController?=null
    private var menu:GameGuideView?=null
    private var resumed=false
    private var ready=false
    private var failed=false
    private var busy=false
        set(value){field=value;menu?.blocked=value}
    private val nativeLock=Any()
    private var nativeClosed=false
    private val heldHardware=mutableSetOf<Int>()
    private var gameUri=""
    private var core=""
    private var title=""
    private var system=""
    private val history=RewindHistory()
    private var rewindReason=""
    private var rewindPending=false
    private var rewindGeneration=0
    private var lastSample=0L
    private var elapsed=0L
    private var started=0L
    private var slot=1
    private var variables=arrayOf<Variable>()
    private val mappings by lazy {ControllerMappings(this,system)}
    private var mappingDevice="touch"
    private var mappingName="On-screen controller"
    private var captureButton:Int?=null
    private var lastPad:Int?=null
    private val knownPads=mutableSetOf<Int>()
    private val axisButtons=mutableSetOf<Int>()
    private val inputManager by lazy {getSystemService(android.hardware.input.InputManager::class.java)}
    private val deviceListener=object:android.hardware.input.InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(id:Int){if(ControllerMappings.isPad(InputDevice.getDevice(id))){knownPads.add(id);lastPad=id};refreshControllers()}
        override fun onInputDeviceChanged(id:Int){if(ControllerMappings.isPad(InputDevice.getDevice(id)))knownPads.add(id);refreshControllers()}
        override fun onInputDeviceRemoved(id:Int){if(!knownPads.remove(id))return;if(lastPad==id)lastPad=null;if(ready){pauseCore();if(menu==null)openMenu()};if(captureButton!=null){captureButton=null;controllerMenu()};refreshControllers()}
    }
    private fun refreshControllers(){if(menu?.heading=="Controllers")controllerMenu()}
    private fun key(name:String)="emulation_${name}_$system"
    private fun enabled(name:String)=prefs.getBoolean(key(name),true)
    private fun time()=elapsed+if(started>0)SystemClock.uptimeMillis()-started else 0L

    override fun onCreate(state:Bundle?){
        super.onCreate(state);window.decorView.systemUiVisibility=5894
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root=FrameLayout(this);root.setBackgroundColor(Color.BLACK);setContentView(root)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        gameUri=intent.getStringExtra("game_uri")?:"";core=intent.getStringExtra("core")?:""
        title=intent.getStringExtra("game_name")?:"Game"
        system=intent.getStringExtra("game_system")?:AutoEmulator.CORES.entries.firstOrNull{it.value==core}?.key?:"Console"
        inputManager.registerInputDeviceListener(deviceListener,Handler(Looper.getMainLooper()))
        lastPad=ControllerMappings.devices().firstOrNull()?.id
        knownPads.addAll(ControllerMappings.devices().map{it.id})
        if(!AutoEmulator.CORES.containsValue(core)){error("Unsupported core");return}
        showMessage("Loading $system", "$title\nPreparing core and game. First use downloads the core.")
        work.launch {
            try {
                val paths=withContext(Dispatchers.IO){synchronized(PREPARE_LOCK){prepare()}}
                if(isFinishing)return@launch
                val data=GLRetroViewData(this@EmbeddedGameActivity)
                data.coreFilePath=paths.first.absolutePath;data.gameFilePath=paths.second.absolutePath
                data.systemDirectory=File(getExternalFilesDir(null),"Emulation/system").apply{mkdirs()}.absolutePath
                data.savesDirectory=saveDir.absolutePath;data.rumbleEventsEnabled=false
                data.variables=prefs.all.filterKeys{it.startsWith("emulation_variable_${core}_")}.mapNotNull{(k,v)->if(v is String)Variable(k.removePrefix("emulation_variable_${core}_"),v,"")else null}.toTypedArray()
                val sram=File(saveDir,"battery.sav");if(sram.isFile)data.saveRAMState=sram.readBytes()
                val view=GLRetroView(this@EmbeddedGameActivity,data);retro=view
                work.launch {view.getGLRetroErrors().collect{code->if(code==GLRetroView.ERROR_SERIALIZATION){rewindReason="This core cannot serialize this game.";menu?.invalidate()}else error("Core could not run this game (error $code). Check format and BIOS files or choose an external emulator.")}}
                work.launch {view.getGLRetroEvents().collect{if(it is GLRetroView.GLRetroEvents.FrameRendered){if(!ready){ready=true;markPlayed();applyPresentation();loadVariables()};sampleRewind()}}}
                registry.addObserver(view);root.removeAllViews();menu=null
                root.addView(view,FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));root.addOnLayoutChangeListener{_,_,_,_,_,_,_,_,_->applyDisplay()}
                addControls();applyDisplay()
                if(resumed)resumeCore()
            }catch(e:Exception){if(e !is CancellationException)error(e.message?:"Emulator setup failed")}
        }
    }
    private fun prepare():Pair<File,File>{
        if(!android.os.Build.SUPPORTED_ABIS.contains("arm64-v8a"))throw IOException("Embedded cores currently require an ARM64 device")
        val cores=File(filesDir,"cores").apply{mkdirs()}
        val engine=File(cores,"${core}_libretro_android.so")
        if(!engine.isFile){
            val connection=URL("https://buildbot.libretro.com/nightly/android/latest/arm64-v8a/${engine.name}.zip").openConnection()
            connection.connectTimeout=20000;connection.readTimeout=30000
            val temp=File(cores,engine.name+".part")
            try {ZipInputStream(connection.getInputStream()).use {zip->
                var found=false;var entry=zip.nextEntry
                while(entry!=null){if(entry.name.substringAfterLast('/')==engine.name){FileOutputStream(temp).use{copyBounded(zip,it,128L*1024*1024)};found=true;break};entry=zip.nextEntry}
                if(!found)throw IOException("Core download did not contain the requested engine")
            }
            FileInputStream(temp).use{val header=ByteArray(5);if(it.read(header)!=5||header[0]!=127.toByte()||header[1]!=69.toByte()||header[4]!=2.toByte())throw IOException("Invalid ARM64 core download")}
            if(!temp.renameTo(engine))throw IOException("Could not save downloaded core")
            } finally {temp.delete()}
        }
        val key=MessageDigest.getInstance("SHA-256").digest(gameUri.toByteArray()).joinToString(""){"%02x".format(it)}
        saveDir=File(getExternalFilesDir(null),"Emulation/saves/$core/$key").apply{mkdirs()}
        val ext=title.substringAfterLast('.',"").lowercase()
        if(ext in listOf("cue","m3u","7z","rar"))throw IOException("This multi-file/archive format needs an external emulator for now. Select a single ROM, CHD, or supported disc image.")
        val content=File(cacheDir,"emulation").apply{mkdirs()}
        content.listFiles()?.forEach{it.delete()}
        val output=File(content,"game.${if(ext.matches(Regex("[a-z0-9]{1,8}")))ext else "rom"}")
        val input=contentResolver.openInputStream(Uri.parse(gameUri))?:throw IOException("ROM access expired. Reselect the ROM folder.")
        input.use{source->
            if(ext=="zip"){
                ZipInputStream(source).use {zip->
                    var entry=zip.nextEntry;var extracted:File?=null
                    val allowed=setOf("nes","sfc","smc","gb","gbc","gba","n64","z64","v64","nds","gen","md","sms","gg","pce")
                    while(entry!=null){val suffix=entry.name.substringAfterLast('.',"").lowercase();if(!entry.isDirectory&&suffix in allowed){if(extracted!=null)throw IOException("Archive contains multiple games. Extract and choose one game first.");extracted=File(content,"game.$suffix");FileOutputStream(extracted).use{copyBounded(zip,it,512L*1024*1024)}};entry=zip.nextEntry}
                    return Pair(engine,extracted?:throw IOException("No supported ROM found in ZIP"))
                }
            }
            FileOutputStream(output).use{copyBounded(source,it,4L*1024*1024*1024)}
        }
        return Pair(engine,output)
    }
    private fun copyBounded(input:InputStream,out:OutputStream,max:Long){val buffer=ByteArray(65536);var total=0L;while(true){if(isFinishing)throw IOException("Cancelled");val n=input.read(buffer);if(n<0)break;total+=n;if(total>max)throw IOException("File exceeds supported size");out.write(buffer,0,n)}}
    private fun markPlayed(){val library=EmulatorLibrary(this);library.load();val game=library.games.find{it.uri==gameUri};if(game!=null){game.played=System.currentTimeMillis();library.prefs.edit().putString("loaded_game_uri",gameUri).apply();library.save()}}

    private fun showMessage(heading:String,message:String){root.removeAllViews();controller=null;val guide=GameGuideView(this);menu=guide;guide.show(heading,message,listOf(GameGuideView.Row("Return to Blades"){finish()})){finish()};root.addView(guide,FrameLayout.LayoutParams(-1,-1))}
    private fun error(message:String){if(failed||isFinishing)return;failed=true;pauseCore();ready=false;showMessage("Game could not start",message)}
    private fun addControls(){val overlay=XboxTouchController(this,system,object:XboxTouchController.Listener{
        override fun onKey(action:Int,keyCode:Int){if(menu==null&&ready)retro?.sendKeyEvent(action,ControllerMappings.retro(mappings.map("touch",ControllerMappings.retro(keyCode))),0)}
        override fun onStick(source:Int,x:Float,y:Float){if(menu==null&&ready&&source!=GLRetroView.MOTION_SOURCE_DPAD)retro?.sendMotionEvent(source,x,y,0)}
        override fun onGuide(){openMenu()}
        override fun onEditorClosed(){if(!isFinishing)openMenu()}
    });controller=overlay;root.addView(overlay,FrameLayout.LayoutParams(-1,-1))}
    private fun pauseCore(){axisButtons.clear();controller?.releaseAll();heldHardware.forEach{retro?.sendKeyEvent(1,it,0)};heldHardware.clear();for(source in listOf(GLRetroView.MOTION_SOURCE_DPAD,GLRetroView.MOTION_SOURCE_ANALOG_LEFT,GLRetroView.MOTION_SOURCE_ANALOG_RIGHT))retro?.sendMotionEvent(source,0f,0f,0);if(started>0){elapsed+=SystemClock.uptimeMillis()-started;started=0};if(registry.currentState.isAtLeast(Lifecycle.State.RESUMED))registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)}
    private fun resumeCore(){if(!resumed||failed||menu!=null||controller?.isEditing==true)return;if(started==0L)started=SystemClock.uptimeMillis();registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)}
    private fun panel(title:String,sub:String,rows:List<GameGuideView.Row>,back:()->Unit={openMenu()}){val guide=menu?:GameGuideView(this).also{menu=it;root.addView(it,FrameLayout.LayoutParams(-1,-1))};guide.show(title,sub,rows,back);guide.blocked=busy;guide.bringToFront()}
    private fun row(label:String,run:()->Unit)=GameGuideView.Row(label,run)
    private fun openMenu(){if(!ready||failed)return;pauseCore();panel("Game Guide", "$system • ${EmulatorLibrary.cleanTitle(title)}",listOf(
        row("Resume game"){closeMenu()},row("Display and audio"){displayMenu()},row("$system core settings"){coreMenu()},
        row("Save states"){statesMenu()},row("Rewind timeline"){rewindMenu()},
        row("On-screen controller: ${if(controller?.controlsVisible==true)"Shown" else "Hidden"}"){controller?.setControlsVisible(controller?.controlsVisible!=true);openMenu()},
        row("Edit controller layout"){if(!busy){menu?.let{root.removeView(it)};menu=null;controller?.bringToFront();controller?.openEditor()}},
        row("Controllers and button mapping"){controllerMenu()},
        row("Return to Blades"){if(!busy){saveBattery();finish()}}
    ),{closeMenu()})}
    private fun closeMenu(){if(busy)return;menu?.let{root.removeView(it)};menu=null;resumeCore()}
    private fun controllerMenu(){captureButton=null;val pads=ControllerMappings.devices();panel("Controllers",if(pads.isEmpty())"No hardware controller connected. Connect Bluetooth or USB; standard Android gamepads map automatically." else "${pads.size} controller(s) detected. Automatic positional mapping is active unless overridden.",listOf(row("On-screen button mapping"){mappingDevice="touch";mappingName="On-screen controller";mappingMenu()})+pads.map{d->row("${d.name}${if(lastPad==d.id)" (active)" else ""}"){mappingDevice=d.descriptor;mappingName=d.name;mappingMenu()}}+row("Back"){openMenu()})}
    private fun mappingMenu(){captureButton=null;panel("Button mapping", "$mappingName • $system. Gameplay mappings do not change menu navigation.",ControllerMappings.buttons.map{target->row("${ControllerMappings.label(target)} ← ${mappings.sources(mappingDevice,target)}"){
        if(mappingDevice=="touch")panel("Assign button", "Choose the on-screen button for ${ControllerMappings.label(target)}",ControllerMappings.buttons.map{source->row(ControllerMappings.label(source)){mappings.bind("touch",source,target);mappingMenu()}},{mappingMenu()})
        else {captureButton=target;panel("Press a controller button", "${ControllerMappings.label(target)} • $mappingName\nBack or Guide cancels. Analog triggers are supported.",listOf(row("Cancel"){captureButton=null;mappingMenu()}),{captureButton=null;mappingMenu()})}
    }}+row("Reset to automatic mapping"){mappings.reset(mappingDevice);mappingMenu()}, {controllerMenu()})}
    private fun capture(source:Int,device:InputDevice?):Boolean{val target=captureButton?:return false;if(device?.descriptor==mappingDevice){mappings.bind(mappingDevice,source,target);captureButton=null;mappingMenu()};return true}
    private fun displayMenu(){val display=prefs.getString(key("display"),"true")?:"true";val filter=prefs.getString(key("filter"),"default")?:"default";panel("Display and audio", "$system settings • Widescreen stretches the image; it does not patch games.",listOf(
        row("View: ${displayName(display)}"){val choices=listOf("true","full","wide","4:3");prefs.edit().putString(key("display"),choices[(choices.indexOf(display)+1).mod(choices.size)]).apply();applyDisplay();displayMenu()},
        row("Filter: $filter"){val choices=listOf("default","sharp","crt","lcd");prefs.edit().putString(key("filter"),choices[(choices.indexOf(filter)+1).mod(choices.size)]).apply();applyPresentation();displayMenu()},
        row("Game audio: ${if(enabled("audio"))"On" else "Off"}"){prefs.edit().putBoolean(key("audio"),!enabled("audio")).apply();applyPresentation();displayMenu()},row("Back"){openMenu()}
    ))}
    private fun displayName(value:String)=when(value){"full"->"Fullscreen stretch";"wide"->"Widescreen 16:9";"4:3"->"Classic 4:3";else->"True console aspect"}
    private fun nativeRatio():Float=when(system){"Game Boy","Game Boy Color"->10f/9;"Game Boy Advance"->3f/2;"Game Gear"->10f/9;"PSP"->30f/17;"Nintendo DS"->2f/3;else->4f/3}
    private fun applyDisplay(){val view=retro?:return;if(root.width==0||root.height==0)return;val native=nativeRatio();val mode=prefs.getString(key("display"),"true");val desired=when(mode){"full"->root.width.toFloat()/root.height;"wide"->16f/9;"4:3"->4f/3;else->native};var w=root.width;var h=(w/desired).toInt();if(h>root.height){h=root.height;w=(h*desired).toInt()};if(mode=="full"){w=root.width;h=root.height};val lp=view.layoutParams as? FrameLayout.LayoutParams;if(lp==null||lp.width!=w||lp.height!=h){view.layoutParams=FrameLayout.LayoutParams(w,h,Gravity.CENTER)};view.holder.setFixedSize(1200,(1200/native).toInt())}
    private fun applyPresentation(){val view=retro?:return;view.audioEnabled=enabled("audio");view.shader=when(prefs.getString(key("filter"),"default")){"sharp"->ShaderConfig.Sharp;"crt"->ShaderConfig.CRT;"lcd"->ShaderConfig.LCD;else->ShaderConfig.Default};applyDisplay()}
    private fun loadVariables(){val view=retro?:return;view.queueEvent{try{val result=view.getVariables();runOnUiThread{variables=result}}catch(_:Exception){}}}
    private fun coreMenu(){val choices=variables.filter{it.description.orEmpty().contains(';')};val items=choices.map{variable->val name=variable.description.orEmpty().substringBefore(';');row("$name: ${prefs.getString("emulation_variable_${core}_${variable.key}",variable.value)}"){variableMenu(variable)}}.toMutableList();if(items.isEmpty())items.add(row("No adjustable options reported by this core"){});items.add(row("Back"){openMenu()});panel("$system settings","Options come from $core. Some changes need a game relaunch.",items)}
    private fun variableMenu(variable:Variable){val options=variable.description.orEmpty().substringAfter(';',"").split('|').map{it.trim()}.filter{it.isNotEmpty()};panel(variable.description.orEmpty().substringBefore(';'),"Changes are saved for this console core. Relaunch if required.",options.map{value->row(value){val changed=Variable(variable.key,value,variable.description);retro?.queueEvent{try{retro?.updateVariables(changed)}catch(_:Exception){}};prefs.edit().putString("emulation_variable_${core}_${variable.key}",value).apply();rewindGeneration++;history.clear();coreMenu()}}, {coreMenu()})}
    private fun stateFile()=File(saveDir,if(slot==1)"quick.state" else "slot-$slot.state")
    private fun statesMenu(message:String="States belong to this game and core. Loading changes your current progress."){panel("Save states",message,listOf(row("Slot: $slot"){slot=slot%3+1;statesMenu()},row("Save to slot $slot"){saveState()},row("Load slot $slot${if(stateFile().isFile)"" else " (empty)"}"){loadState()},row("Back"){openMenu()}))}
    private fun saveState(){if(busy)return;val target=stateFile();val targetSlot=slot;busy=true;work.launch{try{val bytes=coreCall{serializeState(false)};if(bytes.isEmpty())throw IOException("Core returned an empty state");withContext(Dispatchers.IO){atomicWrite(target,bytes)};statesMenu("State saved in slot $targetSlot.")}catch(e:Exception){statesMenu("This core could not save a state: ${e.message?:"unsupported"}")}finally{busy=false}}}
    private fun loadState(){if(busy)return;val file=stateFile();if(!file.isFile){statesMenu("No state saved in slot $slot.");return};busy=true;work.launch{try{val bytes=withContext(Dispatchers.IO){file.readBytes()};if(!coreCall{unserializeState(bytes,false)})throw IOException("State is not compatible");rewindGeneration++;history.clear();elapsed=0;lastSample=0;statesMenu("State restored. Resume game to continue.")}catch(e:Exception){statesMenu("Could not load state: ${e.message?:"unsupported"}")}finally{busy=false}}}
    private fun atomicWrite(file:File,bytes:ByteArray){val temp=File(file.parentFile,file.name+".tmp");temp.writeBytes(bytes);if(!temp.renameTo(file))throw IOException("Could not write save file")}
    private suspend fun <T> coreCall(action:GLRetroView.()->T):T=suspendCancellableCoroutine{continuation->val view=retro;if(view==null){continuation.resumeWithException(IOException("Core unavailable"));return@suspendCancellableCoroutine};view.queueEvent{try{val result=synchronized(nativeLock){if(nativeClosed)throw IOException("Core closed");view.action()};if(continuation.isActive)continuation.resume(result)}catch(e:Exception){if(continuation.isActive)continuation.resumeWithException(e)}}}
    private fun saveBattery(){val view=retro?:return;if(!ready||failed||!::saveDir.isInitialized)return;try{val bytes=synchronized(nativeLock){if(nativeClosed)return;view.serializeSRAM(false)};if(bytes.isNotEmpty())atomicWrite(File(saveDir,"battery.sav"),bytes)}catch(e:Exception){android.util.Log.e("BladesCore","Battery save failed",e)}}
    private fun sampleRewind(){if(!enabled("rewind")||rewindReason.isNotEmpty()||rewindPending||menu!=null||!resumed||controller?.isEditing==true)return;val now=time();if(now-lastSample<2000)return;lastSample=now;rewindPending=true;val generation=rewindGeneration;val view=retro?:return;view.queueEvent{try{val bytes=synchronized(nativeLock){if(nativeClosed)return@queueEvent;view.serializeState(false)};runOnUiThread{rewindPending=false;if(generation!=rewindGeneration||isDestroyed)return@runOnUiThread;val entry=history.add(now,bytes);if(entry==null){rewindReason="Rewind unavailable: empty state or state larger than 32 MB.";return@runOnUiThread};capturePreview(entry)}}catch(e:Throwable){runOnUiThread{rewindPending=false;rewindReason="Rewind unavailable for this core/game. Normal play and battery saves remain available."}}}}
    private fun capturePreview(entry:RewindHistory.Entry){val view=retro?:return;val previewWidth=min(192,(144*nativeRatio()).toInt());val previewHeight=(previewWidth/nativeRatio()).toInt().coerceAtLeast(1);val bitmap=Bitmap.createBitmap(previewWidth,previewHeight,Bitmap.Config.RGB_565);try{PixelCopy.request(view,bitmap,{result->if(result==PixelCopy.SUCCESS&&entry in history.entries)entry.preview=bitmap else bitmap.recycle();menu?.invalidate()},Handler(Looper.getMainLooper()))}catch(_:Exception){bitmap.recycle()}}
    private fun rewindMenu(){val explanation=when{!enabled("rewind")->"Rewind capture is off for $system.";rewindReason.isNotEmpty()->rewindReason;history.entries.isEmpty()->"Play for a few seconds to collect snapshots.";else->"Approx. 2-second snapshots • RAM-only history • 96 MB maximum"};panel("Rewind gameplay",explanation,listOf(row("Return to selected moment"){restoreMoment()},row("Capture: ${if(enabled("rewind"))"On" else "Off"}"){prefs.edit().putBoolean(key("rewind"),!enabled("rewind")).apply();rewindReason="";lastSample=0;rewindMenu()},row("Back"){openMenu()}));menu?.let{it.timeline=history.entries.toList();it.timelineIndex=max(0,history.entries.lastIndex);it.invalidate()}}
    private fun restoreMoment(){if(busy)return;val selected=menu?.timeline?.getOrNull(menu?.timelineIndex?:0)?:return;busy=true;work.launch{try{if(!coreCall{unserializeState(selected.state,false)})throw IOException("Core rejected state");rewindGeneration++;if(selected in history.entries)history.discardAfter(selected)else history.clear();elapsed=selected.timeMs;lastSample=elapsed;busy=false;closeMenu()}catch(e:Exception){busy=false;rewindReason="Could not restore this snapshot. Try an earlier moment or continue playing.";rewindMenu()}}}
    private fun hardwareButton(action:Int,code:Int,device:InputDevice?){val mapped=ControllerMappings.retro(mappings.map(device?.descriptor?:"keyboard",code));if(action==0)heldHardware.add(mapped)else heldHardware.remove(mapped);retro?.sendKeyEvent(action,mapped,0)}
    override fun dispatchKeyEvent(e:KeyEvent):Boolean{
        if(ControllerMappings.isPad(e.device))lastPad=e.deviceId
        if(captureButton!=null){if(e.action==0&&e.repeatCount==0){if(e.keyCode in listOf(4,82,110)){captureButton=null;mappingMenu()}else capture(e.keyCode,e.device)};return true}
        if(controller?.isEditing==true)return controller?.handleKey(e.keyCode,e)?:true
        menu?.let{if(busy)return true;if(e.keyCode==110){if(e.action==0)it.back();return true};return it.handleKey(e.keyCode,e)}
        if(e.keyCode in listOf(4,82,110)||(!ready&&e.keyCode==97)){if(e.action==0){if(ready)openMenu()else finish()};return true}
        if(e.keyCode in ControllerMappings.buttons||ControllerMappings.isPad(e.device)){if(ready)hardwareButton(e.action,e.keyCode,e.device);return true};return super.dispatchKeyEvent(e)
    }
    override fun onGenericMotionEvent(e:MotionEvent):Boolean{
        if((e.source and InputDevice.SOURCE_JOYSTICK)!=InputDevice.SOURCE_JOYSTICK)return super.onGenericMotionEvent(e)
        if(ControllerMappings.isPad(e.device))lastPad=e.deviceId
        fun axis(vararg candidates:Int):Float{val id=candidates.firstOrNull{e.device?.getMotionRange(it,e.source)!=null}?:return 0f;val value=e.getAxisValue(id);val flat=max(.12f,e.device?.getMotionRange(id,e.source)?.flat?:0f);return if(abs(value)<flat)0f else value}
        val triggers=listOf(104 to axis(17,23),105 to axis(18,22))
        if(captureButton!=null){triggers.firstOrNull{it.second>.6f}?.let{capture(it.first,e.device)};return true}
        if(controller?.isEditing==true)return true;menu?.let{return it.handleMotion(e)};val view=retro?:return true;if(!ready)return true
        triggers.forEach{(key,value)->val pressed=value>.5f;if(pressed!=axisButtons.contains(key)){if(pressed)axisButtons.add(key)else axisButtons.remove(key);hardwareButton(if(pressed)0 else 1,key,e.device)}}
        // Route hat directions through the same remapping as digital D-pad buttons.
        val hx=axis(15);val hy=axis(16)
        listOf(19 to (hy<-.5f),20 to (hy>.5f),21 to (hx<-.5f),22 to (hx>.5f)).forEach{(key,pressed)->if(pressed!=axisButtons.contains(key)){if(pressed)axisButtons.add(key)else axisButtons.remove(key);hardwareButton(if(pressed)0 else 1,key,e.device)}}
        view.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_LEFT,axis(0),axis(1),0)
        view.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,axis(11,12),axis(14,13),0);return true
    }
    override fun onResume(){super.onResume();resumed=true;if(retro!=null)resumeCore()}
    override fun onPause(){resumed=false;pauseCore();saveBattery();super.onPause()}
    override fun onDestroy(){inputManager.unregisterInputDeviceListener(deviceListener);rewindGeneration++;history.clear();work.cancel();synchronized(nativeLock){nativeClosed=true;registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)};super.onDestroy()}
    override fun onBackPressed(){if(busy)return;if(controller?.isEditing==true){controller?.closeEditor();return};menu?.let{it.back();return};if(ready)openMenu()else finish()}
}
