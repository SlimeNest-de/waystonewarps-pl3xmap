package org.jamesphbennett.waystonewarps.squaremap

import dev.mizarc.waystonewarps.WaystoneWarps
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import org.bukkit.plugin.java.JavaPlugin

/**
 * Main plugin class for WaystoneWarps-Squaremap integration.
 *
 * This addon displays waystones from the WaystoneWarps plugin as toggleable markers
 * on squaremap. It automatically syncs with the waystone database and updates markers
 * when the map is loaded.
 */
class WaystoneSquaremapAddon : JavaPlugin() {
    private var layerManager: SquaremapLayerManager? = null
    private var warpRepository: WarpRepository? = null
    private var updateTask: WaystoneUpdateTask? = null

    override fun onEnable() {
        // Load and save default config
        saveDefaultConfig()

        // Check for WaystoneWarps plugin
        val waystonePlugin = server.pluginManager.getPlugin("WaystoneWarps")
        if (waystonePlugin == null || waystonePlugin !is WaystoneWarps) {
            logger.severe("WaystoneWarps plugin not found! This addon requires WaystoneWarps to function.")
            logger.severe("Please install WaystoneWarps: https://github.com/Mizarc/waystone-warps")
            server.pluginManager.disablePlugin(this)
            return
        }

        // Check for squaremap plugin
        if (server.pluginManager.getPlugin("squaremap") == null) {
            logger.severe("squaremap plugin not found! This addon requires squaremap to function.")
            logger.severe("Please install squaremap: https://github.com/jpenilla/squaremap")
            server.pluginManager.disablePlugin(this)
            return
        }

        // Access the WarpRepository from WaystoneWarps
        // Using reflection since there's no public API yet
        try {
            val field = waystonePlugin.javaClass.getDeclaredField("warpRepository")
            field.isAccessible = true
            warpRepository = field.get(waystonePlugin) as WarpRepository
            logger.info("Successfully connected to WaystoneWarps repository")
        } catch (e: Exception) {
            logger.severe("Failed to access WarpRepository from WaystoneWarps: ${e.message}")
            logger.severe("This might be due to a version mismatch. Please ensure you're using compatible versions.")
            server.pluginManager.disablePlugin(this)
            return
        }

        // Initialize squaremap layer manager
        warpRepository?.let { repo ->
            val showLocked = config.getBoolean("display.show-locked-waystones", true)
            val iconSize = config.getInt("display.icon-size", 16)

            layerManager = SquaremapLayerManager(this, repo, showLocked, iconSize)
            layerManager?.initialize()
            logger.info("Squaremap waystone layer initialized successfully!")
            logger.info("Show locked waystones: $showLocked")
            logger.info("Icon size: ${iconSize}px")
        }

        // Start periodic update task if enabled
        warpRepository?.let { repo ->
            val autoRefreshEnabled = config.getBoolean("auto-refresh.enabled", true)
            val refreshInterval = config.getInt("auto-refresh.interval", 60)

            if (autoRefreshEnabled && refreshInterval > 0) {
                updateTask = WaystoneUpdateTask(this, layerManager, repo, refreshInterval)
                updateTask?.start()
            } else {
                logger.info("Auto-refresh disabled. Use /waystones-reload-map to manually refresh markers.")
            }
        }

        // Register reload command
        layerManager?.let {
            getCommand("waystones-reload-map")?.setExecutor(ReloadCommand(it))
        }

        logger.info("WaystoneWarps-Squaremap addon enabled!")
    }

    override fun onDisable() {
        updateTask?.stop()
        layerManager?.shutdown()
        logger.info("WaystoneWarps-Squaremap addon disabled")
    }
}
