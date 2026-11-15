# Wii / Wii U themes — Windows 0.19.0

Both themes are selectable under Nintendo in initial setup and Console / Theme settings. They render live frontend data, not menu screenshots. Wii uses a 4×3 wide-channel grid, silver bottom bar, clock/date, SD/storage and Message Board. Wii U uses a separate 5×3 rounded software grid, user badge, applet row and optional local channel plaza. Both support controller/pointer navigation, paging, channel launch previews, game options, settings, media, keyboard, notifications and HOME overlays. Embedded games retain save slots, timeline and controller settings in the themed overlay. External emulator overlays use the existing close/resume bridge.

Wii U's local plaza is a frontend adaptation using the current profile and real channels; it is not Miiverse or the original networked WaraWara Plaza. Mii/User settings open the frontend's profile manager, not a Nintendo Mii editor. No Nintendo account or discontinued Nintendo online service is simulated.

## Visual references

- Nintendo Wii Menu: https://www.nintendo.com/en-gb/Wii/Wii-Channels/Wii-Menu/Wii-Menu-749371.html
- Wii Channels and Settings manual (channel grid, launch screen, settings, HOME): https://assets.nintendo.eu/image/upload/v1635389389/NAL/Support/WiiOperationsManualChannelsAndSettings.pdf
- Wii U manual (HOME, user/system settings): https://csassets.nintendo.com/noaext/image/private/t_KA_PDF/WiiUP_ENG_FINAL.pdf
- Wii UI reference: https://github.com/andrewplus/Wii.JS
- Additional reference: https://github.com/danintosh/Wii-Menu-HTML
- Wii U homebrew menu reference/resources: https://github.com/wiiu-env/launchiine

## Sound and logo provenance

Wii move/select/back/startup files come from Wii.JS. Wii U select/back interface cues come from launchiine. Exact source URLs and SHA-256 hashes are recorded in `assets/wii/sources.json`; upstream GPL-3.0 license texts accompany them. These are upstream-hosted recordings, not a claim that Nintendo authored or licensed the recreation project. No upstream application code was copied into the theme renderer.

No verified Wii U boot recording was found in the reviewed repositories. Its startup chime is an original synthesized recreation, explicitly not the original console audio. Startup visuals are original canvas animations; the Wii health/startup text is adapted for this frontend. They honor disabled startup animation, mute, volume and reduced motion.

Wii and Wii U logos reuse the existing console-logo assets sourced from the EmulationStation Carbon theme; retained upstream attribution is in `assets/console-logos/UPSTREAM-readme.txt`. Console names/logos and original console assets belong to their respective owners. Multi-Frontend is independent and unaffiliated.

Android is not updated by this desktop theme build.
