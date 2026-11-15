# Nintendo Switch theme — desktop 0.18.0

Initial working Switch-inspired theme: horizontal recent software row, profile shortcuts, circular utility icons, All Software grid, controller/touch navigation, settings sidebar, Basic White/Black, right-side Quick Settings, keyboard/media styling, external and embedded game menus, original synthesized cues and a short slide/click-style startup recreation.

Select Nintendo → Nintendo Switch during setup or console/theme selection. All software includes the frontend library, not just Switch games. The store opens your RomM catalog; online/profile opens frontend profiles. No Nintendo account, eShop purchasing, Switch Online, GameShare or Virtual Game Card services are impersonated. The current utility row adapts the classic Switch arrangement to working frontend tools. Brightness controls the frontend, not Windows hardware. Emulator support remains separate (Eden for Switch).

## Research, November 15, 2026

- [Nintendo HOME overview](https://en-americas-support.nintendo.com/app/answers/detail/a_id/22308/~/home-menu-overview): software row, profiles, system shortcut functions.
- [Nintendo Quick Settings](https://en-americas-support.nintendo.com/app/answers/detail/a_id/22321/p/1076): HOME-triggered quick controls.
- [Nintendo System Settings](https://en-americas-support.nintendo.com/app/answers/detail/a_id/22526): category structure.
- [OJ7/NS-Horizon-React](https://github.com/OJ7/NS-Horizon-React), MIT: inspected HomePage, GameScroll, Header, ButtonsToolbar and AllSoftwarePage styles for reference. Its game row begins near the upper fifth and utility row around the lower two-thirds. No source/assets copied; this app uses its existing canvas/DOM runtime rather than React.
- [XorTroll/uLaunch](https://github.com/XorTroll/uLaunch), GPL-2.0: reviewed as an independent themeable HOME replacement; no code/assets copied.
- [TsilaAllaoui's React/Tauri recreation](https://github.com/TsilaAllaoui/Nintendo-Switch-HomeMenu---React-Tauri): no repository license returned by GitHub; no assets copied.
- [Mipaichik's Switch sound pack](https://github.com/Mipaichik/Nintendo-Switch-UI-Steam-Deck-Sound-Pack-): explicitly attributes copyright to Nintendo, no redistribution license. Located for research, not bundled. `switch-audio.js` instead synthesizes original short navigation/confirmation/notification cues, clearly not authentic Nintendo recordings.

This is a desktop implementation. Android has not received this theme. Vector graphics and UI are rendered live, not screenshots. Existing portrait collections remain accessible through the avatar picker; no new Nintendo character portrait pack is included.
