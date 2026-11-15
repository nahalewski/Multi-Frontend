const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs/promises'),os=require('node:os'),path=require('node:path');
const {createUpdates,compare,status,releaseUrl}=require('../software-updates.cjs');
test('release ordering handles four-part Plex builds, prereleases and unknown rolling tags',()=>{
 assert.equal(compare('v2.10.0','2.9.9'),1);assert.equal(compare('1.40.2.8395-abc123','1.40.2.8400-def123'),-1);
 assert.equal(compare('2.0.0-rc.2','2.0.0'),-1);assert.equal(compare('2.0.0-rc.10','2.0.0-rc.2'),1);
 assert.equal(status('0.18.1','v0.12.0'),'Ahead of published release');assert.equal(status('latest','latest'),'Version unknown');assert.equal(status('','1.0.0'),'Version unknown');
});
test('checks real server version shapes, isolates credentials, caches, and reports installed emulator updates',async()=>{
 const dir=await fs.mkdtemp(path.join(os.tmpdir(),'update-test-'));const exe=path.join(dir,'mGBA.exe');await fs.writeFile(exe,'test');
 const state={settings:{},emulators:{GBA:{path:exe}},services:{romm:{server:'https://romm.local/sub',token:'private-romm'},jellyfin:{server:'https://jelly.local'},plex:{server:'https://plex.local',token:'private-plex'}}};let calls=0;
 const fetcher=async(url,options)=>{calls++;assert.equal(options.redirect,'error');let value;
 if(url.includes('romm.local')){assert.equal(options.headers.Authorization,'Bearer private-romm');value={SYSTEM:{VERSION:'4.0.0'}};}
 else if(url.includes('jelly.local'))value={Version:'10.10.0'};
 else if(url.includes('plex.local')){assert.equal(options.headers['X-Plex-Token'],'private-plex');value={MediaContainer:{version:'1.40.1.1234-abcd',platform:'Linux'}};}
 else {assert.equal(options.headers.Authorization,undefined);assert.equal(options.headers['X-Plex-Token'],undefined);value=url.includes('plex.tv')?{computer:{linux:{name:'Linux',version:'1.40.2.1300-abcd'},windows:{name:'Windows',version:'9.0.0'}}}:{tag_name:url.includes('mgba-emu')?'0.10.5':url.includes('rommapp')?'4.1.0':url.includes('jellyfin')?'10.11.0':'0.12.0'};}
 return{ok:true,text:async()=>JSON.stringify(value)};};
 try{const api=createUpdates({getState:()=>state,appVersion:'0.18.1',userData:dir,fetcher,readVersion:async()=> '0.10.4'});const result=await api.check();
 for(const id of ['romm','jellyfin','plex','mgba'])assert.equal(result.rows.find(r=>r.id===id).status,'Update available',id);
 assert.equal(result.rows.find(r=>r.id==='plex').latest,'1.40.2.1300-abcd');assert.equal(result.rows.find(r=>r.id==='dolphin').status,'Not installed');
 assert.ok(!JSON.stringify(result).includes('private-'));const count=calls;assert.equal(await api.check(),result);assert.equal(calls,count);await api.check(true);assert.ok(calls>count);
 state.services.romm.server='https://other.local';await api.check();assert.ok(calls>count*2);
 }finally{await fs.rm(dir,{recursive:true,force:true});}
});
test('offline services do not become up-to-date; simultaneous refreshes share requests',async()=>{
 let calls=0;const api=createUpdates({getState:()=>({settings:{},services:{romm:{server:'https://romm.local'}}}),appVersion:'1.0.0',userData:os.tmpdir(),fetcher:async()=>{calls++;throw Error('secret URL/token must not escape');}});
 const [a,b]=await Promise.all([api.check(true),api.check(true)]);assert.equal(a,b);assert.equal(calls,2);assert.equal(a.rows.find(r=>r.id==='romm').status,'Server unavailable');assert.equal(a.rows[0].status,'Check failed');assert.ok(!JSON.stringify(a).includes('secret'));assert.equal(a.rows.find(r=>r.id==='plex').status,'Not connected');
});
test('release actions have a fixed allowlist without OS updater routes',()=>{
 for(const id of ['windows','store','winget','https://example.com'])assert.equal(releaseUrl(id),null);
 assert.match(releaseUrl('frontend'),/nahalewski\/Multi-Frontend/);assert.match(releaseUrl('eden'),/^https:\/\/git.eden-emu.dev\//);
});
