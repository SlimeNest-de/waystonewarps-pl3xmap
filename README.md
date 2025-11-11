# WaystoneWarps-Squaremap

## Displays [WaystoneWarps](https://github.com/Mizarc/waystone-warps) as markers on Squaremap with tooltips showing waystone details.


![Screenshot](https://raw.githubusercontent.com/James-P-Bennett/waystonewarps-squaremap/refs/heads/main/images/screenshot.png)


## Requirements

- **Minecraft**: 1.21+ (Paper/Purpur)
- **[WaystoneWarps](https://github.com/Mizarc/waystone-warps)**: 0.3.4+
- **[Squaremap](https://modrinth.com/plugin/squaremap)**: 1.3.9+

## Installation

1. Install WaystoneWarps and Squaremap
2. Download the latest release
3. Place the JAR in your `plugins/` folder
4. Restart your server

Waystone markers will appear automatically on the map. Players can toggle the "Waystones" layer in the map controls.

## Configuration

Edit `plugins/WaystoneWarps-Squaremap/config.yml`:

```yaml
display:
  show-locked-waystones: true  # Show/hide locked waystones
  icon-size: 32                # Icon size in pixels

auto-refresh:
  enabled: true                # Enable automatic marker refresh
  interval: 60                 # Check for changes every 60 seconds
```

## License

Licensed under the same terms as WaystoneWarps.

## Credits

- **WaystoneWarps**: [Mizarc/waystone-warps](https://github.com/Mizarc/waystone-warps)
- **Squaremap**: [jpenilla/squaremap](https://github.com/jpenilla/squaremap)
