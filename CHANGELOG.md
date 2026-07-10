# Changelog

## [2.0.0] - 2026-07-10 - Major Rewrite

### Added
- **Softcore Auto Dupe** category — separate tab from Softcore Utils
- **SlotChangeBackpack, SoftCloseBackpack, SoftCloseChest, SoftCloseVault** — auto-dupe modules
- **SlotViewer** — draws slot ID numbers on every GUI slot
- **Commands:** `.clickslot`, `.repeat`, `.wait`, `.repeat-delay`, `.action`, `.gui`, `.desync`, `.disconnectpackets`, `.delaypackets`, `.sendpackets`
- **UiUtilsBridge** — reflection bridge to `com.ui_utils.SharedVariables`

### Changed
- **BundleDupe** — full implementation with Timeout/Kick methods, 5 lag methods
- **PacketDelay** — C2S packet filter support
- Delays standardized (no +500/+800 additions), default `actionDelay` 800ms
- **Category**: "Softcore" renamed to "Softcore Utils"

### Removed
- All Meteor Rejects–inspired modules (AutoLogin, AutoDisconnect, etc.)

## [1.0.1] - 2026-05-29 - Vault Manager Update

### Added
- **Vaults Plugin Dupe** - Automatically loot items from Vault plugin GUIs

### Enhanced
- Multi-Version Packet Compatibility
- Same-Tick Execution
- Reflection Caching

### Fixed
- Runtime Reflection for obfuscated environments
- Packet batching performance
- Empty Stack Type Mismatch
- Build script versioning

## [1.0.0] - 2026-05-25 - Major Release

### Added
- AutoLogin, AntiCrash, Kick, PacketDelay, BundleDupe, GuiMacros, SoftClose

## [0.2.0] - 2026-02-24

### Added
- Dual-version build system (1.21.4 + 1.21.11)

## [0.1.0] - Initial Release
