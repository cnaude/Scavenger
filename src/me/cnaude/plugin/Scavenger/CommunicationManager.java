package me.cnaude.plugin.Scavenger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommunicationManager {
    public static String MESSAGE_HEADER = ChatColor.WHITE + "[" + ChatColor.GOLD + Scavenger.PLUGIN_NAME + ChatColor.WHITE + "] " + ChatColor.WHITE;

    public void message(Player _player, String _message) {
        if (_player != null)
            _player.sendMessage(MESSAGE_HEADER + _message);
        else
            Scavenger.get().logInfo(_message);
    }

    public void error(Player _player, String _message) {
        if (_player != null)
            _player.sendMessage(MESSAGE_HEADER + ChatColor.RED + "Error: " + _message);
        else
            Scavenger.get().logError(_message);
    }

    public void command(Player _player, String _command, String _description) {
        if (_player != null)
            _player.sendMessage(MESSAGE_HEADER + ChatColor.GOLD + _command + ChatColor.WHITE + ": " + _description);
    }

    public void broadcast(Player _except, String _message) {
        for (World world : Scavenger.get().getServer().getWorlds()) {
            for (Player player : world.getPlayers()) {
                if (_except == null || !_except.getName().equals(player.getName()))
                    message(player, _message);
            }
        }
    }

    public String parse(String _message, CommandSender _sender, Player _target) {
        String player = (_sender instanceof Player) ? ((Player) _sender).getName() : "Console";
        String target = _target.getName();

        return _message.replaceAll("%player%", player).replaceAll("%target%", target);
    }
}
