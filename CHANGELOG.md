# Changelog

## [1.0.1] - 2026-05-29 - Vault Manager Update

### Added
- **VaultManager** - Automatically loot items from Vault plugin GUIs
  - Auto quick-moves all items from vault pages to player inventory
  - Auto-clicks next/previous page navigation buttons
  - Smart detection: only activates on GUIs with "Vault" in title
  - Skips navigation arrow slots (next page slot 53, previous page slot 45)
  - Configurable delays for click speed and page switching

### Fixed
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
