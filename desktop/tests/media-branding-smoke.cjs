const {app,BrowserWindow,ipcMain}=require('electron'),fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
const root=path.join(__dirname,'..'),appRoot=process.argv.includes('--packaged')?path.join(root,'dist/win-unpacked/resources/app.asar'):root;
const state={profile:{name:'Player 1'},settings:{bootAnimation:false,theme:'blades',sound:false,setupComplete:true},services:{},games:[],emulators:{}},now=Math.floor(Date.now()/1800000)*1800000;
const guide={channels:Array.from({length:10},(_,i)=>({id:'c'+i,name:['US | World News','EN | Cinema','US | Sports'][i%3],logo:i===1?'https://logo.test/missing.png':i===2?'':'https://logo.test/channel.svg',group:'Entertainment',tags:['US']})),programmes:[],options:{hidden:[],favorites:[],favoritesOnly:false,preview:false,group:'All',shift:0},labels:{US:'United States'},configured:true,updated:Date.now()};
for(const ch of guide.channels)for(let i=0;i<8;i++)guide.programmes.push({channel:ch.id,start:now+i*1800000,end:now+(i+1)*1800000,title:['Live headlines','Weekend stories','The daily review'][i%3],description:'Programme information from the provider.'});
app.whenReady().then(async()=>{let win;try{
 ipcMain.handle('blades',async(e,name,args)=>{if(name==='theme:set')state.settings.theme=args.theme;return{ok:true,value:name==='layout'?JSON.parse(await fs.readFile(path.join(appRoot,'assets/layout.json'),'utf8')):name==='iptv:get'?guide:name==='iptv:stop'?true:state};});
 win=new BrowserWindow({show:false,width:1280,height:800,webPreferences:{offscreen:true,preload:path.join(appRoot,'preload.cjs'),sandbox:true,contextIsolation:true}});
 win.webContents.session.protocol.handle('https',request=>request.url.endsWith('missing.png')?new Response('',{status:404}):new Response('<svg xmlns="http://www.w3.org/2000/svg" width="80" height="50"><rect width="80" height="50" fill="blue"/></svg>',{headers:{'Content-Type':'image/svg+xml'}}));
 await win.loadFile(path.join(appRoot,'index.html'));await new Promise(r=>setTimeout(r,500));
 const styles=[];
 for(const theme of ['blades','nxe','metro']){
  await win.webContents.executeJavaScript(`state.settings.theme='${theme}';draw();NxeExperience.clear();mediaCenter();`);await new Promise(r=>setTimeout(r,250));
  assert.equal(await win.webContents.executeJavaScript("[...document.querySelectorAll('.service-row img')].filter(i=>i.complete&&i.naturalWidth>0).length"),4,'Four bundled brand logos load in '+theme);
  await fs.writeFile(path.join(root,'media-center-'+theme+'.png'),(await win.webContents.capturePage()).toPNG());
  await win.webContents.executeJavaScript('closePanel();BladesIptv.open()');await new Promise(r=>setTimeout(r,350));
  const result=await win.webContents.executeJavaScript(`(()=>{const channel=document.querySelector('.iptv-channel'),logos=[...document.querySelectorAll('.iptv-channel-logo')];BladesIptv.handle('down');BladesIptv.handle('right');const style=getComputedStyle(document.querySelector('.iptv-guide'));return{loaded:logos.some(p=>p.querySelector('img')?.naturalWidth>0),missingVisible:!logos[1].querySelector('span').hidden,absentVisible:!logos[2].querySelector('span').hidden,width:Math.round(document.querySelector('.iptv-channel').getBoundingClientRect().width/document.querySelector('.iptv-grid-row').getBoundingClientRect().width*100),background:style.backgroundImage,focus:!!document.querySelector('.iptv-programme.focused')};})()`);
  assert(result.loaded,'Channel logo loads');assert(result.missingVisible&&result.absentVisible,'Missing/broken logo fallback');assert.equal(result.width,23,'Channel column retains original width');assert(result.focus);styles.push(result.background);
  await fs.writeFile(path.join(root,'iptv-'+theme+'-theme.png'),(await win.webContents.capturePage()).toPNG());
  await win.webContents.executeJavaScript("BladesIptv.handle('x');if(document.querySelector('.iptv-dialog').hidden)throw Error('Settings missing');BladesIptv.handle('back');BladesIptv.close();");
 }
 assert.equal(new Set(styles).size,3,'All three guide themes must differ');console.log('PASS: four service logos in all themes, channel logos and failure fallback, unchanged Blades column sizing, three distinct guide skins, guide navigation/settings');
}catch(e){console.error(e);process.exitCode=1;}finally{win?.destroy();app.quit();}});
