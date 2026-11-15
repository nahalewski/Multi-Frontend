/* Series dashboard Guide, adapted to features actually available in this frontend.
   Reference: Xbox Wire May 29, 2020 Guide revision (shared by Series X|S).
   https://news.xbox.com/en-us/2020/05/29/may-2020-xbox-one-update/ */
window.SeriesGuide=(()=>{
 let node,tab=0,index=0,actions=[],previousFocus;
 const tabs=[['Home','home'],['People','people'],['Parties & chats','chat'],['Game activity','activity'],['Capture & share','capture'],['Profile & system','profile']];
 const paths={home:'M3 10L12 3l9 7v11h-6v-7H9v7H3Z',people:'M16 21v-3a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v3M9 10a4 4 0 1 0 0-8a4 4 0 0 0 0 8M17 3a4 4 0 0 1 0 7M22 21v-3a4 4 0 0 0-3-3.87',chat:'M3 3h18v14H9l-6 4ZM7 8h10M7 12h7',activity:'M6 3h12v6a6 6 0 0 1-12 0ZM6 5H2v3a4 4 0 0 0 4 4M18 5h4v3a4 4 0 0 1-4 4M12 15v6M7 21h10',capture:'M3 6h5l2-3h4l2 3h5v15H3ZM16 13a4 4 0 1 0-8 0a4 4 0 0 0 8 0',profile:'M20 21a8 8 0 0 0-16 0M17 7A5 5 0 1 0 7 7a5 5 0 0 0 10 0'};
 function icon(name){const svg=document.createElementNS('http://www.w3.org/2000/svg','svg');svg.setAttribute('viewBox','0 0 24 24');svg.setAttribute('aria-hidden','true');const path=document.createElementNS(svg.namespaceURI,'path');path.setAttribute('d',paths[name]||paths.home);svg.append(path);return svg;}
 const active=()=>state.settings.theme==='series';
 const isOpen=()=>!!node&&!node.hidden;
 function cue(kind){window.NxeExperience?.cue(kind);}
 function close(){if(!node)return;node.hidden=true;previousFocus?.isConnected&&previousFocus.focus();}
 function clear(){close();}
 function home(){closePanel();libraryMode=false;window.SeriesTheme?.go?.();draw();}
 function unavailable(title,description){show(title,description,[['Back to guide',()=>open(tab)]],()=>open(tab));}
 function rows(){
  const loaded=state.games.find(g=>g.id===state.loaded),recent=[...state.games].filter(g=>g.played&&g.id!==loaded?.id).sort((a,b)=>b.played-a.played).slice(0,3);
  if(tab===0)return[
   ['Home',home,'Return to your dashboard'],['My games & apps',()=>library(),state.games.length+' games'],
   ...(loaded?[['Play '+loaded.name,()=>launch(loaded),loaded.system,loaded.art]]:[]),
   ['Active Downloads',()=>Downloads.open(),'Content and emulator core transfers'],
   ...recent.map(g=>[g.name,()=>launch(g),'Recently played · '+g.system,g.art]),
   ['Media Center',mediaCenter,'Music, video, live TV and connected libraries']
  ];
  if(tab===1)return[['Your gamer profile',profileSettings,state.profile.name],['RetroAchievements',achievements,'Your connected achievement profile'],['Xbox network friends',()=>unavailable('Xbox network friends','Xbox network friends, presence and invitations are not connected in this frontend.'),'Not connected']];
  if(tab===2)return[['Parties & chat',()=>unavailable('Parties & chats','Xbox network party voice chat, messages and invitations require Xbox services. This frontend does not provide those services.'),'Xbox network is not connected'],['Audio settings',()=>(window.SeriesSettings?SeriesSettings.audio():settings()),'Frontend menu sounds and volume']];
  if(tab===3)return[['Achievements',achievements,'RetroAchievements progress'],['Recently played',()=>library('Recently played'),'Your recent games'],['Favorites',()=>library('Favorites'),'Your saved collection'],...(loaded?[['Game options',()=>gameOptions(loaded),loaded.name]]:[]),['Controller settings',()=>DesktopControls.open(),'Detection, button mapping and dead zone']];
  if(tab===4)return[
   ['Capture screenshot',async()=>{await new Promise(r=>setTimeout(r,120));toast(await call('capture'));},'Save a screenshot from this app',null,true],
   [BladesRecording.active?'Stop recording':'Start recording',()=>BladesRecording.toggle(),'Record this app',null,true],
   ['Recent captures',()=>mediaList('pictures',true),'Screenshots captured in this app'],['Game clips',()=>BladesRecording.gallery(),'App video recordings'],
   ['TV recordings & DVR',()=>FrontendServices.storage(),'Xbox drive content and active DVR'],['DVR schedule',()=>FrontendServices.schedules(),'Scheduled TV recordings']
  ];
  return[['Your profile',profileSettings,state.profile.name],['Settings',settings,'Console preferences and connected services'],['Audio & music',()=>(window.SeriesSettings?SeriesSettings.audio():settings()),'Sound, volume and music'],['Customize Home',()=>SeriesTheme.menu(),'Background, anniversary theme and motion'],['Controller settings',()=>DesktopControls.open(),'Connected accessories'],['Backup & restore',backup,'Keep your settings and accounts'],['Power',()=>window.SeriesSettings?SeriesSettings.power():show('Power','Close this frontend. Your computer will stay on.',[['Return to dashboard',home],['Quit app',()=>call('quit')]],()=>open(5)),'App power options']];
 }
 function activate(action){close();cue('select');if(!action[4]){window.BladesIptv?.close();stop();closePanel();}run(action[1]);}
 function open(next=0){if(!active())return false;if(!isOpen())previousFocus=document.activeElement;tab=Math.max(0,Math.min(5,next));index=0;
  if(!node){node=document.createElement('section');node.id='seriesGuide';node.setAttribute('role','dialog');node.setAttribute('aria-modal','true');node.setAttribute('aria-label','Xbox Guide');node.addEventListener('click',e=>{if(e.target===node)close();});document.body.append(node);}
  node.replaceChildren();node.hidden=false;const pane=document.createElement('article'),rail=document.createElement('nav');rail.className='series-guide-tabs';rail.setAttribute('aria-label','Guide categories');
  tabs.forEach(([label,name],i)=>{const button=document.createElement('button');button.title=label;button.setAttribute('aria-label',label);button.setAttribute('aria-pressed',String(i===tab));button.classList.toggle('current',i===tab);if(i===5&&state.profile.image){const img=document.createElement('img');img.src=state.profile.image;img.alt='';button.append(img);}else button.append(icon(name));button.onclick=()=>open(i);rail.append(button);});
  const heading=document.createElement('h1');heading.textContent=tabs[tab][0];const meta=document.createElement('p');meta.className='series-guide-meta';meta.textContent=state.profile.name+' · '+new Date().toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'});
  const list=document.createElement('div');list.className='series-guide-actions';actions=rows();actions.forEach((action,i)=>{const button=document.createElement('button'),copy=document.createElement('span'),title=document.createElement('strong'),detail=document.createElement('small');title.textContent=action[0];detail.textContent=action[2]||'';copy.append(title,detail);if(action[3]){const img=document.createElement('img');img.src=action[3];img.alt='';button.append(img);}button.append(copy);button.onfocus=()=>index=i;button.onclick=()=>activate(action);list.append(button);});
  const footer=document.createElement('footer');footer.textContent='A Select   B Close   LB / RB Tabs';pane.append(rail,heading,meta,list,footer);node.append(pane);list.firstChild?.focus();cue('tab');return true;
 }
 function handle(key){if(!isOpen())return false;if(!active()){close();return false;}if(key==='back'||key==='guide'){close();cue('back');}else if(['left','lb','right','rb'].includes(key))open((tab+(['left','lb'].includes(key)?5:1))%6);else if(key==='up'||key==='down'){index=Math.max(0,Math.min(actions.length-1,index+(key==='up'?-1:1)));node.querySelectorAll('.series-guide-actions button')[index]?.focus();cue('move');}else if(key==='select')node.querySelectorAll('.series-guide-actions button')[index]?.click();return true;}
 return{open,handle,close,clear,isOpen};
})();
