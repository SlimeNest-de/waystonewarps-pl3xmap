package org.jamesphbennett.waystonewarps.pl3xmap;

import dev.mizarc.waystonewarps.domain.warps.Warp;
import dev.mizarc.waystonewarps.domain.warps.WarpRepository;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages the Pl3xMap layer for waystone markers.
 *
 * This class handles:
 * - Registering the waystones layer with Pl3xMap
 * - Creating and updating markers for each waystone
 * - Refreshing markers when the map is loaded
 */
public class Pl3xmapLayerManager {
    private static final String LAYER_KEY = "waystones";
    private static final String LAYER_LABEL = "Waystones";

    private final Plugin plugin;
    private final WarpRepository warpRepository;
    private final boolean showLockedWaystones;
    private final ConcurrentHashMap<World, SimpleLayer> layers;
    private final WaystoneMarkerFactory markerFactory;

    public Pl3xmapLayerManager(Plugin plugin, WarpRepository warpRepository, boolean showLockedWaystones, int iconSize) {
        this.plugin = plugin;
        this.warpRepository = warpRepository;
        this.showLockedWaystones = showLockedWaystones;
        this.layers = new ConcurrentHashMap<>();
        this.markerFactory = new WaystoneMarkerFactory(plugin, iconSize);
    }

    /**
     * Initialize the Pl3xMap integration.
     * Registers icons and creates layers for all enabled map worlds.
     */
    public void initialize() {
        // Register custom icons with Pl3xMap
        markerFactory.registerIcons();

        // Register layers for all enabled Pl3xMap worlds
        Collection<World> worlds = Pl3xMap.api().getWorldRegistry().values();
        for (World world : worlds) {
            registerLayer(world);
        }

        plugin.getLogger().info("Registered waystone layers for " + layers.size() + " world(s)");
    }

    /**
     * Shutdown the integration and unregister all layers.
     */
    public void shutdown() {
        for (SimpleLayer layer : layers.values()) {
            layer.clearMarkers();
        }
        layers.clear();
        plugin.getLogger().info("Unregistered all waystone layers");
    }

    /**
     * Register a waystone layer for a specific Pl3xMap world.
     */
    private void registerLayer(World world) {
        // Create the layer
        SimpleLayer layer = new SimpleLayer(LAYER_KEY, () -> LAYER_LABEL);
        layer.setDefaultHidden(false);
        layer.setShowControls(true);
        layer.setPriority(10);
        layer.setUpdateInterval(20);

        // Register the layer with Pl3xMap
        world.getLayerRegistry().register(layer);

        // Store layer for later updates
        layers.put(world, layer);

        plugin.getLogger().info("Registered waystone layer for world: " + world.getName());

        // Populate initial markers
        refreshMarkers(world);
    }

    /**
     * Refresh all markers for a specific world by fetching from the database.
     */
    private void refreshMarkers(World world) {
        SimpleLayer layer = layers.get(world);
        if (layer == null) {
            plugin.getLogger().warning("No layer found for world: " + world.getName());
            return;
        }

        // Re-register icons in case Pl3xMap was reloaded
        markerFactory.registerIcons();

        // Get Bukkit world
        org.bukkit.World bukkitWorld = Bukkit.getWorld(world.getName());
        if (bukkitWorld == null) {
            plugin.getLogger().warning("Could not find Bukkit world: " + world.getName());
            return;
        }

        // Clear existing markers
        layer.clearMarkers();

        // Get all warps and filter by world UUID
        UUID worldUUID = bukkitWorld.getUID();
        int markerCount = 0;

        for (Warp warp : warpRepository.getAll()) {
            // Skip if warp is not in this world
            if (!warp.getWorldId().equals(worldUUID)) {
                continue;
            }

            // Skip locked waystones if configured
            if (!showLockedWaystones && warp.isLocked()) {
                continue;
            }

            // Add marker
            addWarpMarker(layer, warp);
            markerCount++;
        }

        plugin.getLogger().info("Refreshed " + markerCount + " waystone marker(s) for world: " + bukkitWorld.getName());
    }

    /**
     * Refresh markers for all worlds.
     * This is called when the browser loads the map or manually via command.
     * Re-registers layers if they were removed by /map reload.
     */
    public void refreshAllMarkers() {
        // Clear old layer references
        layers.clear();
        
        // Get current worlds from Pl3xMap
        Collection<World> currentWorlds = Pl3xMap.api().getWorldRegistry().values();
        
        plugin.getLogger().info("Refreshing markers for " + currentWorlds.size() + " world(s)");
        
        // Re-register icons in case Pl3xMap was reloaded
        markerFactory.registerIcons();
        
        // Register layers for all worlds
        for (World world : currentWorlds) {
            // Check if layer already exists in this world
            if (!world.getLayerRegistry().has(LAYER_KEY)) {
                plugin.getLogger().info("Registering new layer for world: " + world.getName());
                registerLayer(world);
            } else {
                plugin.getLogger().info("Layer already exists for world: " + world.getName() + ", refreshing markers");
                // Store the existing layer reference
                SimpleLayer layer = (SimpleLayer) world.getLayerRegistry().get(LAYER_KEY);
                layers.put(world, layer);
                refreshMarkers(world);
            }
        }
        
        plugin.getLogger().info("Refreshed waystone markers for all worlds");
    }

    /**
     * Add a marker for a specific warp to the layer.
     */
    private void addWarpMarker(SimpleLayer layer, Warp warp) {
        Marker<?> marker = markerFactory.createMarker(warp);
        layer.addMarker(marker);
    }
}
