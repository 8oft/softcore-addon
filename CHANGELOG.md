# Changelog

## [0.2.0] - 2026-02-24

### Added
- Multi-version support for Minecraft 1.21.4 and 1.21.11
- All packet manipulation modules now compatible with both versions

### Changed
- Updated version from 0.1.0 to 0.2.0
- Updated fabric.mod.json to support both 1.21.4 and 1.21.11
- Improved README with version information and build instructions
- Built with Meteor Client 1.21.11-SNAPSHOT (API-compatible with 1.21.4)

### Technical Details
- Minecraft versions use compatible APIs for all Meteor Client features
- No version-specific code required for either 1.21.4 or 1.21.11
- Single JAR artifact works with both game versions

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
