# Desktop 0.6.0 — Xbox dashboard family

Dashboard settings and initial setup now offer Blades, NXE, Metro (2011), Metro Revised (2012), and Xbox Series X|S. Setup applies the chosen dashboard immediately and renders its preview from the live UI.

The Series dashboard has its own Personalization / My background settings. Xbox green and the optional 25th Anniversary recreation are retained independently of other themes. Series panels, Guide, keyboard, IPTV guide, notifications, library and embedded-game Guide share the dark Series treatment. Revised Metro retains the 2012 My Pins/Recent arrangement while the existing 2011 layout remains available.

Startup animations follow the selected dashboard and support skip, mute, volume, reduced motion, disable and preview. They are procedural recreations; animation choreography and all sounds are not verified bit-for-bit console originals. See METRO-REVISED-BOOT-NOTES.md, SERIES-THEME-NOTES.md and assets/theme-audio/sources.json for sources, licenses and remaining approximations.

RomM covers use uniform frames with contained artwork, large-source preference, missing-art fallback and byte-signature MIME detection. This fixes servers labeling JPEG bytes as PNG. Blades My Games no longer stretches square or landscape box art. Existing server artwork was left intact after read-only checks confirmed usable covers; entries without artwork metadata remain visibly marked.

Next milestone: original Xbox dashboard theme, including its distinct green navigation, Guide/settings presentation and startup sequence. It is not yet selectable in this build.
