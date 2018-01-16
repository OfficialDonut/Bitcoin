package us._donut_.bitcoin;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

class BitcoinCommand implements CommandExecutor {

    private Bitcoin plugin;
    private Util util;
    private BitcoinMenu bitcoinMenu;

    BitcoinCommand(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        bitcoinMenu = plugin.getBitcoinMenu();
        util = plugin.getUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bitcoin")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(plugin.getUtil().colorMessage("&cYou cannot use this command from console."));
            } else {
                Player player = (Player) sender;
                if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
                    if (bitcoinMenu.getPlayersExchanging().contains(player)) {
                        bitcoinMenu.getPlayersExchanging().remove(player);
                        player.sendMessage(util.colorMessage("&cCancelled exchange."));
                        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
                    } else if (bitcoinMenu.getPlayersTransferring().contains(player)) {
                        bitcoinMenu.getPlayersTransferring().remove(player);
                        player.sendMessage(util.colorMessage("&cCancelled transfer."));
                        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 1);
                    } else {
                        player.sendMessage(util.colorMessage("&cNothing to cancel."));
                    }
                } else {
                    plugin.getBitcoinMenu().open(player);
                }
            }
            return true;
        }
        return false;
    }
}