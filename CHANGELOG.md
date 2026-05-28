# Changelog

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
