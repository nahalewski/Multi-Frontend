const{app,BrowserWindow,ipcMain}=require('electron'),fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
const root=path.join(__dirname,'..'),base=process.argv.includes('--packaged')?path.join(root,'dist-0.17.0/win-unpacked/resources/app.asar'):root;app.disableHardwareAcceleration();
app.whenReady().then(async()=>{let win;const errors=[];try{
 const art='data:image/svg+xml,'+encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512"><rect width="512" height="512" fill="#1685bd"/><circle cx="300" cy="170" r="100" fill="#4ce0df"/><path d="M0 512L150 210 380 512" fill="#10336c"/></svg>');
 const fixture={profile:{name:'Player One'},settings:{theme:'ps-vita',setupComplete:true,vitaSetupComplete:true,bootAnimation:false,sound:false,nxeMotion:false},games:Array.from({length:17},(_,i)=>({id:String(i),name:'Adventure '+(i+1),system:'GBA',path:'demo'+i+'.gba',art,played:100-i})),emulators:{},services:{},profiles:[{id:'one',name:'Player One'}],activeProfile:'one'};
 ipcMain.handle('blades',async(e,name,args)=>{if(name==='theme:set')fixture.settings.theme=args.theme;if(name==='setting')fixture.settings[args.key]=args.value;if(name==='theme:options')Object.assign(fixture.settings,args);return{ok:true,value:name==='layout'?JSON.parse(await fs.readFile(path.join(base,'assets/layout.json'),'utf8')):name==='media:catalog'?[]:name==='emulators:catalog'?{entries:[],jobs:[],autoInstall:true}:name==='firmware:status'?{folder:'C:\\BIOS',files:[],installed:[],issues:[]}:name==='romm-saves:status'?{enabled:true,jobs:[],configured:false}:fixture};});
 win=new BrowserWindow({show:false,width:1280,height:720,webPreferences:{offscreen:true,preload:path.join(base,'preload.cjs'),sandbox:true,contextIsolation:true}});
 win.webContents.on('console-message',(_e,level,message)=>{if(level>=3&&!/ERR_|Failed to load resource/.test(message))errors.push(message);});
 await win.loadFile(path.join(base,'index.html'));const js=s=>win.webContents.executeJavaScript(s),wait=ms=>new Promise(r=>setTimeout(r,ms)),shot=async name=>{await wait(200);await fs.writeFile(path.join(root,name),(await win.webContents.capturePage()).toPNG());};await wait(500);
 assert.equal(await js('VitaTheme.active()'),true);assert.equal(await js("document.body.classList.contains('theme-ps-vita')"),true);
 await js('NxeExperience.clear();VitaTheme.home();draw()');await shot('vita-home-preview.png');
 await js("control('select')");assert.equal(await js('VitaTheme.inspect().mode'),'live');await shot('vita-livearea-preview.png');
 await js("control('select')");await wait(50);assert.equal(await js('libraryMode'),true);assert.equal(await js('VitaTheme.inspect().mode'),'home');
 await js("VitaTheme.home();control('rb')");assert.equal(await js('VitaTheme.inspect().page'),1);await js("control('lb')");assert.equal(await js('VitaTheme.inspect().page'),0);
 await js("control('back');library();control('right');control('down')");assert.equal(await js('libraryMode'),true);await shot('vita-library-preview.png');
 await js("control('back');settings()");assert.equal(await js("document.querySelector('#panel').hidden"),false);await shot('vita-settings-preview.png');
 await js("closePanel();VitaSystem.openGuide()");await shot('vita-quickmenu-preview.png');
 await js("document.querySelector('input[aria-label=\"Display brightness\"]').value='65';document.querySelector('input[aria-label=\"Display brightness\"]').dispatchEvent(new Event('change'))");await wait(40);assert.equal(fixture.settings.vitaBrightness,.65);
 await js("control('back')");assert.equal(await js('VitaSystem.active()'),false);
 await js("BladesKeyboardUI.open('Search',[['Search','']],()=>{},closePanel)");assert.match(await js("document.querySelector('.blades-keyboard-help').textContent"),/×/);await shot('vita-keyboard-preview.png');await js('BladesKeyboardUI.cancel();closePanel()');
 await js("AchievementPopup.show({preview:true,title:'First adventure',points:10,game:'Demo'})");await shot('vita-notification-preview.png');await js('AchievementPopup.clear()');
 await js('VitaSystem.openSetup()');await shot('vita-setup-preview.png');
 await js("VitaSystem.closeGuide(false);VitaAudio.play('select',0);VitaAudio.boot(0);VitaAudio.stopBoot();ThemeBoot.renderAt(canvas,'ps-vita',3300,false)");await shot('vita-boot-preview.png');
 for(const size of [[1920,1080],[900,1200]]){win.setSize(...size);await wait(60);await js('VitaSystem.closeGuide(false);VitaTheme.home();draw()');assert.equal(await js('hit.length>0'),true);await shot('vita-'+size.join('x')+'-preview.png');}
 await js("state.settings.theme='ps5';draw()");assert.equal(await js("document.body.classList.contains('theme-ps-vita')"),false);assert.equal(await js("document.body.classList.contains('theme-ps5')"),true);
 assert.deepEqual(errors,[]);console.log('PASS: Vita home, LiveArea, library, settings, quick menu, keyboard, notifications, setup and theme switching');
 }catch(e){console.error('FAIL:',e);process.exitCode=1;}finally{win?.destroy();app.quit();}});
