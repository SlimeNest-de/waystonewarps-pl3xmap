package org.jamesphbennett.waystonewarps.pl3xmap;

import dev.mizarc.waystonewarps.domain.warps.WarpRepository;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * Periodic task for detecting waystone changes.
 *
 * Uses hash-based change detection to avoid unnecessary marker refreshes.
 * More efficient than checking every individual warp.
 */
public class WaystoneUpdateTask {
    private final Plugin plugin;
    private final Pl3xmapLayerManager layerManager;
    private final WarpRepository warpRepository;
    private final int intervalSeconds;

    private BukkitTask task;
    private int lastWarpsHash;

    public WaystoneUpdateTask(Plugin plugin, Pl3xmapLayerManager layerManager, WarpRepository warpRepository, int intervalSeconds) {
        this.plugin = plugin;
        this.layerManager = layerManager;
        this.warpRepository = warpRepository;
        this.intervalSeconds = intervalSeconds;
        this.lastWarpsHash = calculateWarpsHash();
    }

    /**
     * Start the periodic update task.
     */
    public void start() {
        if (intervalSeconds <= 0) {
            plugin.getLogger().info("Periodic waystone refresh disabled (interval = 0)");
            return;
        }

        long intervalTicks = intervalSeconds * 20L;
        task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::checkForWarpChanges,
                intervalTicks,
                intervalTicks
        );
        plugin.getLogger().info("Periodic waystone refresh enabled (every " + intervalSeconds + "s)");
    }

    /**
     * Stop the periodic update task.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Calculate a hash of all warp data to detect any changes.
     * Uses size + individual warp hashcodes for better change detection.
     */
    private int calculateWarpsHash() {
        Collection<?> warps = warpRepository.getAll();
        int hash = warps.size();
        for (Object warp : warps) {
            hash = 31 * hash + (warp != null ? warp.hashCode() : 0);
        }
        return hash;
    }

    /**
     * Check if warps have changed and refresh markers if needed.
     */
    private void checkForWarpChanges() {
        int currentHash = calculateWarpsHash();

        if (currentHash != lastWarpsHash) {
            plugin.getLogger().info("Waystone changes detected, refreshing markers...");
            layerManager.refreshAllMarkers();
            lastWarpsHash = currentHash;
        }
    }
}
