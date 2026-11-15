# Windows 0.16.0 — guided setup

New installations start with console selection, then a live theme preview. Setup continues in the selected theme through local profiles, avatars, Plex, Jellyfin, RomM, RetroAchievements, IPTV, YouTube, optional karaoke/multiplayer accounts, PIA status, emulators, library folders and a summary. Each account can be skipped. Progress survives restart. Existing installations keep their settings; replay from Profile → Setup walkthrough.

Plex supports browser authorization and API tokens. Jellyfin supports username/password sessions and API tokens. RomM uses a long-lived Client API Token; RetroAchievements uses username and Web API key. IPTV supports Xtream credentials or M3U/XMLTV. YouTube needs no account. PIA authentication remains in its official desktop client. Service secrets remain in the existing OS-encrypted settings store; failed connection tests do not replace saved credentials.

100 new generated portraits: 10 for each of nine available themes, plus 10 for the Series 25th Anniversary variant. Original gamer pictures and custom image upload remain available. Generated art is original console-inspired portrait art, not official console avatar assets.

Selectable standalone emulators: Eden (Switch), experimental Lichii (Switch 2), Dolphin, Cemu, PCSX2, Xenia Canary, Azahar, RPCS3, PPSSPP, DuckStation, xemu, Vita3K, Flycast, mGBA and RetroArch. Eight embedded core choices cover older systems. Individual disable toggles are enforced during detection and launch. Enabled missing emulators can install now or on launch. Games, BIOS, keys and firmware are not downloaded. Vita3K packages still need installation through Vita3K. Standalone RetroArch does not automatically choose/download its own separate DLL cores; the embedded core choices run in the frontend.

Standalone automatic installation is currently Windows-only. Existing installations are reused. Windows Package Manager handles selected packages using its hash-checked manifests. Other downloads use upstream GitHub release metadata and SHA-256 hashes. Dolphin uses its official release API/CDN. Archives are checked for traversal/link entries and extracted into isolated version folders. Download failures can be retried; disable is not uninstall. Source details are retained with managed installations.

Switch 2 is a separate platform. NSP/XCI files in a folder named Switch 2 / Switch2 are detected accordingly; RomM Switch 2 identifiers and Game Options → Choose console are also supported. File extensions alone cannot distinguish Switch generations.

References: https://github.com/zortos293/Lichii · https://git.eden-emu.dev/eden-emu/eden · https://dolphin-emu.org/update/latest/beta · https://developer.plex.tv/pms/ · https://docs.romm.app/5.4.0/developers/client-api-tokens/

Validation includes the all-theme walkthrough UI, saved resume, avatar selection, account verification/rollback, installer validation and retry, disable enforcement, and an isolated live xemu release download/extraction without executing it. Emulator/game compatibility is not implied by installer tests. Android has not received this walkthrough.
