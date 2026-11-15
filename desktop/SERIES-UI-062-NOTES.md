# Series UI review — 0.6.2

Replaced the inherited page structures in the desktop Series theme:

- My games & apps uses a library rail, sort/filter controls, square artwork grid, groups, apps, full library and management entry points.
- Game options use a compact contextual dialog with game card, favorites, Home pinning and existing game management actions. Home pins are saved per game.
- Marketplace has a Store-style navigation rail, platform browsing, cover grid, search and product/download pages for the personal RomM catalog.
- Media Center has branded entertainment app tiles. Jellyfin and Plex have library grids and authenticated poster retrieval.
- Search uses category tabs and result tiles, including local games/apps and explicit RomM search. Gamepad entry opens the app keyboard.
- Active Downloads uses Manage Queue, recently installed artwork, progress, history and real cancel/retry/play actions.
- Remaining settings, profile, achievement and playback subpages use a Series-specific shell instead of the NXE detail decorator. YouTube results use thumbnail cards.
- New views clean up async requests/focus when navigating away. Scrollbars are hidden while scrolling remains enabled.

These are native frontend recreations, not captured console screens. Personal services replace Microsoft services. Download pause/resume, Xbox network social features and commercial Store purchases are not represented as working features.

Reference structure: Xbox Wire's August 2020 Microsoft Store update, April 2023 Search update, and Xbox library/queue/context-menu references documented in SERIES-THEME-NOTES.md and SERIES-UTILITY-NOTES.md.

Validation: unit suite, sequential cross-theme UI audit, integrated Series route test, rendered screenshot inspection and packaged Series journey check.
