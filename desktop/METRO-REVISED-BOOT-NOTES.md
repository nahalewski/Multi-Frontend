# Metro revised and theme boot research

## Visual references

- [Xbox Wire, October 15 2012](https://news.xbox.com/en-us/2012/10/15/new-xbox-360-dashboard-update-starts-rolling-out-today/): revised layout with more tiles, Recent and My Pins. The 2011 Metro remains available separately.
- [Retail revised home reference](https://img.opencritic.com/article/IhFkHt7otlhy9fbAEwHJJMb3HldHVrfS98WDCBBxesENsnOxDlJkIYZmscFhmY.jpg): measured the three stacked left tiles, two-row hero, right column and two lower tiles. Source image is a study reference, not a UI background or bundled asset.
- [Xbox Wire's official 2020 boot confirmation](https://news.xbox.com/en-us/2020/05/07/xbox-20-20-inside-xbox-recap/): Series X white emblem/wordmark over black, slow reveal and soft soundscape.
- [Official Xbox On boot history video](https://www.youtube.com/watch?v=_oeheCDYSrY): reference for the Xbox 360 silver orb, green ribbons and white background. Current animation is drawn at display resolution, not a captured boot movie; the 360 ribbon choreography is a recreation, not pixel-exact extracted animation.
- The boot emblem shares the exact CC0 Xbox vector path used by `assets/series/xbox.svg` (see the Series asset provenance). The 360 orb fills the vector's negative-space X green and shades the surrounding quadrants silver.

## Repositories inspected

- [ZivvoZ/DashX360](https://github.com/ZivvoZ/dashx360): original 360 audio files and boot MP4s. Used the small named Metro audio cues, not its implementation or video. Credits: Microsoft / Xbox original material; zivvoz / DashX360 archive/project. Its README requests attribution and explicitly retains original asset ownership; this is not an open-license claim for Microsoft assets.
- [davidkgriggs/XBOXSERIESish](https://github.com/davidkgriggs/XBOXSERIESish): MIT Playnite theme; used navigation and activation WAVs. License included. Recording provenance is not specified, so these are described as sourced theme cues rather than verified retail recordings.
- [thedev0ps/Xbox-360-Dashboard-Archive](https://github.com/thedev0ps/Xbox-360-Dashboard-Archive): versioned Blades, NXE, Kinect and Metro dashboard resources, useful for future fidelity studies. No firmware imported.
- [Kaal31/benderthemes](https://github.com/Kaal31/benderthemes) and [deckthemes-assets](https://github.com/Kaal31/deckthemes-assets): inspected theme/asset catalogs; original Microsoft audio attribution means the code license alone does not grant rights to console recordings. No code or assets copied from these.

## Audio honesty

`assets/theme-audio/sources.json` records each downloaded cue URL, byte size and SHA256. Metro/revised use sourced Metro navigation, selection, back, notification, page left/right and 2010-era startup. Blades keeps the existing Blades WAV bank. NXE still uses the existing synthesized NxeAudio cues. Series uses XBOXSERIESish navigation/activation and recreated back/notification/startup tones. Blades/NXE startup still uses the existing `boot_recreated.wav`. These are not all verified original console sounds.

## Integration

Load `theme-audio.js`, `boot-theme.js`, `boot-theme.css`. Route `sound()` to `ThemeAudio.play(move/select/back/tab, left/right)`; route NxeExperience cues there too. `ThemeBoot.play(state.settings)` returns a Promise, is enabled unless `bootAnimation:false`, honors volume/mute and OS/theme reduced motion, and can be skipped with keyboard, pointer or gamepad. `ThemeBoot.stop()` cancels frames, gamepad polling and startup audio. `ThemeBoot.active()` lets shared controls consume input before dashboard navigation.

`MetroTheme.active()` accepts `metro` and `metro-revised`; revised uses the same working library/filter/Guide adapters with a distinct 2012 home grid. Keep both `theme-metro` and `theme-metro-revised` body classes for revised so existing Metro panel/EPG styles apply.
