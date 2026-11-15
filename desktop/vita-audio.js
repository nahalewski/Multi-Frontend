/* Original synthesized Vita-inspired UI cues. No Sony recordings; see VITA-SOURCES.md. */
window.VitaAudio=(()=>{
 let context;const voices=new Set(),bootVoices=new Set();
 const clamp=v=>Math.max(0,Math.min(1,Number(v)||0));
 function tone(hz,at,length,volume,{end=hz,harmonic=.14,boot=false}={}){
  if(voices.size>48)return;
  const ctx=context||(context=new AudioContext());ctx.resume().catch(()=>{});
  const gain=ctx.createGain(),a=ctx.createOscillator(),b=ctx.createOscillator(),partial=ctx.createGain(),start=ctx.currentTime+at;
  a.type='sine';b.type='sine';a.frequency.setValueAtTime(hz,start);a.frequency.exponentialRampToValueAtTime(Math.max(1,end),start+length);b.frequency.setValueAtTime(hz*2.003,start);b.frequency.exponentialRampToValueAtTime(Math.max(1,end*2.003),start+length);partial.gain.value=harmonic;
  gain.gain.setValueAtTime(0,start);gain.gain.linearRampToValueAtTime(clamp(volume)*.12,start+.009);gain.gain.exponentialRampToValueAtTime(.00001,start+length);
  a.connect(gain);b.connect(partial);partial.connect(gain);gain.connect(ctx.destination);
  const voice={a,b,gain,partial};voices.add(voice);if(boot)bootVoices.add(voice);
  a.onended=()=>{voices.delete(voice);bootVoices.delete(voice);a.disconnect();b.disconnect();partial.disconnect();gain.disconnect();};
  a.start(start);b.start(start);a.stop(start+length+.015);b.stop(start+length+.015);
 }
 function play(kind='move',volume=.5){if(!clamp(volume))return;try{
  if(kind==='select'||kind==='login'){tone(880,0,.15,volume,{end:1108.73});tone(1318.51,.045,.19,volume*.65);}
  else if(kind==='back'){tone(830.61,0,.14,volume,{end:554.37,harmonic:.08});}
  else if(kind==='notify'){[659.25,987.77,1318.51].forEach((hz,i)=>tone(hz,i*.095,.45,volume*.72));}
  else if(kind==='tab'){tone(622.25,0,.13,volume,{end:830.61});tone(1108.73,.025,.16,volume*.35);}
  else tone(1174.66,0,.07,volume*.72,{end:1046.5,harmonic:.04});
 }catch{}}
 function boot(volume=.5){stopBoot();if(!clamp(volume))return;try{[329.63,493.88,659.25,987.77].forEach((hz,i)=>tone(hz,.16+i*.11,1.7,volume*.43,{boot:true,harmonic:.06}));tone(1318.51,.7,1.5,volume*.22,{boot:true,harmonic:.02});}catch{}}
 function stopBoot(){for(const voice of bootVoices){try{voice.gain.gain.cancelScheduledValues(0);voice.gain.gain.value=0;voice.a.stop();voice.b.stop();}catch{}}bootVoices.clear();}
 function dispose(){stopBoot();for(const voice of voices){try{voice.a.stop();voice.b.stop();}catch{}}voices.clear();context?.close().catch(()=>{});context=null;}
 window.addEventListener('beforeunload',dispose);
 return{play,boot,stopBoot,dispose};
})();
