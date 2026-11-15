const{app,BrowserWindow}=require('electron'),fs=require('node:fs/promises'),path=require('node:path'),assert=require('node:assert/strict');
app.disableHardwareAcceleration();
app.whenReady().then(async()=>{let win;try{
 const root=path.join(__dirname,'..');win=new BrowserWindow({show:false,width:1280,height:720,webPreferences:{offscreen:true,sandbox:true}});
 await win.loadURL('data:text/html,<html><body><button id="other">Elsewhere</button></body></html>');
 const js=source=>win.webContents.executeJavaScript(source),wait=ms=>new Promise(resolve=>setTimeout(resolve,ms));
 await win.webContents.insertCSS(await fs.readFile(path.join(root,'window-controls.css'),'utf8'));
 await js("window.calls=[];window.blades={call:(name,args)=>calls.push({name,...args})};window.NxeExperience={open:()=>calls.push({action:'guide'})};window.state={settings:{deadzone:.45}};window.dashboardKeys=0;document.addEventListener('keydown',()=>dashboardKeys++);");
 for(const name of ['window-controls.js','controller-renderer.js'])await js(await fs.readFile(path.join(root,name),'utf8')+';void 0');
 const hidden=()=>js("document.querySelector('#windowControls').classList.contains('window-controls-hidden')");
 assert.equal(await hidden(),false);await wait(2100);assert.equal(await hidden(),true,'Idle chrome fades away');
 await js("document.dispatchEvent(new PointerEvent('pointermove',{pointerType:'mouse',clientX:1270,clientY:12}));");await wait(260);assert.equal(await js("getComputedStyle(document.querySelector('#windowControls')).opacity"),'1');
 await js("window.pad={id:'Xbox test',buttons:Array.from({length:17},()=>({pressed:false})),axes:[0,0]};pad.buttons[0].pressed=true;DesktopControls.pressed(pad);");assert.equal(await hidden(),true,'Real controller mapping path hides chrome');
 await js("document.dispatchEvent(new PointerEvent('pointermove',{pointerType:'mouse',clientX:500,clientY:400}));");assert.equal(await hidden(),true,'Moving away does not reveal chrome');
 await js("document.dispatchEvent(new PointerEvent('pointermove',{pointerType:'mouse',clientX:1270,clientY:12}));");assert.equal(await hidden(),false,'Pointer returns after controller use');
 await js("document.dispatchEvent(new PointerEvent('pointermove',{pointerType:'mouse',clientX:500,clientY:400}));");await wait(950);assert.equal(await hidden(),true,'Leaving hotspot fades chrome');
 await js("document.querySelector('#windowControls button').focus();document.querySelector('#windowControls button').dispatchEvent(new FocusEvent('focusin',{bubbles:true}));");assert.equal(await hidden(),false,'Keyboard focus reveals controls');await wait(2100);assert.equal(await hidden(),false,'Focused chrome remains accessible');
 await js("document.querySelector('#windowControls button').dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',bubbles:true}));");assert.equal(await js('dashboardKeys'),0,'Window keyboard actions do not also activate dashboard');
 await js("for(const button of document.querySelectorAll('#windowControls button'))button.click();");assert.deepEqual(await js('calls.map(c=>c.action)'),['guide','minimize','maximize','close']);
 await js("pad.buttons[0].pressed=false;pad.axes[0]=.9;DesktopControls.pressed(pad);");assert.equal(await hidden(),true,'Analog activity hides focused controls');assert.equal(await js("document.querySelector('#windowControls').contains(document.activeElement)"),false);
 await js("document.dispatchEvent(new PointerEvent('pointerdown',{pointerType:'mouse',clientX:1270,clientY:12}));document.querySelector('#windowControls button').focus();document.querySelector('#windowControls button').dispatchEvent(new FocusEvent('focusin',{bubbles:true}));document.dispatchEvent(new PointerEvent('pointermove',{pointerType:'mouse',clientX:500,clientY:400}));");
 for(let i=0;i<3;i++){await wait(300);await js("document.dispatchEvent(new PointerEvent('pointermove',{pointerType:'mouse',clientX:500,clientY:400}));");}
 assert.equal(await hidden(),true,'Pointer click focus and mouse motion elsewhere cannot keep chrome visible');
 await js("window.Ps4Theme={active:()=>true};pad.mapping='standard';pad.axes=[0,0];pad.buttons[9].pressed=true;");assert.equal(await js('DesktopControls.pressed(pad).x'),true,'PS4 maps standard Options/Menu to options');await js('pad.buttons[8].pressed=true');assert.deepEqual(await js('(()=>{const mapped=DesktopControls.pressed(pad);return [mapped.guide,mapped.x];})()'),[true,false],'Guide chord takes precedence over Options');
 console.log('PASS: idle fade, pointer hotspot reveal, controller button/stick hiding, keyboard accessibility and all window actions');
 }catch(error){console.error('FAIL:',error);process.exitCode=1;}finally{win?.destroy();app.quit();}});
