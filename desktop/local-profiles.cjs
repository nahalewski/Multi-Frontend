const crypto=require('node:crypto');
function ensure(state){
 if(!Array.isArray(state.profiles)||!state.profiles.length){const id=crypto.randomUUID();state.profiles=[{...state.profile,id}];state.activeProfile=id;state.profile={...state.profiles[0]};}
 if(!state.profiles.some(p=>p.id===state.activeProfile)){state.activeProfile=state.profiles[0].id;state.profile={...state.profiles[0]};}
 return state.profiles;
}
function sync(state){ensure(state);const index=state.profiles.findIndex(p=>p.id===state.activeProfile);state.profiles[index]={...state.profile,id:state.activeProfile};state.profile.id=state.activeProfile;}
function create(state,name){ensure(state);name=String(name||'').trim().slice(0,32);if(!name)throw Error('Enter a user name.');if(state.profiles.length>=32)throw Error('This console supports up to 32 local users.');if(state.profiles.some(p=>p.name.toLowerCase()===name.toLowerCase()))throw Error('That local user already exists.');sync(state);const user={id:crypto.randomUUID(),name,picture:'0',zone:'Recreation'};state.profiles.push(user);state.activeProfile=user.id;state.profile={...user};return user;}
function select(state,id){sync(state);const user=state.profiles.find(p=>p.id===id);if(!user)throw Error('User not found.');state.activeProfile=id;state.profile={...user};return user;}
module.exports={ensure,sync,create,select};
