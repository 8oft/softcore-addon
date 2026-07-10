# Changelog

## [2.0.0] - 2026-07-08 - Major Rewrite

### Added
- **Softcore Auto Dupe** category — separate tab from Softcore Utils
- **SwapCloneBackpack** — swap backpack to offhand and clone contents via quick-move
- **SlotChangeBackpack** — duplicate items by dropping or moving backpack inside its own GUI
  - Drop mode: dump items, throw backpack, pick up from ground, recover duped items
  - Move mode: dump items, move backpack to another slot, reopen, recover duped items
  - Configurable interact-mode, dupe-method, click-delay, action-delay
  - Repeat mode — auto-repeat until inventory has only 1 empty main slot left
- **SoftCloseBackpack, SoftCloseChest, SoftCloseVault** — auto-dupe cycle modules (dump > reopen > steal > soft-open > steal > close)
  - Configurable mode (QuickMove/Pickup), interact-mode
  - Repeat mode on all modules
- **AutoSoftClose** — fully automated dupe cycle (legacy)
- **SlotViewer** — draws slot ID numbers on every GUI slot
- **Commands:**
  - `.clickslot <slot> <button> <action>` — raw slot click
  - `.repeat <times> <cmd>` — repeat chat command, `%index%` placeholder
  - `.wait <ms> <cmd>` — delayed command execution
  - `.repeat-delay <ms> <times> <cmd>` — repeat with stagger delay
  - `.action open` — interact with targeted block
  - `.gui save/load/close/softclose/steal/dump/offhand/drop`
  - `.desync` — close GUI server-side only
  - `.disconnectpackets`, `.delaypackets`, `.sendpackets`
- **UiUtilsBridge** — reflection bridge to `com.ui_utils.SharedVariables`
- **EnumArgumentType** — generic enum argument type
- **MsTimer** — scheduled executor utility

### Changed
- **Category**: "Softcore" renamed to "Softcore Utils"
- **BundleDupe** — replaced with full implementation (Timeout/Kick, 5 lag methods)
- **PacketDelay** — added C2S packet filter
- **GuiMoveBackpack** renamed to **SlotChangeBackpack**
- All auto dupe modules use `mc.interactionManager.interactItem()` (proper sequence) instead of raw packet with seq=0
- Delays standardized — removed all hardcoded +500/+800 additions, default `actionDelay` 500→800ms
- ShiftRightClick: sneak held until GUI confirms open
- InventoryRightClick: opens InventoryScreen then `interactItem()` (was broken)
- CloseNormal adds `mc.setScreen(null)`
- All schedule/onActivate callbacks wrapped in `mc.execute()` for main thread safety
- BundleDupe MsTimer callbacks wrapped in `mc.execute()`
- DesyncCommand added `currentScreenHandler == null` guard

### Fixed
- **ui-utils detection** — reflection retries on every call (no permanent failure cache), uses context classloader fallback
- **PICKUP mode** in steal/dump — 2-click sequence (pickup + place) instead of orphaned pickup

### Removed
- All Meteor Rejects–inspired modules (AutoLogin, AutoDisconnect, etc.)
- Standalone `.softclose` command (moved to `.gui softclose`)
- ChunkUnloadBackpack

## [1.0.1] - 2026-05-29 - Vault Manager Update

### Added
- **Vaults Plugin Dupe** - Automatically loot items from Vault plugin GUIs
  - Toggle-on module: activate once, auto-loots all vault pages
  - Sends all `QUICK_MOVE` + `PICKUP` packets in same client tick
  - Configurable packet repeat per slot (1-67, default 5)
  - Smart detection: only activates on GUIs with "vault" in title
  - Skips navigation arrow slots (next page, previous page)
  - Reflection-based `ClickSlotC2SPacket` construction for multi-version compatibility
  - Netty channel batching: all packets queued then flushed at once

### Enhanced
- **Multi-Version Packet Compatibility** - Reflection handles both `ItemStack` and `ItemStackHash`/`HashedStack` across Minecraft 1.21.4, 1.21.10, and 1.21.11
- **Same-Tick Execution** - All inventory action packets sent instantly without delay
- **Reflection Caching** - Constructor and empty stack lookups cached per session
- **Error Logging** - Detailed exception output for debugging packet failures

### Fixed
- **Runtime Reflection** - Field name remapping handled correctly for obfuscated Fabric environments
- **Packet Batching** - Fixed slow one-by-one sending by using `channel.write()` + single `flush()`
- **Empty Stack Type Mismatch** - Correctly resolves `ItemStack` vs `HashedStack` at runtime
- **Build Script**: Updated `build-release.bat` for new version numbering

---

## [1.0.0] - 2026-05-25 - Major Release

### Added
- **Major Release**: Complete rewrite with new module architecture
- **AutoLogin** - Automatic server login with smart command detection
- **AntiCrash** - Client crash prevention from malicious packets
- **Kick Module** - Multiple kick methods (Disconnect, Invalid Position, Self Hurt, Invalid Chat)
- **PacketDelay** - Packet queuing system for exploits
- **BundleDupe** - Bundle-based duplication exploit
- **GuiMacros** - GUI-restricted macro system (inventory/chest only)
- **SoftClose** - Instant GUI closing for containers
- **Comprehensive GUI Support** - Support for all major container types

### Enhanced
- **Multi-Version Support** - Added support for Minecraft 1.21.4, 1.21.10, and 1.21.11
- **Module Categories** - Organized modules into Utility, Combat & Exploits, and Settings categories
- **Configuration System** - Improved settings with proper validation
- **Error Handling** - Comprehensive error handling and user feedback
- **Performance** - Optimized packet handling and module performance

### Fixed
- **Lint Warnings** - Resolved all unused imports and variables
- **Compilation Issues** - Fixed all build errors and warnings
- **Module Registration** - Proper module registration and initialization
- **Packet Compatibility** - Updated packet handling for current Minecraft versions

### Technical
- **Code Quality** - Clean code practices with proper documentation
- **Build System** - Updated Gradle configuration for multi-version builds
- **Dependencies** - Updated to latest compatible dependencies
- **API Compatibility** - Ensured compatibility with latest Meteor Client API

---

## [0.2.0] - 2026-02-24

### Added
- Dual-version build system supporting Minecraft 1.21.4 and 1.21.11
- `./gradlew buildAll` - Builds separate JARs for both versions automatically
- Version-specific JAR outputs

### Changed
- Updated version from 0.1.0 to 0.2.0
- Refactored build.gradle.kts for dynamic version selection
- Added version mapping for yarn, meteor, and minecraft dependencies

---

## [0.1.0] - Initial Release

### Features
- InventoryCloseCanceller, InventoryCloseDelayer, HotbarSwapExploit
- ShiftClickExploit, ChestReopenHelper, RaceConditionTester
- ItemDupeHelper, DeathChestDebugger, PacketLogger, AutoDisconnect

### Target
- Minecraft 1.21.11, Meteor Client
