# PS4 emulation — shadPS4

Researched November 15, 2026. The official shadPS4 project was selected for its active development, stable Windows SDL releases and substantially greater GitHub adoption among the projects compared. Stars are popularity, not a compatibility rating.

| Official repository | Stars at review | Latest push at review |
| --- | ---: | --- |
| [shadPS4](https://github.com/shadps4-emu/shadPS4) | 34,184 | 2026-11-14 |
| [Orbital](https://github.com/AlexAltea/orbital) | 3,652 | 2024-04-06 |
| [fpPS4](https://github.com/red-prig/fpPS4) | 2,129 | 2026-11-14 |
| [GPCS4](https://github.com/Inori/GPCS4) | 2,030 | 2023-08-29 |
| [ChonkyStation4](https://github.com/liuk7071/ChonkyStation4) | 160 | 2026-09-20 |

The latest stable official release observed was **v.0.21.0**. Installation resolves the official latest release dynamically and verifies its GitHub SHA-256 asset digest. No third-party fork or unofficial repack is used.

## Using it

1. In first-run setup or Emulators, enable **shadPS4**. Install selected emulators, or let automatic installation run on first game launch.
2. Keep extracted PS4 games beneath a folder named `PS4` or `PlayStation 4`, with each game in its own subfolder containing `eboot.bin`. Scan that folder. The game folder name becomes the library title.
3. Existing `shadPS4.exe` installs are detected by the Windows emulator scanner. Managed installs are assigned to PlayStation 4 automatically.
4. Fullscreen can be changed through the frontend's per-game emulator settings. The SDL core launches directly with `--game`, `--fullscreen=true|false`, and `--same-process`, keeping the running game attached to the frontend's process lifecycle.

PKG files are installers, not directly launched by this adapter. Use the [official QtLauncher](https://github.com/shadps4-emu/shadps4-qtlauncher) to prepare your own games when needed, then scan the extracted folder. Required system modules and games are not supplied or downloaded by Multi-Frontend. No PS4 save-state, rewind or resolution-control support is claimed.

PS4 emulation remains experimental; consult the [official compatibility tracker](https://github.com/shadps4-emu/shadps4-game-compatibility). This integration targets the Windows desktop frontend, not Android. CLI behavior was checked against [v0.21.0 source](https://github.com/shadps4-emu/shadPS4/blob/v.0.21.0/src/main.cpp).
