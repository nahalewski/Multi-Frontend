# Multi-Frontend 0.19.0 — Wii and Wii U

## Windows

- Added selectable Wii and Wii U themes under Nintendo in initial setup and Console & Theme settings.
- Wii 4×3 channel grid, Wii U 5×3 software grid, channel launch previews, paging, real game artwork, HOME menus and controller navigation.
- Themed settings, keyboard, notifications, media controls and in-game menus. Existing supported save states, gameplay timeline and controller settings remain accessible.
- Startup animations and console logos. Wii uses sourced navigation/startup cues; Wii U uses sourced interface cues and an original recreated startup chime, not the original recording.
- Software update checks for the frontend, installed/assigned emulators, RomM, Jellyfin and Plex. Removed Windows Update handoffs; no Microsoft Store or winget update categories. Checks do not install server updates.

## Included desktop changes since 0.12.0

- PS4, PS4 Anniversary, PS5, Vita and Switch themes, with setup and themed overlays.
- Guided console/theme/account/emulator setup, avatar selection and shadPS4 installation support.
- Window controls that fade for controller use, and the latest theme and navigation refinements.
- Live 0.18.0 feature commercial with real library artwork and an original upbeat soundtrack, plus new Wii/Wii U screenshots in the repository.

## Download and verification

Download `Blades360-0.19.0-win-x64.exe` and verify it against `Blades360-0.19.0-win-x64.exe.sha256`. This is an unsigned Windows x64 portable development build. Existing settings remain in application data; back them up before upgrading.

158 automated tests passed. Live-app checks covered both Nintendo themes, launch previews, paging, Back/HOME navigation, settings grid controller movement, media, keyboard, boot rendering and portrait layout. Sourced audio files were verified to decode.

## Limits

- Android is unchanged. Its existing 1.0 debug APK remains available in [v0.12.0](https://github.com/nahalewski/Multi-Frontend/releases/tag/v0.12.0); this release does not supply a new Android build.
- Wii U's local plaza uses the current frontend profile and real channels; it is not Miiverse/WaraWara online service. Profile management is not a Nintendo Mii editor.
- Emulator compatibility varies. Embedded core revisions are pinned to the frontend; unknown/rolling executable versions are not falsely reported as current. Server upgrades require the host's existing installation method.
- macOS build tooling exists but no tested Mac binary is included.

See `desktop/WII-NOTES.md`, `desktop/SOFTWARE-UPDATES-NOTES.md` and asset provenance/license files for sources and implementation details. No personal accounts, tokens, ROMs, BIOS or server data are included.
