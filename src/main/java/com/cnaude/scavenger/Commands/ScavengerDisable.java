package com.cnaude.scavenger.Commands;

import com.cnaude.scavenger.Scavenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author cnaude
 */
public class ScavengerDisable implements CommandExecutor {

    final Scavenger plugin;

    public ScavengerDisable(Scavenger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (plugin.hasPermission(sender, "scavenger.disable")) {
            plugin.config.setEnabled(false);
            plugin.message(sender, "Scavenger is now off.");
        } else {
            plugin.message(sender, plugin.config.msgNoPerm());
        }
        return true;
    }
}
