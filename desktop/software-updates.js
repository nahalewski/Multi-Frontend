/* Reuses the active theme's panel, focus and controller navigation. */
window.SoftwareUpdates=(()=>{
 let report=null,ticket=0,group='all';
 const labels={all:'Software updates',frontend:'Frontend updates',emulators:'Emulator updates',services:'Server updates'};
 function alive(id){return id===ticket&&!$('#panel').hidden&&$('#heading').textContent===labels[group];}
 async function open(force=false,which='all'){
  group=which;const id=++ticket;show(labels[group],'Checking published versions…',[['Back',()=>{ticket++;settings();}]],()=>{ticket++;settings();});
  try{const result=await call('updates:check',{refresh:force});if(!alive(id))return;report=result;render();}catch(e){if(alive(id))show(labels[group],e.message,[['Try again',()=>open(true,which)],['Back',settings]],settings);}
 }
 function render(){const rows=report.rows.filter(r=>group==='all'||r.group===group);show(labels[group],'Last checked: '+new Date(report.checkedAt).toLocaleString()+'\nFrontend, emulators and connected media servers.',[
  ['Check again',()=>open(true,group)],...(group==='all'?[['Frontend',()=>open(false,'frontend')],['Emulators & cores',()=>open(false,'emulators')],['RomM, Jellyfin & Plex',()=>open(false,'services')]]:[]),
  ...rows.map(r=>[r.name+' · '+r.status,()=>detail(r)]),...(group==='all'?[]:[['All software updates',()=>open()]])],()=>{ticket++;settings();});}
 function detail(r){ticket++;show(r.name,[r.status,'Installed / server: '+(r.current||'Not reported'),'Latest published: '+(r.latest||'Not reported'),r.note].filter(Boolean).join('\n'),[
  ['Refresh checks',()=>open(true,group)],...(r.url?[['Official releases / update instructions',()=>call('updates:release',{id:r.id})]]:[]),...(r.group==='emulators'?[['Emulator setup',()=>Onboarding.emulators()]]:[]),...(r.status==='Not connected'?[['Connect '+r.name,()=>configure(r.id)]]:[]),['Back',render]],render);}
 return{open};
})();
