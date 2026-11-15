// Series uses square application artwork. Keep the catalog's original box art intact.
// Steam distinguishes its 184x184 App Icon from the 600x900 Library Capsule:
// https://partner.steamgames.com/doc/store/assets/community
const fs=require('node:fs/promises'),path=require('node:path'),crypto=require('node:crypto');
const {searchSwitch}=require('./artwork.cjs');
const maxBytes=8*1024*1024;
const allowed=host=>/^cdn\d*\.epicgames\.com$/i.test(host)||host==='img-eshop.cdn.nintendo.net';
function square(size){return size.width>=64&&size.height>=64&&Math.max(size.width/size.height,size.height/size.width)<=1.08;}
function fingerprint(game){return crypto.createHash('sha256').update(game.art||'').digest('hex').slice(0,20);}
function decodeIco(bytes,nativeImage){
 if(bytes.length<6||bytes.readUInt16LE(0)!==0||bytes.readUInt16LE(2)!==1)throw Error('Invalid game icon');const entries=[],count=Math.min(bytes.readUInt16LE(4),256);
 for(let i=0;i<count;i++){const at=6+i*16;if(at+16>bytes.length)break;const width=bytes[at]||256,height=bytes[at+1]||256,size=bytes.readUInt32LE(at+8),offset=bytes.readUInt32LE(at+12);if(width>=64&&height>=64&&offset>=6&&offset+size<=bytes.length)entries.push({width,height,data:bytes.subarray(offset,offset+size)});}
 for(const entry of entries.sort((a,b)=>b.width*b.height-a.width*a.height)){const{data,width,height}=entry;if(data.subarray(0,8).equals(Buffer.from([137,80,78,71,13,10,26,10])))return nativeImage.createFromBuffer(data);
  if(data.length<40||data.readUInt32LE(0)!==40||data.readInt32LE(4)!==width||data.readInt32LE(8)!==height*2||data.readUInt16LE(14)!==32||data.readUInt32LE(16)!==0||data.length<40+width*height*4)continue;
  const output=Buffer.alloc(width*height*4);for(let y=0;y<height;y++)data.copy(output,y*width*4,40+(height-1-y)*width*4,40+(height-y)*width*4);
  let alpha=false;for(let at=3;at<output.length;at+=4)if(output[at]){alpha=true;break;}if(!alpha){const maskAt=40+width*height*4,stride=Math.ceil(width/32)*4;for(let y=0;y<height;y++)for(let x=0;x<width;x++){const mask=data[maskAt+(height-1-y)*stride+(x>>3)]||0;output[(y*width+x)*4+3]=mask&(128>>(x%8))?0:255;}}
  return nativeImage.createFromBitmap(output,{width,height,scaleFactor:1});
 }throw Error('No high resolution icon');
}
async function localCandidates(game,env){const result=[];
 if(game.provider==='Steam'){
  const id=game.launchUri?.match(/^steam:\/\/rungameid\/(\d+)$/)?.[1];
  if(id){const roots=[path.join(env['ProgramFiles(x86)']||'C:\\Program Files (x86)','Steam'),...(game.installDir?[path.resolve(game.installDir,'../../..')]:[])];
   const shortcuts=[env.APPDATA&&path.join(env.APPDATA,'Microsoft/Windows/Start Menu/Programs/Steam'),env.USERPROFILE&&path.join(env.USERPROFILE,'Desktop')].filter(Boolean);
   for(const directory of shortcuts){try{for(const entry of (await fs.readdir(directory,{withFileTypes:true})).slice(0,300)){if(!entry.isFile()||!entry.name.endsWith('.url'))continue;const shortcut=path.join(directory,entry.name);if((await fs.stat(shortcut)).size>16384)continue;const text=await fs.readFile(shortcut,'utf8');if(text.match(/^URL=(.+)$/m)?.[1].trim()!=='steam://rungameid/'+id)continue;const icon=text.match(/^IconFile=(.+)$/m)?.[1].trim();if(!icon||!icon.toLowerCase().endsWith('.ico'))continue;const resolved=path.resolve(icon);if(roots.some(root=>resolved.toLowerCase().startsWith(path.resolve(root,'steam/games').toLowerCase()+path.sep)))result.push({file:resolved,kind:'steam-icon',ico:true});}}catch{}}
   for(const root of new Set(roots)){const base=path.join(root,'appcache/librarycache'),folder=path.join(base,id),folders=[folder];try{for(const entry of (await fs.readdir(folder,{withFileTypes:true})).slice(0,100))if(entry.isDirectory()&&!entry.isSymbolicLink()&&/^[a-f0-9]+$/i.test(entry.name))folders.push(path.join(folder,entry.name));else if(entry.isFile()&&/^[a-f0-9]{40}\.(jpg|png)$/i.test(entry.name))result.push({file:path.join(folder,entry.name),kind:'steam-icon'});}catch{}
    for(const name of ['icon.jpg','icon.png','app_icon.jpg','app_icon.png','library_icon.jpg','library_icon.png']){result.push({file:path.join(base,id+'_'+name),kind:'steam-icon'});for(const sub of folders)result.push({file:path.join(sub,name),kind:'steam-icon'});}
   }
  }
 }
 return result;
}
async function epicCandidates(game,env){if(game.provider!=='Epic')return[];try{const file=path.join(env.ProgramData||'C:\\ProgramData','Epic/EpicGamesLauncher/Data/Catalog/catcache.bin');if((await fs.stat(file)).size>16*1024*1024)return[];const raw=await fs.readFile(file,'utf8'),rows=JSON.parse(raw.trim().startsWith('[')?raw:Buffer.from(raw,'base64').toString('utf8')),id=decodeURIComponent(game.launchUri?.match(/^com\.epicgames\.launcher:\/\/apps\/([^?]+)/)?.[1]||''),entry=rows.find(v=>(v.releaseInfo||[]).some(r=>r.appId===id));return(entry?.keyImages||[]).filter(v=>typeof v.url==='string'&&/^https:\/\/cdn\d*\.epicgames\.com\//i.test(v.url)).sort((a,b)=>Number(/square|thumbnail|dieselgamebox/i.test(b.type))-Number(/square|thumbnail|dieselgamebox/i.test(a.type))).slice(0,5).map(v=>({url:v.url,kind:'epic-square'}));}catch{return[];}}
function createSeriesArtwork({getState,save,nativeImage,getFileIcon,readImage=async()=>null,env=process.env,fetcher=fetch,switchCatalogPath=path.join(__dirname,'assets/switch_art.json'),allowDownload=()=>{}}){
 const pending=new Map();let switchCatalog;
 function validImage(image){if(!image||image.isEmpty())throw Error('Invalid artwork image');const size=image.getSize();if(!size.width||!size.height||size.width>8192||size.height>8192)throw Error('Artwork dimensions too large');return size;}
 function squareData(image){const size=validImage(image),side=Math.min(size.width,size.height),cropped=image.crop({x:Math.floor((size.width-side)/2),y:Math.floor((size.height-side)/2),width:side,height:side});return'data:image/png;base64,'+cropped.resize({width:512,height:512,quality:'best'}).toPNG().toString('base64');}
 async function load(candidate){if(candidate.file){if((await fs.stat(candidate.file)).size>maxBytes)throw Error('Artwork exceeds 8 MiB');const bytes=await fs.readFile(candidate.file);return candidate.ico?decodeIco(bytes,nativeImage):nativeImage.createFromBuffer(bytes);}const url=new URL(candidate.url);if(url.protocol!=='https:'||url.username||url.password||!allowed(url.hostname))throw Error('Unsupported Series artwork source');allowDownload();const response=await fetcher(url,{redirect:'error',signal:AbortSignal.timeout(12000)});if(!response.ok)throw Error('Artwork unavailable');if(Number(response.headers.get('content-length'))>maxBytes)throw Error('Artwork exceeds 8 MiB');let size=0;const chunks=[];for await(const chunk of response.body){size+=chunk.length;if(size>maxBytes)throw Error('Artwork exceeds 8 MiB');chunks.push(Buffer.from(chunk));}return nativeImage.createFromBuffer(Buffer.concat(chunks));}
 async function store(game,image,source){const value=squareData(image);game.seriesArt=value;game.seriesArtSource={...source,title:game.name,originalFingerprint:fingerprint(game)};await save();return{id:game.id,matched:true,seriesArt:value,seriesArtSource:game.seriesArtSource};}
 async function prepare(game,force){if(game.seriesArt&&!force&&(game.seriesArtSource?.kind!=='box-crop'||game.seriesArtSource.originalFingerprint===fingerprint(game)))return{id:game.id,matched:true,seriesArt:game.seriesArt,seriesArtSource:game.seriesArtSource};
  const candidates=await localCandidates(game,env);candidates.push(...await epicCandidates(game,env));
  if(['Switch','Nintendo Switch'].includes(game.system)){try{switchCatalog??=JSON.parse(await fs.readFile(switchCatalogPath,'utf8'));const exact=searchSwitch(switchCatalog,game.name).find(v=>v.exactId||v.exactTitle);if(exact)candidates.push({url:exact.url,kind:'switch-square',titleId:exact.matchedId||exact.titleId});}catch{}}
  for(const candidate of candidates){try{const image=await load(candidate);if(!square(validImage(image)))continue;return await store(game,image,{kind:candidate.kind,...(candidate.url?{url:candidate.url}:{}),...(candidate.titleId?{titleId:candidate.titleId}:{})});}catch{}}
  if(game.pc&&game.executable&&getFileIcon){try{await fs.access(game.executable);const image=await getFileIcon(game.executable);if(square(validImage(image)))return await store(game,image,{kind:'executable-icon'});}catch{}}
  if(game.art&&/^data:image\/(png|jpeg|webp|gif);base64,/i.test(game.art)&&game.art.length<12*1024*1024){try{const bytes=Buffer.from(game.art.split(',')[1],'base64'),image=nativeImage.createFromBuffer(bytes);return await store(game,image,{kind:'box-crop'});}catch{}}
  if(game.art){try{const bytes=await readImage(game.art);if(bytes)return await store(game,nativeImage.createFromBuffer(bytes),{kind:'box-crop'});}catch{}}
  return{id:game.id,matched:false};
 }
 async function command(name,args={}){if(name!=='artwork:series')throw Error('Unknown Series artwork action');const game=getState().games.find(g=>g.id===args.id);if(!game)throw Error('Game unavailable');if(pending.has(game.id))return pending.get(game.id);const task=prepare(game,!!args.force);pending.set(game.id,task);try{return await task;}finally{pending.delete(game.id);}}
 async function applyFile(id,file){const game=getState().games.find(g=>g.id===id);if(!game)throw Error('Game unavailable');return store(game,await load({file}),{kind:'custom'});}
 return{command,applyFile};
}
module.exports={createSeriesArtwork,square,localCandidates,epicCandidates,allowed,decodeIco};
