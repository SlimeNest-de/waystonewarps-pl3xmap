package org.jamesphbennett.waystonewarps.squaremap

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Command to manually reload waystone markers on the map.
 */
class ReloadCommand(private val layerManager: SquaremapLayerManager) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("waystonewarps.squaremap.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command.")
            return true
        }

        sender.sendMessage("§eRefreshing waystone markers on the map...")
        layerManager.refreshAllMarkers()
        sender.sendMessage("§aWaystone markers refreshed successfully!")

        return true
    }
}
