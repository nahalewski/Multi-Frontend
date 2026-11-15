# Blades360 multiplayer service

Independent Node service for optional accounts, guest sessions, karaoke preparation, remote duet rooms, voice relay and private duet recordings. It runs independently of the desktop frontend. The configured public address is https://mtp.orosapp.us; Cloudflare connects to loopback port 8788.

Windows data: `%LOCALAPPDATA%\Blades360 Multiplayer\data`; runtime: `app`; separator Python environment: `python`. The hidden supervisor restarts Node and the dedicated tunnel when either exits. Startup uses the current user's Windows Run entry, so it starts at sign-in, not before sign-in. Closing the frontend leaves this server running. The PC must remain awake and connected for remote singers.

The Python environment requires Python 3.11, `audio-separator[gpu]`, and `faster-whisper`. FFmpeg and FFprobe are required. Separation uses UVR MDX instrumental HQ; timed lyrics use a matching local LRC, LRCLIB, or labelled local Whisper transcription. Results are cached per user and source hash. Separation/transcription are imperfect; users can replace lyrics with LRC. Audio never goes to an AI API.

Duets require explicit Ready/record consent from both singers. Each client records its original microphone and uploads its take. FFmpeg aligns the takes against a shared server clock, mixes them with the backing, and grants both participants access. Use headphones. Internet monitoring has delay; this is not zero-latency remote performance. Raw takes are removed after a successful mix. Failed takes remain for recovery. Accounts are optional; guest access depends on retaining that browser/desktop profile's session. Passwords use scrypt, bearer tokens are hashed on disk, and desktop tokens are encrypted in settings. There is no password-recovery flow yet.

Limits: 80 MB/15 minutes per source, 100 tracks per user, 1 GB source quota per user, 20 GB server source quota, 5 GB free-disk reserve, 50 concurrent rooms, two singers per room, 100 recordings per user. Keep server data backed up. Cloudflare TLS protects traffic; the local database/audio files are protected by the Windows user account, not separately encrypted at rest.

Game lobbies and a bounded input relay are present as a foundation. Emulator-specific multiplayer/netplay adapters are not yet implemented. Do not describe the relay as working cross-emulator multiplayer.

## Community and stitching

Community supports explicit publishing, search, newest/top-rated views, playback, and one like/dislike per user per performance. In-app collaborators must each approve publishing; allowing stitching is a separate opt-in. Guests can participate, with their identity tied to the saved guest session. New recordings retain aligned lossless vocal stems and a backing copy under `data/recordings/<id>`, in addition to the mixed MP3. Original temporary microphone uploads are removed after mixing.

Stitching copies the backing and retained singer into a new recording, then aligns the new singer's microphone. A two-voice performance requires selecting one voice to replace. A solo permits either replacement or adding a second voice. Singer attribution and source lineage are retained; the original audio is never edited. Existing stitch permission supplies approval for the retained contributor; the new contributor must approve their own publication. Withdrawing approval unpublishes the source and blocks new stitches, while previously saved derivatives remain independent.

Uploads accept a finished mix (listening/ratings only) or an aligned instrumental plus one/two isolated vocals. Sources must share the same start time. Older mixed-only recordings do not have recoverable individual voices and are explicitly marked unavailable for vocal replacement. Private source data, voter identities and server file paths are not exposed by Community responses. FFmpeg processing is bounded and rejects playlist/network-input formats. Back up the entire data directory to preserve stems, votes, sharing permissions and accounts.

Tests: `node --test tests/*.test.cjs`; desktop `tests/karaoke-duet-smoke.cjs` exercises two fake microphone clients and a real FFmpeg mix. No real microphone is used in that test.

References: https://github.com/nomadkaraoke/python-audio-separator · https://github.com/SYSTRAN/faster-whisper · https://lrclib.net/docs · https://developers.cloudflare.com/network/websockets/
