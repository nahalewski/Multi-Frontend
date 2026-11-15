/* Window chrome stays out of the console UI until the pointer or keyboard needs it. */
window.DesktopWindowControls=(()=>{
 const bar=document.createElement('nav');bar.id='windowControls';bar.setAttribute('aria-label','Window controls');
 const drag=document.createElement('span');drag.className='window-drag';drag.title='Drag window';bar.append(drag);
 const guide=document.createElement('button');guide.type='button';guide.textContent='⊞';guide.title='Open Guide (Home / View + Menu)';guide.setAttribute('aria-label','Open Guide');guide.onclick=()=>window.NxeExperience?.open();bar.append(guide);
 for(const [action,label,glyph]of [['minimize','Minimize','−'],['maximize','Maximize or restore','□'],['close','Close','×']]){const button=document.createElement('button');button.type='button';button.title=label;button.setAttribute('aria-label',label);button.textContent=glyph;button.onclick=()=>window.blades.call('window:control',{action});bar.append(button);}
 document.body.append(bar);
 let timer=0,pointerInside=false,controllerMode=false,keyboardFocus=true;
 const focused=()=>keyboardFocus&&bar.contains(document.activeElement);
 function reveal(){clearTimeout(timer);bar.classList.remove('window-controls-hidden');}
 function hide(){clearTimeout(timer);bar.classList.add('window-controls-hidden');}
 function idle(delay=650){clearTimeout(timer);timer=setTimeout(()=>{timer=0;if(!pointerInside&&!focused())hide();},delay);}
 function nearBar(event){const rect=bar.getBoundingClientRect();return event.clientX>=rect.left-12&&event.clientX<=rect.right&&event.clientY>=0&&event.clientY<=rect.bottom+12;}
 function pointer(event){if(event.type==='pointermove'&&event.pointerType==='touch')return;if(event.type==='pointerdown')keyboardFocus=false;const inside=nearBar(event),wasInside=pointerInside;pointerInside=inside;if(inside){controllerMode=false;reveal();if(event.pointerType==='touch'){pointerInside=false;idle(2400);}}else if(!controllerMode&&!focused()&&(wasInside||!timer)&&!bar.classList.contains('window-controls-hidden'))idle();}
 document.addEventListener('pointermove',pointer,{passive:true});
 document.addEventListener('pointerdown',pointer,{passive:true});
 document.addEventListener('keydown',()=>{keyboardFocus=true;},true);
 document.addEventListener('pointerleave',()=>{pointerInside=false;if(!focused())idle();});
 bar.addEventListener('focusin',()=>{controllerMode=false;reveal();});
 bar.addEventListener('focusout',()=>{if(!pointerInside)idle();});
 bar.addEventListener('keydown',event=>{event.stopPropagation();if(event.key==='Escape'){event.preventDefault();document.activeElement?.blur();pointerInside=false;hide();}});
 function controllerActivity(){controllerMode=true;pointerInside=false;if(bar.contains(document.activeElement))document.activeElement.blur();hide();}
 window.addEventListener('blur',()=>{pointerInside=false;hide();});
 idle(1800);
 return{controllerActivity};
})();
