package com.cnaude.scavenger.Commands;

import com.cnaude.scavenger.Scavenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ScavengerOn implements CommandExecutor {

    final Scavenger plugin;

    public ScavengerOn(Scavenger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (sender instanceof Player) {
            if (plugin.hasPermission(sender, "scavenger.self.on")) {
                plugin.ignoreList.removePlayer(sender.getName());
                plugin.message(sender, plugin.config.msgSelfRecoveryEnabled());
            } else {
                plugin.message(sender, plugin.config.msgNoPerm());
            }
        }
        return true;
    }
}
