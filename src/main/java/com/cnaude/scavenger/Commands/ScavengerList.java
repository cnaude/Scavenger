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
public class ScavengerList implements CommandExecutor {

    final Scavenger plugin;

    public ScavengerList(Scavenger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (plugin.hasPermission(sender, "scavenger.list")) {
            plugin.rm.printRestorations(sender);
        } else {
            plugin.message(sender, plugin.config.msgNoPerm());
        }
        return true;
    }
}
