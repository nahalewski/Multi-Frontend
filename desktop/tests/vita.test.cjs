const {test}=require('node:test'),assert=require('node:assert/strict'),P=require('../vita-preferences.cjs');
test('Vita is a selectable PlayStation console theme',()=>{
 assert(require('../dashboard-themes.js').valid('ps-vita'));
 assert.equal(require('../console-catalog.js').forTheme('ps-vita').id,'vita');
});
test('Vita settings preserve other themes and reject corrupt values',()=>{
 const settings={theme:'ps-vita',sound:false,seriesBackground:'anniversary25'};
 assert.deepEqual(P.update(settings,{vitaBackground:'aqua',vitaBrightness:.5,vitaSetupComplete:true}),{...settings,vitaBackground:'aqua',vitaBrightness:.5,vitaSetupComplete:true});
 for(const value of [NaN,Infinity,-1,0,2,'0.5'])assert.throws(()=>P.update(settings,{vitaBrightness:value}));
 assert.throws(()=>P.update(settings,{vitaBackground:'invalid'}));assert.throws(()=>P.update(settings,{vitaSetupComplete:'yes'}));
 const validate=require('../settings-validation.cjs').validateDesktopState;
 assert.throws(()=>validate({settings:{vitaBrightness:8},games:[]}));
 assert.equal(validate({settings:{theme:'ps-vita',vitaBackground:'violet',vitaBrightness:1},games:[]}).settings.theme,'ps-vita');
});
