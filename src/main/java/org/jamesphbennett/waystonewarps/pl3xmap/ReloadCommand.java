package org.jamesphbennett.waystonewarps.pl3xmap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Command to manually reload waystone markers on the map.
 * Executes asynchronously to avoid blocking the main server thread.
 */
public class ReloadCommand implements CommandExecutor {
    private final Plugin plugin;
    private final Pl3xmapLayerManager layerManager;

    public ReloadCommand(Plugin plugin, Pl3xmapLayerManager layerManager) {
        this.plugin = plugin;
        this.layerManager = layerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("waystonewarps.pl3xmap.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        sender.sendMessage("§eRefreshing waystone markers on the map...");

        // Run async to avoid blocking main thread
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                layerManager.refreshAllMarkers();
                sender.sendMessage("§aWaystone markers refreshed successfully!");
            } catch (Exception e) {
                sender.sendMessage("§cError refreshing markers: " + e.getMessage());
                plugin.getLogger().warning("Failed to refresh markers: " + e.getMessage());
            }
        });

        return true;
    }
}
