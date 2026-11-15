window.SeriesArtwork=(()=>{
 const pending=new Set(),attempted=new Map(),queue=[];let active=0;
 function key(game){return game.id+'|'+(game.art||'').length+'|'+(game.art||'').slice(-40);}
 function ensure(game){if(!game||state.settings.theme!=='series'||(game.seriesArt&&game.seriesArtSource?.kind!=='box-crop'))return;const id=key(game);if(pending.has(id)||Date.now()-(attempted.get(id)||0)<300000)return;pending.add(id);queue.push({id:game.id,key:id});drain();}
 function apply(result){if(!result?.matched)return;for(const collection of [state.games,typeof games!=='undefined'?games:[]])for(const game of collection)if(game.id===result.id){game.seriesArt=result.seriesArt;game.seriesArtSource=result.seriesArtSource;}if(state.settings.theme==='series')draw();}
 function drain(){while(active<2&&queue.length){const task=queue.shift();if(state.settings.theme!=='series'){pending.delete(task.key);continue;}active++;call('artwork:series',{id:task.id}).then(apply).catch(()=>{}).finally(()=>{attempted.set(task.key,Date.now());if(attempted.size>10000)attempted.delete(attempted.keys().next().value);pending.delete(task.key);active--;drain();});}}
 async function refresh(game){if(!game)return;const result=await call('artwork:series',{id:game.id,force:true});apply(result);return result;}
 return{ensure,refresh,apply};
})();
