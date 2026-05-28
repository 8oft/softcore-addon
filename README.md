# Softcore Addon for Meteor Client

A comprehensive Meteor Client addon featuring utility modules, exploits, and quality-of-life improvements.

**Version**: 1.0.1  
**Minecraft**: 1.21.4, 1.21.10, 1.21.11  
**Meteor Client**: Latest snapshot

## Features

### 🔧 Utility Modules
- **AutoLogin** - Automatically logs you into servers when joining
- **AntiCrash** - Prevents client crashes from malicious packets
- **PacketDelay** - Delays selected packets for various exploits
- **GuiMacros** - Execute macros in inventory/chest GUIs only
- **SoftClose** - Instantly close chest/inventory GUIs

### 🏛️ Vault Manager
- **VaultManager** - Automatically loot items from Vault plugin GUIs

### ⚔️ Exploits
- **Kick** - Multiple methods to kick yourself from servers
- **BundleDupe** - Bundle-based duplication exploit

## Installation

1. Download the latest release from the [Releases](https://github.com/8oft/meteor-softcore-addon/releases) page
2. Place the JAR file in your `.minecraft/mods` folder
3. Launch Minecraft with Meteor Client
4. The addon will be automatically loaded

## Configuration

All modules can be configured through the Meteor Client GUI:
- Navigate to `Modules` → `Softcore` category
- Click on any module to configure its settings
- Use `.t <module> on/off` to toggle modules

## Module Details

### AutoLogin
- **Smart Detection**: Automatically detects login commands from chat
- **Configurable Commands**: Set custom login and register commands
- **Delay Setting**: Adjustable delay before executing commands
- **Server Support**: Works with most authentication plugins

### AntiCrash
- **Packet Filtering**: Blocks explosion packets and excessive particle spam
- **Crash Prevention**: Prevents common crash attempts
- **Logging**: Optional logging of blocked crash attempts
- **Lightweight**: Minimal performance impact

### PacketDelay
- **Packet Selection**: Choose which packets to delay
- **Queue System**: Packets are queued and sent on deactivation
- **Logging**: Optional packet logging for debugging
- **Exploit Support**: Useful for various packet-based exploits

### GuiMacros
- **GUI Restricted**: Only works in inventory/chest GUIs (not pause/chat)
- **Three Macros**: Configure up to 3 custom macros
- **Simple Commands**: Use `.macro1`, `.macro2`, `.macro3` in chat
- **Valid GUIs**: Supports chests, inventory, crafting tables, anvils, enchantment tables, brewing stands, furnaces, hoppers, shulker boxes, beacons, lecterns, looms, cartography tables, grindstones, and smithing tables

### SoftClose
- **Instant Closing**: Close GUIs without animation
- **Smart Detection**: Only works in valid container GUIs
- **Comprehensive Support**: Supports all major container types
- **Simple Usage**: Activate module to close current GUI

### VaultManager
- **Auto Loot**: Automatically quick-moves all items from vault pages
- **Page Navigation**: Auto-clicks next/previous page buttons
- **Smart Detection**: Only activates on Vault plugin GUIs (title contains "Vault")
- **Navigation Skip**: Automatically skips navigation arrow slots
- **Configurable Delay**: Adjust click speed and page switch delay

### BundleDupe
- **Configurable Slot ID**: Set custom bundle slot ID (negative values often work)
- **Multiple Modes**: Basic and Advanced dupe modes
- **Packet Interception**: Intercepts and modifies bundle-related packets
- **Container Support**: Works in any container screen

## Building

### For Minecraft 1.21.11 (Default)
```bash
./gradlew clean build
```
Output: `build/libs/softcore-addon-1.0.1-1.21.11.jar`

### For Minecraft 1.21.10
```bash
./gradlew clean build "-PmcVersion=1.21.10"
```
Output: `build/libs/softcore-addon-1.0.1-1.21.10.jar`

### For Minecraft 1.21.4
```bash
./gradlew clean build "-PmcVersion=1.21.4"
```
Output: `build/libs/softcore-addon-1.0.1-1.21.4.jar`

### Build All Versions
```bash
./gradlew clean build
./gradlew clean build "-PmcVersion=1.21.10"
./gradlew clean build "-PmcVersion=1.21.4"
```

## Version Support

Each JAR is built for its specific Minecraft version:
- **softcore-addon-1.0.1-1.21.4.jar** - For Minecraft 1.21.4
- **softcore-addon-1.0.1-1.21.10.jar** - For Minecraft 1.21.10
- **softcore-addon-1.0.1-1.21.11.jar** - For Minecraft 1.21.11

## Requirements

- **Minecraft**: Java 21 or higher
- **Meteor Client**: Latest version
- **Fabric Loader**: 0.18.2 or higher

## Credits

- **Meteor Client** - Base client framework
- **meteor-rejects** - Inspiration for AutoLogin, AntiCrash, and Kick modules
- **dupersunited-public-addon** - Inspiration for PacketDelay, BundleDupe, and GuiMacros modules

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

---

**Disclaimer**: This addon is for educational purposes only. Use responsibly and follow server rules.
