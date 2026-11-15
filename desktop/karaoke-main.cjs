const fs=require('node:fs/promises'),path=require('node:path');
const ORIGIN='https://mtp.orosapp.us';
function createKaraoke({getState,save,fetcher=fetch,origin=ORIGIN}){
 function local(){const state=getState();require('./local-profiles.cjs').ensure(state);return state.activeProfile;}
 function saved(){return getState().onlineSessions?.[local()];}
 async function request(route,{method='GET',body,binary=false,headers={},token=saved()?.token}={}){const response=await fetcher(origin+'/v1/'+route,{method,headers:{...(token?{Authorization:'Bearer '+token}:{}),...(body&&!binary?{'Content-Type':'application/json'}:{}),...headers},body:body===undefined?undefined:binary?body:JSON.stringify(body),signal:AbortSignal.timeout(binary?120000:30000),redirect:'error'});const result=await response.json().catch(()=>({}));if(!response.ok)throw Error(result.error||'Multiplayer server unavailable ('+response.status+').');return result;}
 async function store(session,id=local()){getState().onlineSessions??={};getState().onlineSessions[id]={token:session.token,user:session.user};await save();return session;}
 async function ensure(){const id=local(),current=saved();if(current?.token){try{const result=await request('me');return await store({...current,user:result.user},id);}catch(e){if(!/session|unauthor|sign in|401/i.test(e.message))throw e;}}return store(await request('auth/guest',{method:'POST',body:{name:getState().profile.name},token:''}),id);}
 async function command(name,args={}){
  if(name==='status'){const health=await fetcher(origin+'/health',{signal:AbortSignal.timeout(10000)}).then(r=>{if(!r.ok)throw Error('Multiplayer server unavailable');return r.json();});return{online:true,origin,user:saved()?.user||null,features:health.capabilities};}
  if(name==='session'){await ensure();return{origin,user:saved().user};}
  if(name==='adopt'){if(!/^[A-Za-z0-9_-]{43}$/.test(String(args.token)))throw Error('Invalid session');const id=local(),result=await request('me',{token:args.token});await store({token:args.token,user:result.user},id);return result.user;}
  if(name==='register'||name==='login'){await ensure();const id=local(),result=await request('auth/'+name,{method:'POST',body:{username:args.username,password:args.password,name:getState().profile.name}});await store(result,id);return result.user;}
  if(name==='logout'){await request('auth/logout',{method:'POST',body:{}});delete getState().onlineSessions[local()];await save();return true;}
  if(name==='open'){const session=await ensure(),params=new URLSearchParams({embedded:'1',theme:getState().settings.theme||'blades',token:session.token});if(['join','recordings','profile','community'].includes(args.view))params.set('view',args.view);if(args.track&&/^[a-f0-9-]{36}$/i.test(args.track))params.set('track',args.track);return{url:origin+'/#'+params,origin};}
  if(name==='upload'){await ensure();const root=getState().settings.folder_music;if(!root)throw Error('Choose your music folder first.');const base=await fs.realpath(root),file=await fs.realpath(String(args.path||'')),relative=path.relative(base,file);if(!relative||relative.startsWith('..')||path.isAbsolute(relative)||!['.mp3','.wav','.ogg','.flac','.m4a','.aac','.opus'].includes(path.extname(file).toLowerCase()))throw Error('Choose a music file inside your selected music folder.');const info=await fs.stat(file);if(!info.isFile()||info.size>80*1024*1024)throw Error('Choose a music file smaller than 80 MB.');const result=await request('tracks',{method:'POST',binary:true,body:await fs.readFile(file),headers:{'X-Filename':encodeURIComponent(path.basename(file))}});const lrc=file.slice(0,-path.extname(file).length)+'.lrc';try{if((await fs.stat(lrc)).size<1024*1024)await request('tracks/'+result.track.id+'/lyrics',{method:'POST',body:{lrc:await fs.readFile(lrc,'utf8')}});}catch(e){if(e.code!=='ENOENT')throw e;}await request('tracks/'+result.track.id+'/prepare',{method:'POST',body:{}});return{track:result.track.id};}
  throw Error('Unknown karaoke action');
 }
 return{command};
}
module.exports={createKaraoke,ORIGIN};
