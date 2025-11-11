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

/**
 * Manages Pl3xMap layers for waystone markers across all worlds.
 * 
 * Responsibilities:
 * - Register and maintain waystone layers for each Pl3xMap world
 * - Create and update waystone markers based on WarpRepository data
 * - Handle Pl3xMap reloads by detecting and re-registering missing layers
 * - Filter waystones by world and visibility settings
 * 
 * Thread-safe: Uses ConcurrentHashMap for concurrent access support.
 */
public class Pl3xmapLayerManager {
    private static final String LAYER_KEY = "waystones";
    private static final String LAYER_LABEL = "Waystones";
    private static final int LAYER_PRIORITY = 10;
    private static final int LAYER_UPDATE_INTERVAL = 20; // seconds

    private final Plugin plugin;
    private final WarpRepository warpRepository;
    private final boolean showLockedWaystones;
    private final ConcurrentHashMap<World, SimpleLayer> layers;
    private final WaystoneMarkerFactory markerFactory;

    /**
     * Creates a new layer manager.
     * 
     * @param plugin The plugin instance for logging and scheduling
     * @param warpRepository Repository containing waystone data
     * @param showLockedWaystones Whether to display private/locked waystones
     * @param iconSize Size of waystone icons in pixels
     */
    public Pl3xmapLayerManager(Plugin plugin, WarpRepository warpRepository, boolean showLockedWaystones, int iconSize) {
        this.plugin = plugin;
        this.warpRepository = warpRepository;
        this.showLockedWaystones = showLockedWaystones;
        this.layers = new ConcurrentHashMap<>();
        this.markerFactory = new WaystoneMarkerFactory(plugin, iconSize);
    }

    /**
     * Initializes the Pl3xMap integration.
     * Registers custom waystone icons and creates layers for all enabled worlds.
     */
    public void initialize() {
        try {
            // Register custom icons with Pl3xMap
            markerFactory.registerIcons();

            // Register layers for all enabled Pl3xMap worlds
            Collection<World> worlds = Pl3xMap.api().getWorldRegistry().values();
            for (World world : worlds) {
                registerLayer(world);
            }

            plugin.getLogger().info("Registered waystone layers for " + layers.size() + " world(s)");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Pl3xMap integration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shuts down the integration and cleans up all resources.
     * Should be called during plugin shutdown to prevent memory leaks.
     */
    public void shutdown() {
        try {
            for (SimpleLayer layer : layers.values()) {
                layer.clearMarkers();
            }
            layers.clear();
            plugin.getLogger().info("Unregistered all waystone layers");
        } catch (Exception e) {
            plugin.getLogger().warning("Error during shutdown: " + e.getMessage());
        }
    }

    /**
     * Registers a waystone layer for a specific Pl3xMap world.
     * 
     * @param world The Pl3xMap world to register the layer for
     */
    private void registerLayer(World world) {
        if (world == null) {
            plugin.getLogger().warning("Attempted to register layer for null world");
            return;
        }

        try {
            // Create the layer
            SimpleLayer layer = new SimpleLayer(LAYER_KEY, () -> LAYER_LABEL);
            layer.setDefaultHidden(false);
            layer.setShowControls(true);
            layer.setPriority(LAYER_PRIORITY);
            layer.setUpdateInterval(LAYER_UPDATE_INTERVAL);

            // Register the layer with Pl3xMap
            world.getLayerRegistry().register(layer);

            // Store layer for later updates
            layers.put(world, layer);

            plugin.getLogger().info("Registered waystone layer for world: " + world.getName());

            // Populate initial markers
            refreshMarkers(world);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register layer for world " + world.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Refreshes all markers for a specific world.
     * Fetches current waystone data, clears existing markers, and creates new ones.
     * 
     * @param world The Pl3xMap world to refresh markers for
     */
    private void refreshMarkers(World world) {
        if (world == null) {
            plugin.getLogger().warning("Attempted to refresh markers for null world");
            return;
        }

        SimpleLayer layer = layers.get(world);
        if (layer == null) {
            plugin.getLogger().warning("No layer found for world: " + world.getName());
            return;
        }

        try {
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

            Collection<Warp> warps = warpRepository.getAll();
            for (Warp warp : warps) {
                // Null check for safety
                if (warp == null) continue;

                // Skip if warp is not in this world
                if (!worldUUID.equals(warp.getWorldId())) {
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
        } catch (Exception e) {
            plugin.getLogger().severe("Error refreshing markers for world " + world.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Refreshes markers for all worlds.
     * Detects Pl3xMap reloads and automatically re-registers layers.
     * Called manually via command, automatically after reload, or when changes are detected.
     */
    public void refreshAllMarkers() {
        try {
            // Clear old layer references (they may be stale after /map reload)
            layers.clear();

            // Get current worlds from Pl3xMap
            Collection<World> currentWorlds = Pl3xMap.api().getWorldRegistry().values();

            plugin.getLogger().info("Refreshing markers for " + currentWorlds.size() + " world(s)");

            // Re-register icons in case Pl3xMap was reloaded
            markerFactory.registerIcons();

            // Register layers for all worlds
            for (World world : currentWorlds) {
                if (world == null) continue;

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
        } catch (Exception e) {
            plugin.getLogger().severe("Critical error during marker refresh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds a waystone marker to a layer.
     * 
     * @param layer The layer to add the marker to
     * @param warp The warp/waystone to create a marker for
     */
    private void addWarpMarker(SimpleLayer layer, Warp warp) {
        if (layer == null || warp == null) {
            plugin.getLogger().warning("Cannot add marker: layer or warp is null");
            return;
        }

        try {
            Marker<?> marker = markerFactory.createMarker(warp);
            layer.addMarker(marker);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to add marker for warp " + warp.getName() + ": " + e.getMessage());
        }
    }
}
