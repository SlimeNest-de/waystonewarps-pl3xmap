package org.jamesphbennett.waystonewarps.pl3xmap;

import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listens for Pl3xMap reload events and automatically re-registers layers.
 * 
 * When /map reload is executed, Pl3xMapEnabledEvent is fired.
 * This listener detects the reload and triggers layer re-registration.
 */
public class Pl3xMapReloadListener implements EventListener {
    private final Plugin plugin;
    private final Pl3xmapLayerManager layerManager;
    
    public Pl3xMapReloadListener(Plugin plugin, Pl3xmapLayerManager layerManager) {
        this.plugin = plugin;
        this.layerManager = layerManager;
    }
    
    @EventHandler
    public void onPl3xMapEnabled(Pl3xMapEnabledEvent event) {
        plugin.getLogger().info("Detected Pl3xMap reload, re-registering waystone layers...");
        
        // Re-register all layers and markers
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            layerManager.refreshAllMarkers();
            plugin.getLogger().info("Waystone layers re-registered after Pl3xMap reload");
        }, 20L); // Wait 1 second to ensure Pl3xMap is fully loaded
    }
}
