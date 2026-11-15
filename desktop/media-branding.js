window.MediaBranding=(()=>{
 const services={YouTube:{file:'youtube.png',name:'YouTube'},Jellyfin:{file:'jellyfin.svg',name:'Jellyfin'},Plex:{file:'plex.png',name:'Plex'},IPTV:{file:'zorba.png',name:'Zorba.network · IPTV'}};
 function logo(service,cls){const plate=document.createElement('span');plate.className=cls;const img=document.createElement('img');img.src='assets/services/'+service.file;img.alt='';img.decoding='async';plate.append(img);return plate;}
 function decorate(title){const panel=document.querySelector('#panel');panel.classList.toggle('media-center-branded',title==='Media Center');panel.querySelector('.service-preview')?.remove();if(title!=='Media Center')return;
  const detail=panel.querySelector('.nxe-detail'),preview=document.createElement('div');preview.className='service-preview';preview.setAttribute('aria-hidden','true');preview.append(logo(services.YouTube,'service-preview-logo'));if(detail){detail.querySelector('.nxe-detail-emblem')?.remove();detail.prepend(preview);}
  panel.querySelectorAll('#rows button').forEach(button=>{const service=services[button.textContent];if(!service){button.addEventListener('focus',()=>preview.replaceChildren());return;}const label=document.createElement('span');label.className='service-name';label.textContent=button.textContent;button.replaceChildren(logo(service,'service-logo'),label);button.title=service.name;button.classList.add('service-row');button.addEventListener('focus',()=>{preview.replaceChildren(logo(service,'service-preview-logo'));});});
 }
 return {decorate};
})();
