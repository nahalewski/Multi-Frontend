const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs/promises'),os=require('node:os'),path=require('node:path');
const preferences=require('../ps4-preferences.cjs'),harness=require('./settings-harness.cjs');
test('PS4 setup rejects invalid preferences without mutating existing settings',()=>{
 const settings=Object.freeze({theme:'ps4',setupComplete:false,ps4Setup:Object.freeze({step:'network',language:'en-US',zone:'system'})});
 for(const args of [null,[],{unknown:true},{step:'bad'},{language:'xx'},{zone:'Mars/Olympus'},{zone:3},{hour12:'true'},{power:'shutdown'},{agreement:1},{loginPrompt:'false'},{complete:true}])assert.throws(()=>preferences.setup(settings,args));
 assert.equal(settings.ps4Setup.step,'network');assert.equal(settings.setupComplete,false);
 const next=preferences.setup(settings,{step:'camera',networkSkipped:true});assert.equal(next.ps4Setup.step,'camera');assert.equal(next.ps4Setup.language,'en-US');assert.equal(next.ps4Setup.networkSkipped,true);assert.notEqual(next,settings);assert.notEqual(next.ps4Setup,settings.ps4Setup);
 const complete=preferences.setup(next,{agreement:true,complete:true,step:'finish',power:'low-motion',hour12:false});assert.equal(complete.setupComplete,true);assert.equal(complete.ps4Setup.complete,true);assert.equal(complete.nxeMotion,false);assert.equal(complete.ps4Setup.hour12,false);
});
test('PS4 initial setup stage and completion persist through actual main IPC settings writes',async()=>{
 const dir=await fs.mkdtemp(path.join(os.tmpdir(),'ps4-setup-'));
 try{const {api}=harness(dir);await api.init();await api.command('theme:set',{theme:'ps4-anniversary'});await api.command('ps4:setup',{step:'network',language:'en-GB',zone:'Europe/London'});const before=await fs.readFile(path.join(dir,'settings.bin'),'utf8');await assert.rejects(api.command('ps4:setup',{complete:true}),/acknowledge/);assert.equal(await fs.readFile(path.join(dir,'settings.bin'),'utf8'),before,'failed completion does not change durable settings');
 await api.init(JSON.parse(before));assert.equal(api.getState().settings.ps4Setup.step,'network');await api.command('ps4:setup',{agreement:true,step:'finish',complete:true,loginPrompt:true});const saved=JSON.parse(await fs.readFile(path.join(dir,'settings.bin'),'utf8'));assert.equal(saved.settings.setupComplete,true);assert.equal(saved.settings.ps4Setup.complete,true);assert.equal(saved.settings.ps4Setup.language,'en-GB');assert.equal(saved.settings.theme,'ps4-anniversary');
 }finally{await fs.rm(dir,{recursive:true,force:true});}
});
test('PS4 Quick Menu customization validates known unique categories',()=>{for(const value of [null,{},['sound','sound'],['psn'],Array(8).fill('power')])assert.throws(()=>preferences.quick({},value));assert.deepEqual(preferences.quick({theme:'ps4'},['music','power']),{theme:'ps4',ps4QuickItems:['music','power']});});
