(function(root,factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;else root.BladesTimeline=api;})(globalThis,function(){
 class Timeline{
  constructor(maxBytes=64*1024*1024,maxEntries=120){this.maxBytes=maxBytes;this.maxEntries=maxEntries;this.entries=[];this.bytes=0;}
  push(state,at,thumbnail=''){if(!(state instanceof Uint8Array)||!state.length||state.length>this.maxBytes)return false;const copy=new Uint8Array(state);this.entries.push({state:copy,at,thumbnail});this.bytes+=copy.byteLength;while(this.bytes>this.maxBytes||this.entries.length>this.maxEntries){this.bytes-=this.entries.shift().state.byteLength;}return true;}
  branch(index){if(!Number.isInteger(index)||index<0||index>=this.entries.length)throw Error('Timeline position unavailable');const entry=this.entries[index];this.entries=this.entries.slice(0,index+1);this.bytes=this.entries.reduce((n,e)=>n+e.state.byteLength,0);return new Uint8Array(entry.state);}
  clear(){this.entries=[];this.bytes=0;}
 }
 return {Timeline};
});
