# PS5 assets and references

## Menu and startup sounds

- [ilhamasy/cleanyourtv-ps5-theme](https://github.com/ilhamasy/cleanyourtv-ps5-theme), commit `7042dfac753711deb6c20453bc864f558e840b8c`: PS5-labelled navigation, enter, back and startup sounds. Retained upstream MIT notice credits Gathin (CleanYourTV) and Ilham Asyari (Norma Launcher/PS5 adaptations). Modification: navigation/enter/back were MP3 data in files named WAV; decoded to PCM WAV for consistent local playback. No PS4 sounds were substituted.
- [DAK404/PS5-Plasma-Soundtheme](https://github.com/DAK404/PS5-Plasma-Soundtheme), commit `a83644caf74b5212dbcf536366a1cca4058e5f00`: notification, login and screenshot sounds. Upstream GPL-2.0 license, README and credits retained. Upstream credits u/spryfigure for identifying the FLAC sources.
- [TheKersalMassive/ReskinPS5](https://github.com/TheKersalMassive/ReskinPS5), commit `567a4dfe82a374352c5ef3f24a8b1518c9a39489`: PS5 label image. This repository has no license file; the image is a Sony mark. It has an opaque light-grey background, not transparency.

Original Sony console sound effects, PlayStation marks and designs retain Sony's rights. Repository code licenses do not establish ownership or relicense Sony's original assets. This is an independent frontend recreation, not official console software. No proprietary SST font, commercial game artwork, background music or console firmware is included in this set.

`manifest.json` records exact source URLs, commits, source hashes (when converted), runtime hashes, byte sizes, codec and duration. The startup cue is 1.56 seconds; a longer animation may continue after this cue. The sound pack is identified as PS5 by its upstream maintainers; no claim is made of a bit-perfect firmware extraction.

## Layout and setup references

- [Sony: customize PS5 Control Center](https://www.playstation.com/en-gb/support/games/customize-ps5-control-center/): single PS press, activity cards above bottom icon controls, Options to move/hide permitted controls.
- [Sony: first-time setup](https://www.playstation.com/en-gb/support/hardware/ps5-get-started-set-up/): controller, language, network, display adjustment, Rest Mode power choices and agreement.
- [Sony PS5 Quick Start Guide](https://www.playstation.com/content/dam/global_pdc/en-au/corporate/support/manuals/ps5-docs/7000/CFI-70XX_PS5_Quick_Start_Guide_Web%24en-au.pdf): home screen and Control Center reference.
- [davidkgriggs/PS5ish](https://github.com/davidkgriggs/PS5ish) and ReskinPS5 were inspected as desktop UI references; no UI code or font copied.
