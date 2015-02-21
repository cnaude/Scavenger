package com.cnaude.scavenger.Commands;

import com.cnaude.scavenger.Scavenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author cnaude
 */
public class ScavengerEnable implements CommandExecutor {

    final Scavenger plugin;

    public ScavengerEnable(Scavenger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (plugin.hasPermission(sender, "scavenger.enable")) {
            plugin.config.setEnabled(true);
            plugin.message(sender, "Scavenger is now on.");
        } else {
            plugin.message(sender, plugin.config.msgNoPerm());
        }
        return true;
    }
}
