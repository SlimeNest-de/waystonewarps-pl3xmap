# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.0.1] - 2025-11-11

### Added
- **Automatic Pl3xMap reload recovery**: Plugin now listens for `Pl3xMapEnabledEvent` and automatically re-registers all layers after `/map reload` without manual intervention
- Input validation for all configuration values (icon size, refresh interval)
- Comprehensive error handling with stack traces for debugging
- Thread-safety documentation throughout codebase

### Fixed
- Icon registration issue after `/map reload` command
- Layer disappearing after `/map reload` - now automatically recovered via event listener
- Improved hash calculation to detect all waystone property changes (name, visibility, position, locked status)

### Changed
- Version number now synchronized with `pom.xml` using Maven filtering
- Removed `systemPath` dependency for cleaner Maven build
- All JavaDoc comments converted to English and simplified
- Enhanced XSS protection with null-safe HTML escaping
- Resource streams now properly closed in finally blocks

## [1.0.0] - 2025-11-11

### Added
- Initial release of WaystoneWarps-Pl3xMap
- Pl3xMap support (converted from Squaremap)
- Display waystones as markers on Pl3xMap with custom icons
- Rich HTML tooltips showing waystone information (owner, access level, world, coordinates, creation time)
- Auto-refresh system with configurable interval
- Manual reload command `/waystones-reload-map`
- Configuration file with display and refresh settings

### Changed
- Converted from Kotlin to Java for better compatibility
- Migrated from Gradle to Maven build system
- Optimized marker refresh performance with hash-based change detection
- Async command execution to prevent server lag
- StringBuilder optimization for tooltip generation

---

**Fork Information:**
- Original: [waystonewarps-squaremap](https://github.com/James-P-Bennett/waystonewarps-squaremap) by James-P-Bennett
- Fork: [waystonewarps-pl3xmap](https://github.com/SlimeNest-de/waystonewarps-pl3xmap) by SlimeNest.de
- Converted from Squaremap to Pl3xMap API

