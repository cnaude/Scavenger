package com.cnaude.scavenger.Commands;

import com.cnaude.scavenger.Scavenger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ScavengerRestoreInv implements CommandExecutor {

    final Scavenger plugin;

    public ScavengerRestoreInv(Scavenger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (sender instanceof Player) {
            plugin.rm.cmdRestore((Player) sender);
        } else {
            sender.sendMessage(ChatColor.RED + "You are not a player.");
        }
        return true;
    }
}
