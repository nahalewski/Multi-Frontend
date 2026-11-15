# Software updates — Windows 0.18.1

All console themes now open the same themed in-app software update checker. Checks cover the frontend, assigned/managed standalone emulators, RomM, Jellyfin and Plex. Windows Update handoffs are removed; Microsoft Store and winget update categories are not exposed. Existing emulator installation support is separate and unchanged.

Checks are read-only. Details show installed/server and latest published versions plus a fixed official release/instructions link. Server upgrades must be applied on the server host with its existing installation method. No server restart, OS update, automatic downgrade, or executable launch occurs during a check.

Results are cached for ten minutes, invalidated when service/emulator configuration changes, and manually refreshable. Concurrent requests share one check; provider failures are isolated. Unknown executable versions and rolling/git builds are explicitly reported as unknown rather than incorrectly current. Embedded EmulatorJS cores stay pinned to the verified frontend manifest and update with frontend releases.

Version sources:

- Frontend and GitHub-hosted emulators: official repository latest published non-prerelease release.
- Eden: https://git.eden-emu.dev/eden-emu/eden/releases
- Dolphin: https://dolphin-emu.org/update/latest/beta
- RomM: configured server `/api/heartbeat`, `SYSTEM.VERSION`; https://github.com/rommapp/romm/blob/master/backend/endpoints/heartbeat.py
- Jellyfin: configured server `/System/Info/Public`, `Version`; https://github.com/jellyfin/jellyfin-kodi/blob/master/jellyfin_kodi/jellyfin/api.py and https://github.com/jellyfin/jellyfin/releases
- Plex: configured server root JSON `MediaContainer.version`/`platform`, public platform-matched release from https://plex.tv/api/downloads/5.json; API documentation https://developer.plex.tv/pms/

Credentials remain in the main process, are sent only to their configured server, and are not returned in update reports. Release links are selected from a fixed product allowlist.
