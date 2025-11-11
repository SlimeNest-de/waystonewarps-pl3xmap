# Changelog

## [1.0.1] - 2025-11-11

### Fixed
- Fixed icon registration issue after `/map reload` command
- Fixed layer disappearing after `/map reload` - layers are now automatically re-registered
- Icons are now automatically re-registered when needed
- Improved hash calculation to detect all waystone property changes (name, visibility, position)

### Changed
- Version number now synchronized with `pom.xml` using Maven filtering
- Removed `systemPath` dependency for cleaner Maven build (no more warnings)
- Enhanced `refreshAllMarkers()` to detect and recover from Pl3xMap reloads

### Technical
- Layer registry check before refresh to ensure layers exist
- Automatic layer re-registration if removed by Pl3xMap reload
- Improved error handling for missing layers

## [1.0.0] - 2025-11-11

### Added
- Initial release of WaystoneWarps-Pl3xMap
- Pl3xMap support (converted from Squaremap)
- Display waystones as markers on Pl3xMap with custom icons
- Detailed tooltips showing waystone information
- Auto-refresh system with configurable interval
- Manual reload command `/waystones-reload-map`
- Configuration file with display and refresh settings

### Changed
- Converted from Kotlin to Java for better compatibility
- Migrated from Gradle to Maven build system
- Optimized marker refresh performance

### Technical
- Enhanced change detection using deep hash calculation
- Async command execution to prevent server lag
- StringBuilder optimization for tooltip generation
- Improved error handling and logging

---

**Fork Information:**
- Forked from [waystonewarps-squaremap](https://github.com/James-P-Bennett/waystonewarps-squaremap)
- Adapted for [SlimeNest.de](https://slimenest.de) server
- Converted from Squaremap to Pl3xMap API
