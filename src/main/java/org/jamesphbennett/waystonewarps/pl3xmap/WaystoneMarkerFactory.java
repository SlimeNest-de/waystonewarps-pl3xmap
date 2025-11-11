package org.jamesphbennett.waystonewarps.pl3xmap;

import dev.mizarc.waystonewarps.domain.warps.Warp;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Factory for creating waystone markers with custom icons and tooltips.
 * 
 * Responsibilities:
 * - Register custom icon images with Pl3xMap
 * - Create marker objects with appropriate icons (locked/unlocked)
 * - Generate HTML tooltips with waystone information
 * 
 * Security: All user-generated content is properly escaped to prevent XSS attacks.
 */
public class WaystoneMarkerFactory {
    private static final String WAYSTONE_ICON_KEY = "waystone_icon";
    private static final String WAYSTONE_LOCKED_ICON_KEY = "waystone_locked_icon";
    private static final String WAYSTONE_ICON_PATH = "waystone_icon.png";
    private static final String WAYSTONE_LOCKED_ICON_PATH = "waystone_locked_icon.png";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    
    private static final String TOOLTIP_STYLE_CONTAINER = "padding: 12px; font-family: sans-serif; min-width: 200px;";
    private static final String TOOLTIP_STYLE_HEADER = "margin: 0 0 12px 0; color: #5c7cfa; border-bottom: 2px solid #5c7cfa; padding-bottom: 4px;";
    private static final String TOOLTIP_STYLE_TABLE = "width: 100%; border-collapse: collapse;";
    private static final String TOOLTIP_STYLE_CELL_LABEL = "padding: 4px 8px; font-weight: bold; color: #495057;";
    private static final String TOOLTIP_STYLE_CELL_VALUE = "padding: 4px 8px; color: #212529;";
    
    private final Plugin plugin;
    private final int iconSize;

    /**
     * Creates a new marker factory.
     * 
     * @param plugin The plugin instance for resource access and logging
     * @param iconSize Size of waystone icons in pixels
     * @throws IllegalArgumentException if plugin is null or iconSize is invalid
     */
    public WaystoneMarkerFactory(Plugin plugin, int iconSize) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (iconSize <= 0) {
            throw new IllegalArgumentException("Icon size must be positive");
        }
        
        this.plugin = plugin;
        this.iconSize = iconSize;
    }

    /**
     * Registers waystone icons with Pl3xMap.
     * 
     * Idempotent: Can be called multiple times safely. Icons are only
     * registered if not already present in the Pl3xMap registry.
     */
    public void registerIcons() {
        registerIcon(WAYSTONE_ICON_PATH, WAYSTONE_ICON_KEY, "waystone icon");
        registerIcon(WAYSTONE_LOCKED_ICON_PATH, WAYSTONE_LOCKED_ICON_KEY, "locked waystone icon");
    }

    /**
     * Registers a single icon with Pl3xMap.
     * 
     * Loads the icon image from plugin resources and registers it.
     * Skips registration if the icon is already registered.
     * 
     * @param resourcePath Path to icon image in plugin resources
     * @param iconKey Unique identifier for the icon
     * @param description Human-readable description for logging
     */
    private void registerIcon(String resourcePath, String iconKey, String description) {
        // Check if already registered to avoid duplicate registration errors
        if (Pl3xMap.api().getIconRegistry().has(iconKey)) {
            return;
        }
        
        InputStream stream = null;
        try {
            stream = plugin.getResource(resourcePath);
            if (stream == null) {
                plugin.getLogger().warning(resourcePath + " not found in plugin resources");
                return;
            }
            
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                plugin.getLogger().warning("Failed to read image from " + resourcePath);
                return;
            }
            
            Pl3xMap.api().getIconRegistry().register(new IconImage(iconKey, image, "png"));
            plugin.getLogger().info("Registered " + description);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register " + description + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure stream is always closed
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to close resource stream: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates a marker for a waystone with appropriate icon and tooltip.
     * 
     * @param warp The waystone to create a marker for
     * @return The created marker
     * @throws IllegalArgumentException if warp is null
     */
    public Marker<?> createMarker(Warp warp) {
        if (warp == null) {
            throw new IllegalArgumentException("Warp cannot be null");
        }
        
        // Choose icon based on locked state
        String iconKey = warp.isLocked() ? WAYSTONE_LOCKED_ICON_KEY : WAYSTONE_ICON_KEY;

        // Create marker with tooltip
        Options options = Options.builder()
                .tooltipContent(buildTooltip(warp))
                .build();

        Point point = Point.of(warp.getPosition().getX(), warp.getPosition().getZ());
        Icon icon = new Icon("waystone_" + warp.getId(), point, iconKey, (double) iconSize);
        icon.setOptions(options);
        
        return icon;
    }

    /**
     * Builds an HTML tooltip for a waystone marker.
     * 
     * Generates a formatted HTML table with waystone information including
     * name, owner, access status, world, coordinates, and creation timestamp.
     * 
     * Security: All user-generated content is sanitized via escapeHtml
     * to prevent XSS attacks.
     * 
     * @param warp The waystone to generate a tooltip for
     * @return HTML string for the tooltip
     */
    private String buildTooltip(Warp warp) {
        StringBuilder tooltip = new StringBuilder(512);
        
        // Get waystone data with null safety
        OfflinePlayer owner = Bukkit.getOfflinePlayer(warp.getPlayerId());
        String ownerName = owner.getName();
        if (ownerName == null) {
            ownerName = "Unknown";
        }
        
        String lockStatus = warp.isLocked() 
            ? "<span style='color: #ff6b6b;'>🔒 Private</span>"
            : "<span style='color: #51cf66;'>🔓 Public</span>";
        
        org.bukkit.World world = Bukkit.getWorld(warp.getWorldId());
        String worldName = world != null ? world.getName() : "Unknown World";
        
        Instant creationInstant = warp.getCreationTime();
        String creationTime = creationInstant != null 
            ? DATE_FORMATTER.format(creationInstant)
            : "Unknown";

        // Build HTML tooltip with proper escaping
        tooltip.append("<div style='").append(TOOLTIP_STYLE_CONTAINER).append("'>")
               .append("<h3 style='").append(TOOLTIP_STYLE_HEADER).append("'>")
               .append(escapeHtml(warp.getName()))
               .append("</h3>")
               .append("<table style='").append(TOOLTIP_STYLE_TABLE).append("'>");
        
        addTableRow(tooltip, "Owner:", escapeHtml(ownerName));
        addTableRow(tooltip, "Access:", lockStatus); // lockStatus is already safe HTML
        addTableRow(tooltip, "World:", escapeHtml(worldName));
        
        // Location row with monospace font
        tooltip.append("<tr>")
               .append("<td style='").append(TOOLTIP_STYLE_CELL_LABEL).append("'>Location:</td>")
               .append("<td style='").append(TOOLTIP_STYLE_CELL_VALUE).append(" font-family: monospace;'>")
               .append(warp.getPosition().getX()).append(", ")
               .append(warp.getPosition().getY()).append(", ")
               .append(warp.getPosition().getZ())
               .append("</td></tr>");
        
        // Creation timestamp row
        tooltip.append("<tr>")
               .append("<td style='").append(TOOLTIP_STYLE_CELL_LABEL).append("'>Created:</td>")
               .append("<td style='").append(TOOLTIP_STYLE_CELL_VALUE).append(" color: #868e96; font-size: 12px;'>")
               .append(escapeHtml(creationTime))
               .append("</td></tr>")
               .append("</table>")
               .append("</div>");

        return tooltip.toString();
    }

    /**
     * Adds a table row to the tooltip HTML.
     * 
     * @param tooltip StringBuilder to append to
     * @param label Row label (left column)
     * @param value Row value (right column)
     */
    private void addTableRow(StringBuilder tooltip, String label, String value) {
        tooltip.append("<tr>")
               .append("<td style='").append(TOOLTIP_STYLE_CELL_LABEL).append("'>").append(label).append("</td>")
               .append("<td style='").append(TOOLTIP_STYLE_CELL_VALUE).append("'>").append(value).append("</td>")
               .append("</tr>");
    }

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     * 
     * @param text Text to escape
     * @return HTML-safe text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
