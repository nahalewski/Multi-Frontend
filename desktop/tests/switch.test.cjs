const {test}=require('node:test'),assert=require('node:assert/strict'),P=require('../switch-preferences.cjs');
test('Switch is selectable and its preferences are validated',()=>{
 assert(require('../dashboard-themes.js').valid('switch'));assert.equal(require('../console-catalog.js').forTheme('switch').brand,'Nintendo');
 assert.deepEqual(P.update({volume:.5},{switchColor:'white',switchBrightness:.6}),{volume:.5,switchColor:'white',switchBrightness:.6});
 for(const v of [0,2,'1',NaN])assert.throws(()=>P.update({},{switchBrightness:v}));assert.throws(()=>P.update({},{switchColor:'red'}));
 assert.throws(()=>require('../settings-validation.cjs').validateDesktopState({settings:{switchColor:'bad'},games:[]}));
});
