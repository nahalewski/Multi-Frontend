# Blades360 Desktop 0.11.0 — Karaoke Community

- Music → Karaoke → Community in every dashboard theme, plus a Community tab inside Karaoke.
- Share in-app solo/duet recordings, browse and search shared performances, sort newest/top rated, play/seek, and like/dislike. Each user has one changeable vote per recording; self-voting is disabled. Accounts remain optional.
- Record a solo from a prepared song. New solos and duets retain aligned, isolated vocal tracks and the backing audio.
- Stitch a shared performance: preview a singer's isolated vocal, choose either singer to replace, or add a second voice to a solo. Record your own microphone against the backing and retained singer. The new recording preserves attribution and never overwrites the original.
- Upload finished mixes for listening/ratings, or upload aligned backing and one/two separate vocal files to enable replacement. Uploads begin private and can be resumed from My recordings after a failed part upload.
- Every in-app singer approves public sharing of their voice. Stitching is optional and requires all contributing singers' permission. An existing permission to stitch follows the retained vocal into a derivative; the new singer still chooses whether to publish their own take. Unpublishing blocks new listening/stitch requests; existing saved derivatives remain separate recordings.
- Community controls match the selected theme and use the existing controller/text-entry integration.

Validation: server integration checks cover actual FFmpeg mixes, both singers' permissions, votes, private access, stem uploads, solo additions, original preservation, unpublishing, and cancelling a countdown. Electron tests exercise a real fake-microphone stitch, Community rendering in six themes and publication of the new recording. Existing duet capture also passed.

Limit: finished mixes and older recordings made before vocal-track preservation can be shared, but cannot reliably replace a single singer. Supply separate vocals or make a new in-app recording to enable stitching. This is a Windows and multiplayer-server update; no Android APK is rebuilt.
