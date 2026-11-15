# Series settings review — 0.6.5

## Implemented

- My games & apps: aggregate used-storage meter, percentage and free space from Windows volumes; refreshed at most every 30 seconds while visiting the library.
- Storage devices: capacity cards for mounted Windows volumes, registered Xbox drive identification, free/used space and library counts. Device-specific game browsing and existing transfer/DVR/content management. This does not register other drives as Xbox drives or change download destinations.
- TV & display options: detected monitors, driver-advertised resolution and refresh-rate choices, native HDR capability/state on supported modern Windows, 4K/120 Hz capability details, move frontend to a monitor and fullscreen.
- Resolution/refresh/HDR trials use a separate hidden Windows helper. Unconfirmed changes revert after 20 seconds, including if the frontend exits. Keep saves a display mode only after explicit confirmation. No monitor modes were changed during development; helper mutation paths were tested with a stub and actual capabilities were read-only.
- Network: connected adapters, IP/gateway/DNS/profile details, Microsoft connectivity-endpoint test, existing PIA controls. HTTP response time is not labeled gaming ping or download speed.
- Power: Windows login startup, app-lifetime keep-display-awake, startup animation and frontend exit.
- Console information: frontend version, Windows version, CPU/core count, memory, library count, rename, locale/time-zone information.
- All six settings categories retain their existing family, profile, accounts, backup, emulator, capture, controller and accessibility controls. New System entries cover Time & language and Windows Update.

## Deliberate Windows handoffs / limits

Explicitly labeled buttons ask to open Windows for Wi-Fi joining, IP/DNS edits, audio device/system volume, Bluetooth pairing, display scaling/calibration, advanced HDR, Windows Update, OS language/time and OS accessibility. These are not disguised as completed in-app controls.

VRR, Dolby Vision, driver-specific enhancements and Auto HDR are not toggled through unverified registry values. HDR controls are capability-gated; older Windows builds without the modern API show unknown and offer the Windows page. The frontend is not an HDR video renderer merely because desktop HDR is enabled.

Storage usage includes Windows and other files, not just games. Copying installed PC games remains an archive copy; launcher relocation/DRM is not implied. No format, uninstall, network disconnect, shutdown or restart action was introduced.

## Verification and references

Native read-only checks on this PC: two displays with advertised modes, one HDR-capable and one not; twelve storage volumes; four active network adapters. Tested in-app storage cards and filtering, resolution/refresh confirmation flow, network diagnostics UI, startup action, explicit Windows handoff, small-window bounds, keyboard and existing theme routes.

- https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-changedisplaysettingsexa
- https://learn.microsoft.com/en-us/windows/win32/api/wingdi/ne-wingdi-displayconfig_device_info_type
- https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-querydisplayconfig
- https://news.xbox.com/en-us/2020/11/06/is-your-tv-ready-to-power-your-dreams/
- https://www.t3.com/reviews/seagate-expansion-card-review (storage-screen visual reference)
