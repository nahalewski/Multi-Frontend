# Series X|S visual reference and implementation

Research checked 14 November 2026 before implementation.

## Primary visual reference

- [Xbox Wire: Welcome To Your New Xbox Home, 26 July 2023](https://news.xbox.com/en-us/2023/07/26/welcome-to-your-new-xbox-home/). Its official 1920×1080 Home image was inspected directly. Profile is at the top left, a compact five-icon quick-access strip sits at the top center, the upper-middle remains available to wallpaper, recent square game tiles sit low on screen with a larger focused tile, and wide secondary tiles fill the next row. The app adapts Microsoft's Store/Game Pass slots to the user's RomM and media services; it does not imply Xbox service integration.
- [Xbox Wire: 25 years community designs, 24 August 2026](https://news.xbox.com/en-us/2026/08/24/xbox-anniversary-x25-fanfest-gear-community-designs/). This confirms an anniversary customization within the existing Series console interface. Its Klobrille character collage is an earlier, different background.
- [Xbox 25th Anniversary hub](https://www.xbox.com/en-US/xbox-25th-anniversary).
- [Official anniversary logo visual](https://xboxwire.thesourcemediaassets.com/sites/2/2026/08/25thHero_Aug3_16x9-11c00838353536c7e314.jpg): directly inspected to reproduce the wide geometric 25 shape and layered green highlights rather than substituting a plain text numeral. This downloaded reference is outside the packaged assets; runtime draws its own paths.
- [TrueAchievements: new 25th Anniversary logo background](https://www.trueachievements.com/news/xbox-25-anniversary-dynamic-background). The 12 November 2026 report and embedded post identify the newer Xbox mark beside 25 with moving star points and cloud wisps. The implementation recreates that arrangement with original Canvas geometry and animation; it is not the original Microsoft dynamic-background binary or an exact extracted video.

## GitHub resources examined

- [saVantCZ/DashX](https://github.com/saVantCZ/DashX): relevant Series-inspired Playnite reference. README uses square covers; repository landing page does not provide a clear reusable asset license. No files copied.
- [mufflerexoz/XFSE-Compact](https://github.com/mufflerexoz/XFSE-Compact): GPL-3.0 Playnite theme with modern Series/full-screen-Xbox-app styling. Its author explicitly notes that some media originated elsewhere, including directly from Xbox. No source or media files copied into this renderer.
- [bryanshake/theseus](https://github.com/bryanshake/theseus): original Xbox dashboard research resource for the next OG milestone, not a Series asset source.

## Delivered behavior

Series is its own dashboard selection. Within Series personalization, Xbox green and 25th Anniversary are background choices; selecting either updates the current preview immediately. The anniversary option leaves the Series navigation unchanged. Reduced motion freezes the star/cloud movement. Home actions, a 12-game library page, search, filters, options, Guide, media, IPTV and software keyboard remain live app interfaces.

Dashboard backgrounds and UI icons in this module are code-drawn. The Xbox emblem uses the [Simple Icons 11.15.0 Xbox vector](https://github.com/simple-icons/simple-icons/blob/11.15.0/icons/xbox.svg), distributed under CC0 with its license included in `assets/series/SIMPLE-ICONS-LICENSE.md`; Xbox trademarks remain Microsoft's. No game advertising screenshots are used. Box covers remain proportionally contained in uniform square Series cells. Original console sounds have not been claimed or embedded by this module; sound integration is handled separately.

Integration: include `series-renderer.js` after `nxe-renderer.js`, `series.css` after shared/guide/media/keyboard styles, and dispatch DesktopTheme.render/handle when theme is `series`. Persist `seriesBackground` (`default` or `anniversary25`) and `seriesMotion`. Toggle `theme-series` alongside `theme-nxe`. `SeriesTheme.menu()` exposes personalizations, and `SeriesTheme.preview(ctx,w,h,variant)` supplies setup previews.

## Settings fidelity follow-up

The initial implementation inherited the frontend's flat Console settings list. The Series console instead uses a persistent left category rail and a right-hand tile grid. Research for this follow-up inspected the [General settings screenshot](https://www.highspeedinternet.com/app/uploads/2025/12/xbox-access-network-settings.webp) and checked [Xbox Wire's October 2022 settings paths](https://news.xbox.com/en-us/2022/10/19/xbox-october-update-rolls-out-today/) and [official setup guidance](https://news.xbox.com/en-us/2024/11/15/getting-started-with-xbox-series-xs-2024/).

`series-settings.js` adds General, Account, System, Devices & connections, Preferences, Accessibility. General uses the reference's two-column/three-row arrangement: Network settings, Personalization, TV & display options, Online safety & family, Volume & audio output, Power options. The remaining categories organize actual app capabilities rather than adding nonfunctional Xbox Live billing, console firmware, HDMI-CEC, or hardware power controls. For example, TV & display adjusts frontend fullscreen, Power options controls startup animation and app exit, Account links the configured personal services, and Accessibility exposes controller remapping and motion/audio controls.

Controller and keyboard support spatial tile navigation, rail entry/exit, category bumpers and settings search. `SeriesSettings.close()` only removes its injected rail and styling, allowing shared menus to cleanly replace this page. Root `settings()` delegates to `SeriesSettings.open()` only for Series; other dashboard settings remain unchanged.

## My games & apps and More options follow-up

The previous library was a plain cover grid with collection tabs. The redesigned library follows the actual Series rail: Games, Apps, Groups, Full library, Play history, Manage. The primary reference is [Xbox Wire's April 2026 console update](https://news.xbox.com/en-us/2026/04/30/april-xbox-update-2026/) and its [official Groups screenshot](https://xboxwire.thesourcemediaassets.com/sites/2/2026/04/More-groups-on-Home-e4ec65657104850977c1-1536x864.jpg), inspected before design. A [game-options reference](https://images.minitool.com/partitionwizard.com/images/uploads/2024/12/black-ops-6-dev-error-10493-1.png) was inspected for the compact dark popup and outlined selection treatment.

`series-library.js` draws the rail and sort/filter toolbar around the live library; uses 12 square cover cells per page; provides service app tiles; displays Favorites, Recently played and console-based groups; and routes Manage to actual download/storage/import tools. Groups are generated collections, not falsely advertised editable Xbox account groups. User game pins are stored locally through `game:pin` and prioritized on Home.

More options is a real modal context card over the library. Game card shows local title information. Manage game and add-ons exposes existing game/emulator/artwork/copy actions supplied by the shared game-options implementation. Unsupported Microsoft uninstall/account actions are not presented. Filtering and sorting also use compact contextual popups. Source and reference images are not used as runtime screenshots.
