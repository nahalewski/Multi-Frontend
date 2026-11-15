class KaraokeMic extends AudioWorkletProcessor{
 constructor(){super();this.samples=[];this.phase=0;}
 process(inputs){const source=inputs[0]?.[0];if(source){const ratio=sampleRate/16000;for(let i=0;i<source.length;i++){this.phase+=1;if(this.phase>=ratio){this.phase-=ratio;this.samples.push(Math.max(-32768,Math.min(32767,Math.round(source[i]*32767))));if(this.samples.length>=320){const pcm=new Int16Array(this.samples.splice(0,320));this.port.postMessage(pcm.buffer,[pcm.buffer]);}}}}return true;}
}registerProcessor('karaoke-mic',KaraokeMic);
