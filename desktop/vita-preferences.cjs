const backgrounds=['blue','aqua','violet','sunset'];
function update(settings,args){
 const next={...settings};
 if(args.vitaBackground!==undefined){if(!backgrounds.includes(args.vitaBackground))throw Error('Unknown Vita background');next.vitaBackground=args.vitaBackground;}
 if(args.vitaBrightness!==undefined){if(!Number.isFinite(args.vitaBrightness)||args.vitaBrightness<.35||args.vitaBrightness>1)throw Error('Vita brightness must be between 0.35 and 1');next.vitaBrightness=args.vitaBrightness;}
 if(args.vitaSetupComplete!==undefined){if(typeof args.vitaSetupComplete!=='boolean')throw Error('Invalid Vita setup completion');next.vitaSetupComplete=args.vitaSetupComplete;}
 return next;
}
module.exports={backgrounds,update};
