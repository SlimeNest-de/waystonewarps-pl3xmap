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
 * This addon displays waystones from the WaystoneWarps plugin as toggleable markers
 * on Pl3xMap. It automatically syncs with the waystone database and updates markers
 * when the map is loaded.
 */
public class WaystonePl3xmapAddon extends JavaPlugin {
    private Pl3xmapLayerManager layerManager;
    private WarpRepository warpRepository;
    private WaystoneUpdateTask updateTask;

    @Override
    public void onEnable() {
        // Load and save default config
        saveDefaultConfig();

        // Check for WaystoneWarps plugin
        Plugin waystonePlugin = getServer().getPluginManager().getPlugin("WaystoneWarps");
        if (waystonePlugin == null || !(waystonePlugin instanceof WaystoneWarps)) {
            getLogger().severe("WaystoneWarps plugin not found! This addon requires WaystoneWarps to function.");
            getLogger().severe("Please install WaystoneWarps: https://github.com/Mizarc/waystone-warps");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Check for Pl3xMap plugin
        if (getServer().getPluginManager().getPlugin("Pl3xMap") == null) {
            getLogger().severe("Pl3xMap plugin not found! This addon requires Pl3xMap to function.");
            getLogger().severe("Please install Pl3xMap: https://modrinth.com/plugin/pl3xmap");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Access the WarpRepository from WaystoneWarps
        // Using reflection since there's no public API yet
        try {
            Field field = waystonePlugin.getClass().getDeclaredField("warpRepository");
            field.setAccessible(true);
            warpRepository = (WarpRepository) field.get(waystonePlugin);
            getLogger().info("Successfully connected to WaystoneWarps repository");
        } catch (Exception e) {
            getLogger().severe("Failed to access WarpRepository from WaystoneWarps: " + e.getMessage());
            getLogger().severe("This might be due to a version mismatch. Please ensure you're using compatible versions.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize Pl3xMap layer manager
        if (warpRepository != null) {
            boolean showLocked = getConfig().getBoolean("display.show-locked-waystones", true);
            int iconSize = getConfig().getInt("display.icon-size", 16);

            layerManager = new Pl3xmapLayerManager(this, warpRepository, showLocked, iconSize);
            layerManager.initialize();
            getLogger().info("Pl3xMap waystone layer initialized successfully!");
            getLogger().info("Show locked waystones: " + showLocked);
            getLogger().info("Icon size: " + iconSize + "px");
        }

        // Start periodic update task if enabled
        if (warpRepository != null) {
            boolean autoRefreshEnabled = getConfig().getBoolean("auto-refresh.enabled", true);
            int refreshInterval = getConfig().getInt("auto-refresh.interval", 60);

            if (autoRefreshEnabled && refreshInterval > 0) {
                updateTask = new WaystoneUpdateTask(this, layerManager, warpRepository, refreshInterval);
                updateTask.start();
            } else {
                getLogger().info("Auto-refresh disabled. Use /waystones-reload-map to manually refresh markers.");
            }
        }

        // Register reload command
        if (layerManager != null) {
            getCommand("waystones-reload-map").setExecutor(new ReloadCommand(this, layerManager));
        }

        // Register Pl3xMap reload listener
        if (layerManager != null) {
            Pl3xMapReloadListener reloadListener = new Pl3xMapReloadListener(this, layerManager);
            Pl3xMap.api().getEventRegistry().register(reloadListener);
            getLogger().info("Registered Pl3xMap reload listener");
        }

        getLogger().info("WaystoneWarps-Pl3xMap addon enabled!");
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.stop();
        }
        if (layerManager != null) {
            layerManager.shutdown();
        }
        getLogger().info("WaystoneWarps-Pl3xMap addon disabled");
    }
}
