# Softcore Addon for Meteor Client

A Meteor Client addon with utility modules, exploits, and automation tools. Only uses classes from working, well-known addons (dupersunited-public-addon, ui-utils).

**Version**: 2.0.0
**Minecraft**: 1.21.10, 1.21.11
**Meteor Client**: Latest snapshot

## Features

### 🛠️ Softcore Utils
| Module | Description |
|--------|-------------|
| **PacketDelay** | Delay selected packets — queue them, flush on deactivate |
| **BundleDupe** | Bundle-based dupe with 2 methods (Timeout/Kick), 5 lag methods |
| **GuiMacros** | Execute macros only inside GUIs (inventory/chest) |
| **PacketLogger** | Log incoming/outgoing packets |
| **SlotViewer** | Draws slot ID numbers on every GUI slot |

### 🏆 Softcore Auto Dupe
| Module | Description |
|--------|-------------|
| **SoftCloseBackpack** | Dupe cycle using a backpack/shulker in hand — dump, close, reopen, steal, soft-open, steal, close |
| **SoftCloseChest** | Dupe cycle using a placed storage block (chest, barrel, etc.) |
| **SoftCloseVault** | Dupe cycle using a command-based storage (e.g., /pv) |
| **SlotChangeBackpack** | Duplicate items by dropping or moving backpack inside its own GUI |

All auto dupe modules support:
- QuickMove/Pickup transfer modes
- Configurable click-delay and action-delay
- Repeat mode — auto-repeat until inventory has only 1 empty main slot left
- RightClick / ShiftRightClick / InventoryRightClick interact modes

### Commands
| Command | Description |
|---------|-------------|
| `.action open` | Interact with the block you're looking at (chests, etc.) |
| `.clickslot <slot> <button> <action>` | Raw slot click (aliases: `.cs`, `.cslot`) |
| `.delaypackets on/off` | Toggle ui-utils packet delaying |
| `.desync` | Close GUI server-side, keep open client-side |
| `.disconnectpackets` | Flush delayed packets then disconnect |
| `.gui save` / `.gui load` | Save/restore GUI state (req. ui-utils) |
| `.gui steal <pickup\|quickmove> [delay]` | Mass take items from container |
| `.gui dump <pickup\|quickmove> [delay]` | Mass deposit items into container |
| `.gui offhand <slot>` | Swap slot item to offhand |
| `.gui drop <slot> [all]` | Drop item from slot |
| `.gui close` | Close GUI normally |
| `.gui softclose` | Close GUI without packet |
| `.repeat <times> <cmd>` | Repeat chat command N times, `%index%` |
| `.wait <ms> <cmd>` | Execute command after delay |
| `.repeat-delay <ms> <times> <cmd>` | Repeat with stagger delay |
| `.sendpackets on/off` | Toggle ui-utils packet sending |

## Requirements

- **Minecraft**: Java 21 or higher
- **Meteor Client**: Latest version
- **Fabric Loader**: 0.18.2 or higher
- **[UI Utils](https://github.com/Coderx-Gamer/ui-utils)** (optional) — enables `.gui save/load`, `.delaypackets`, `.sendpackets`, `.disconnectpackets`

## Installation

1. Download the latest release from the [Releases](https://github.com/8oft/meteor-softcore-addon/releases) page
2. Place the JAR in `.minecraft/mods`
3. Launch Minecraft with Meteor Client

## Configuration

All modules configurable through Meteor Client GUI:
- **Softcore Utils** category — utility modules
- **Softcore Auto Dupe** category — dupe automation

## Building

### For Minecraft 1.21.11 (Default)
```bash
./gradlew clean build
```
Output: `build/libs/softcore-addon-2.0.0-1.21.11.jar`

### For Minecraft 1.21.10
```bash
./gradlew clean build "-PmcVersion=1.21.10"
```
Output: `build/libs/softcore-addon-2.0.0-1.21.10.jar`

## Credits

- **Meteor Client** — Base client framework
- **dupersunited-public-addon** — PacketDelay, BundleDupe, GuiMacros, ClickSlotCommand, RepeatCommand, WaitCommand, RepeatDelayCommand, ForEachPlayerCommand
- **UI Utils** — SharedVariables bridge for save/load, delay/send packets commands

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

---

**Disclaimer**: For educational purposes only. Use responsibly.
