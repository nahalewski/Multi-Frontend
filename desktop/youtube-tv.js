/* YouTube-style TV presentation; existing public-data and playback backend is retained. */
window.YouTubeTV=(()=>{
 let nav=null;
 const active=()=>!$('#panel').hidden&&$('#panel').classList.contains('youtube-tv');
 function clear(){nav?.remove();nav=null;$('#panel').classList.remove('youtube-tv','youtube-tv-grid','youtube-tv-details');$('#panel .youtube-video-hero')?.remove();}
 function decorate(mode='list',items=[]){
  if(state.settings.theme!=='series')return;nav?.remove();const panel=$('#panel');panel.classList.add('youtube-tv');nav=document.createElement('nav');nav.className='youtube-tv-nav';nav.setAttribute('aria-label','YouTube navigation');
  const brand=document.createElement('div');brand.className='youtube-tv-brand';const logo=new Image();logo.src='assets/services/youtube.png';logo.alt='';brand.append(logo,document.createTextNode('YouTube'));nav.append(brand);
  const yt=window.BladesYouTube;for(const [name,fn]of [['Search',yt.search],['Home',yt.home],['Subscriptions',yt.subscriptions],['Subscription feed',yt.feed],['Watch Later',()=>yt.collection('later','Watch Later')],['Favorites',()=>yt.collection('favorites','Favorites')],['History',()=>yt.collection('history','History')],['Play queue',()=>yt.collection('queue','Play queue')],['Settings',yt.settings],['Back to apps',mediaCenter]]){const b=document.createElement('button');b.textContent=name;b.onclick=()=>run(fn);nav.append(b);}panel.append(nav);
  if(mode==='grid'){panel.classList.add('youtube-tv-grid');[...$('#rows').children].forEach((b,i)=>{if(!items[i])return;const item=items[i],thumb=document.createElement('div');thumb.className='youtube-tv-thumbnail';if(/^[\w-]{11}$/.test(item.id)){const img=new Image();img.src='https://i.ytimg.com/vi/'+item.id+'/mqdefault.jpg';img.alt='';img.loading='lazy';img.onerror=()=>img.remove();thumb.append(img);}if(Number(item.duration)>0){const duration=document.createElement('small'),s=Math.floor(item.duration);duration.textContent=Math.floor(s/60)+':'+String(s%60).padStart(2,'0');thumb.append(duration);}const name=document.createElement('strong'),channel=document.createElement('span');name.textContent=item.name;channel.textContent=item.channel||'YouTube video';b.replaceChildren(thumb,name,channel);});}
  if(mode==='details'&&items[0]){panel.classList.add('youtube-tv-details');const hero=document.createElement('img');hero.className='youtube-video-hero';hero.alt=items[0].name;if(/^[\w-]{11}$/.test(items[0].id))hero.src='https://i.ytimg.com/vi/'+items[0].id+'/hqdefault.jpg';panel.append(hero);}
  if(mode==='grid')[...$('#rows').children].slice(items.length).forEach(b=>b.classList.add('youtube-extra-action'));
  $('#panel footer').textContent='A Select   B Back   Y Search   D-pad Browse';
 }
 function handle(key){if(!active())return false;if(key==='y'){run(()=>BladesYouTube.search());return true;}const buttons=[...nav.querySelectorAll('button')],at=buttons.indexOf(document.activeElement);if(at>=0){if(key==='up'||key==='down')buttons[Math.max(0,Math.min(buttons.length-1,at+(key==='up'?-1:1)))]?.focus();else if(key==='right'){focusRow();}else if(key==='select')buttons[at].click();else if(key==='back')run(parent||mediaCenter);return true;}
  const grid=$('#panel').classList.contains('youtube-tv-grid'),cols=innerWidth<=900?2:3;if(key==='left'&&(!grid||panelIndex%cols===0)){buttons[0]?.focus();return true;}if(grid&&['left','right','up','down'].includes(key)){panelIndex=Math.max(0,Math.min(panelActions.length-1,panelIndex+({left:-1,right:1,up:-cols,down:cols}[key])));focusRow();return true;}return false;
 }
 return{decorate,clear,handle};
})();
