// Read-only: use saved playlist metadata, no stream or EPG requests.
const {app,safeStorage,BrowserWindow,ipcMain}=require('electron'),fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');app.setName('blades360-desktop');
app.whenReady().then(async()=>{let win;try{
 const root=path.join(__dirname,'..'),raw=JSON.parse(safeStorage.decryptString(await fs.readFile(path.join(app.getPath('userData'),'settings.bin'))));
 const guide={...raw.iptvCache,channels:[...raw.iptvCache.channels].sort((a,b)=>Number(!!b.tvg)-Number(!!a.tvg)),onDemand:false,configured:true,labels:{US:'United States'},options:{hidden:[],favorites:[],favoritesOnly:false,preview:false,group:'All',shift:0}};
 ipcMain.handle('blades',async(e,name)=>{assert(['iptv:get','iptv:stop'].includes(name),'No stream/guide requests in logo test');return{ok:true,value:name==='iptv:get'?guide:true};});
 win=new BrowserWindow({show:false,width:1280,height:800,webPreferences:{offscreen:true,sandbox:true,contextIsolation:true,preload:path.join(root,'preload.cjs')}});
 await win.loadFile(path.join(__dirname,'iptv-fixture.html'));await new Promise(r=>setTimeout(r,500));await win.webContents.executeJavaScript(`BladesIptv.close();window.call=async(name,args)=>{const r=await window.blades.call(name,args);if(!r.ok)throw Error(r.error);return r.value;};BladesIptv.open()`);
 let loaded=0;for(let i=0;i<30;i++){await new Promise(r=>setTimeout(r,500));loaded=await win.webContents.executeJavaScript("[...document.querySelectorAll('.iptv-channel-logo img')].filter(i=>i.complete&&i.naturalWidth>0).length");if(loaded>=5)break;}assert(loaded>0,'Actual channel logo must decode');
 for(const theme of ['blades','nxe','metro']){await win.webContents.executeJavaScript(`document.body.className='${theme==='blades'?'':theme==='nxe'?'theme-nxe':'theme-nxe theme-metro'}'`);await new Promise(r=>setTimeout(r,150));await fs.writeFile(path.join(root,'iptv-real-logos-'+theme+'.png'),(await win.webContents.capturePage()).toPNG());}
 console.log('PASS: '+loaded+' real channel logos decoded; three theme previews saved; no live stream or EPG requests');
}catch(e){console.error('FAIL: '+e.message);process.exitCode=1;}finally{win?.destroy();app.quit();}});
