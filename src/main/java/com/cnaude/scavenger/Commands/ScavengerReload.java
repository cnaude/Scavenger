package com.cnaude.scavenger.Commands;

import com.cnaude.scavenger.Scavenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author cnaude
 */
public class ScavengerReload implements CommandExecutor {

    final Scavenger plugin;

    public ScavengerReload(Scavenger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (plugin.hasPermission(sender, "scavenger.reload")) {
            plugin.loadConfig();
            plugin.message(sender, "Configuration reloaded.");
        } else {
            plugin.message(sender, plugin.config.msgNoPerm());
        }
        return true;
    }
}
