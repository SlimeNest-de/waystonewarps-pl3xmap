package org.jamesphbennett.waystonewarps.pl3xmap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Command executor for manually reloading waystone markers.
 * 
 * Provides the /waystones-reload-map command for server operators
 * to manually refresh all waystone markers on the map.
 * 
 * Executes asynchronously to prevent blocking the main server thread.
 * 
 * Permission: waystonewarps.pl3xmap.reload
 */
public class ReloadCommand implements CommandExecutor {
    private static final String PERMISSION = "waystonewarps.pl3xmap.reload";
    private static final String MSG_NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String MSG_RELOADING = "§eRefreshing waystone markers on the map...";
    private static final String MSG_SUCCESS = "§aWaystone markers refreshed successfully!";
    private static final String MSG_ERROR_PREFIX = "§cError refreshing markers: ";
    
    private final Plugin plugin;
    private final Pl3xmapLayerManager layerManager;

    /**
     * Creates a new reload command executor.
     * 
     * @param plugin The plugin instance for logging and scheduling
     * @param layerManager The layer manager to refresh
     * @throws IllegalArgumentException if any parameter is null
     */
    public ReloadCommand(Plugin plugin, Pl3xmapLayerManager layerManager) {
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
     * Executes the reload command.
     * Checks permissions, then schedules an asynchronous refresh of all markers.
     * 
     * @param sender The command sender
     * @param command The command object
     * @param label The command label used
     * @param args Command arguments (unused)
     * @return true to indicate the command was handled
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permissions
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(MSG_NO_PERMISSION);
            return true;
        }

        sender.sendMessage(MSG_RELOADING);

        // Execute refresh asynchronously to avoid blocking main thread
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                layerManager.refreshAllMarkers();
                sender.sendMessage(MSG_SUCCESS);
            } catch (Exception e) {
                sender.sendMessage(MSG_ERROR_PREFIX + e.getMessage());
                plugin.getLogger().warning("Failed to refresh markers via command: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return true;
    }
}
