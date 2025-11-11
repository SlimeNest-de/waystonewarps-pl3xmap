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

## ⚠️ Disclaimer

**Use at your own risk!** This plugin was adapted for private use on SlimeNest.de. While functional, it may not be compatible with all server setups.

![Screenshot](https://raw.githubusercontent.com/SlimeNest-de/waystonewarps-pl3xmap/refs/heads/main/images/screenshot.png)

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

### Prerequisites

- **Java Development Kit (JDK)**: 21 or higher
- **Maven**: 3.9.0 or higher
- **Waystone Warps Plugin**: Download from [Modrinth](https://modrinth.com/plugin/waystone-warps) or [GitHub](https://github.com/Mizarc/waystone-warps)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/SlimeNest-de/waystonewarps-pl3xmap.git
   cd waystonewarps-pl3xmap
   ```

2. **Download Waystone Warps JAR**
   - Download `WaystoneWarps-0.3.5.jar` (or newer)
   - Place it in the `libs/` folder in the project root

3. **Install Waystone Warps to local Maven repository**
   ```bash
   mvn install:install-file "-Dfile=libs/WaystoneWarps-0.3.5.jar" "-DgroupId=dev.mizarc" "-DartifactId=waystonewarps" "-Dversion=0.3.5" "-Dpackaging=jar"
   ```
   
   > **Note:** This step is required only once. It installs the Waystone Warps JAR into your local Maven repository (`~/.m2/repository/`).

### Building

```bash
# Clean and compile the project
mvn clean package

# The output JAR will be located at:
# target/waystonewarps-pl3xmap-<version>.jar
```

### Project Structure

```
waystonewarps-pl3xmap/
├── src/main/
│   ├── java/                        # Java source files
│   └── resources/
│       ├── config.yml               # Default configuration
│       ├── plugin.yml               # Plugin metadata
│       ├── waystone_icon.png        # Public waystone icon
│       └── waystone_locked_icon.png # Private waystone icon
├── libs/                            # Local JAR dependencies
│   └── WaystoneWarps-0.3.5.jar     # Waystone Warps plugin
├── pom.xml                          # Maven build configuration
└── README.md
```

### Testing

1. Build the plugin using the instructions above
2. Copy the JAR from `target/` to your test server's `plugins/` folder
3. Ensure WaystoneWarps and Pl3xMap are installed
4. Restart the server and check the console for any errors

### Troubleshooting

**Problem:** `BUILD FAILURE` with "Could not resolve dependencies"
- **Solution:** Make sure you've installed Waystone Warps to your local Maven repository (step 3 in Setup)

**Problem:** Icons not appearing on the map
- **Solution:** Execute `/map reload` followed by `/waystones-reload-map`

**Problem:** Version mismatch warnings
- **Solution:** Update the `<version>` in `pom.xml` and rebuild

## 📝 License

Licensed under the same terms as WaystoneWarps.

## 🙏 Credits

- **Original Plugin**: [James-P-Bennett/waystonewarps-squaremap](https://github.com/James-P-Bennett/waystonewarps-squaremap)
- **WaystoneWarps**: [Mizarc/waystone-warps](https://github.com/Mizarc/waystone-warps)
- **Pl3xMap**: [granny/Pl3xMap](https://github.com/granny/Pl3xMap)
- **Adapted for**: [SlimeNest.de](https://slimenest.de) Server

---

**Repository**: https://github.com/SlimeNest-de/waystonewarps-pl3xmap
