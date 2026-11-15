// Verified PlayStation product identifiers; used only when catalog names are filenames.
// Sources: PlatPrices product IDs; Vita3K compatibility and VitaGrafix title lists.
const titles={PPSA27625:'Fantasy Life i: The Girl Who Steals Time',PPSA29629:'Marvel Cosmic Invasion',PPSA29343:'Beast of Reincarnation',PCSE00670:"LEGO Marvel's Avengers",PCSB00859:'Skullgirls 2nd Encore',PCSE00912:'Dragon Quest Builders',PCSE00249:'Hotline Miami',PCSA00096:'Sly Cooper and the Thievius Raccoonus',PCSA00097:'Sly 2: Band of Thieves',PCSA00098:'Sly 3: Honor Among Thieves',PCSA00126:'God of War Collection'};
function displayName(v){const filename=String(v.fs_name||''),name=String(v.name||filename||'Game');const id=filename.match(/PPSA\d{5}|PCS[A-Z]\d{5}/)?.[0];return id&&titles[id]&&(/PPSA\d|PCS[A-Z]\d|\[GamesMega\]/i.test(name))?titles[id]:name;}
const key=s=>String(s).normalize('NFKD').replace(/[\u0300-\u036f]/g,'').toLowerCase().replace(/[^a-z0-9]/g,'');
module.exports={displayName,key};
