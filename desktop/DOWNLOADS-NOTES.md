# Windows 0.5.3

RomM downloads now retain platform metadata and enter My Games immediately. Startup recovers recognized ROM files from older download metadata on the registered Xbox drive. Covers and catalog details are fetched through authenticated backend requests.

Active Downloads is available from Marketplace, Guide and library tools in Blades, NXE and Metro. It provides progress, cancellation, transfer history, retry and launching completed games. One content transfer can run at a time; queueing and pause/resume are not implemented.

Windows detects supported emulator executables in common installation and portable-emulator locations without replacing manual mappings. External emulator installation and required BIOS remain user-managed. Vita3K is detected but requires manual setup.

Embedded supported systems use verified bundled cores first. Missing or corrupt cores are fetched from pinned official EmulatorJS packages, checked against package and file hashes, and cached before launch. This does not add support for consoles without a configured embedded core.

Validation: 71 unit tests, 20 existing UI checks, a new three-theme Downloads/RomM smoke test, a fresh-cache GBA core download and save/load smoke test, and authenticated live RomM artwork/library recovery.
