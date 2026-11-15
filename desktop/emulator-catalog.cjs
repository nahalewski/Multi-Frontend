const cores={fceumm:['NES'],snes9x:['SNES'],gambatte:['Game Boy','Game Boy Color'],mgba:['GBA'],mupen64plus_next:['N64'],melonds:['Nintendo DS'],pcsx_rearmed:['PS1','PlayStation'],genesis_plus_gx:['Mega Drive','Master System','Game Gear']};
const catalog=[...Object.entries(cores).map(([core,systems])=>({id:'core-'+core,name:systems.join(' / ')+' core',core,systems,kind:'Embedded core'})),
 {id:'eden',name:'Eden',systems:['Switch'],winget:'Utopia.Eden.MSVC',files:['eden.exe']},
 {id:'lichii',name:'Lichii',systems:['Switch 2'],repo:'zortos293/Lichii',asset:'^lichii-windows-x86_64\\.zip$',files:['lichii.exe'],note:'Experimental Switch 2 support. Requires your own games, keys and firmware.'},
 {id:'dolphin',name:'Dolphin',systems:['GameCube','Wii','GameCube / Wii'],dolphin:true,files:['Dolphin.exe']},
 {id:'cemu',name:'Cemu',systems:['Wii U'],winget:'Cemu.Cemu',files:['Cemu.exe']},
 {id:'pcsx2',name:'PCSX2',systems:['PlayStation 2'],winget:'PCSX2Team.PCSX2',files:['pcsx2-qt.exe','pcsx2.exe']},
 {id:'xenia',name:'Xenia Canary',systems:['Xbox 360'],repo:'xenia-canary/xenia-canary',asset:'^xenia_canary_windows\\.7z$',files:['xenia_canary.exe','xenia.exe']},
 {id:'azahar',name:'Azahar',systems:['Nintendo 3DS'],repo:'azahar-emu/azahar',asset:'^azahar-windows-msvc-.*\\.zip$',files:['azahar.exe']},
 {id:'shadps4',name:'shadPS4',systems:['PlayStation 4'],repo:'shadps4-emu/shadPS4',asset:'^shadps4-win64-sdl-.*\\.zip$',files:['shadPS4.exe'],note:'Experimental PS4 emulation. Use extracted game dumps (eboot.bin), not PKG installers. Requires your own supported games and system modules.'},
 {id:'rpcs3',name:'RPCS3',systems:['PlayStation 3'],repo:'RPCS3/rpcs3-binaries-win',asset:'_win64_msvc\\.7z$',files:['rpcs3.exe']},
 {id:'ppsspp',name:'PPSSPP',systems:['PSP'],winget:'PPSSPPTeam.PPSSPP',files:['PPSSPPWindows64.exe','PPSSPPWindows.exe']},
 {id:'duckstation',name:'DuckStation',systems:['PS1','PlayStation'],winget:'Stenzek.DuckStation',files:['duckstation-qt-x64-release.exe','duckstation.exe']},
 {id:'xemu',name:'xemu',systems:['Xbox'],repo:'xemu-project/xemu',asset:'^xemu-win-x86_64-release\\.zip$',files:['xemu.exe']},
 {id:'vita3k',name:'Vita3K',systems:['PS Vita'],repo:'Vita3K/Vita3K',asset:'^windows-latest\\.zip$',files:['Vita3K.exe'],manualLaunch:true,note:'Install Vita packages inside Vita3K; raw files cannot be launched directly.'},
 {id:'flycast',name:'Flycast',systems:['Dreamcast','Naomi'],repo:'flyinghead/flycast',asset:'^flycast-win64-.*\\.zip$',files:['flycast.exe']},
 {id:'mgba',name:'mGBA',systems:['GBA','Game Boy','Game Boy Color'],winget:'JeffreyPfau.mGBA',files:['mGBA.exe']},
 {id:'retroarch',name:'RetroArch',systems:[],winget:'Libretro.RetroArch',files:['retroarch.exe'],note:'Optional standalone frontend. Embedded cores above run directly inside Multi-Frontend.'}
];
function get(id){return catalog.find(x=>x.id===id);}
function enabled(state,entry){if(!entry)return false;const value=state.settings?.emulatorChoices?.[entry.id];if(typeof value==='boolean')return value;return !!entry.core||Object.values(state.emulators||{}).some(e=>matches(entry,e));}
function matches(entry,em){return entry.files?.some(f=>require('node:path').basename(String(em?.path||'').replace(/\\/g,'/')).toLowerCase()===f.toLowerCase());}
function disabled(state,em){const entry=catalog.find(c=>matches(c,em));return !!entry&&state.settings?.emulatorChoices?.[entry.id]===false;}
module.exports={catalog,cores,get,enabled,matches,disabled};
