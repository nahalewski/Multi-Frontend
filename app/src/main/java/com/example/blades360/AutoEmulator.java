package com.example.blades360;

import java.util.*;

/** Explicit, deterministic system routing. Saved user overrides take precedence. */
final class AutoEmulator {
  static final String PREFIX="libretro:";
  static final Map<String,String> CORES=new LinkedHashMap<>();
  static {
    CORES.put("NES","fceumm");CORES.put("Super Nintendo","snes9x");
    CORES.put("Game Boy","gambatte");CORES.put("Game Boy Color","gambatte");CORES.put("Game Boy Advance","mgba");
    CORES.put("Mega Drive","genesis_plus_gx");CORES.put("Master System","genesis_plus_gx");CORES.put("Game Gear","genesis_plus_gx");
    CORES.put("Nintendo 64","mupen64plus_next_gles3");CORES.put("Nintendo DS","melonds");
    CORES.put("PlayStation","pcsx_rearmed");CORES.put("PSP","ppsspp");CORES.put("Dreamcast","flycast");
  }
  static String choose(String system,List<EmulatorLibrary.App> installed){
    String core=CORES.get(system);if(core!=null)return PREFIX+core;
    String[] candidates;
    switch(system){
      case "Nintendo 3DS":candidates=new String[]{"org.azahar","io.github.lime3ds","org.citra"};break;
      case "Nintendo Switch":candidates=new String[]{"dev.eden.eden_emulator","org.yuzu","org.sudachi"};break;
      case "Xbox 360":candidates=new String[]{"emu.x360.mobile","aenu.ax360e","xendroid","xenia"};break;
      case "GameCube":case "Wii":case "GameCube / Wii":candidates=new String[]{"org.dolphinemu"};break;
      case "Wii U":candidates=new String[]{"info.cemu"};break;
      case "PlayStation 2":candidates=new String[]{"xyz.aethersx2","nethersx2","armsx2"};break;
      case "PS Vita":candidates=new String[]{"org.vita3k"};break;
      default:return "";
    }
    for(String match:candidates)for(EmulatorLibrary.App app:installed)if(app.pkg.toLowerCase(Locale.ROOT).contains(match))return app.pkg;
    return "";
  }
  static String label(String value){return "In-app / "+value.substring(PREFIX.length()).replace('_',' ');}
}
