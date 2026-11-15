const steps=['controller','language','network','camera','timezone','datetime','power','agreement','welcome','user','avatar','account','finish'];
function setup(settings,args={}){if(!args||typeof args!=='object'||Array.isArray(args))throw Error('Invalid setup preferences');const next={step:'controller',language:'en-US',zone:'system',hour12:true,power:'standard',...(settings.ps4Setup||{})};
 for(const key of Object.keys(args))if(!['step','language','zone','hour12','power','agreement','complete','networkSkipped','cameraSkipped','loginPrompt'].includes(key))throw Error('Unknown setup preference');
 if(args.step!==undefined){if(!steps.includes(args.step))throw Error('Invalid setup stage');next.step=args.step;}
 if(args.language!==undefined){if(!['en-US','en-GB'].includes(args.language))throw Error('Unsupported interface language');next.language=args.language;}
 if(args.zone!==undefined){if(typeof args.zone!=='string'||args.zone.length>80)throw Error('Invalid time zone');if(args.zone!=='system')try{new Intl.DateTimeFormat('en',{timeZone:args.zone}).format();}catch{throw Error('Invalid time zone');}next.zone=args.zone;}
 if(args.power!==undefined){if(!['standard','low-motion'].includes(args.power))throw Error('Invalid power preference');next.power=args.power;}
 for(const key of ['hour12','agreement','complete','networkSkipped','cameraSkipped','loginPrompt'])if(args[key]!==undefined){if(typeof args[key]!=='boolean')throw Error('Invalid '+key);next[key]=args[key];}
 if(next.complete&&!next.agreement)throw Error('Read and acknowledge the frontend information first.');
 const result={...settings,ps4Setup:next};if(args.power!==undefined)result.nxeMotion=args.power==='standard';if(args.complete===true)result.setupComplete=true;return result;
}
function quick(settings,items){const allowed=['sound','music','notifications','trophies','profile','power'];if(!Array.isArray(items)||items.length>allowed.length||new Set(items).size!==items.length||items.some(i=>!allowed.includes(i)))throw Error('Invalid quick menu items');return{...settings,ps4QuickItems:[...items]};}
module.exports={steps,setup,quick};
