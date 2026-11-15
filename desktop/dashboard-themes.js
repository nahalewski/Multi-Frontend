(function(root){
 const choices=Object.freeze([
  {id:'blades',name:'Blades (2005)'},
  {id:'nxe',name:'New Xbox Experience (2008)'},
  {id:'metro',name:'Metro (2011)'},
  {id:'metro-revised',name:'Metro Revised (2012)'},
  {id:'series',name:'Xbox Series X|S'},
  {id:'ps3-xmb',name:'PlayStation 3 · XMB'}
 ]);
 const api={choices,name:id=>choices.find(t=>t.id===id)?.name||choices[0].name,valid:id=>choices.some(t=>t.id===id)};
 if(typeof module!=='undefined'&&module.exports)module.exports=api;else root.DashboardThemes=api;
})(globalThis);
