(function(root){const consoles=[
 {id:'xbox360',brand:'Xbox',name:'Xbox 360',themes:['blades','nxe','metro','metro-revised']},{id:'xbox-series',brand:'Xbox',name:'Xbox Series X|S',themes:['series']},{id:'xbox-og',brand:'Xbox',name:'Original Xbox',themes:[]},
 {id:'ps3',brand:'PlayStation',name:'PlayStation 3',themes:['ps3-xmb']},{id:'ps4',brand:'PlayStation',name:'PlayStation 4',themes:[]},{id:'ps5',brand:'PlayStation',name:'PlayStation 5',themes:[]},{id:'psp',brand:'PlayStation',name:'PSP',themes:[]},{id:'vita',brand:'PlayStation',name:'PS Vita',themes:[]},
 {id:'wii',brand:'Nintendo',name:'Wii',themes:[]},{id:'wiiu',brand:'Nintendo',name:'Wii U',themes:[]},{id:'switch',brand:'Nintendo',name:'Nintendo Switch',themes:[]},
 {id:'steam',brand:'Steam',name:'SteamOS / Steam Deck',themes:[]},{id:'dreamcast',brand:'SEGA',name:'Dreamcast',themes:[]},{id:'atari',brand:'Atari',name:'Atari',themes:[]}
 ];const api={consoles,brands:[...new Set(consoles.map(c=>c.brand))],forTheme:theme=>consoles.find(c=>c.themes.includes(theme))};if(typeof module!=='undefined'&&module.exports)module.exports=api;else root.ConsoleCatalog=api;})(globalThis);
