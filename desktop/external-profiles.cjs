const path=require('node:path');
const profiles={
 pcsx2:{name:'PCSX2',fullscreen:true,hideUI:true,fastBoot:true},duckstation:{name:'DuckStation',fullscreen:true,hideUI:true,fastBoot:true},
 dolphin:{name:'Dolphin',fullscreen:true,hideUI:true,resolution:true,volume:true},rpcs3:{name:'RPCS3',fullscreen:true,hideUI:true},
 ppsspp:{name:'PPSSPP',fullscreen:true,hideUI:true},cemu:{name:'Cemu',fullscreen:true},eden:{name:'Eden',fullscreen:true},xemu:{name:'xemu',fullscreen:true},retroarch:{name:'RetroArch',fullscreen:true,volume:true,vsync:true},
 azahar:{name:'Azahar',fullscreen:true},mgba:{name:'mGBA',fullscreen:true},xenia:{name:'Xenia',fullscreen:true},unknown:{name:'Custom emulator'}
};
function identify(emulator){const name=path.basename(String(emulator?.path||'').replace(/\\/g,'/')).toLowerCase();return Object.keys(profiles).find(k=>k!=='unknown'&&name.startsWith(k))||'unknown';}
function settings(value={}){return{fullscreen:value.fullscreen!==false,hideUI:value.hideUI!==false,fastBoot:typeof value.fastBoot==='boolean'?value.fastBoot:null,resolution:[1,2,3,4,6].includes(value.resolution)?value.resolution:null,volume:Number.isInteger(value.volume)&&value.volume>=0&&value.volume<=100?value.volume:null,vsync:typeof value.vsync==='boolean'?value.vsync:null};}
function validate(value){if(!value||typeof value!=='object'||Array.isArray(value))throw Error('Invalid emulator settings');for(const[k,v]of Object.entries(value)){if(!['fullscreen','hideUI','fastBoot','resolution','volume','vsync'].includes(k))throw Error('Unknown emulator setting');if(v===null&& !['fullscreen','hideUI'].includes(k))continue;if(['fullscreen','hideUI','fastBoot','vsync'].includes(k)&&typeof v!=='boolean')throw Error('Invalid emulator toggle');if(k==='resolution'&&![1,2,3,4,6].includes(v))throw Error('Invalid resolution');if(k==='volume'&&(!Number.isInteger(v)||v<0||v>100))throw Error('Invalid volume');}return settings(value);}
function plan(emulator,game,value,configPath){const id=identify(emulator),s=settings(value),profile=profiles[id];let args=[...(emulator.args||[])];if(args.some(a=>typeof a!=='string'))throw Error('Invalid emulator arguments');if(id==='unknown')return{id,profile,args:[...args,game.path],config:null};
 // Remove only switches owned by this adapter; retain core paths and custom arguments.
 const owned={pcsx2:['-fullscreen','-nofullscreen','-nogui','-batch'],duckstation:['-fullscreen','-nofullscreen','-nogui','-batch'],dolphin:['-b','--batch','-e','--exec'],rpcs3:['--no-gui','--fullscreen'],ppsspp:['--fullscreen','--windowed','--escape-exit'],cemu:['-f','--fullscreen','-g','--game'],eden:['-f','--fullscreen'],xemu:['-full-screen','-dvd_path'],retroarch:['-f','--fullscreen'],azahar:['-f','--fullscreen'],mgba:['-f','--fullscreen'],xenia:['--fullscreen']}[id];args=args.filter(a=>!owned.includes(a));let config=null;
 if(['pcsx2','duckstation'].includes(id)){args=args.filter(a=>a!=='--');if(s.fullscreen)args.push('-fullscreen');else args.push('-nofullscreen');if(s.hideUI)args.push('-nogui');if(s.fastBoot!==null){args=args.filter(a=>!['-fastboot','-slowboot'].includes(a));args.push(s.fastBoot?'-fastboot':'-slowboot');}args.push('--',game.path);}
 else if(id==='dolphin'){if(s.hideUI)args.push('-b');args.push('-C','Dolphin.Display.Fullscreen='+s.fullscreen);if(s.resolution!==null)args.push('-C','Graphics.Settings.InternalResolution='+s.resolution);if(s.volume!==null)args.push('-C','Dolphin.DSP.Volume='+s.volume);args.push('-e',game.path);}
 else if(id==='rpcs3'){if(s.hideUI||s.fullscreen)args.push('--no-gui');if(s.fullscreen)args.push('--fullscreen');args.push(game.path);}
 else if(id==='ppsspp'){args.push(s.fullscreen?'--fullscreen':'--windowed');if(s.hideUI)args.push('--escape-exit');args.push(game.path);}
 else if(id==='cemu'){args.push('--fullscreen='+s.fullscreen,'-g',game.path);}
 else if(id==='xenia'){args=args.filter(a=>!a.startsWith('--fullscreen='));args.push('--fullscreen='+s.fullscreen,game.path);}
 else if(id==='mgba'||id==='azahar'){if(s.fullscreen)args.push('-f');args.push(game.path);}
 else if(id==='eden'){if(s.fullscreen)args.push('-f');args.push(game.path);}
 else if(id==='xemu'){if(s.fullscreen)args.push('-full-screen');args.push('-dvd_path',game.path);}
 else if(id==='retroarch'){config='config_save_on_exit = "false"\nvideo_fullscreen = "'+s.fullscreen+'"\nvideo_windowed_fullscreen = "true"\n';if(s.volume!==null)config+='audio_volume = "'+(s.volume?20*Math.log10(s.volume/100):-80)+'"\n';if(s.vsync!==null)config+='video_vsync = "'+s.vsync+'"\n';args.push('--appendconfig',configPath,game.path);}
 return{id,profile,args,config};}
module.exports={identify,profiles,settings,validate,plan};
