# Softcore Addon

A Softcore addon for Meteor Client by Softcore Team

**Version**: 0.2.0  
**Minecraft**: 1.21.4 or 1.21.11  
**Meteor Client**: Latest snapshot

Join our Discord for free low quality dupes: https://discord.gg/ECnEBx55sD

## Building

### For Minecraft 1.21.11 (Default)
```bash
./gradlew clean build
```
Output: `build/libs/softcore-addon-0.2.0-1.21.11.jar` (47.5 KB)

### For Minecraft 1.21.4
```bash
./gradlew clean build "-PmcVersion=1.21.4"
```
Output: `build/libs/softcore-addon-0.2.0-1.21.4.jar` (48.6 KB)

### Build Both Versions
```bash
./gradlew clean build
./gradlew clean build "-PmcVersion=1.21.4"
```

## Version Support

Each JAR is built for its specific Minecraft version:
- **softcore-addon-0.2.0-1.21.4.jar** - For Minecraft 1.21.4
- **softcore-addon-0.2.0-1.21.11.jar** - For Minecraft 1.21.11

## Features

- **Packet Manipulation Tools** for testing server exploits
- **Inventory Testing Modules** for development
- **Debug Utilities** for analysis
