# Changelog

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
