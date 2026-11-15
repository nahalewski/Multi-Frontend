# External emulator integration

Version 0.7.0 adds per-game launch adapters and a dashboard-themed external Guide. Ctrl+Shift+G is registered only during an external session. A hidden, session-scoped XInput watcher also opens it for Back+Start. Other controllers can use the keyboard shortcut or an XInput translation layer. Windows may reserve the Xbox button for Game Bar.

The Guide requests a normal window close, waits four seconds, then terminates the tracked emulator process if it has not exited. Success is reported only after its exit event. Closing the frontend also closes its active emulator. Unsaved progress can be lost when an unresponsive emulator is stopped. Setup/error/exit-confirmation windows are not suppressed. The Guide does not suspend external processes, so games may keep running. Settings shown here apply on the next launch; no generic live-control or external save-state/scrubbing API is claimed. Embedded core settings and save controls remain available in their existing themed Guide; embedded play now enters fullscreen and restores the previous window mode on exit.

Launch profiles: PCSX2, DuckStation, Dolphin, RPCS3, PPSSPP, Cemu, Eden, Azahar, xemu, Xenia, mGBA, RetroArch. Unknown executables retain custom arguments with no guessed switches. Firmware, emulator installation and initial setup remain prerequisites. RPCS3 fullscreen requires its no-GUI mode. RetroArch uses a separate append-config and disables saving that merged configuration over the user's normal config. Per-game settings are stored with games and included in settings backups.

References checked November 14, 2026:

- https://pcsx2.net/docs/advanced/cli/
- https://github.com/stenzek/duckstation/blob/master/src/duckstation-qt/qthost.cpp
- https://github.com/dolphin-emu/dolphin/blob/master/Readme.md
- https://github.com/dolphin-emu/dolphin/blob/master/Source/Core/Common/Config/Config.cpp
- https://github.com/dolphin-emu/dolphin/blob/master/Source/Core/Core/Config/MainSettings.cpp
- https://github.com/dolphin-emu/dolphin/blob/master/Source/Core/Core/Config/GraphicsSettings.cpp
- https://github.com/RPCS3/rpcs3/blob/master/rpcs3/rpcs3.cpp
- https://www.ppsspp.org/docs/reference/command-line/
- https://github.com/cemu-project/Cemu/blob/main/src/config/LaunchSettings.cpp
- https://github.com/eden-emulator/mirror/blob/master/docs/user/CommandLine.md
- https://github.com/azahar-emu/azahar/issues/2548 (short -f works; avoid the reported long-option regression)
- https://xemu.app/docs/cli/
- https://github.com/xenia-project/xenia/blob/master/src/xenia/app/emulator_window.cc
- https://github.com/mgba-emu/mgba/blob/master/src/feature/commandline.c
- https://docs.libretro.com/guides/cli-intro/
