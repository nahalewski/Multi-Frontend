# Media branding and themed programme guides — 0.5.2

Media Center shows bundled YouTube, Jellyfin, Plex and Zorba.network IPTV logos. NXE and Metro detail panes preview the focused service logo. Sources are recorded in assets/services/SOURCES.md. Existing service actions and connections are preserved.

Channel rows show the provider's channel logo, contained without stretching. A three-character badge remains visible until an image loads and on missing or failed images. Failed URLs retry after ten minutes, so guide navigation does not repeatedly request unavailable images. Logo URLs are restricted to HTTP(S) without embedded credentials. Images are lazy-loaded and send no referring page URL.

Blades retains its original guide colors, preview layout, programme grid and column width, with only channel branding added. NXE uses green/silver gradients, raised controls and light programme cells. Metro uses gray backgrounds, flat rectangular controls, Segoe UI and green focus outlines. Guide settings dialogs and fullscreen preview follow the same theme. Fullscreen video remains borderless.

Validated through media-branding-smoke.cjs in all three themes, including channel logo load/failure/missing cases, service logo decoding, preserved Blades column sizing and guide navigation/settings. Included in the full desktop UI audit.
