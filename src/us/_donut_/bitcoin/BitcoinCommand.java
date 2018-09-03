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
import us._donut_.bitcoin.configuration.Message;
import us._donut_.bitcoin.configuration.Sounds;

import java.util.List;

import static us._donut_.bitcoin.util.Util.*;

class BitcoinCommand implements CommandExecutor, Listener {

    private Bitcoin plugin = Bitcoin.plugin;
    private BitcoinManager bitcoinManager;
    private BitcoinMenu bitcoinMenu;
    private BlackMarket blackMarket;
    private Mining mining;
    private Sounds sounds;

    BitcoinCommand() {
        bitcoinManager = plugin.getBitcoinManager();
        bitcoinMenu = plugin.getBitcoinMenu();
        blackMarket = plugin.getBlackMarket();
        mining = plugin.getMining();
        sounds = plugin.getSounds();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bitcoin")) {

            if (args.length == 0) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.main")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                plugin.getBitcoinMenu().open(player);
            }

            else if (args[0].equalsIgnoreCase("cancel")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (bitcoinMenu.getPlayersExchanging().contains(player)) {
                    bitcoinMenu.getPlayersExchanging().remove(player);
                    player.sendMessage(Message.CANCELLED_EXCHANGE.toString());
                    player.playSound(player.getLocation(), sounds.getSound("cancelled_exchange"), 1, 1);
                } else if (bitcoinMenu.getPlayersTransferring().contains(player)) {
                    bitcoinMenu.getPlayersTransferring().remove(player);
                    player.sendMessage(Message.CANCELLED_TRANSFER.toString());
                    player.playSound(player.getLocation(), sounds.getSound("cancelled_transfer"), 1, 1);
                } else if (bitcoinMenu.getPlayersBuying().contains(player)) {
                    bitcoinMenu.getPlayersBuying().remove(player);
                    player.sendMessage(Message.CANCELLED_PURCHASE.toString());
                    player.playSound(player.getLocation(), sounds.getSound("cancelled_purchase"), 1, 1);
                } else {
                    player.sendMessage(Message.NOTHING_TO_CANCEL.toString());
                }
            }

            else if (args[0].equalsIgnoreCase("help")) {
                if (!sender.hasPermission("bitcoin.help")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                sender.sendMessage(Message.HELP_COMMAND.toString());
            }

            else if (args[0].equalsIgnoreCase("value")) {
                if (!sender.hasPermission("bitcoin.value")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                sender.sendMessage(Message.VALUE_COMMAND.toString().replace("{VALUE}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue())));
            }

            else if (args[0].equalsIgnoreCase("mine")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.mine")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                mining.openInterface(player);

            }

            else if (args[0].equalsIgnoreCase("stats")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.stats")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (args.length == 1) {
                    player.sendMessage(Message.STATISTIC_COMMAND_SELF.toString()
                            .replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId())))
                            .replace("{AMOUNT_SOLVED}", formatNumber((bitcoinManager.getPuzzlesSolved(player.getUniqueId()))))
                            .replace("{AMOUNT_MINED}", formatRoundNumber(bitcoinManager.getBitcoinsMined(player.getUniqueId())))
                            .replace("{MIN}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) / 60.0).split("\\.")[0])
                            .replace("{SEC}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) % 60)));
                } else {
                    OfflinePlayer statPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (!bitcoinManager.getPlayerFileConfigs().containsKey(statPlayer.getUniqueId())) { sender.sendMessage(Message.NEVER_JOINED.toString().replace("{PLAYER}", args[1])); return true; }
                    player.sendMessage(Message.STATISTIC_COMMAND_OTHER.toString()
                            .replace("{PLAYER}", bitcoinManager.getOfflinePlayerName(statPlayer))
                            .replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(statPlayer.getUniqueId())))
                            .replace("{AMOUNT_SOLVED}", formatNumber((bitcoinManager.getPuzzlesSolved(statPlayer.getUniqueId()))))
                            .replace("{AMOUNT_MINED}", formatRoundNumber(bitcoinManager.getBitcoinsMined(statPlayer.getUniqueId())))
                            .replace("{MIN}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) / 60.0).split("\\.")[0])
                            .replace("{SEC}", String.valueOf(bitcoinManager.getBestPuzzleTime(player.getUniqueId()) % 60)));
                }
            }

            else if (args[0].equalsIgnoreCase("sell")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.sell")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (!plugin.getEconomy().hasEconomy()) { player.sendMessage(Message.NO_ECONOMY.toString()); }
                if (args.length < 2) { player.sendMessage(Message.SELL_COMMAND_INVALID_ARG.toString()); return true; }
                try {
                    double exchangeAmount = Double.valueOf(args[1]);
                    if (exchangeAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(Message.NOT_ENOUGH_BITCOINS.toString().replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId())))); return true; }
                    if (exchangeAmount <= 0) { player.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                    bitcoinManager.withdraw(player.getUniqueId(), exchangeAmount);
                    bitcoinManager.addToBank(exchangeAmount);
                    player.sendMessage(Message.COMPLETE_EXCHANGE.toString()
                            .replace("{AMOUNT}", formatNumber((exchangeAmount)))
                            .replace("{NEW_AMOUNT}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue() * exchangeAmount)));
                    plugin.getEconomy().depositPlayer(player, player.getWorld().getName(), bitcoinManager.getBitcoinValue() * exchangeAmount);
                } catch (NumberFormatException e) {
                    player.sendMessage(Message.INVALID_NUMBER.toString());
                }
            }

            else if (args[0].equalsIgnoreCase("transfer")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.transfer")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (args.length < 3) { player.sendMessage(Message.TRANSFER_COMMAND_INVALID_ARG.toString()); return true; }
                Player recipient = Bukkit.getPlayer(args[1]);
                if (recipient == null) { player.sendMessage(Message.NOT_ONLINE.toString().replace("{PLAYER}", args[1])); return true; }
                if (recipient.equals(player)) { player.sendMessage(Message.CANNOT_TRANSFER_TO_SELF.toString()); return true; }
                try {
                    double transferAmount = Double.valueOf(args[2]);
                    if (transferAmount > bitcoinManager.getBalance(player.getUniqueId())) { player.sendMessage(Message.NOT_ENOUGH_BITCOINS.toString().replace("{BALANCE}", formatRoundNumber(bitcoinManager.getBalance(player.getUniqueId())))); return true; }
                    if (transferAmount <= 0) { player.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                    bitcoinManager.withdraw(player.getUniqueId(), transferAmount);
                    bitcoinManager.deposit(recipient.getUniqueId(), transferAmount);
                    player.sendMessage(Message.COMPLETE_TRANSFER.toString()
                            .replace("{AMOUNT}", formatNumber((transferAmount)))
                            .replace("{RECIPIENT}", recipient.getDisplayName()));
                    recipient.sendMessage(Message.RECEIVE_BITCOINS.toString()
                            .replace("{AMOUNT}", formatNumber((transferAmount)))
                            .replace("{SENDER}", player.getDisplayName()));
                } catch (NumberFormatException e) {
                    player.sendMessage(Message.INVALID_NUMBER.toString());
                }
            }

            else if (args[0].equalsIgnoreCase("buy")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (!player.hasPermission("bitcoin.buy")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (!plugin.getEconomy().hasEconomy()) { player.sendMessage(Message.NO_ECONOMY.toString()); }
                if (args.length < 2) { player.sendMessage(Message.BUY_COMMAND_INVALID_ARG.toString()); return true; }
                try {
                    double buyAmount = Double.valueOf(args[1]);
                    if (buyAmount > bitcoinManager.getAmountInBank()) { player.sendMessage(Message.NOT_ENOUGH_IN_BANK.toString().replace("{AMOUNT}", formatRoundNumber(bitcoinManager.getAmountInBank()))); return true; }
                    if (buyAmount <= 0) { player.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                    double cost = (buyAmount * bitcoinManager.getBitcoinValue()) * (1 + bitcoinManager.getPurchaseTaxPercentage() / 100);
                    if (cost > plugin.getEconomy().getBalance(player)) { player.sendMessage(Message.NOT_ENOUGH_MONEY.toString()); return true; }
                    bitcoinManager.deposit(player.getUniqueId(), buyAmount);
                    bitcoinManager.removeFromBank(buyAmount);
                    player.sendMessage(Message.COMPLETE_PURCHASE.toString()
                            .replace("{AMOUNT}", formatNumber((buyAmount)))
                            .replace("{COST}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getBitcoinValue() * buyAmount))
                            .replace("{TAX}", bitcoinManager.getExchangeCurrencySymbol() + formatRound2Number(bitcoinManager.getPurchaseTaxPercentage() / 100 * cost)));
                    plugin.getEconomy().withdrawPlayer(player, player.getWorld().getName(), cost);
                } catch (NumberFormatException e) {
                    player.sendMessage(Message.INVALID_NUMBER.toString());
                }
            }

            else if (args[0].equalsIgnoreCase("give")) {
                if (!sender.hasPermission("bitcoin.give")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (args.length < 3) { sender.sendMessage(Message.GIVE_COMMAND_INVALID_ARG.toString()); return true; }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (!bitcoinManager.getPlayerFileConfigs().containsKey(recipient.getUniqueId())) { sender.sendMessage(Message.NEVER_JOINED.toString().replace("{PLAYER}", args[1])); return true; }
                try {
                    double giveAmount = Double.valueOf(args[2]);
                    if (giveAmount <= 0) { sender.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                    if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + giveAmount >= bitcoinManager.getCirculationLimit()) { sender.sendMessage(Message.EXCEEDS_LIMIT.toString().replace("{LIMIT}", formatRoundNumber(bitcoinManager.getCirculationLimit()))); return true; }
                    bitcoinManager.deposit(recipient.getUniqueId(), giveAmount);
                    sender.sendMessage(Message.GIVE_COMMAND.toString().replace("{AMOUNT}", formatNumber((giveAmount))).replace("{PLAYER}", bitcoinManager.getOfflinePlayerName(recipient)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Message.INVALID_NUMBER.toString());
                }
            }

            else if (args[0].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("bitcoin.remove")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (args.length < 3) { sender.sendMessage(Message.REMOVE_COMMAND_INVALID_ARG.toString()); return true; }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (!bitcoinManager.getPlayerFileConfigs().containsKey(recipient.getUniqueId())) { sender.sendMessage(Message.NEVER_JOINED.toString().replace("{PLAYER}", args[1])); return true; }
                try {
                    double removeAmount = Double.valueOf(args[2]);
                    if (removeAmount > bitcoinManager.getBalance(recipient.getUniqueId())) { sender.sendMessage(Message.OTHER_PLAYER_NOT_ENOUGH_BITCOINS.toString().replace("{PLAYER}", bitcoinManager.getOfflinePlayerName(recipient)).replace("{BALANCE}", formatNumber((bitcoinManager.getBalance(recipient.getUniqueId()))))); return true; }
                    if (removeAmount <= 0) { sender.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                    bitcoinManager.withdraw(recipient.getUniqueId(), removeAmount);
                    sender.sendMessage(Message.REMOVE_COMMAND.toString().replace("{AMOUNT}", formatNumber((removeAmount))).replace("{PLAYER}", bitcoinManager.getOfflinePlayerName(recipient)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Message.INVALID_NUMBER.toString());
                }
            }

            else if (args[0].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("bitcoin.set")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (args.length < 3) { sender.sendMessage(Message.SET_COMMAND_INVALID_ARG.toString()); return true; }
                OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
                if (!bitcoinManager.getPlayerFileConfigs().containsKey(recipient.getUniqueId())) { sender.sendMessage(Message.NEVER_JOINED.toString().replace("{PLAYER}", args[1])); return true; }
                try {
                    double newBalance = Double.valueOf(args[2]);
                    if (newBalance < 0) { sender.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                    if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + (newBalance - bitcoinManager.getBalance(recipient.getUniqueId())) >= bitcoinManager.getCirculationLimit()) { sender.sendMessage(Message.EXCEEDS_LIMIT.toString().replace("{LIMIT}", formatRoundNumber(bitcoinManager.getCirculationLimit()))); return true; }
                    bitcoinManager.setBalance(recipient.getUniqueId(), newBalance);
                    sender.sendMessage(Message.SET_COMMAND.toString().replace("{AMOUNT}", formatNumber((newBalance))).replace("{PLAYER}", bitcoinManager.getOfflinePlayerName(recipient)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Message.INVALID_NUMBER.toString());
                }
            }

            else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("bitcoin.reload")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                plugin.reload();
                sender.sendMessage(Message.RELOAD_COMMAND.toString());
            }

            else if (args[0].equalsIgnoreCase("top")) {
                if (!sender.hasPermission("bitcoin.top")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }

                List<OfflinePlayer> topBalPlayers = bitcoinManager.getTopBalPlayers();
                StringBuilder top5Balances = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    top5Balances.append(Message.TOP_BAL_COMMAND_FORMAT.toString()
                            .replace("{PLACE}", String.valueOf(i + 1))
                            .replace("{PLAYER}", i < topBalPlayers.size() ? bitcoinManager.getOfflinePlayerName(topBalPlayers.get(i)) : "N/A")
                            .replace("{BALANCE}", i < topBalPlayers.size() ? formatRoundNumber(bitcoinManager.getBalance(topBalPlayers.get(i).getUniqueId())) : "0"));
                    if (i != 4) { top5Balances.append("\n"); }
                }

                List<OfflinePlayer> topTimePlayers = bitcoinManager.getTopTimePlayers();
                StringBuilder top5Times = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    top5Times.append(Message.TOP_TIME_COMMAND_FORMAT.toString()
                            .replace("{PLACE}", String.valueOf(i + 1))
                            .replace("{PLAYER}", i < topTimePlayers.size() ? bitcoinManager.getOfflinePlayerName(topTimePlayers.get(i)) : "N/A")
                            .replace("{MIN}", i < topTimePlayers.size() ? String.valueOf(bitcoinManager.getBestPuzzleTime(topTimePlayers.get(i).getUniqueId()) / 60.0).split("\\.")[0] : "0")
                            .replace("{SEC}", i < topTimePlayers.size() ? String.valueOf(bitcoinManager.getBestPuzzleTime(topTimePlayers.get(i).getUniqueId()) % 60) : "0"));
                    if (i != 4) { top5Times.append("\n"); }
                }

                List<OfflinePlayer> topSolvedPlayers = bitcoinManager.getTopSolvedPlayers();
                StringBuilder top5Solved = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    top5Solved.append(Message.TOP_SOLVED_COMMAND_FORMAT.toString()
                            .replace("{PLACE}", String.valueOf(i + 1))
                            .replace("{PLAYER}", i < topSolvedPlayers.size() ? bitcoinManager.getOfflinePlayerName(topSolvedPlayers.get(i)) : "N/A")
                            .replace("{AMOUNT}", i < topSolvedPlayers.size() ? formatNumber(bitcoinManager.getPuzzlesSolved(topSolvedPlayers.get(i).getUniqueId())) : "0"));
                    if (i != 4) { top5Solved.append("\n"); }
                }

                if (args.length == 1 || (!args[1].equalsIgnoreCase("bal") && !args[1].equalsIgnoreCase("time") && !args[1].equalsIgnoreCase("solved"))) {
                    sender.sendMessage(Message.TOP_BAL_COMMAND_HEADER.toString());
                    sender.sendMessage(top5Balances.toString());
                    sender.sendMessage("");
                    sender.sendMessage(Message.TOP_TIME_COMMAND_HEADER.toString());
                    sender.sendMessage(top5Times.toString());
                    sender.sendMessage("");
                    sender.sendMessage(Message.TOP_SOLVED_COMMAND_HEADER.toString());
                    sender.sendMessage(top5Solved.toString());
                } else {
                    if (args[1].equalsIgnoreCase("bal")) {
                        sender.sendMessage(Message.TOP_BAL_COMMAND_HEADER.toString());
                        sender.sendMessage(top5Balances.toString());
                    } else if (args[1].equalsIgnoreCase("time")) {
                        sender.sendMessage(Message.TOP_TIME_COMMAND_HEADER.toString());
                        sender.sendMessage(top5Times.toString());
                    } else if (args[1].equalsIgnoreCase("solved")) {
                        sender.sendMessage(Message.TOP_SOLVED_COMMAND_HEADER.toString());
                        sender.sendMessage(top5Solved.toString());
                    }
                }
            }

            else if (args[0].equalsIgnoreCase("bank")) {
                if (!sender.hasPermission("bitcoin.bank")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                sender.sendMessage(Message.BANK_COMMAND.toString().replace("{AMOUNT}", formatRoundNumber(bitcoinManager.getAmountInBank())));
            }

            else if (args[0].equalsIgnoreCase("tax")) {
                if (!sender.hasPermission("bitcoin.tax")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                sender.sendMessage(Message.TAX_COMMAND.toString().replace("{TAX}", bitcoinManager.getPurchaseTaxPercentage() + "%"));
            }

            else if (args[0].equalsIgnoreCase("circulation")) {
                if (!sender.hasPermission("bitcoin.circulation")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                sender.sendMessage(Message.CIRCULATION_COMMAND.toString()
                        .replace("{AMOUNT}", formatRoundNumber(bitcoinManager.getBitcoinsInCirculation()))
                        .replace("{LIMIT}", bitcoinManager.getCirculationLimit() > 0 ? formatRoundNumber(bitcoinManager.getCirculationLimit()) : "none"));
            }

            else if (args[0].equalsIgnoreCase("blackmarket")) {
                if (sender instanceof ConsoleCommandSender) { sender.sendMessage(Message.CANNOT_USE_FROM_CONSOLE.toString()); return true; }
                Player player = (Player) sender;
                if (args.length == 1) {
                    if (!player.hasPermission("bitcoin.blackmarket")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                    blackMarket.open(player);
                } else {
                    if (args.length < 4) { player.sendMessage(Message.BLACK_MARKET_COMMAND_INVALID_ARG.toString()); return true; }
                    if (args[1].equalsIgnoreCase("setslot")) {
                        if (!player.hasPermission("bitcoin.blackmarket.edit")) { player.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                        try {
                            int slot = Integer.valueOf(args[2]) - 1;
                            double price = Double.valueOf(args[3]);
                            if (slot < 0 || slot > 53 || price < 0) { player.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                            Integer stock = null;
                            if (args.length > 4) {
                                try {
                                    stock = Integer.parseInt(args[4]);
                                    if (stock < 1) { player.sendMessage(Message.INVALID_NUMBER.toString()); return true; }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(Message.INVALID_NUMBER.toString());
                                }
                            }
                            if (!Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.7")) {
                                blackMarket.editItem(slot, player.getInventory().getItemInMainHand(), price, stock);
                            } else {
                                blackMarket.editItem(slot, player.getInventory().getItemInHand(), price, stock);
                            }
                            player.sendMessage(Message.BLACK_MARKET_SET_ITEM.toString());
                        } catch (NumberFormatException e) {
                            player.sendMessage(Message.INVALID_NUMBER.toString());
                        }
                    } else {
                        player.sendMessage(Message.BLACK_MARKET_COMMAND_INVALID_ARG.toString());
                    }
                }
            }

            else if (args[0].equalsIgnoreCase("reset")) {
                if (!sender.hasPermission("bitcoin.reset")) { sender.sendMessage(Message.NO_PERMISSION.toString()); return true; }
                if (args.length < 2) { sender.sendMessage(Message.RESET_COMMAND_INVALID_ARG.toString()); return true; }
                if (!args[1].equalsIgnoreCase("bal") && !args[1].equalsIgnoreCase("mined") && !args[1].equalsIgnoreCase("time") && !args[1].equalsIgnoreCase("solved")) { sender.sendMessage(Message.RESET_COMMAND_INVALID_ARG.toString()); return true; }

                if (args[1].equalsIgnoreCase("bal")) {
                    bitcoinManager.resetBalances();
                    sender.sendMessage(Message.RESET_COMMAND_BAL.toString());
                } else if (args[1].equalsIgnoreCase("mined")) {
                    bitcoinManager.resetMined();
                    sender.sendMessage(Message.RESET_COMMAND_MINED.toString());
                } else if (args[1].equalsIgnoreCase("time")) {
                    bitcoinManager.resetTimes();
                    sender.sendMessage(Message.RESET_COMMAND_TIME.toString());
                } else if (args[1].equalsIgnoreCase("solved")) {
                    bitcoinManager.resetSolved();
                    sender.sendMessage(Message.RESET_COMMAND_SOLVED.toString());
                }
            }

            else {
                sender.sendMessage(Message.INVALID_COMMAND.toString());
            }

            return true;
        }
        return false;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String commandName = event.getMessage().split(" ")[0];
        String customCommandName = Message.COMMAND_NAME.toString();
        if (!customCommandName.equalsIgnoreCase("/bitcoin")) {
            if (commandName.equalsIgnoreCase(customCommandName)) {
                event.setMessage(event.getMessage().replace(customCommandName, "/bitcoin"));
            } else if (commandName.equalsIgnoreCase("/bitcoin") || commandName.equalsIgnoreCase("/btc")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Unknown command. Type \"/help\" for help.");
            }
        }
    }
}