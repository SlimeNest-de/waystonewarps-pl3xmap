package org.jamesphbennett.waystonewarps.pl3xmap;

import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import org.bukkit.plugin.Plugin;

/**
 * Event listener for automatic recovery from Pl3xMap reloads.
 * 
 * When /map reload is executed, Pl3xMap clears all registered layers
 * and fires a Pl3xMapEnabledEvent. This listener detects that event and
 * automatically re-registers all waystone layers.
 * 
 * Thread-safe: Event handlers are called on the main Bukkit thread.
 */
public class Pl3xMapReloadListener implements EventListener {
    private static final long RELOAD_DELAY_TICKS = 20L; // 1 second
    
    private final Plugin plugin;
    private final Pl3xmapLayerManager layerManager;
    
    /**
     * Creates a new reload listener.
     * 
     * @param plugin The plugin instance for logging and scheduling
     * @param layerManager The layer manager to refresh after reload
     * @throws IllegalArgumentException if any parameter is null
     */
    public Pl3xMapReloadListener(Plugin plugin, Pl3xmapLayerManager layerManager) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (layerManager == null) {
            throw new IllegalArgumentException("Layer manager cannot be null");
        }
        
        this.plugin = plugin;
        this.layerManager = layerManager;
    }
    
    /**
     * Handles Pl3xMap reload events.
     * Schedules delayed re-registration to ensure Pl3xMap is fully initialized.
     * 
     * @param event The Pl3xMap enabled event
     */
    @EventHandler
    public void onPl3xMapEnabled(Pl3xMapEnabledEvent event) {
        plugin.getLogger().info("Detected Pl3xMap reload, re-registering waystone layers...");
        
        // Schedule re-registration with delay to ensure Pl3xMap is ready
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                layerManager.refreshAllMarkers();
                plugin.getLogger().info("Waystone layers re-registered after Pl3xMap reload");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to re-register layers after Pl3xMap reload: " + e.getMessage());
                e.printStackTrace();
            }
        }, RELOAD_DELAY_TICKS);
    }
}
