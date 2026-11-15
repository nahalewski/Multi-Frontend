# Windows 0.14.0 — PS4 setup, Quick Menu and anniversary refresh

- Two-column PS4 Quick Menu, measured against Sony's official reference. Contextual detail pane, customizable categories, game close confirmation and controller/keyboard navigation. Right side remains visible.
- Equivalent in-game menu for embedded cores, with pause/resume, state slots, timeline, volume, controls and captures. External emulator Guide uses the same desktop overlay and restores emulator focus on exit.
- PlayStation 20th Anniversary wallpapers and colored icons from KnuxBoy04/skin.orbis; live menus remain rendered UI, not screenshots. Separate movement/select/back/notification/startup sounds from Kodi-PS4-Soundtheme. Exact commits, hashes and ownership notices: assets/ps4/SOURCES.md and manifest.json. No verified anniversary-specific audio was found; it uses the PS4 sound set.
- Persistent first-run setup: controller, English locale, network/retry/skip, camera notice/skip, time zone and clock format, frontend power/motion preferences, frontend acknowledgement, user name, avatar, optional account and completion.
- Setup resumes after restart; completion is saved only after acknowledgement. Existing profiles and libraries are retained. Camera face login and PSN sign-in are not simulated. The acknowledgement is frontend information, not Sony's license contract. Date/time uses the PC clock, with a dashboard display time zone.
- Returning launches show controller welcome and local user selection; disable this in Settings → System. Replay initial setup there too.

Validation: 128 Node tests; full first-run setup and persistence; both PS4 variants; all-theme setup; desktop/embedded Quick Menu, save/volume/resume behavior and packaged UI checks.
