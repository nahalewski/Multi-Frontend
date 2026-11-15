const {test}=require('node:test'),assert=require('node:assert/strict');
const {systemFor,normalize}=require('../romm-catalog.cjs');
test('explicit RomM platform takes precedence over ambiguous file formats',()=>{
 for(const [slug,expected] of Object.entries({ps5:'PlayStation 5',psvita:'PS Vita',ps3:'PlayStation 3',ps4:'PlayStation 4',ngc:'GameCube',android:'Android',win:'PC',sega32:'Sega 32X'}))assert.equal(systemFor('Game.pkg',slug),expected);
 assert.equal(normalize({id:1,fs_name:'Game.zip',platform_slug:'psvita',name:'Game'}).system,'PS Vita');
 assert.equal(systemFor('Game.gba'),'GBA');
});
