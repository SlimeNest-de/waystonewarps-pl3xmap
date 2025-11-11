package org.jamesphbennett.waystonewarps.pl3xmap;

import dev.mizarc.waystonewarps.domain.warps.Warp;
import dev.mizarc.waystonewarps.domain.warps.WarpRepository;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

/**
 * Periodic task for detecting and handling waystone changes.
 * 
 * Uses hash-based change detection to efficiently detect when waystones
 * are added, removed, or modified. Only triggers marker refreshes when
 * actual changes occur.
 * 
 * Thread-safe: Runs on Bukkit's scheduler thread.
 */
public class WaystoneUpdateTask {
    private final Plugin plugin;
    private final Pl3xmapLayerManager layerManager;
    private final WarpRepository warpRepository;
    private final int intervalSeconds;

    private BukkitTask task;
    private int lastWarpsHash;

    /**
     * Creates a new update task.
     * 
     * @param plugin The plugin instance for logging and scheduling
     * @param layerManager The layer manager to refresh when changes are detected
     * @param warpRepository Repository containing waystone data
     * @param intervalSeconds Interval between checks in seconds (must be positive)
     * @throws IllegalArgumentException if any parameter is null or interval is invalid
     */
    public WaystoneUpdateTask(Plugin plugin, Pl3xmapLayerManager layerManager, WarpRepository warpRepository, int intervalSeconds) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (layerManager == null) {
            throw new IllegalArgumentException("Layer manager cannot be null");
        }
        if (warpRepository == null) {
            throw new IllegalArgumentException("Warp repository cannot be null");
        }
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Interval must be positive");
        }
        
        this.plugin = plugin;
        this.layerManager = layerManager;
        this.warpRepository = warpRepository;
        this.intervalSeconds = intervalSeconds;
        this.lastWarpsHash = calculateWarpsHash();
    }

    /**
     * Starts the periodic update task.
     * Schedules a repeating task that checks for waystone changes.
     */
    public void start() {
        if (intervalSeconds <= 0) {
            plugin.getLogger().info("Periodic waystone refresh disabled (interval <= 0)");
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
     * Stops the periodic update task.
     * Cancels the scheduled task if running. Safe to call multiple times.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Calculates a hash representing the current state of all waystones.
     * Combines total count with each waystone's hash code for change detection.
     * 
     * @return Hash representing the current waystone state
     */
    private int calculateWarpsHash() {
        try {
            Collection<Warp> warps = warpRepository.getAll();
            int hash = warps.size();
            
            // Combine hash codes of all individual warps
            for (Warp warp : warps) {
                if (warp != null) {
                    // Use prime number for better hash distribution
                    hash = 31 * hash + warp.hashCode();
                }
            }
            
            return hash;
        } catch (Exception e) {
            plugin.getLogger().warning("Error calculating waystone hash: " + e.getMessage());
            return 0; // Return safe default
        }
    }

    /**
     * Checks if waystones have changed and triggers a refresh if needed.
     * Compares current hash with previously stored hash.
     */
    private void checkForWarpChanges() {
        try {
            int currentHash = calculateWarpsHash();

            if (currentHash != lastWarpsHash) {
                plugin.getLogger().info("Waystone changes detected, refreshing markers...");
                layerManager.refreshAllMarkers();
                lastWarpsHash = currentHash;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking for waystone changes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
