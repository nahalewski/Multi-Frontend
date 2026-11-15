# Blades360 desktop 0.4.4

The November 2026 Windows audit fixes PC cover artwork in both themes, long Blades titles overlapping covers, media players hidden behind menus, stale IPTV guide updates, repeated copy/recording starts, mixed controller input, and settings restore/input errors. PC discovery now excludes explicitly marked launcher extras and DLC. Steam, Epic and GOG covers use exact installed-game identifiers; missing covers can still be selected manually.

YouTube search can work while Google rejects playback with a network bot check. The frontend reports this explicitly and does not claim that updating the runtime bypasses it. Physical-controller compatibility, every external emulator, and macOS still require their own device testing.

Desktop frontend for Windows x64 (including ROG Xbox Ally), macOS Apple Silicon and Intel. This runs its own desktop UI and embedded WebAssembly emulators, without an Android emulator wrapper.

## Dashboard themes

System Settings > Console Settings > Dashboard theme switches between Blades and New Xbox Experience (2008). NXE has vertical categories, horizontal live cards, a reflective floor and themed settings panels. Up/down changes categories; left/right selects cards; A opens a card. The choice persists and is included in settings backups. This is a recreated frontend theme, without Xbox Live social services or 3D Xbox Avatars.

### NXE additions in 0.3

- Compact Xbox Guide with functional tabs on the dashboard (Guide / F1 / Home) and inside embedded games (Guide / F1). In-game tabs expose gameplay, save slots/timeline and console/controller settings.
- Animated angled dashboard cards, reflective backgrounds, Classic/Spectrum/Day/Night color schemes, and reduced-motion setting (also honors the OS preference).
- Paged game-cover grid with filters and controller navigation: arrows/D-pad move, LB/RB page, A plays, X opens options.
- Full-screen detail layouts for game options, achievements, profiles, media and settings; NXE keyboard, IPTV guide, media controls and emulator submenus.
- Queued Guide-style notification banners, local-profile-loaded banner and newly synced achievement totals. RetroAchievements remains read-only; no live in-game achievement earning is implied.
- Separate original synthesized menu, tab, select/back and notification cues. These are recreated sounds, not verified original Microsoft recordings. Backgrounds and interface are also code-rendered recreations, not pixel-perfect extracted assets. No 3D avatars.

Reference material: [Xbox 360 dashboard architecture and NXE Guide screenshots](https://www.copetti.org/writings/consoles/xbox-360/) and [NXE UI design reference](https://cj-ramos.myportfolio.com/nxe-retail-kiosk-ui).

Validation: `electron tests/nxe-experience-smoke.cjs`, `electron tests/theme-smoke.cjs`, `electron emulation-smoke.cjs --gba --frame --nxe`, and the existing unit suite. The emulator smoke boots an original generated GBA ROM and checks actual save/load, timeline and Guide behavior. macOS still needs testing on a Mac.

## Features

- Original blade geometry, Convection fonts/sounds, keyboard/mouse/touch/gamepad navigation, profile/initial setup and gamer pictures, full-screen mode and Blades on-screen keyboard.
- ROM scanning, console groups, favorites, search/recent games, exact-match automatic artwork and manual cover search. Select the console for ambiguous disc images in X Options. External emulator launch remains available per game.
- Packaged EmulatorJS/libretro cores for NES, SNES, GB/GBC, GBA, Sega 8/16-bit, N64, Nintendo DS and PS1. Console logo splash, in-game Blades Guide, ten save slots, battery saves, rewind timeline previews, per-game aspect/audio/core settings, controller mapping and transparent touch controls/analog sticks. Ten named touch layouts support resizing/hiding/rearrangement. See `emulation-integration.md` for details and licenses.
- IPTV M3U/XMLTV guide with programme grid, preview/full-screen, favorites, groups, language/country filters, timezone correction and cached-guide fallback. One channel player; bounded HLS resource loader without disabling browser security.
- Public YouTube search/channels/playlists, local subscriptions, Watch Later, favorites, queue/history/resume, comments and Blades quality/caption/chapter/speed controls. Verified official yt-dlp downloads/updates; see `youtube-NOTES.md`.
- Paged Jellyfin/Plex/RomM browsing, profile/folder navigation, direct-stream media, Blades playback options and external subtitles. Local music playlists, shuffle/repeat and resume.
- App-only screenshot/recording galleries. Recording captures Blades360's WebContents and audio, not other apps or the desktop. F9 starts/stops recording.
- RetroAchievements full paginated progress, earned totals, achievement lists and unlock details. This displays account data; in-game achievement earning is not implemented.
- Controller detection, standard automatic mapping, per-device button remapping and stick dead zone.
- Blades PIA toggle, US region selection and real status through the installed official PIA client. Its routing, login and kill-switch settings apply. IP checks distinguish connection from verified geolocation; GPS is unchanged.
- Encrypted full-settings backup includes credentials, covers and per-game embedded core/controller settings. Android migration maps profile/service/frontend settings and matches imported favorites when desktop ROM folders are scanned.

## Run and build

```
npm ci
npm test
npm start
npm run build:win
npm run build:mac
```

Windows portable executable: `dist/Blades360-0.2.0-win-x64.exe`. ROG Xbox Ally uses this build in Gamepad mode. A selects, B returns, X opens options, LB/RB changes blade. Embedded games use F1/Escape, controller Guide or the on-screen Guide button. Keyboard arrows/Enter/Escape/X/Y/Q/E navigate the dashboard.

Mac bundles are generated for arm64/x64. Windows-generated ZIPs are **unsigned and not Mac launch-tested**. Rebuild/sign/notarize on a Mac for distribution; do not disable Gatekeeper. The manual GitHub workflow builds artifacts when dispatched in your own repository. Nothing has been uploaded/published.

## Backups and portability

OS secure storage encrypts local settings. Export uses AES-256-GCM and PBKDF2-HMAC-SHA256 (600,000 iterations), with a 12-character minimum password. Wrong passwords/tampering are rejected before restore. A prior encrypted settings copy is retained. Executable assignments clear after restore: select trusted desktop emulators again. ROMs, BIOS, save-state/battery files, music and recordings stay separate from settings backups.

Android folder URIs/package IDs cannot run on desktop. Import maps supported settings/connections, retains original backup data in encrypted storage, and matches title metadata when desktop ROM folders are scanned. PIA sign-in stays in its official desktop client. Android controller IDs/layouts need desktop configuration; Android library/artwork export remains separate.

## Verification and limitations

Automated tests cover backups, migration, playlist paths, pagination, achievement totals, artwork matching, IPTV parsing/timezones/filtering/resource authorization, PIA status, YouTube URL/runtime verification, recording permissions and emulator timeline/state/settings isolation. Integrated Electron tests exercise the actual index, masked keyboard/controller flow, menu focus, generated WAV decoding, player ownership and app recording followed by WebM playback. Public HLS playback passed through the guide. Generated original NES and GBA programs boot/save/load/rewind inside sandboxed frames; no commercial test ROMs are supplied.

Other bundled cores have not been gameplay-tested. PSP/Dreamcast and modern systems use external emulators. Multi-file CUE/archives require external emulators. BIOS-dependent games need your BIOS. Timeline snapshots are periodic and memory-bounded, not every frame.

YouTube live search succeeded; playback testing received a bot-login challenge on this PC connection. No cookies or restricted-content bypass is used. User IPTV service playback, live Jellyfin/Plex/RomM playback and RetroAchievements account requests were not verified in this release. Codecs depend on Electron/direct streams; automatic server transcoding is not implemented. PIA reports connected to US Florida on this PC, but external geolocation was unavailable. Mac execution and physical ROG Xbox Ally controls remain untested.
