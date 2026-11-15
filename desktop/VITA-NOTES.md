# PlayStation Vita theme — desktop 0.17.0

Select **PlayStation → PS Vita → PlayStation Vita** in console/theme settings or first-run setup.

- Interactive staggered 3–4–3 bubble home and game-library pages, vertical page indicators, four background palettes, touch paging, mouse and controller navigation.
- App-specific LiveArea with a Start gate, information/actions and peel-close corner. Open Applications presents recently opened frontend pages; this is not OS-level app suspension.
- Vita-style system settings, graphite quick menu, silver keyboard, achievement/controller notifications and media controls.
- Local profile introduction followed by the existing optional account and emulator setup. Existing PlayStation portraits remain available.
- External-game quick menu preserves resume, settings and close callbacks; embedded games use a themed pause menu with supported save/load/timeline controls.
- Original synthesized menu cues and shortened vector wave/clock/page-peel startup. These are Vita-inspired recreations, not authentic Sony sound recordings or captured screens.

The theme changes presentation, not emulator compatibility. Vita3K remains a separate emulator. No Sony account integration, firmware, commercial games, Near, Party service, or Vita hardware features are simulated as working services. Brightness affects the frontend only. Desktop service features remain available; no Android update is included in this build.

Research: [Sony screen guide](https://manuals.playstation.net/document/en/psvita/basic/screens.html), [LiveArea guide](https://manuals.playstation.net/document/en/psvita/game/livearea.html), [Quick menu](https://manuals.playstation.net/document/en/psvita/basic/menu.html), [Keyboard](https://manuals.playstation.net/document/en/psvita/basic/osk.html). See `VITA-SOURCES.md` for repository/license review and asset provenance.

Validation includes the desktop unit suite, actual Electron screenshot/navigation checks, and an embedded-core fixture exercising pause, save state and resume. Live server playback and real game compatibility are not established by these UI checks.
