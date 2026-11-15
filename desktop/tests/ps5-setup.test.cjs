const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs/promises'),os=require('node:os'),path=require('node:path');
const preferences=require('../ps5-preferences.cjs'),harness=require('./settings-harness.cjs');
test('PS5 setup validates stages, safe area and agreement without mutating preferences',()=>{
 const settings=Object.freeze({theme:'ps5',setupComplete:false,ps5Setup:Object.freeze({step:'network',language:'en-US',safeArea:100})});
 for(const args of [null,[],{unknown:true},{step:'camera'},{language:'xx'},{safeArea:89},{safeArea:101},{safeArea:95.5},{safeArea:'95'},{power:'shutdown'},{agreement:1},{loginPrompt:'false'},{complete:true}])assert.throws(()=>preferences.setup(settings,args));
 assert.equal(settings.ps5Setup.step,'network');assert.equal(settings.setupComplete,false);
 for(const safeArea of [90,95,100])assert.equal(preferences.setup(settings,{safeArea}).ps5Setup.safeArea,safeArea);
 for(const step of preferences.steps)assert.equal(preferences.setup(settings,{step}).ps5Setup.step,step);
 const next=preferences.setup(settings,{step:'hdr',networkSkipped:true,safeArea:96});assert.equal(next.ps5Setup.language,'en-US');assert.equal(next.ps5Setup.networkSkipped,true);assert.notEqual(next.ps5Setup,settings.ps5Setup);
 const complete=preferences.setup(next,{agreement:true,complete:true,step:'finish',power:'low-motion'});assert.equal(complete.setupComplete,true);assert.equal(complete.ps5Setup.complete,true);assert.equal(complete.nxeMotion,false);
});
test('PS5 actual main IPC persists setup and center preferences and rejects invalid writes atomically',async()=>{
 const dir=await fs.mkdtemp(path.join(os.tmpdir(),'ps5-setup-'));
 try{const {api}=harness(dir);await api.init();await api.command('theme:set',{theme:'ps5'});await api.command('ps5:setup',{step:'display',language:'en-GB',safeArea:94});
 const before=await fs.readFile(path.join(dir,'settings.bin'),'utf8');for(const args of [{complete:true},{safeArea:89},{safeArea:101},{step:'bad'}])await assert.rejects(api.command('ps5:setup',args));await assert.rejects(api.command('ps5:center-options',{items:['home','home']}));assert.equal(await fs.readFile(path.join(dir,'settings.bin'),'utf8'),before);
 await api.init(JSON.parse(before));assert.equal(api.getState().settings.ps5Setup.step,'display');await api.command('ps5:center-options',{items:['home','music','power']});await api.command('ps5:setup',{agreement:true,step:'finish',complete:true,loginPrompt:true});const saved=JSON.parse(await fs.readFile(path.join(dir,'settings.bin'),'utf8'));assert.equal(saved.settings.setupComplete,true);assert.equal(saved.settings.ps5Setup.complete,true);assert.equal(saved.settings.ps5Setup.safeArea,94);assert.equal(saved.settings.ps5Setup.language,'en-GB');assert.equal(saved.settings.theme,'ps5');assert.deepEqual(saved.settings.ps5CenterItems,['home','music','power']);
 }finally{await fs.rm(dir,{recursive:true,force:true});}
});
test('PS5 Control Center accepts only nonempty unique supported items and copies arrays',()=>{for(const value of [null,{},[],['sound','sound'],['psn'],Array(10).fill('power')])assert.throws(()=>preferences.center({},value));const items=['music','power'];const result=preferences.center({theme:'ps5'},items);items.push('home');assert.deepEqual(result,{theme:'ps5',ps5CenterItems:['music','power']});});
