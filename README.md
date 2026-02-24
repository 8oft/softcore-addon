# Softcore Addon

A Softcore addon for Meteor Client by Softcore Team

**Version**: 0.2.0  
**Minecraft**: 1.21.4 or 1.21.11  
**Meteor Client**: Latest snapshot

Join our Discord for free low quality dupes: https://discord.gg/ECnEBx55sD

## Building

### Build for All Versions (Recommended)
Automatically builds separate JARs for both 1.21.4 and 1.21.11:

```bash
./gradlew buildAll
```

Output: `build/libs/softcore-addon-0.2.0-1.21.4.jar` and `build/libs/softcore-addon-0.2.0-1.21.11.jar`

### Build for Specific Version

**For Minecraft 1.21.11 (Default):**
```bash
./gradlew build
```
Output: `build/libs/softcore-addon-0.2.0-1.21.11.jar`

**For Minecraft 1.21.4:**
```bash
./gradlew build -PmcVersion=1.21.4
```
Output: `build/libs/softcore-addon-0.2.0-1.21.4.jar`

## Version Support

This addon is built for specific Minecraft versions. Each JAR is optimized for its target version:
- **softcore-addon-0.2.0-1.21.4.jar** - Minecraft 1.21.4
- **softcore-addon-0.2.0-1.21.11.jar** - Minecraft 1.21.11 (Default build)

## Features

- **Packet Manipulation Tools** for testing server exploits
- **Inventory Testing Modules** for development
- **Debug Utilities** for analysis
