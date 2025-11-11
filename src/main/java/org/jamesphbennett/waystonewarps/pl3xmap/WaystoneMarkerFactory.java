package org.jamesphbennett.waystonewarps.pl3xmap;

import dev.mizarc.waystonewarps.domain.warps.Warp;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Factory class for creating waystone markers.
 *
 * Handles:
 * - Icon registration
 * - Marker creation with appropriate icons
 * - Tooltip generation (hover and click)
 */
public class WaystoneMarkerFactory {
    private static final String WAYSTONE_ICON_KEY = "waystone_icon";
    private static final String WAYSTONE_LOCKED_ICON_KEY = "waystone_locked_icon";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Plugin plugin;
    private final int iconSize;

    public WaystoneMarkerFactory(Plugin plugin, int iconSize) {
        this.plugin = plugin;
        this.iconSize = iconSize;
    }

    /**
     * Register waystone icons with Pl3xMap.
     */
    public void registerIcons() {
        registerIcon("waystone_icon.png", WAYSTONE_ICON_KEY, "waystone icon");
        registerIcon("waystone_locked_icon.png", WAYSTONE_LOCKED_ICON_KEY, "locked waystone icon");
    }

    /**
     * Helper method to register a single icon.
     * Skips if already registered to avoid errors on reload.
     */
    private void registerIcon(String resourcePath, String iconKey, String description) {
        try {
            // Check if icon is already registered
            if (Pl3xMap.api().getIconRegistry().has(iconKey)) {
                return; // Already registered, skip
            }
            
            InputStream stream = plugin.getResource(resourcePath);
            if (stream != null) {
                BufferedImage image = ImageIO.read(stream);
                Pl3xMap.api().getIconRegistry().register(new IconImage(iconKey, image, "png"));
                plugin.getLogger().info("Registered " + description);
                stream.close();
            } else {
                plugin.getLogger().warning(resourcePath + " not found in resources");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register " + description + ": " + e.getMessage());
        }
    }

    /**
     * Create a marker for a waystone.
     */
    public Marker<?> createMarker(Warp warp) {
        // Choose icon based on locked state
        String iconKey = warp.isLocked() ? WAYSTONE_LOCKED_ICON_KEY : WAYSTONE_ICON_KEY;

        // Create icon marker with tooltips
        Options options = Options.builder()
                .tooltipContent(buildTooltip(warp))
                .build();

        Point point = Point.of(warp.getPosition().getX(), warp.getPosition().getZ());
        Icon icon = new Icon("waystone_" + warp.getId(), point, iconKey, (double) iconSize);
        icon.setOptions(options);
        return icon;
    }

    /**
     * Build the tooltip (shown on hover/click).
     */
    private String buildTooltip(Warp warp) {
        StringBuilder tooltip = new StringBuilder(512);
        
        // Get data
        String ownerName = Bukkit.getOfflinePlayer(warp.getPlayerId()).getName();
        if (ownerName == null) ownerName = "Unknown";
        
        String lockStatus = warp.isLocked() 
            ? "<span style='color: #ff6b6b;'>🔒 Private</span>"
            : "<span style='color: #51cf66;'>🔓 Public</span>";
        
        org.bukkit.World world = Bukkit.getWorld(warp.getWorldId());
        String worldName = world != null ? world.getName() : "Unknown World";
        
        String creationTime = DATE_FORMATTER.format(warp.getCreationTime());

        // Build HTML
        tooltip.append("<div style='padding: 12px; font-family: sans-serif; min-width: 200px;'>")
               .append("<h3 style='margin: 0 0 12px 0; color: #5c7cfa; border-bottom: 2px solid #5c7cfa; padding-bottom: 4px;'>")
               .append(escapeHtml(warp.getName()))
               .append("</h3>")
               .append("<table style='width: 100%; border-collapse: collapse;'>");
        
        addTableRow(tooltip, "Owner:", escapeHtml(ownerName));
        addTableRow(tooltip, "Access:", lockStatus);
        addTableRow(tooltip, "World:", escapeHtml(worldName));
        
        tooltip.append("<tr>")
               .append("<td style='padding: 4px 8px; font-weight: bold; color: #495057;'>Location:</td>")
               .append("<td style='padding: 4px 8px; color: #212529; font-family: monospace;'>")
               .append(warp.getPosition().getX()).append(", ")
               .append(warp.getPosition().getY()).append(", ")
               .append(warp.getPosition().getZ())
               .append("</td></tr>");
        
        tooltip.append("<tr>")
               .append("<td style='padding: 4px 8px; font-weight: bold; color: #495057;'>Created:</td>")
               .append("<td style='padding: 4px 8px; color: #868e96; font-size: 12px;'>")
               .append(creationTime)
               .append("</td></tr>")
               .append("</table>")
               .append("</div>");

        return tooltip.toString();
    }

    /**
     * Helper method to add a table row to the tooltip.
     */
    private void addTableRow(StringBuilder tooltip, String label, String value) {
        tooltip.append("<tr>")
               .append("<td style='padding: 4px 8px; font-weight: bold; color: #495057;'>").append(label).append("</td>")
               .append("<td style='padding: 4px 8px; color: #212529;'>").append(value).append("</td>")
               .append("</tr>");
    }

    /**
     * Escape HTML special characters to prevent XSS.
     */
    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
