package org.jamesphbennett.waystonewarps.pl3xmap;

import dev.mizarc.waystonewarps.WaystoneWarps;
import dev.mizarc.waystonewarps.domain.warps.WarpRepository;
import net.pl3x.map.core.Pl3xMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

/**
 * Main plugin class for WaystoneWarps-Pl3xMap integration.
 * 
 * Displays waystones from the WaystoneWarps plugin as interactive markers on Pl3xMap.
 * 
 * Features:
 * - Automatic marker synchronization with waystone database
 * - Toggleable layer controls for showing/hiding waystones
 * - Rich tooltips with waystone information
 * - Configurable visibility for private/locked waystones
 * - Periodic auto-refresh to detect waystone changes
 * - Automatic recovery from Pl3xMap reloads
 * - Manual reload command for operators
 * 
 * Dependencies: WaystoneWarps 0.3.5+, Pl3xMap 1.21.5+, Paper/Purpur/Folia 1.21.4+
 */
public class WaystonePl3xmapAddon extends JavaPlugin {
    private Pl3xmapLayerManager layerManager;
    private WarpRepository warpRepository;
    private WaystoneUpdateTask updateTask;

    @Override
    public void onEnable() {
        try {
            // Load and save default config
            saveDefaultConfig();

            // Verify dependencies
            if (!verifyDependencies()) {
                return; // Plugin disabled in verifyDependencies()
            }

            // Initialize integration
            initializeIntegration();
            
            getLogger().info("WaystoneWarps-Pl3xMap addon enabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Critical error during plugin initialization: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Stop update task
            if (updateTask != null) {
                updateTask.stop();
                updateTask = null;
            }
            
            // Shutdown layer manager
            if (layerManager != null) {
                layerManager.shutdown();
                layerManager = null;
            }
            
            getLogger().info("WaystoneWarps-Pl3xMap addon disabled");
            
        } catch (Exception e) {
            getLogger().warning("Error during plugin shutdown: " + e.getMessage());
        }
    }

    /**
     * Verifies that all required dependencies are present and compatible.
     * 
     * @return true if all dependencies are satisfied, false otherwise
     */
    private boolean verifyDependencies() {
        // Check for WaystoneWarps plugin
        Plugin waystonePlugin = getServer().getPluginManager().getPlugin("WaystoneWarps");
        if (waystonePlugin == null || !(waystonePlugin instanceof WaystoneWarps)) {
            getLogger().severe("WaystoneWarps plugin not found! This addon requires WaystoneWarps to function.");
            getLogger().severe("Please install WaystoneWarps: https://github.com/Mizarc/waystone-warps");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        // Check for Pl3xMap plugin
        if (getServer().getPluginManager().getPlugin("Pl3xMap") == null) {
            getLogger().severe("Pl3xMap plugin not found! This addon requires Pl3xMap to function.");
            getLogger().severe("Please install Pl3xMap: https://modrinth.com/plugin/pl3xmap");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        // Access the WarpRepository from WaystoneWarps using reflection
        // (no public API available yet)
        try {
            Field field = waystonePlugin.getClass().getDeclaredField("warpRepository");
            field.setAccessible(true);
            warpRepository = (WarpRepository) field.get(waystonePlugin);
            
            if (warpRepository == null) {
                throw new IllegalStateException("WarpRepository is null");
            }
            
            getLogger().info("Successfully connected to WaystoneWarps repository");
            
        } catch (NoSuchFieldException e) {
            getLogger().severe("Failed to access WarpRepository: Field 'warpRepository' not found.");
            getLogger().severe("This indicates a version mismatch with WaystoneWarps.");
            getLogger().severe("Please ensure you're using compatible versions (WaystoneWarps 0.3.5+).");
            getServer().getPluginManager().disablePlugin(this);
            return false;
            
        } catch (IllegalAccessException e) {
            getLogger().severe("Failed to access WarpRepository: Access denied.");
            getLogger().severe("This may be caused by Java security restrictions.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
            
        } catch (Exception e) {
            getLogger().severe("Failed to access WarpRepository: " + e.getMessage());
            getLogger().severe("Please ensure you're using compatible versions.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        return true;
    }

    /**
     * Initializes the Pl3xMap integration components.
     */
    private void initializeIntegration() {
        if (warpRepository == null) {
            getLogger().warning("Cannot initialize integration: WarpRepository is null");
            return;
        }

        // Read configuration
        boolean showLocked = getConfig().getBoolean("display.show-locked-waystones", true);
        int iconSize = getConfig().getInt("display.icon-size", 16);
        
        // Validate icon size
        if (iconSize <= 0 || iconSize > 128) {
            getLogger().warning("Invalid icon size: " + iconSize + ", using default 16px");
            iconSize = 16;
        }

        // Initialize layer manager
        layerManager = new Pl3xmapLayerManager(this, warpRepository, showLocked, iconSize);
        layerManager.initialize();
        
        getLogger().info("Pl3xMap waystone layer initialized successfully!");
        getLogger().info("Show locked waystones: " + showLocked);
        getLogger().info("Icon size: " + iconSize + "px");

        // Start periodic update task if enabled
        boolean autoRefreshEnabled = getConfig().getBoolean("auto-refresh.enabled", true);
        int refreshInterval = getConfig().getInt("auto-refresh.interval", 60);
        
        // Validate refresh interval
        if (refreshInterval <= 0) {
            getLogger().info("Auto-refresh disabled (interval <= 0)");
        } else if (autoRefreshEnabled) {
            updateTask = new WaystoneUpdateTask(this, layerManager, warpRepository, refreshInterval);
            updateTask.start();
        } else {
            getLogger().info("Auto-refresh disabled in configuration");
        }

        // Register reload command
        if (getCommand("waystones-reload-map") != null) {
            getCommand("waystones-reload-map").setExecutor(new ReloadCommand(this, layerManager));
            getLogger().info("Registered /waystones-reload-map command");
        } else {
            getLogger().warning("Failed to register /waystones-reload-map command");
        }

        // Register Pl3xMap reload listener for automatic recovery
        try {
            Pl3xMapReloadListener reloadListener = new Pl3xMapReloadListener(this, layerManager);
            Pl3xMap.api().getEventRegistry().register(reloadListener);
            getLogger().info("Registered Pl3xMap reload listener for automatic recovery");
        } catch (Exception e) {
            getLogger().warning("Failed to register Pl3xMap reload listener: " + e.getMessage());
            getLogger().warning("Automatic recovery from /map reload may not work");
        }
    }
}
