(function(root){const themes=['blades','nxe','metro','metro-revised','series','ps3-xmb','ps4','ps4-anniversary','ps5','series-anniversary25'],names=['Explorer','Pilot','Robot','Companion','Champion','Dreamer','Guardian','Voyager','Wild Spirit','Knight'];
const xs=[0,139,277,402,527,657,782,908,1033,1141,1254],ys=[0,130,265,393,521,657,790,914,1026,1135,1254];
const entries=themes.flatMap((theme,row)=>names.map((name,col)=>({id:theme+'-'+col,theme,name:name+' '+(row+1),x:xs[col]+4,y:ys[row]+4,width:xs[col+1]-xs[col]-8,height:ys[row+1]-ys[row]-8})));
const api={entries,themes,get:id=>entries.find(x=>x.id===id),forTheme:settings=>entries.filter(x=>x.theme===(settings.theme==='series'&&settings.seriesBackground==='anniversary25'?'series-anniversary25':settings.theme==='ps-vita'?'ps3-xmb':settings.theme==='switch'?'nxe':settings.theme))};if(typeof module!=='undefined'&&module.exports)module.exports=api;else root.AvatarCatalog=api;
})(globalThis);
