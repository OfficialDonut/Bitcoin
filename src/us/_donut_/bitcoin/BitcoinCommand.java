package us._donut_.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

class BitcoinCommand implements CommandExecutor, Listener {

    private Bitcoin plugin;
    private Util util;
    private BitcoinManager bitcoinManager;
    private BitcoinMenu bitcoinMenu;
    private Mining mining;
    private Messages messages;
    private Sounds sounds;

    BitcoinCommand(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = plugin.getUtil();
        bitcoinManager = plugin.getBitcoinManager();
        bitcoinMenu = plugin.getBitcoinMenu();
        mining = plugin.getMining();
        messages = plugin.getMessages();
        sounds = plugin.getSounds();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bitcoin")) {

            if (args.length == 0) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(messages.getMessage("cannot_use_from_console")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.main")) { player.sendMessage(messages.getMessage("no_permission")); return true; }
                plugin.getBitcoinMenu().open(player);
            }

            else if (args[0].equalsIgnoreCase("cancel")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(messages.getMessage("cannot_use_from_console")); return true; }
                Player player = (Player) sender;
                if (bitcoinMenu.getPlayersExchanging().contains(player)) {
                    bitcoinMenu.getPlayersExchanging().remove(player);
                    player.sendMessage(messages.getMessage("cancelled_exchange"));
                    player.playSound(player.getLocation(), sounds.getSound("cancelled_exchange"), 1, 1);
                } else if (bitcoinMenu.getPlayersTransferring().contains(player)) {
                    bitcoinMenu.getPlayersTransferring().remove(player);
                    player.sendMessage(messages.getMessage("cancelled_transfer"));
                    player.playSound(player.getLocation(), sounds.getSound("cancelled_transfer"), 1, 1);
                } else if (bitcoinMenu.getPlayersBuying().contains(player)) {
                    bitcoinMenu.getPlayersBuying().remove(player);
                    player.sendMessage(messages.getMessage("cancelled_transfer"));
                    player.playSound(player.getLocation(), sounds.getSound("cancelled_transfer"), 1, 1);
                } else {
                    player.sendMessage(messages.getMessage("nothing_to_cancel"));
                }
            }

            else if (args[0].equalsIgnoreCase("help")) {
                if (!sender.hasPermission("bitcoin.help")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                sender.sendMessage(messages.getMessage("help_command"));
            }

            else if (args[0].equalsIgnoreCase("value")) {
                if (!sender.hasPermission("bitcoin.value")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                sender.sendMessage(messages.getMessage("value_command").replace("{VALUE}", bitcoinManager.getExchangeCurrencySymbol() + bitcoinManager.getBitcoinValue()));
            }

            else if (args[0].equalsIgnoreCase("mine")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(messages.getMessage("cannot_use_from_console")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.mine")) { player.sendMessage(messages.getMessage("no_permission")); return true; }
                mining.openInterface(player);

            }

            else if (args[0].equalsIgnoreCase("stats")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(messages.getMessage("cannot_use_from_console")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.stats")) { player.sendMessage(messages.getMessage("no_permission")); return true; }
                if (args.length == 1) {
                    player.sendMessage(messages.getMessage("statistic_command_self").replace("{BALANCE}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(player.getUniqueId())))).replace("{AMOUNT_SOLVED}", String.valueOf(bitcoinManager.getPuzzlesSolved(player.getUniqueId()))).replace("{AMOUNT_MINED}", String.valueOf(bitcoinManager.getBitcoinsMined(player.getUniqueId()))));
                } else {
                    OfflinePlayer statPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (!bitcoinManager.getPlayerFileConfigs().containsKey(statPlayer.getUniqueId())) { sender.sendMessage(messages.getMessage("never_joined").replace("{PLAYER}", args[1])); return true; }
                    player.sendMessage(messages.getMessage("statistic_command_other").replace("{PLAYER}", statPlayer.getName()).replace("{BALANCE}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(statPlayer.getUniqueId())))).replace("{AMOUNT_SOLVED}", String.valueOf(bitcoinManager.getPuzzlesSolved(statPlayer.getUniqueId()))).replace("{AMOUNT_MINED}", String.valueOf(bitcoinManager.getBitcoinsMined(statPlayer.getUniqueId()))));
                }
            }

            else if (args[0].equalsIgnoreCase("sell")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(messages.getMessage("cannot_use_from_console")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.sell")) { player.sendMessage(messages.getMessage("no_permission")); return true; }
                if (!plugin.getEconomy().hasEconomy()) { player.sendMessage(messages.getMessage("no_economy")); }
                if (args.length < 2) { player.sendMessage(messages.getMessage("sell_command_invalid_arg")); return true; }
                try {
                    double exchangeAmount = Double.valueOf(args[1]);
                    if (exchangeAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(messages.getMessage("not_enough_bitcoins").replace("{BALANCE}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(player.getUniqueId()))))); return true; }
                    if (exchangeAmount <= 0) { player.sendMessage(messages.getMessage("invalid_number")); return true; }
                    bitcoinManager.withdraw(player.getUniqueId(), exchangeAmount);
                    bitcoinManager.addToBank(exchangeAmount);
                    player.sendMessage(messages.getMessage("complete_exchange").replace("{AMOUNT}", String.valueOf(exchangeAmount)).replace("{NEW_AMOUNT}", bitcoinManager.getExchangeCurrencySymbol() + util.round(2, bitcoinManager.getBitcoinValue() * exchangeAmount)));
                    plugin.getEconomy().depositPlayer(player, player.getWorld().getName(), bitcoinManager.getBitcoinValue() * exchangeAmount);
                } catch (NumberFormatException e) {
                    player.sendMessage(messages.getMessage("invalid_number"));
                }
            }

            else if (args[0].equalsIgnoreCase("transfer")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(messages.getMessage("cannot_use_from_console")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.transfer")) { player.sendMessage(messages.getMessage("no_permission")); return true; }
                if (args.length < 3) { player.sendMessage(messages.getMessage("transfer_command_invalid_arg")); return true; }
                Player recipient = Bukkit.getPlayer(args[1]);
                if (recipient == null) { player.sendMessage(messages.getMessage("not_online").replace("{PLAYER}", args[1])); return true; }
                if (recipient.equals(player)) { player.sendMessage(messages.getMessage("cannot_transfer_to_self")); return true; }
                try {
                    double transferAmount = Double.valueOf(args[2]);
                    if (transferAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(messages.getMessage("not_enough_bitcoins").replace("{BALANCE}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(player.getUniqueId()))))); return true; }
                    if (transferAmount <= 0) { player.sendMessage(messages.getMessage("invalid_number")); return true; }
                    bitcoinManager.withdraw(player.getUniqueId(), transferAmount);
                    bitcoinManager.deposit(recipient.getUniqueId(), transferAmount);
                    player.sendMessage(messages.getMessage("complete_transfer").replace("{AMOUNT}", String.valueOf(transferAmount)).replace("{RECIPIENT}", recipient.getName()));
                    recipient.sendMessage(messages.getMessage("receive_bitcoins").replace("{AMOUNT}", String.valueOf(transferAmount)).replace("{SENDER}", player.getName()));
                } catch (NumberFormatException e) {
                    player.sendMessage(messages.getMessage("invalid_number"));
                }
            }

            else if (args[0].equalsIgnoreCase("buy")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(messages.getMessage("cannot_use_from_console")); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.buy")) { player.sendMessage(messages.getMessage("no_permission")); return true; }
                if (!plugin.getEconomy().hasEconomy()) { player.sendMessage(messages.getMessage("no_economy")); }
                if (args.length < 2) { player.sendMessage(messages.getMessage("buy_command_invalid_arg")); return true; }
                try {
                    double buyAmount = Double.valueOf(args[1]);
                    if (buyAmount > bitcoinManager.getAmountInBank()) { player.sendMessage(messages.getMessage("not_enough_in_bank").replace("{AMOUNT}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getAmountInBank())))); return true; }
                    if (buyAmount <= 0) { player.sendMessage(messages.getMessage("invalid_number")); return true; }
                    double cost = (buyAmount * bitcoinManager.getBitcoinValue()) * (1 + bitcoinManager.getPurchaseTaxPercentage() / 100);
                    if (cost > plugin.getEconomy().getBalance(player)) { player.sendMessage(messages.getMessage("not_enough_money")); return true; }
                    bitcoinManager.deposit(player.getUniqueId(), buyAmount);
                    bitcoinManager.removeFromBank(buyAmount);
                    player.sendMessage(messages.getMessage("complete_purchase").replace("{AMOUNT}", String.valueOf(buyAmount)).replace("{COST}", bitcoinManager.getExchangeCurrencySymbol() + util.round(2, bitcoinManager.getBitcoinValue() * buyAmount)).replace("{TAX}", bitcoinManager.getExchangeCurrencySymbol() + util.round(2, bitcoinManager.getPurchaseTaxPercentage() / 100 * cost)));
                    plugin.getEconomy().withdrawPlayer(player, player.getWorld().getName(), cost);
                } catch (NumberFormatException e) {
                    player.sendMessage(messages.getMessage("invalid_number"));
                }
            }

            else if (args[0].equalsIgnoreCase("give")) {
                if (!sender.hasPermission("bitcoin.give")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                if (args.length < 3) { sender.sendMessage(messages.getMessage("give_command_invalid_arg")); return true; }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (!bitcoinManager.getPlayerFileConfigs().containsKey(recipient.getUniqueId())) { sender.sendMessage(messages.getMessage("never_joined").replace("{PLAYER}", args[1])); return true; }
                try {
                    double giveAmount = Double.valueOf(args[2]);
                    if (giveAmount <= 0) { sender.sendMessage(messages.getMessage("invalid_number")); return true; }
                    if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + giveAmount >= bitcoinManager.getCirculationLimit()) { sender.sendMessage(messages.getMessage("exceeds_limit").replace("{LIMIT}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getCirculationLimit())))); return true; }
                    bitcoinManager.deposit(recipient.getUniqueId(), giveAmount);
                    sender.sendMessage(messages.getMessage("give_command").replace("{AMOUNT}", String.valueOf(giveAmount)).replace("{PLAYER}", recipient.getName()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(messages.getMessage("invalid_number"));
                }
            }

            else if (args[0].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("bitcoin.remove")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                if (args.length < 3) { sender.sendMessage(messages.getMessage("remove_command_invalid_arg")); return true; }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (!bitcoinManager.getPlayerFileConfigs().containsKey(recipient.getUniqueId())) { sender.sendMessage(messages.getMessage("never_joined").replace("{PLAYER}", args[1])); return true; }
                try {
                    double removeAmount = Double.valueOf(args[2]);
                    if (removeAmount > bitcoinManager.getBalance(recipient.getUniqueId())) { sender.sendMessage(messages.getMessage("other_player_not_enough_bitcoins").replace("{PLAYER}", recipient.getName()).replace("{BALANCE}", String.valueOf(bitcoinManager.getBalance(recipient.getUniqueId())))); return true; }
                    if (removeAmount <= 0) { sender.sendMessage(messages.getMessage("invalid_number")); return true; }
                    bitcoinManager.withdraw(recipient.getUniqueId(), removeAmount);
                    sender.sendMessage(messages.getMessage("remove_command").replace("{AMOUNT}", String.valueOf(removeAmount)).replace("{PLAYER}", recipient.getName()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(messages.getMessage("invalid_number"));
                }
            }

            else if (args[0].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("bitcoin.set")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                if (args.length < 3) { sender.sendMessage(messages.getMessage("set_command_invalid_arg")); return true; }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (!bitcoinManager.getPlayerFileConfigs().containsKey(recipient.getUniqueId())) { sender.sendMessage(messages.getMessage("never_joined").replace("{PLAYER}", args[1])); return true; }
                try {
                    double newBalance = Double.valueOf(args[2]);
                    if (newBalance < 0) { sender.sendMessage(messages.getMessage("invalid_number")); return true; }
                    if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + (newBalance - bitcoinManager.getBalance(recipient.getUniqueId())) >= bitcoinManager.getCirculationLimit()) { sender.sendMessage(messages.getMessage("exceeds_limit").replace("{LIMIT}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getCirculationLimit())))); return true; }
                    bitcoinManager.setBalance(recipient.getUniqueId(), newBalance);
                    sender.sendMessage(messages.getMessage("set_command").replace("{AMOUNT}", String.valueOf(newBalance)).replace("{PLAYER}", recipient.getName()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(messages.getMessage("invalid_number"));
                }
            }

            else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("bitcoin.reload")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                plugin.reload();
                sender.sendMessage(messages.getMessage("reload_command"));
            }

            else if (args[0].equalsIgnoreCase("top")) {
                if (!sender.hasPermission("bitcoin.top")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                List<OfflinePlayer> topPlayers = bitcoinManager.getTopPlayers();
                StringBuilder top5 = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    if (i < topPlayers.size()) {
                        top5.append(messages.getMessage("top_command_format").replace("{PLACE}", String.valueOf(i + 1)).replace("{PLAYER}", topPlayers.get(i).getName()).replace("{BALANCE}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBalance(topPlayers.get(i).getUniqueId())))));
                    } else {
                        top5.append(messages.getMessage("top_command_format").replace("{PLACE}", String.valueOf(i + 1)).replace("{PLAYER}", "N/A").replace("{BALANCE}", "0.0"));
                    }
                    if (i != 4) { top5.append("\n"); }
                }
                sender.sendMessage(messages.getMessage("top_command_header"));
                sender.sendMessage(top5.toString());
            }

            else if (args[0].equalsIgnoreCase("bank")) {
                if (!sender.hasPermission("bitcoin.bank")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                sender.sendMessage(messages.getMessage("bank_command").replace("{AMOUNT}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getAmountInBank()))));
            }

            else if (args[0].equalsIgnoreCase("tax")) {
                if (!sender.hasPermission("bitcoin.tax")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                sender.sendMessage(messages.getMessage("tax_command").replace("{TAX}", bitcoinManager.getPurchaseTaxPercentage() + "%"));
            }

            else if (args[0].equalsIgnoreCase("circulation")) {
                if (!sender.hasPermission("bitcoin.circulation")) { sender.sendMessage(messages.getMessage("no_permission")); return true; }
                if (bitcoinManager.getCirculationLimit() > 0) {
                    sender.sendMessage(messages.getMessage("circulation_command").replace("{AMOUNT}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBitcoinsInCirculation()))).replace("{LIMIT}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getCirculationLimit()))));
                } else {
                    sender.sendMessage(messages.getMessage("circulation_command").replace("{AMOUNT}", String.valueOf(util.round(bitcoinManager.getDisplayRoundAmount(), bitcoinManager.getBitcoinsInCirculation()))).replace("{LIMIT}", "none"));
                }
            }

            else {
                sender.sendMessage(messages.getMessage("invalid_command"));
            }

            return true;
        }
        return false;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!messages.getMessage("command_name").equalsIgnoreCase("/bitcoin") && event.getMessage().split(" ")[0].equalsIgnoreCase(messages.getMessage("command_name"))) {
            event.getPlayer().performCommand(event.getMessage().replace(messages.getMessage("command_name"), "/bitcoin"));
            event.setCancelled(true);
        }
        if (event.getMessage().split(" ")[0].equalsIgnoreCase("/bitcoin") && !messages.getMessage("command_name").equalsIgnoreCase("/bitcoin")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Unknown command. Type \"/help\" for help.");
        }
    }
}