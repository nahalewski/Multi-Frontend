const {app,BrowserWindow,ipcMain}=require('electron');
const fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
const root=path.join(__dirname,'..');
const fixture={profile:{name:'Player 1'},settings:{bootAnimation:false,sound:false,setupComplete:true},services:{},games:[{id:'test',name:'Test game',system:'GBA',art:'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect width="100" height="100" fill="green"/></svg>'}],emulators:{}};
app.whenReady().then(async()=>{let win;try{
 ipcMain.handle('blades',async(e,name,args)=>{if(name==='theme:set'){assert.ok(['nxe','blades'].includes(args.theme));fixture.settings.theme=args.theme;}return {ok:true,value:name==='layout'?JSON.parse(await fs.readFile(path.join(root,'assets/layout.json'),'utf8')):fixture};});
 win=new BrowserWindow({show:false,width:1280,height:720,webPreferences:{offscreen:true,preload:path.join(root,'preload.cjs'),sandbox:true,contextIsolation:true}});
 const errors=[];win.webContents.on('console-message',(_,level,message)=>{if(level===3)errors.push(message);});
 await win.loadFile(path.join(root,'index.html'));await new Promise(r=>setTimeout(r,700));
 const result=await win.webContents.executeJavaScript(`(async()=>{
  settings();const available=panelActions[0][0].includes('Dashboard theme');await panelActions[0][1]();await panelActions[1][1]();const switched=DesktopTheme.active()&&document.body.classList.contains('theme-nxe');closePanel();
  control('right');const cards=selected===1;control('down');const categories=blade===2;control('up');control('right');control('right');control('select');const gameLibrary=libraryMode&&games.length===1;control('back');const back=!libraryMode;
  settings();await DesktopTheme.set('blades');const restored=!DesktopTheme.active()&&!document.body.classList.contains('theme-nxe');await DesktopTheme.set('nxe');closePanel();blade=1;selected=0;draw();return {available,switched,cards,categories,gameLibrary,back,restored};})()`);
 for(const [key,value]of Object.entries(result))assert.equal(value,true,key);
 await new Promise(r=>setTimeout(r,200));
 await fs.writeFile(path.join(root,'nxe-preview.png'),(await win.webContents.capturePage()).toPNG());
 await win.reload();await new Promise(r=>setTimeout(r,500));assert.equal(await win.webContents.executeJavaScript('DesktopTheme.active()'),true,'theme restored on reload');
 win.setSize(800,1000);await new Promise(r=>setTimeout(r,150));assert.equal(await win.webContents.executeJavaScript('hit.every(h=>Number.isFinite(h.x)&&Number.isFinite(h.y))'),true);
 assert.deepEqual(errors,[]);console.log('PASS: theme selector, saved theme reload, card/category controls, game library, back, Blades restoration, portrait resize');
 }catch(e){console.error(e);process.exitCode=1;}finally{win?.destroy();app.quit();}});
