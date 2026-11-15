const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs/promises'),os=require('node:os'),path=require('node:path');
const catalog=require('../emulator-catalog.cjs'),P=require('../external-profiles.cjs'),core=require('../core.cjs');
test('PS4 launch uses the SDL CLI, explicit fullscreen and a tracked process',()=>{
 const game={path:'D:\\ROMS\\PS4\\A Game & More\\eboot.bin'};
 const emulator={path:'C:\\Emulators\\shadPS4.exe',args:['--fullscreen','false','--game=old','--big-picture','--show-fps']};
 assert.deepEqual(P.plan(emulator,game,{},'').args,['--show-fps','--same-process','--fullscreen=true','--game',game.path]);
 assert(P.plan(emulator,game,{fullscreen:false},'').args.includes('--fullscreen=false'));
 assert.throws(()=>P.plan(emulator,{path:'D:\\game.pkg'},{},''),/extracted PS4/);
 assert.equal(P.profiles.shadps4.hideUI,undefined);
});
test('PS4 scanner recognizes only eboot in a PS4 folder and does not mislabel PS3',()=>{
 assert.equal(core.system('D:\\ROMS\\PS4\\Title\\eboot.bin'),'PlayStation 4');
 assert.equal(core.system('/games/PlayStation 4/Title/eboot.bin'),'PlayStation 4');
 assert.equal(core.system('/games/PS3/Title/PS3_GAME/USRDIR/EBOOT.BIN'),null);
 assert.equal(core.system('/games/PS4/Title/data.bin'),null);
 assert.equal(core.system('/games/PS4/Title.pkg'),null);
});
test('shadPS4 discovery assigns PS4 and honors disabled preference',async t=>{
 const dir=await fs.mkdtemp(path.join(os.tmpdir(),'shad-discovery-'));t.after(()=>fs.rm(dir,{recursive:true,force:true}));
 const file=path.join(dir,'shadPS4.exe');await fs.writeFile(file,'fixture');const state={settings:{},emulators:{}};
 const discovery=require('../emulator-discovery.cjs').createDiscovery({getState:()=>state,save:async()=>{},roots:[dir],platform:'win32'});
 assert.equal((await discovery.scan()).assigned,1);assert.equal(state.emulators['PlayStation 4'].path,file);
 state.emulators={};state.settings.emulatorChoices={shadps4:false};assert.equal((await discovery.scan()).assigned,0);
});
test('shadPS4 manager selects a verified stable Windows SDL asset',async()=>{
 const entry=catalog.get('shadps4'),url='https://github.com/shadps4-emu/shadPS4/releases/download/v.0.21.0/shadps4-win64-sdl-0.21.0.zip';
 let assets=[{name:'shadps4-win64-sdl-0.21.0.zip',browser_download_url:url,digest:'sha256:'+'a'.repeat(64)}];
 const manager=require('../emulator-manager.cjs').createManager({getState:()=>({settings:{},emulators:{}}),save:async()=>{},userData:os.tmpdir(),fetcher:async u=>{assert.equal(u,'https://api.github.com/repos/shadps4-emu/shadPS4/releases/latest');return{ok:true,json:async()=>({tag_name:'v.0.21.0',assets})};}});
 assert.equal((await manager.release(entry)).url,url);assets[0].digest=null;await assert.rejects(manager.release(entry),/verified Windows/);
});
