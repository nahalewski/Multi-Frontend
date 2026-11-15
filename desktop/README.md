# Blades360 desktop / Multi-Frontend

Version 0.12.0. See the [project README](../README.md) for the feature matrix and limitations.

Run `npm ci`, `npm test`, then `npm start`. Build the Windows x64 portable executable with `npm run build:win`. `npm run build:mac` is available for development; Mac behavior has not been validated for this release.

Six themes: Blades, NXE, Metro, revised Metro, Xbox Series and PS3 XMB. The XMB update adds category textures, layered animated waves, improved vertical spacing and a fading startup logo. All existing game, media, settings and karaoke routes remain accessible.

Service accounts, media, ROMs, firmware and emulator installations are supplied by the user. Karaoke multiplayer runs as a separate service. See `assets/xmb/SOURCES.md` and asset-level notices for credits.
