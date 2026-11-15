const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs/promises'),os=require('node:os'),path=require('node:path'),crypto=require('node:crypto'),{once}=require('node:events'),{WebSocket}=require('ws');
const {createServer}=require('../server.cjs');
test('timed lyrics parse offsets, multiple stamps and metadata',()=>{const lines=require('../lyrics.cjs').parseLrc('[ar:Artist]\n[offset:100]\n[00:01.50][00:03.00]Hello\n[00:05]World');assert.equal(lines.length,3);assert.equal(lines[0].text,'Hello');assert.ok(Math.abs(lines[0].time-1.6)<.001);});
test('accounts, private tracks, duet consent, relay, recording ownership and restart persistence',async()=>{
 const root=await fs.mkdtemp(path.join(os.tmpdir(),'blades-duet-test-'));let app;const peers=[];try{
 const jobs={folder:id=>path.join(root,'tracks',id),inspect:async()=>({title:'Fixture',duration:8,artist:''}),prepare:async()=>{},mix:async room=>{const id=crypto.randomUUID();app.store.data.recordings[id]={id,title:'Test duet',owners:Object.keys(room.members),singers:Object.values(room.members).map(m=>m.name),created:Date.now()};await app.store.save();return id;}};
 app=await createServer({root,jobs});const addr=await app.listen(0),base='http://127.0.0.1:'+addr.port;
 async function request(route,token,body,method=body===undefined?'GET':'POST',headers={}){const r=await fetch(base+route,{method,headers:{...(token?{Authorization:'Bearer '+token}:{}),...(body&&!Buffer.isBuffer(body)?{'Content-Type':'application/json'}:{}),...headers},body:body===undefined?undefined:Buffer.isBuffer(body)?body:JSON.stringify(body)});return{status:r.status,...await r.json()};}
 const a=await request('/v1/auth/guest',null,{name:'Singer A'}),b=await request('/v1/auth/guest',null,{name:'Singer B'}),c=await request('/v1/auth/guest',null,{name:'Outsider'});assert.equal(a.status,201);
 const uploaded=await request('/v1/tracks',a.token,Buffer.alloc(500,1),'POST',{'X-Filename':'test.wav'});assert.equal(uploaded.status,201);const trackId=uploaded.track.id;app.store.data.tracks[trackId].status='ready';
 assert.equal((await request('/v1/tracks/'+trackId,c.token)).status,404);
 const created=await request('/v1/rooms',a.token,{trackId}),code=created.room.code;
 assert.equal((await request('/v1/rooms/'+code+'/start',a.token,{})).status,400);
 assert.equal((await request('/v1/rooms/'+code+'/take',a.token,Buffer.alloc(100))).status,400);
 assert.equal((await request('/v1/rooms/'+code+'/join',b.token,{})).status,200);
 assert.equal((await request('/v1/rooms/'+code+'/join',c.token,{})).status,400);
 assert.equal((await request('/v1/tracks/'+trackId,b.token)).status,200);
 await request('/v1/rooms/'+code+'/ready',a.token,{ready:true});await request('/v1/rooms/'+code+'/ready',b.token,{ready:true});
 assert.equal((await request('/v1/rooms/'+code+'/start',b.token,{})).status,400);
 assert.equal((await request('/v1/rooms/'+code+'/start',a.token,{})).status,200);
 async function connect(token){const ws=new WebSocket(base.replace('http','ws')+'/v1/live');peers.push(ws);await once(ws,'open');const ready=once(ws,'message');ws.send(JSON.stringify({type:'auth',token,room:code}));await ready;return ws;}
 const sa=await connect(a.token),sb=await connect(b.token);const relayed=new Promise((resolve,reject)=>{const timer=setTimeout(()=>reject(Error('Relay timed out')),2000);sb.on('message',(data,binary)=>{if(binary){clearTimeout(timer);resolve(data);}});});sa.send(Buffer.alloc(640,42));assert.equal((await relayed).length,640);
 app.rooms.get(code).startAt=Date.now()-1000;await request('/v1/rooms/'+code+'/stop',a.token,{});await request('/v1/rooms/'+code+'/take',a.token,Buffer.alloc(100));await request('/v1/rooms/'+code+'/take',b.token,Buffer.alloc(100));await new Promise(r=>setTimeout(r,50));
 assert.equal((await request('/v1/recordings',a.token)).recordings.length,1);assert.equal((await request('/v1/recordings',b.token)).recordings.length,1);assert.equal((await request('/v1/recordings',c.token)).recordings.length,0);
 const registration=await request('/v1/auth/register',a.token,{username:'fixture_user',password:'fixture-password-123'});assert.equal(registration.user.id,a.user.id);assert.equal(registration.user.guest,false);assert.equal((await request('/v1/recordings',registration.token)).recordings.length,1);
 assert.equal((await request('/v1/auth/login',null,{username:'fixture_user',password:'incorrect'})).status,400);
 const signed=await request('/v1/auth/login',null,{username:'FIXTURE_USER',password:'fixture-password-123'});assert.equal(signed.user.id,a.user.id);
 await request('/v1/auth/logout',signed.token,{});assert.equal((await request('/v1/me',signed.token)).status,401);
 assert.equal((await fetch(base+'/health',{headers:{Origin:'https://untrusted.example'}})).status,403);
 for(const ws of peers)ws.terminate();await app.close();app=null;const stored=await require('../store.cjs').createStore(root);assert.equal(Object.keys(stored.data.recordings).length,1);assert.ok(!JSON.stringify(stored.data).includes('fixture-password-123'));
 }finally{for(const ws of peers)ws.terminate();if(app)await app.close();assert.ok(root.startsWith(path.join(os.tmpdir(),'blades-duet-test-')));await fs.rm(root,{recursive:true,force:true});}
});
