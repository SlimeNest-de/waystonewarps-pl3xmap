package org.jamesphbennett.waystonewarps.squaremap

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

/**
 * Periodic task for detecting waystone changes.
 *
 * Since WaystoneWarps doesn't expose custom events, we periodically check
 * for changes by comparing a hash of all warp data.
 *
 * For immediate updates, use the /waystones-reload command.
 */
class WaystoneUpdateTask(
    private val plugin: Plugin,
    private val layerManager: SquaremapLayerManager?,
    private val warpRepository: WarpRepository,
    private val intervalSeconds: Int
) {

    private var task: BukkitTask? = null
    private var lastWarpsHash = 0

    init {
        lastWarpsHash = calculateWarpsHash()
    }

    /**
     * Start the periodic update task.
     */
    fun start() {
        if (intervalSeconds <= 0) {
            plugin.logger.info("Periodic waystone refresh disabled (interval = 0)")
            return
        }

        val intervalTicks = intervalSeconds * 20L
        task = plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable { checkForWarpChanges() },
            intervalTicks, // Initial delay
            intervalTicks  // Repeat interval
        )
        plugin.logger.info("Periodic waystone refresh enabled (every ${intervalSeconds}s)")
    }

    /**
     * Stop the periodic update task.
     */
    fun stop() {
        task?.cancel()
        task = null
    }

    /**
     * Calculate a hash of all warp data to detect any changes.
     */
    private fun calculateWarpsHash(): Int {
        return warpRepository.getAll().hashCode()
    }

    /**
     * Check if warps have changed (added, removed, or modified) and refresh markers if needed.
     * Uses a hash to detect any changes including position, name, locked status, etc.
     */
    private fun checkForWarpChanges() {
        val currentHash = calculateWarpsHash()

        if (currentHash != lastWarpsHash) {
            plugin.logger.info("Waystone changes detected, refreshing markers...")
            layerManager?.refreshAllMarkers()
            lastWarpsHash = currentHash
        }
    }
}
