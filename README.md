# WaystoneWarps-Pl3xMap

🗺️ **A Minecraft plugin for the SlimeNest.de Server**

Displays [WaystoneWarps](https://github.com/Mizarc/waystone-warps) as markers on Pl3xMap with tooltips showing waystone details.

## 📜 About this Project

This is a fork of [waystonewarps-squaremap](https://github.com/James-P-Bennett/waystonewarps-squaremap) by James-P-Bennett, adapted for the **SlimeNest.de Server** to work with **Pl3xMap** instead of Squaremap.

### Changes from the Original:
- ✅ **Pl3xMap support** instead of Squaremap
- ✅ **Converted from Gradle to Maven**
- ✅ **Converted from Kotlin to Java**
- ✅ Optimized performance and code quality

![Screenshot](https://raw.githubusercontent.com/James-P-Bennett/waystonewarps-squaremap/refs/heads/main/images/screenshot.png)

## 📋 Requirements

- **Minecraft**: 1.21+ (Paper/Purpur/Folia)
- **[WaystoneWarps](https://github.com/Mizarc/waystone-warps)**: 0.3.5+
- **[Pl3xMap](https://modrinth.com/plugin/pl3xmap)**: 1.21.5+

## 🚀 Installation

1. Install **WaystoneWarps** and **Pl3xMap**
2. Download the latest release from this repository
3. Place the JAR file in your server's `plugins/` folder
4. Restart your server

Waystone markers will appear automatically on the map. Players can toggle the "Waystones" layer in the map controls.

## ⚙️ Configuration

Edit `plugins/WaystoneWarps-Pl3xMap/config.yml`:

```yaml
display:
  show-locked-waystones: true  # Show/hide locked waystones
  icon-size: 16                # Icon size in pixels

auto-refresh:
  enabled: true                # Enable automatic marker refresh
  interval: 60                 # Check for changes every 60 seconds
```

### Commands

- `/waystones-reload-map` - Manually refresh all waystone markers
  - **Permission**: `waystonewarps.pl3xmap.reload`

## 🔧 Development

### Build Instructions

Prerequisites:
- Java 21+
- Maven 3.9+
- WaystoneWarps JAR in `libs/` folder

```bash
# Compile the project
mvn clean package

# Output JAR
target/waystonewarps-pl3xmap-1.0.0.jar
```

## 📝 License

Licensed under the same terms as WaystoneWarps.

## 🙏 Credits

- **Original Plugin**: [James-P-Bennett/waystonewarps-squaremap](https://github.com/James-P-Bennett/waystonewarps-squaremap)
- **WaystoneWarps**: [Mizarc/waystone-warps](https://github.com/Mizarc/waystone-warps)
- **Pl3xMap**: [granny/Pl3xMap](https://github.com/granny/Pl3xMap)
- **Adapted for**: [SlimeNest.de](https://slimenest.de) Server

---

**Repository**: https://github.com/SlimeNest-de/waystonewarps-pl3xmap
