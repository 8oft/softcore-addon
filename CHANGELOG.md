# Changelog

## [2.0.0] - 2026-07-08 - Major Rewrite (continued)

### Added
- **SwapCloneBackpack** — swap backpack to offhand and clone contents via quick-move
- **SlotChangeBackpack** — duplicate items by dropping or moving backpack inside its own GUI
  - Drop mode: dump items, throw backpack, pick up from ground, recover duped items
  - Move mode: dump items, move backpack to another slot, reopen, recover duped items
  - Configurable interact-mode (RightClick/ShiftRightClick/InventoryRightClick), dupe-method (Drop/Move), click-delay, action-delay
  - Repeat mode — auto-repeat until inventory has only 1 empty main slot left
- **SoftCloseBackpack, SoftCloseChest, SoftCloseVault** — auto-dupe cycle modules (dump > reopen > steal > soft-open > steal > close)
  - Configurable mode (QuickMove/Pickup), interact-mode (RightClick/ShiftRightClick/InventoryRightClick)
  - Repeat mode on all modules
- **DesyncCommand** NPE guard — added `currentScreenHandler == null` check before accessing `syncId`

### Changed
- **GuiMoveBackpack** renamed to **SlotChangeBackpack**
- All auto dupe modules now use `mc.interactionManager.interactItem()` (proper 1.21.1 sequence number) instead of raw `PlayerInteractItemC2SPacket` with hardcoded sequence=0
- Delays standardized — removed all hardcoded +500/+800 additions, each phase uses `actionDelay` consistently with nested schedule pattern for reopen steps
- Default `actionDelay` 500 to 800ms for consistent timing
- ShiftRightClick: sneak now held until GUI confirms open (not released after 100ms)
- InventoryRightClick: now opens InventoryScreen client-side then calls `interactItem()` (was broken: sent ClickSlotC2SPacket button=1 which only splits stacks)
- CloseNormal adds `mc.setScreen(null)` to prevent stale GUI state
- All `schedule()` callbacks wrapped in `mc.execute()` for main thread safety
- `onActivate()` wrapped in `mc.execute()` for main thread safety
- BundleDupe — MsTimer callbacks wrapped in `mc.execute()` for main thread safety

### Fixed
- **BundleDupe** — MsTimer callbacks calling `toggle()`, `sendInteractItem()`, `executeLagMethod()` on background thread (now wrapped in `mc.execute()`)
- **All dupe modules** — `currentScreenHandler == null` guard added before access
- **All dupe modules** — `schedule()` onDone callbacks now run `mc.execute()` (was running MsTimer thread)
- **Reopen timing** — interact and GUI check were scheduled at same delay, now nested so check runs `actionDelay` after interact
- **RightClick/ShiftRightClick interact** — now uses `mc.interactionManager.interactItem()` with correct sequence (was hardcoded `PlayerInteractItemC2SPacket(..., 0, ...)` which 1.21.1 servers reject)

### Removed
- **ChunkUnloadBackpack** — removed entirely

## [2.0.0] - 2026-07-08 - Major Rewrite (original release)

### Added
- **Softcore Auto Dupe** category — separate tab from Softcore Utils
- **AutoSoftClose** — fully automated dupe cycle: dump > reopen > steal > soft-reopen > steal > close
  - Configurable `open-command`, `click-delay`, `action-delay`, `mode` (QuickMove/Pickup)
  - Works on any storage plugin via command
  - No GUI-required precondition — toggles from anywhere
- **SlotViewer** — draws slot ID numbers on every GUI slot (container + inventory)
  - Configurable text color
- **Commands:**
  - `.clickslot <slot> <button> <action>` — raw slot click with optional `times` + `type`
  - `.repeat <times> <cmd>` — repeat chat command N times, `%index%` placeholder
  - `.wait <ms> <cmd>` — delayed command execution
  - `.repeat-delay <ms> <times> <cmd>` — repeat with delay between each
  - `.action open` — `PlayerInteractBlockC2SPacket` to open targeted block
  - `.gui save` / `.gui load` — save/restore GUI state via ui-utils
  - `.gui close` — close GUI normally
  - `.gui softclose` — close GUI without packet (moved from standalone `.softclose`)
  - `.gui steal <pickup|quickmove> [delay]` — mass take items with staggered clicks
  - `.gui dump <pickup|quickmove> [delay]` — mass deposit items
  - `.gui offhand <slot>` — swap item to offhand
  - `.gui drop <slot> [all]` — drop item from slot (1 or stack)
  - `.desync` — close GUI server-side only
  - `.disconnectpackets` — flush delayed packets then disconnect
  - `.delaypackets on/off` — toggle ui-utils packet delaying
  - `.sendpackets on/off` — toggle ui-utils packet sending
- **UiUtilsBridge** — reflection bridge to `com.ui_utils.SharedVariables`
- **EnumArgumentType** — generic enum argument type for brigadier commands
- **MsTimer** — scheduled executor utility (ported from du-addon-public)

### Changed
- **Category**: "Softcore" renamed to "Softcore Utils"
- **BundleDupe** — replaced stub with full implementation from du-addon-public (Timeout/Kick methods, 5 lag methods, KeepAlive cancellation, NBT exploit packets)
- **PacketDelay** — added C2S packet filter, uses `PacketUtils.getC2SPackets()`

### Removed
- All Meteor Rejects-inspired modules: AutoLogin, AutoDisconnect, HotbarSwapExploit, InventoryCloseCanceller, Kick, SoftClose, VaultManager, AntiCrash, ModuleExample
- Standalone `.softclose` command (moved to `.gui softclose`)
- Old `CommandExample.java`

### Fixed
- **ui-utils detection** — reflection retries on every call (no permanent failure cache), uses context classloader fallback
- **PICKUP mode** in steal/dump — 2-click sequence (pickup + place) instead of orphaned pickup

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
- Version-specific JAR outputs: `softcore-addon-0.2.0-1.21.4.jar` and `softcore-addon-0.2.0-1.21.11.jar`

### Changed
- Updated version from 0.1.0 to 0.2.0
- Refactored build.gradle.kts to support dynamic version selection
- Added version mapping for yarn, meteor, and minecraft dependencies in build.gradle.kts
- JAR filenames now include target Minecraft version
- Updated fabric.mod.json to use version template variable (filled per build)
- Improved README with comprehensive build instructions
- Added gradle.properties configuration for build targeting

### Technical Details
- Each build generates a JAR specifically optimized for its target version
- Version selection via `-PmcVersion=1.21.4` or `-PmcVersion=1.21.11` (default)
- Version mapping centralized in build.gradle.kts for easy maintenance
- No shared single JAR - each version gets its own artifact for reliability

### Build Commands
```bash
# Build for all versions at once
./gradlew buildAll

# Build for 1.21.11 (default)
./gradlew build

# Build for 1.21.4
./gradlew build -PmcVersion=1.21.4
```

---

## [0.1.0] - Initial Release

### Features
- InventoryCloseCanceller - Block inventory close packets
- InventoryCloseDelayer - Delay close packets for testing
- HotbarSwapExploit - Test hotbar swap timing exploits
- ShiftClickExploit - Test shift-click inventory operations
- ChestReopenHelper - Quickly reopen chests
- RaceConditionTester - Test race condition vulnerabilities
- ItemDupeHelper - Track and assist with item testing
- DeathChestDebugger - Debug Death Chest plugin interactions
- PacketLogger - Log all incoming/outgoing packets
- AutoDisconnect - Auto-disconnect after delay

### Target
- Minecraft 1.21.11
- Meteor Client
