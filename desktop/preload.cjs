const {contextBridge,ipcRenderer}=require('electron');
contextBridge.exposeInMainWorld('blades',{onExternalSession:callback=>ipcRenderer.on('external-session',(_event,value)=>callback(value)),call:(name,args)=>ipcRenderer.invoke('blades',name,args),onLibraryChanged:callback=>ipcRenderer.on('library-changed',()=>callback())});
