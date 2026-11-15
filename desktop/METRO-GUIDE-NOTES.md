# Desktop 0.5.0

IPTV now prioritizes channels with provider guide IDs without removing channels. Xtream schedules are filtered by time before limiting their size, so long historical listings cannot displace current programmes. Provider-managed XMLTV feeds use per-channel requests instead of downloading the oversized whole feed; custom XMLTV URLs still work. Successful schedules are persisted, failed requests can retry independently, and unavailable schedules are distinguished from loading schedules.

The read-only live-account UI check started with an empty programme cache and rendered current programme cells for six visible channels. No stream was opened during this check. Channels for which the provider supplies no EPG cannot display an invented schedule.

Metro is an initial functional desktop theme with horizontal categories, green tiles, game covers, profile score, gamepad navigation, settings, Xbox Guide and live setup preview. It shares the frontend's existing media and emulator functions. It is a recreation in canvas/CSS, not a dashboard screenshot. Further visual refinement toward the original 2011 dashboard is possible; it is not a pixel-exact finished replica.

Reference: [Microsoft's November 2011 Metro dashboard announcement](https://blogs.microsoft.com/ai/xbox-360-dashboard-update-is-arriving-december-6/).

Select System Settings → Dashboard theme → Metro (2011). Blades remains the initial setup default. Saved themes and accounts are preserved.

Checks: `npm test`, `node tools/run-ui-audit.cjs`, and the manual read-only `electron --disable-gpu tests/iptv-guide-account-live.cjs`.
