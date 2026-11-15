/* Original vector animation inspired by Vita startup and its peel-to-open start screen. */
window.VitaBoot=(()=>{
 const clamp=v=>Math.max(0,Math.min(1,v));
 function backdrop(g,w,h,time=0){
  const gradient=g.createLinearGradient(0,0,w,h);gradient.addColorStop(0,'#003892');gradient.addColorStop(.46,'#0879c0');gradient.addColorStop(1,'#002658');g.fillStyle=gradient;g.fillRect(0,0,w,h);
  for(let i=0;i<5;i++){
   const shift=Math.sin(time*.00017+i*.55)*h*.025,y=h*(.29+i*.095)+shift;
   g.beginPath();g.moveTo(-w*.2,y);g.bezierCurveTo(w*.22,y-h*.32,w*.46,y+h*.5,w*1.2,y-h*.04);g.lineTo(w*1.2,y+h*.045);g.bezierCurveTo(w*.46,y+h*.53,w*.22,y-h*.25,-w*.2,y+h*.04);g.closePath();
   const band=g.createLinearGradient(0,y-h*.3,0,y+h*.4);band.addColorStop(0,'#a0edff08');band.addColorStop(.55,i%2?'#c7f5ff24':'#90d4ff38');band.addColorStop(1,'#1576aa00');g.fillStyle=band;g.fill();
   g.beginPath();g.moveTo(-w*.2,y);g.bezierCurveTo(w*.22,y-h*.32,w*.46,y+h*.5,w*1.2,y-h*.04);g.strokeStyle='#c5efff22';g.lineWidth=Math.max(1,w/960);g.stroke();
  }
 }
 function renderAt(canvas,milliseconds,reduced=false){
  const dpr=window.devicePixelRatio||1,w=window.innerWidth,h=window.innerHeight;if(canvas.width!==w*dpr||canvas.height!==h*dpr){canvas.width=w*dpr;canvas.height=h*dpr;}
  const g=canvas.getContext('2d'),s=Math.min(w/960,h/544),t=reduced?3.3:milliseconds/1000;g.setTransform(dpr,0,0,dpr,0,0);g.globalAlpha=1;g.fillStyle='#000';g.fillRect(0,0,w,h);
  if(t<2.25){
   g.globalAlpha=clamp(t/.7)*clamp((2.25-t)/.5);g.fillStyle='#f9fbff';g.textAlign='center';g.font=`300 ${58*s}px 'Segoe UI',sans-serif`;g.fillText('PS VITA',w/2,h*.49);g.font=`300 ${16*s}px 'Segoe UI',sans-serif`;g.fillStyle='#c2c7cf';g.fillText('PlayStation®Vita',w/2,h*.56);g.globalAlpha=1;return;
  }
  const reveal=clamp((t-2.1)/.6),peel=reduced?0:clamp((t-4.45)/1.05);
  backdrop(g,w,h,reduced?0:milliseconds);
  // Fold the start-screen card diagonally away to reveal the home-page background.
  if(peel<1){
   const span=peel*(w+h),right=Math.max(0,w-span),down=Math.min(h,span);
   const points=[[0,0],[w,0],[w,h],[0,h]],clip=[],limit=w-span;
   for(let i=0;i<points.length;i++){const a=points[i],b=points[(i+1)%points.length],da=a[0]-a[1]-limit,db=b[0]-b[1]-limit;if(da<=0)clip.push(a);if((da<=0)!==(db<=0)){const f=da/(da-db);clip.push([a[0]+(b[0]-a[0])*f,a[1]+(b[1]-a[1])*f]);}}
   g.save();g.beginPath();clip.forEach(([x,y],i)=>i?g.lineTo(x,y):g.moveTo(x,y));g.closePath();g.clip();
   backdrop(g,w,h,reduced?0:milliseconds+2000);
   const glow=g.createRadialGradient(w*.75,h*.3,0,w*.75,h*.3,w*.8);glow.addColorStop(0,'#46c9ee4d');glow.addColorStop(1,'#011f6600');g.fillStyle=glow;g.fillRect(0,0,w,h);
   const now=new Date();g.textAlign='right';g.fillStyle='#fff';g.shadowColor='#002e6870';g.shadowBlur=10*s;g.font=`300 ${72*s}px 'Segoe UI',sans-serif`;g.fillText(now.toLocaleTimeString([],{hour:'2-digit',minute:'2-digit',hour12:false}),w-56*s,h*.39);
   g.font=`300 ${24*s}px 'Segoe UI',sans-serif`;g.fillText(now.toLocaleDateString([],{month:'long',day:'numeric'}),w-60*s,h*.47);g.font=`300 ${17*s}px 'Segoe UI',sans-serif`;g.fillText(now.toLocaleDateString([],{weekday:'long'}),w-60*s,h*.52);g.shadowBlur=0;
   g.textAlign='left';g.font=`300 ${24*s}px 'Segoe UI',sans-serif`;g.fillText('PS VITA',44*s,h-44*s);
   g.restore();
   const corner=reduced?26*s:Math.max(24*s,Math.min(w*.5,span*.38)),x=right,y=0;
   g.save();g.beginPath();g.moveTo(x,0);g.lineTo(Math.min(w,x+corner),Math.min(h,down+corner));g.lineTo(w,down);g.closePath();g.shadowColor='#00153480';g.shadowBlur=18*s;g.fillStyle='#d8f2f7';g.fill();g.shadowBlur=0;g.restore();
  }
  if(reveal<1){g.fillStyle=`rgba(0,0,0,${1-reveal})`;g.fillRect(0,0,w,h);}g.globalAlpha=1;
 }
 return{renderAt,backdrop,duration:5700};
})();
