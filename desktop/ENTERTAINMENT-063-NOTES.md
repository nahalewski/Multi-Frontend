# Series entertainment — 0.6.3

Jellyfin, Plex, YouTube and Zorba are square app tiles on Series Home and in My games & apps / Apps. Home keeps four game slots alongside the four service tiles; the full library remains available.

YouTube in the Series theme uses a dedicated dark TV-style navigation rail, three-column thumbnail grid (two in compact windows), duration labels, channel names, video detail artwork and controller navigation. Existing search, public playback, local subscriptions, Watch Later, favorites, history, queue and settings remain connected to the existing backend. Home shows actual locally saved/history items, not invented recommendations. No YouTube account sign-in is introduced.

The Series IPTV guide now recreates Xbox One OneGuide's dark full-screen grid, flat rectangular cells, green programme selection and channel/favorites navigation. It retains the app's preview, programme data, logos, filters, options and DVR. The Blades, NXE and Metro guide styles are unchanged.

Visual references:
- https://news.xbox.com/en-us/2015/04/08/xbox-one-over-the-air-tuner/ (official OneGuide screenshots)
- https://news.xbox.com/en-us/2014/08/15/xbox-one-system-update-september-preview/
- https://9to5google.com/2026/05/08/youtube-app-tv-sidebar-subscription-library-access/ (YouTube television navigation)

Validation includes the existing UI/unit suites, an entertainment journey test covering app navigation, YouTube details/search/controller focus, guide settings and compact viewport, rendered preview inspection and a packaged-build check. Live service availability is not asserted by fixture tests.
