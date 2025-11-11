package org.jamesphbennett.waystonewarps.squaremap

import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import xyz.jpenilla.squaremap.api.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the squaremap layer for waystone markers.
 *
 * This class handles:
 * - Registering the waystones layer with squaremap
 * - Creating and updating markers for each waystone
 * - Refreshing markers when the map is loaded
 */
class SquaremapLayerManager(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val showLockedWaystones: Boolean = true,
    iconSize: Int = 16
) {
    private val squaremapApi: Squaremap = SquaremapProvider.get()
    private val providers = ConcurrentHashMap<WorldIdentifier, SimpleLayerProvider>()
    private val worldMapping = ConcurrentHashMap<WorldIdentifier, org.bukkit.World>()
    private val markerFactory = WaystoneMarkerFactory(plugin, iconSize)

    companion object {
        private const val LAYER_KEY = "waystones"
        private const val LAYER_LABEL = "Waystones"
    }

    /**
     * Initialize the squaremap integration.
     * Registers icons and creates layers for all enabled map worlds.
     */
    fun initialize() {
        // Register custom icons
        markerFactory.registerIcons(squaremapApi.iconRegistry())

        // Register layers for all enabled map worlds
        squaremapApi.mapWorlds().forEach { mapWorld ->
            registerLayer(mapWorld)
        }

        plugin.logger.info("Registered waystone layers for ${providers.size} world(s)")
    }

    /**
     * Shutdown the integration and unregister all layers.
     */
    fun shutdown() {
        providers.keys.forEach { worldId ->
            squaremapApi.getWorldIfEnabled(worldId).ifPresent { mapWorld ->
                mapWorld.layerRegistry().unregister(Key.of(LAYER_KEY))
            }
        }
        providers.clear()
        plugin.logger.info("Unregistered all waystone layers")
    }

    /**
     * Register a waystone layer for a specific map world.
     */
    private fun registerLayer(mapWorld: MapWorld) {
        val worldId = mapWorld.identifier()

        // Find the corresponding Bukkit world by matching all worlds
        // and checking if squaremap has enabled them
        var bukkitWorld: org.bukkit.World? = null
        for (world in Bukkit.getWorlds()) {
            val testMapWorld = squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world))
            if (testMapWorld.isPresent && testMapWorld.get().identifier() == worldId) {
                bukkitWorld = world
                break
            }
        }

        if (bukkitWorld == null) {
            plugin.logger.warning("Could not find Bukkit world for MapWorld: ${worldId.value()}")
            return
        }

        // Store the mapping from identifier to Bukkit world
        worldMapping[worldId] = bukkitWorld
        plugin.logger.info("Mapped squaremap world '${worldId.value()}' to Bukkit world '${bukkitWorld.name}'")

        // Create the layer provider
        val provider = SimpleLayerProvider.builder(LAYER_LABEL)
            .showControls(true)       // Show toggle in layer controls
            .defaultHidden(false)     // Visible by default
            .layerPriority(10)        // Order in controls (lower = higher)
            .zIndex(100)              // Render order on map (higher = on top)
            .build()

        // Register the layer with squaremap
        mapWorld.layerRegistry().register(Key.of(LAYER_KEY), provider)

        // Store provider for later updates
        providers[worldId] = provider

        // Populate initial markers
        refreshMarkers(worldId)
    }

    /**
     * Refresh all markers for a specific world by fetching from the database.
     */
    private fun refreshMarkers(worldId: WorldIdentifier) {
        val provider = providers[worldId] ?: run {
            plugin.logger.warning("No provider found for world: ${worldId.value()}")
            return
        }

        // Get Bukkit world from our mapping
        val bukkitWorld = worldMapping[worldId] ?: run {
            plugin.logger.warning("No world mapping found for identifier: ${worldId.value()}")
            return
        }

        // Clear existing markers
        provider.clearMarkers()

        // Get all warps in this world
        val allWarps = warpRepository.getAll()
        plugin.logger.info("Total warps in database: ${allWarps.size}")
        plugin.logger.info("Looking for warps in world: ${bukkitWorld.name} (UUID: ${bukkitWorld.uid})")

        // Filter by world and optionally by locked status
        val warps = allWarps.filter { warp ->
            warp.worldId == bukkitWorld.uid && (showLockedWaystones || !warp.isLocked)
        }
        plugin.logger.info("Found ${warps.size} warps matching world UUID")

        // Add a marker for each warp
        warps.forEach { warp ->
            addWarpMarker(provider, warp)
            plugin.logger.info("Added marker for warp: ${warp.name} at ${warp.position.x}, ${warp.position.z}")
        }

        plugin.logger.info("Refreshed ${warps.size} waystone markers for world: ${bukkitWorld.name}")
    }

    /**
     * Refresh markers for all worlds.
     * This is called when the browser loads the map.
     */
    fun refreshAllMarkers() {
        providers.keys.forEach { worldId ->
            refreshMarkers(worldId)
        }
        plugin.logger.info("Refreshed waystone markers for all worlds")
    }

    /**
     * Add a marker for a specific warp to the provider.
     */
    private fun addWarpMarker(provider: SimpleLayerProvider, warp: Warp) {
        val markerKey = Key.of("waystone_${warp.id}")
        val marker = markerFactory.createMarker(warp)
        provider.addMarker(markerKey, marker)
    }

}
