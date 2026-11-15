# Series Search and Manage Queue

Visual research used [Xbox Wire's April 2023 Search update](https://news.xbox.com/en-us/2023/04/12/xbox-april-2023-update-available-today/) and its official gallery screenshot, plus the [retail Manage Queue screenshot](https://cdn.mos.cms.futurecdn.net/WEf6jS4FSKcBiT22Vndywf.jpg). Search uses a broad input, visible LB/RB filter categories and a cover gallery. Queue uses a management rail, recently completed strip and wide thumbnail/progress rows. Screens are live designed controls, not screenshots.

Search covers local games, installed frontend apps and an explicit RomM catalog tab. RomM uses existing authenticated media commands. No public Microsoft Store or Xbox account search is implied. Text can be entered directly or with the existing controller keyboard. Stale results are discarded when queries change or the view closes.

Manage Queue shows existing transfers and history. It supports cancellation, retry and launch of completed imported games. The current backend runs one content transfer at a time and has no pause/resume, so no fake Pause all control is displayed. The view refreshes every 1.5 seconds while open and releases polling on close.

Integration: load `series-search.js`, `series-downloads.js` and `series-utility.css`. Route Series search to `SeriesSearch.open()`, and guard `Downloads.open()` to route Series to `SeriesDownloads.open()`. Both expose `active()`, `handle(key)`, `close()`. Shared closePanel/theme changes should call both close methods; control priority belongs after the on-screen keyboard and before general panel navigation. Both open routines close existing panels before constructing their own overlays.
