const fs=require('node:fs/promises'),path=require('node:path');
async function createStore(dir){
 await fs.mkdir(dir,{recursive:true});const file=path.join(dir,'database.json');let data={users:{},sessions:{},tracks:{},recordings:{}};
 try{data={...data,...JSON.parse(await fs.readFile(file,'utf8'))};}catch(e){if(e.code!=='ENOENT')throw Error('Multiplayer database cannot be read; refusing to overwrite it.');}
 let tail=Promise.resolve();
 return{data,save(){const snapshot=JSON.stringify(data);tail=tail.catch(()=>{}).then(async()=>{await fs.writeFile(file+'.tmp',snapshot,{mode:0o600});await fs.rename(file+'.tmp',file);});return tail;},async idle(){await tail;}};
}
module.exports={createStore};
