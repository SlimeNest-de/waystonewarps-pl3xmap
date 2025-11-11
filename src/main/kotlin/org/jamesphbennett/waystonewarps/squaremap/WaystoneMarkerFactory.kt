package org.jamesphbennett.waystonewarps.squaremap

import dev.mizarc.waystonewarps.domain.warps.Warp
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import xyz.jpenilla.squaremap.api.Key
import xyz.jpenilla.squaremap.api.Registry
import xyz.jpenilla.squaremap.api.Point
import xyz.jpenilla.squaremap.api.marker.Marker
import xyz.jpenilla.squaremap.api.marker.MarkerOptions
import java.awt.image.BufferedImage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

/**
 * Factory class for creating waystone markers.
 *
 * Handles:
 * - Icon registration
 * - Marker creation with appropriate icons
 * - Tooltip generation (hover and click)
 */
class WaystoneMarkerFactory(private val plugin: Plugin, private val iconSize: Int = 16) {

    companion object {
        private val WAYSTONE_ICON_KEY = Key.of("waystone_icon")
        private val WAYSTONE_LOCKED_ICON_KEY = Key.of("waystone_locked_icon")

        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
    }

    /**
     * Register waystone icons with squaremap.
     */
    fun registerIcons(iconRegistry: Registry<BufferedImage>) {
        try {
            // Register normal waystone icon
            plugin.getResource("waystone_icon.png")?.use { stream ->
                val image = ImageIO.read(stream)
                iconRegistry.register(WAYSTONE_ICON_KEY, image)
                plugin.logger.info("Registered waystone icon")
            } ?: plugin.logger.warning("waystone_icon.png not found in resources")

            // Register locked waystone icon
            plugin.getResource("waystone_locked_icon.png")?.use { stream ->
                val image = ImageIO.read(stream)
                iconRegistry.register(WAYSTONE_LOCKED_ICON_KEY, image)
                plugin.logger.info("Registered locked waystone icon")
            } ?: plugin.logger.warning("waystone_locked_icon.png not found in resources")

        } catch (e: Exception) {
            plugin.logger.severe("Failed to register waystone icons: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Create a marker for a waystone.
     */
    fun createMarker(warp: Warp): Marker {
        // Get the coordinates
        val point = Point.of(warp.position.x.toDouble(), warp.position.z.toDouble())

        // Choose icon based on locked state
        val iconKey = if (warp.isLocked) WAYSTONE_LOCKED_ICON_KEY else WAYSTONE_ICON_KEY

        // Create icon marker
        val icon = Marker.icon(point, iconKey, iconSize)

        // Configure marker options with tooltips
        val options = MarkerOptions.builder()
            .hoverTooltip(buildHoverTooltip(warp))
            .clickTooltip(buildClickTooltip(warp))
            .build()

        icon.markerOptions(options)

        return icon
    }

    /**
     * Build the hover tooltip (shown on mouseover).
     */
    private fun buildHoverTooltip(warp: Warp): String {
        return """
            <div style='text-align: center; padding: 5px; font-family: sans-serif;'>
                <b style='font-size: 14px; color: #5c7cfa;'>${escapeHtml(warp.name)}</b>
            </div>
        """.trimIndent()
    }

    /**
     * Build the click tooltip (shown on click with detailed info).
     */
    private fun buildClickTooltip(warp: Warp): String {
        val lockStatus = if (warp.isLocked) {
            "<span style='color: #ff6b6b;'>🔒 Private</span>"
        } else {
            "<span style='color: #51cf66;'>🔓 Public</span>"
        }

        // Get player name
        val ownerName = Bukkit.getOfflinePlayer(warp.playerId).name ?: "Unknown"

        // Format creation time
        val creationTime = DATE_FORMATTER.format(warp.creationTime)

        // Get world name
        val worldName = Bukkit.getWorld(warp.worldId)?.name ?: "Unknown World"

        return """
            <div style='padding: 12px; font-family: sans-serif; min-width: 200px;'>
                <h3 style='margin: 0 0 12px 0; color: #5c7cfa; border-bottom: 2px solid #5c7cfa; padding-bottom: 4px;'>
                    ${escapeHtml(warp.name)}
                </h3>
                <table style='width: 100%; border-collapse: collapse;'>
                    <tr>
                        <td style='padding: 4px 8px; font-weight: bold; color: #495057;'>Owner:</td>
                        <td style='padding: 4px 8px; color: #212529;'>${escapeHtml(ownerName)}</td>
                    </tr>
                    <tr>
                        <td style='padding: 4px 8px; font-weight: bold; color: #495057;'>Access:</td>
                        <td style='padding: 4px 8px;'>$lockStatus</td>
                    </tr>
                    <tr>
                        <td style='padding: 4px 8px; font-weight: bold; color: #495057;'>World:</td>
                        <td style='padding: 4px 8px; color: #212529;'>${escapeHtml(worldName)}</td>
                    </tr>
                    <tr>
                        <td style='padding: 4px 8px; font-weight: bold; color: #495057;'>Location:</td>
                        <td style='padding: 4px 8px; color: #212529; font-family: monospace;'>
                            ${warp.position.x}, ${warp.position.y}, ${warp.position.z}
                        </td>
                    </tr>
                    <tr>
                        <td style='padding: 4px 8px; font-weight: bold; color: #495057;'>Created:</td>
                        <td style='padding: 4px 8px; color: #868e96; font-size: 12px;'>$creationTime</td>
                    </tr>
                </table>
            </div>
        """.trimIndent()
    }

    /**
     * Escape HTML special characters to prevent XSS.
     */
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
