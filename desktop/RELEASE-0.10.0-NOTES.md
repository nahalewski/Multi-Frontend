# Blades360 Desktop 0.10.0

## XMB
- PS3 cursor, enter, back and coldboot audio from the attributed MaXMB assets; notifications use the enter cue, not a claimed original trophy sound.
- Trophy-style wording and the existing XMB notification presentation.
- Multiple local users, Create User / Switch User, independent optional online sessions.
- PlayStation Store-style personal RomM catalog, platform browsing, search, game information, artwork and downloads.
- Triangle-style video control panel: play/pause, stop, seek, next, screen mode, volume, information, subtitle/speed settings. Existing providers and media features remain available.

## Karaoke in every theme
- Music → Karaoke, with an additional direct XMB Music entry.
- Local music-folder import, cached instrumental separation, timed lyrics from matching .lrc, LRCLIB, or labelled local transcription.
- Solo singing, two-person remote duet rooms, explicit recording consent, live partner monitor, synchronized backing, separately captured microphone takes and aligned MP3 mixes.
- Both singers can access their saved duet. Private per-user libraries/recordings; optional accounts or guest sessions. Guest account registration preserves owned recordings.
- Upload retry retains an unsaved take in the open client; app shutdown waits for karaoke to finish saving.
- Remote access: https://mtp.orosapp.us. Independent server and dedicated Cloudflare tunnel launch at this Windows user's sign-in. The server continues after the frontend closes. This PC must be awake/online.

## Validation
- 123 desktop unit/integration tests, 41 existing UI checks, XMB setup smoke, six-theme karaoke/UI smoke including the real HTTPS embedded frame and fake microphone permission check.
- Two isolated fake-microphone clients exercised consent, start synchronization, voice relay, early stop, real FFmpeg mixing, and shared private recording ownership.
- Audio-separator produced real stems and local Whisper transcription completed. Synthetic test audio was used; no real user microphone was recorded.

## Limits / remaining work
- Internet partner monitoring has latency; vocal removal and transcribed lyrics are not perfect. Import corrected LRC when needed.
- Server game-room/input-relay foundations are implemented; emulator-specific netplay adapters are not yet connected.
- Account recovery, friends/invites, pitch scoring and shared karaoke queues are not implemented. User-created local profiles share this console's game library/settings; online karaoke libraries and recordings are separate.
- This release updates Windows. The Android app has not been rebuilt with these additions.
- XMB is a recreation for the frontend, not Sony firmware or a PSN connection. Store uses the user's RomM catalog.

Source references: Sony PS3 user guide (video panel, Users, Store), MaXMB audio assets in `assets/theme-audio/xmb-sources.json`, audio-separator, faster-whisper, LRCLIB and Cloudflare. See the multiplayer service README for deployment details and limits.
