const test=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs/promises');
const os=require('node:os');
const path=require('node:path');
const {createEmulation}=require('../emulation-main.cjs');
const harness=require('./settings-harness.cjs');

test('PS4 variants persist as PlayStation 4 across settings reload',async()=>{
 const dir=await fs.mkdtemp(path.join(os.tmpdir(),'ps4-theme-persist-'));
 try{
  const {api}=harness(dir);await api.init();
  for(const theme of ['ps4','ps4-anniversary']){
   await api.command('theme:set',{theme});
   const saved=JSON.parse(await fs.readFile(path.join(dir,'settings.bin'),'utf8'));
   await api.init(saved);
   assert.equal(api.getState().settings.theme,theme);
   assert.equal(require('../console-catalog.js').forTheme(api.getState().settings.theme).id,'ps4');
  }
 }finally{assert(dir.startsWith(path.join(os.tmpdir(),'ps4-theme-persist-')));await fs.rm(dir,{recursive:true,force:true});}
});

test('embedded game retains PS4 presentation and serves local sounds without exposing arbitrary assets',async()=>{
 const dir=await fs.mkdtemp(path.join(os.tmpdir(),'ps4-session-'));
 const game={id:'fixture',name:'Fixture',system:'NES',path:path.join(dir,'fixture.nes')};
 await fs.writeFile(game.path,Buffer.from('test ROM fixture'));
 let theme='ps4';
 const emu=createEmulation({userData:dir,getGame:()=>game,getPresentation:()=>({theme,profile:'Local user',sound:true,volume:.3})});
 try{
  for(theme of ['ps4','ps4-anniversary']){
   const session=await emu.launch(game.id),base=new URL('.',session.url);
   const config=await(await fetch(new URL('config.json',base))).json();
   assert.equal(config.presentation.theme,theme);assert.equal(config.presentation.profile,'Local user');
   assert.equal(config.presentation.sound,true);assert.equal(config.presentation.volume,.3);
   for(const name of ['navigate.mp3','startup.mp3','logo.png']){
    const result=await fetch(new URL('assets/ps4/'+name,base));assert.equal(result.status,200,name);
    assert.deepEqual(Buffer.from(await result.arrayBuffer()),await fs.readFile(path.join(__dirname,'../assets/ps4',name)));
   }
   assert.equal((await fetch(new URL('assets/ps4/manifest.json',base))).status,404);
   assert.equal((await fetch(new URL('/config.json',base))).status,404);
   await emu.stop();
  }
  theme='not-a-theme';const session=await emu.launch(game.id);
  assert.equal((await(await fetch(new URL('config.json',new URL('.',session.url)))).json()).presentation.theme,'blades');
 }finally{await emu.stop();assert(dir.startsWith(path.join(os.tmpdir(),'ps4-session-')));await fs.rm(dir,{recursive:true,force:true});}
});
