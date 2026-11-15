// Read-only account check: fetch listings, never tune a stream or write settings.
const {app,BrowserWindow,ipcMain,safeStorage}=require('electron'),fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
app.setName('blades360-desktop');
app.whenReady().then(async()=>{let win;try{
 const root=path.join(__dirname,'..'),state=JSON.parse(safeStorage.decryptString(await fs.readFile(path.join(app.getPath('userData'),'settings.bin'))));
 state.iptvCache.programmes=[];state.iptvOptions={hidden:[],favorites:[],favoritesOnly:false,preview:false,group:'All',shift:0};
 const service=require('../iptv.cjs').createIptv({getState:()=>state,save:async()=>{}});
 ipcMain.handle('blades',async(e,name,args)=>{assert(['iptv:get','iptv:guide','iptv:stop'].includes(name),'Guide must not tune streams');try{const value=await service.command(name,args);if(name==='iptv:get')console.log('Guide enabled: '+value.onDemand);if(name==='iptv:guide')console.log('Guide rows: '+value.length);return{ok:true,value};}catch{console.log('Guide request failed');return{ok:false,error:'Guide request failed'};}});
 win=new BrowserWindow({show:false,width:1280,height:800,webPreferences:{offscreen:true,sandbox:true,contextIsolation:true,preload:path.join(root,'preload.cjs')}});
 await win.loadFile(path.join(__dirname,'iptv-fixture.html'));await new Promise(r=>setTimeout(r,400));
 await win.webContents.executeJavaScript(`BladesIptv.close();window.call=async(name,args)=>{const r=await window.blades.call(name,args);if(!r.ok)throw Error(r.error);return r.value;};BladesIptv.open();`);
 let populated=0;for(let i=0;i<120;i++){await new Promise(r=>setTimeout(r,750));populated=await win.webContents.executeJavaScript("[...document.querySelectorAll('.iptv-grid-row')].filter(row=>row.querySelector('.iptv-programme:not(.no-guide)')).length");if(populated>=5)break;}
 assert(populated>=5,'At least five visible real channels must have current guide cells');
 await new Promise(r=>setTimeout(r,2500));
 const logos=await win.webContents.executeJavaScript("[...document.querySelectorAll('.iptv-channel-logo img')].filter(i=>i.complete&&i.naturalWidth>0).length");
 assert(logos>0,'At least one real channel logo must load');
 for(const theme of ['blades','nxe','metro']){await win.webContents.executeJavaScript(`document.body.className='${theme==='blades'?'':theme==='nxe'?'theme-nxe':'theme-nxe theme-metro'}'`);await new Promise(r=>setTimeout(r,150));await fs.writeFile(path.join(root,'iptv-guide-live-'+theme+'.png'),(await win.webContents.capturePage()).toPNG());}
 console.log('Real provider channel logos loaded: '+logos);await win.webContents.executeJavaScript('BladesIptv.close()');
 console.log('PASS: '+populated+' real channels display current programme cells from an empty guide cache, no streams opened');
}catch(e){console.error('FAIL: '+e.message);process.exitCode=1;}finally{win?.destroy();app.quit();}});
