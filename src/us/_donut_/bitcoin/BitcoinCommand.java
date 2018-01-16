package us._donut_.bitcoin;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

class BitcoinCommand implements CommandExecutor, Listener {

    private Bitcoin plugin;
    private BitcoinMenu bitcoinMenu;
    private Messages messages;

    BitcoinCommand(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        bitcoinMenu = plugin.getBitcoinMenu();
        messages = plugin.getMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bitcoin")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(messages.getMessage("cannot_use_from_console"));
            } else {
                Player player = (Player) sender;
                if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
                    if (bitcoinMenu.getPlayersExchanging().contains(player)) {
                        bitcoinMenu.getPlayersExchanging().remove(player);
                        player.sendMessage(messages.getMessage("cancelled_exchange"));
                        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
                    } else if (bitcoinMenu.getPlayersTransferring().contains(player)) {
                        bitcoinMenu.getPlayersTransferring().remove(player);
                        player.sendMessage(messages.getMessage("cancelled_transfer"));
                        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
                    } else {
                        player.sendMessage(messages.getMessage("nothing_to_cancel"));
                    }
                } else {
                    plugin.getBitcoinMenu().open(player);
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!messages.getMessage("command_name").equalsIgnoreCase("/bitcoin") && event.getMessage().equalsIgnoreCase(messages.getMessage("command_name"))) {
            event.getPlayer().performCommand("bitcoin");
            event.setCancelled(true);
        }
        if (event.getMessage().split(" ")[0].equalsIgnoreCase("/bitcoin") && !messages.getMessage("command_name").equalsIgnoreCase("/bitcoin")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Unknown command. Type \"/help\" for help.");
        }
    }
}