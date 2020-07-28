package us.donut.bitcoin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.donut.bitcoin.config.Messages;
import us.donut.bitcoin.hooks.ServerEconomy;
import us.donut.bitcoin.mining.ComputerManager;
import us.donut.bitcoin.mining.MiningManager;

import java.util.*;

public class BitcoinCommand implements CommandExecutor, Listener {

    private static BitcoinCommand instance;
    private Bitcoin plugin;
    private BitcoinManager bitcoinManager;
    private PlayerDataManager playerDataManager;
    private ComputerManager computerManager;
    private List<String> commands = Arrays.asList("help", "value", "give", "remove", "set", "reload", "top", "bank", "tax", "circulation", "computer", "reset");
    private List<String> playerCommands = Arrays.asList("main", "cancel", "mine", "stats", "sell", "buy", "transfer", "blackmarket");
    private Map<UUID, Long> buyDelays = new HashMap<>();

    private BitcoinCommand() {
        plugin = Bitcoin.getInstance();
        bitcoinManager = BitcoinManager.getInstance();
        playerDataManager = PlayerDataManager.getInstance();
        computerManager = ComputerManager.getInstance();
    }

    public static BitcoinCommand getInstance() {
        return instance != null ? instance : (instance = new BitcoinCommand());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String commandName = event.getMessage().split(" ")[0];
        String customCommandName = Messages.COMMAND_NAME.toString();
        if (!customCommandName.equalsIgnoreCase("/bitcoin")) {
            if (commandName.equalsIgnoreCase(customCommandName)) {
                event.setMessage(event.getMessage().replace(customCommandName, "/bitcoin"));
            } else if (commandName.equalsIgnoreCase("/bitcoin") || commandName.equalsIgnoreCase("/btc")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Unknown command. Type \"/help\" for help.");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subCommand = (args.length == 0 ? "main" : args[0]).toLowerCase();
        if (!commands.contains(subCommand) && !playerCommands.contains(subCommand)) {
            sender.sendMessage(Messages.INVALID_COMMAND.toString());
        } else if (!sender.hasPermission("bitcoin." + subCommand)) {
            sender.sendMessage(Messages.NO_PERMISSION.toString());
        } else if (commands.contains(subCommand)) {
            if (subCommand.equals("reload")) {
                plugin.reload();
                sender.sendMessage(Messages.RELOAD_COMMAND.toString());
            } else if (subCommand.equals("help")) {
                sender.sendMessage(Messages.HELP_COMMAND.toString());
            } else if (subCommand.equals("value")) {
                sender.sendMessage(Messages.get("value_command", bitcoinManager.getFormattedValue()));
            } else if (subCommand.equals("give")) {
                giveCommand(sender, args);
            } else if (subCommand.equals("remove")) {
                removeCommand(sender, args);
            } else if (subCommand.equals("set")) {
                setCommand(sender, args);
            } else if (subCommand.equals("top")) {
                topCommand(sender, args);
            } else if (subCommand.equals("bank")) {
                sender.sendMessage(Messages.get("bank_command", bitcoinManager.format(bitcoinManager.getAmountInBank())));
            } else if (subCommand.equals("tax")) {
                sender.sendMessage(Messages.get("tax_command", bitcoinManager.getPurchaseTaxPercentage() + "%"));
            } else if (subCommand.equals("circulation")) {
                sender.sendMessage(Messages.get("circulation_command", bitcoinManager.format(bitcoinManager.getBitcoinsInCirculation()), bitcoinManager.getCirculationLimit() > 0 ? bitcoinManager.format(bitcoinManager.getCirculationLimit()) : "none"));
            } else if (subCommand.equals("computer")) {
                computerCommand(sender, args);
            } else if (subCommand.equals("reset")) {
                resetCommand(sender, args);
            }
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.CANNOT_USE_FROM_CONSOLE.toString());
        } else {
            Player player = (Player) sender;
            if (subCommand.equals("main")) {
                BitcoinMenu.getInstance().open(player);
            } else if (subCommand.equals("cancel")) {
                BitcoinMenu.getInstance().cancelAction(player);
            } else if (subCommand.equals("mine")) {
                mineCommand(player);
            } else if (subCommand.equals("blackmarket")) {
                blackMarketCommand(player, args);
            } else if (subCommand.equals("stats")) {
                statsCommand(player, args);
            } else if (subCommand.equals("sell")) {
                sellCommand(player, args);
            } else if (subCommand.equals("buy")) {
                buyCommand(player, args);
            } else if (subCommand.equals("transfer")) {
                transferCommand(player, args);
            }
        }

        return true;
    }

    private void giveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Messages.GIVE_COMMAND_INVALID_ARG.toString());
            return;
        }
        OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
        UUID recipientUUID = recipient.getUniqueId();
        if (!playerDataManager.getBalances().containsKey(recipientUUID)) {
            sender.sendMessage(Messages.get("never_joined", args[1]));
            return;
        }
        try {
            double giveAmount = Double.parseDouble(args[2]);
            if (giveAmount <= 0) {
                sender.sendMessage(Messages.INVALID_NUMBER.toString());
            } else if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + giveAmount >= bitcoinManager.getCirculationLimit()) {
                sender.sendMessage(Messages.get("exceeds_limit", bitcoinManager.format(bitcoinManager.getCirculationLimit())));
            } else {
                playerDataManager.deposit(recipientUUID, giveAmount);
                sender.sendMessage(Messages.get("give_command", Util.formatNumber(giveAmount), playerDataManager.getDisplayName(recipientUUID)));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void removeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Messages.REMOVE_COMMAND_INVALID_ARG.toString());
            return;
        }
        OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
        UUID recipientUUID = recipient.getUniqueId();
        if (!playerDataManager.getBalances().containsKey(recipientUUID)) {
            sender.sendMessage(Messages.get("never_joined", args[1]));
            return;
        }
        try {
            double removeAmount = Double.parseDouble(args[2]);
            if (removeAmount > playerDataManager.getBalance(recipientUUID)) {
                sender.sendMessage(Messages.get("other_player_not_enough_bitcoins", playerDataManager.getDisplayName(recipientUUID), bitcoinManager.format(playerDataManager.getBalance(recipientUUID))));
            } else if (removeAmount <= 0) {
                sender.sendMessage(Messages.INVALID_NUMBER.toString());
            } else {
                playerDataManager.withdraw(recipientUUID, removeAmount);
                sender.sendMessage(Messages.get("remove_command", Util.formatNumber(removeAmount), playerDataManager.getDisplayName(recipientUUID)));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void setCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Messages.SET_COMMAND_INVALID_ARG.toString());
            return;
        }
        OfflinePlayer recipient = Bukkit.getOfflinePlayer(args[1]);
        UUID recipientUUID = recipient.getUniqueId();
        if (!playerDataManager.getBalances().containsKey(recipientUUID)) {
            sender.sendMessage(Messages.get("never_joined", args[1]));
            return;
        }
        try {
            double newBalance = Double.parseDouble(args[2]);
            if (newBalance < 0) {
                sender.sendMessage(Messages.INVALID_NUMBER.toString());
            } else if (bitcoinManager.getCirculationLimit() > 0 && bitcoinManager.getBitcoinsInCirculation() + (newBalance - playerDataManager.getBalance(recipientUUID)) >= bitcoinManager.getCirculationLimit()) {
                sender.sendMessage(Messages.get("exceeds_limit", bitcoinManager.format(bitcoinManager.getCirculationLimit())));
            } else {
                playerDataManager.setBalance(recipientUUID, newBalance);
                sender.sendMessage(Messages.get("set_command", Util.formatNumber(newBalance), playerDataManager.getDisplayName(recipientUUID)));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void computerCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Messages.SET_COMMAND_INVALID_ARG.toString());
            return;
        }
        if (!computerManager.isEnabled()) {
            sender.sendMessage(Messages.COMPUTER_DISABLED.toString());
            return;
        }
        Player recipient = Bukkit.getPlayer(args[1]);
        if (recipient == null) {
            sender.sendMessage(Messages.NOT_ONLINE.toString().replace("{PLAYER}", args[1]));
            return;
        }
        try {
            if (args.length > 2) {
                int num = Integer.parseInt(args[2]);
                for (int i = 0; i < num; i++) {
                    recipient.getInventory().addItem(computerManager.getComputerItem());
                }
            } else {
                recipient.getInventory().addItem(computerManager.getComputerItem());
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void resetCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Messages.RESET_COMMAND_INVALID_ARG.toString());
        } else if (args[1].equalsIgnoreCase("bal")) {
            playerDataManager.resetBalances();
            sender.sendMessage(Messages.RESET_COMMAND_BAL.toString());
        } else if (args[1].equalsIgnoreCase("mined")) {
            playerDataManager.resetMined();
            sender.sendMessage(Messages.RESET_COMMAND_MINED.toString());
        } else if (args[1].equalsIgnoreCase("time")) {
            playerDataManager.resetTimes();
            sender.sendMessage(Messages.RESET_COMMAND_TIME.toString());
        } else if (args[1].equalsIgnoreCase("solved")) {
            playerDataManager.resetSolved();
            sender.sendMessage(Messages.RESET_COMMAND_SOLVED.toString());
        } else {
            sender.sendMessage(Messages.RESET_COMMAND_INVALID_ARG.toString());
        }
    }

    private void topCommand(CommandSender sender, String[] args) {
        List<UUID> topBalPlayers = playerDataManager.getTopBalPlayers();
        StringBuilder top5Balances = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < topBalPlayers.size()) {
            top5Balances.append(Messages.get("top_bal_command_format", i + 1,
                    playerDataManager.getDisplayName(topBalPlayers.get(i)),
                    bitcoinManager.format(playerDataManager.getBalance(topBalPlayers.get(i)))));
            } else {
                top5Balances.append(Messages.get("top_bal_command_format", i + 1, "N/A", 0));
            }
            if (i != 4) {
                top5Balances.append("\n");
            }
        }

        List<UUID> topTimePlayers = playerDataManager.getTopTimePlayers();
        StringBuilder top5Times = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < topTimePlayers.size()) {
                top5Times.append(Messages.get("top_time_command_format", i + 1,
                        playerDataManager.getDisplayName(topTimePlayers.get(i)),
                        String.valueOf(playerDataManager.getBestPuzzleTime(topTimePlayers.get(i)) / 60.0).split("\\.")[0],
                        playerDataManager.getBestPuzzleTime(topTimePlayers.get(i)) % 60));
            } else {
                top5Times.append(Messages.get("top_time_command_format", i + 1, "N/A", 0, 0));
            }
            if (i != 4) {
                top5Times.append("\n");
            }
        }

        List<UUID> topSolvedPlayers = playerDataManager.getTopSolvedPlayers();
        StringBuilder top5Solved = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < topSolvedPlayers.size()) {
                top5Solved.append(Messages.get("top_solved_command_format", i + 1,
                        playerDataManager.getDisplayName(topSolvedPlayers.get(i)),
                        Util.formatNumber(playerDataManager.getPuzzlesSolved(topSolvedPlayers.get(i)))));
            } else {
                top5Solved.append(Messages.get("top_solved_command_format", i + 1, "N/A", 0));
            }
            if (i != 4) {
                top5Solved.append("\n");
            }
        }

        if (args.length == 1 || (!args[1].equalsIgnoreCase("bal") && !args[1].equalsIgnoreCase("time") && !args[1].equalsIgnoreCase("solved"))) {
            sender.sendMessage(Messages.TOP_BAL_COMMAND_HEADER.toString());
            sender.sendMessage(top5Balances.toString());
            sender.sendMessage("");
            sender.sendMessage(Messages.TOP_TIME_COMMAND_HEADER.toString());
            sender.sendMessage(top5Times.toString());
            sender.sendMessage("");
            sender.sendMessage(Messages.TOP_SOLVED_COMMAND_HEADER.toString());
            sender.sendMessage(top5Solved.toString());
        } else if (args[1].equalsIgnoreCase("bal")) {
            sender.sendMessage(Messages.TOP_BAL_COMMAND_HEADER.toString());
            sender.sendMessage(top5Balances.toString());
        } else if (args[1].equalsIgnoreCase("time")) {
            sender.sendMessage(Messages.TOP_TIME_COMMAND_HEADER.toString());
            sender.sendMessage(top5Times.toString());
        } else if (args[1].equalsIgnoreCase("solved")) {
            sender.sendMessage(Messages.TOP_SOLVED_COMMAND_HEADER.toString());
            sender.sendMessage(top5Solved.toString());
        }
    }

    private void statsCommand(Player player, String[] args) {
        UUID uuid = player.getUniqueId();
        if (args.length == 1) {
            player.sendMessage(Messages.get("statistic_command_self",
                    bitcoinManager.format(playerDataManager.getBalance(uuid)),
                    Util.formatNumber((playerDataManager.getPuzzlesSolved(uuid))),
                    bitcoinManager.format(playerDataManager.getBitcoinsMined(uuid)),
                    String.valueOf(playerDataManager.getBestPuzzleTime(uuid) / 60.0).split("\\.")[0],
                    String.valueOf(playerDataManager.getBestPuzzleTime(uuid) % 60)));
        } else {
            OfflinePlayer statPlayer = Bukkit.getOfflinePlayer(args[1]);
            UUID statPlayerUUID = statPlayer.getUniqueId();
            if (!playerDataManager.getBalances().containsKey(statPlayerUUID)) {
                player.sendMessage(Messages.get("never_joined", args[1]));
                return;
            }
            player.sendMessage(Messages.get("statistic_command_other",
                    playerDataManager.getDisplayName(statPlayerUUID),
                    bitcoinManager.format(playerDataManager.getBalance(statPlayerUUID)),
                    Util.formatNumber((playerDataManager.getPuzzlesSolved(statPlayerUUID))),
                    bitcoinManager.format(playerDataManager.getBitcoinsMined(statPlayerUUID)),
                    String.valueOf(playerDataManager.getBestPuzzleTime(statPlayerUUID) / 60.0).split("\\.")[0],
                    String.valueOf(playerDataManager.getBestPuzzleTime(statPlayerUUID) % 60)));
        }
    }

    private void sellCommand(Player player, String[] args) {
        if (!ServerEconomy.isPresent()) {
            player.sendMessage(Messages.NO_ECONOMY.toString());
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Messages.SELL_COMMAND_INVALID_ARG.toString());
            return;
        }
        try {
            double exchangeAmount = Double.parseDouble(args[1]);
            if (exchangeAmount > playerDataManager.getBalance(player.getUniqueId())) {
                player.sendMessage(Messages.get("not_enough_bitcoins", bitcoinManager.format(playerDataManager.getBalance(player.getUniqueId()))));
            } else if (exchangeAmount <= 0) {
                player.sendMessage(Messages.INVALID_NUMBER.toString());
            } else {
                playerDataManager.withdraw(player.getUniqueId(), exchangeAmount);
                bitcoinManager.addToBank(exchangeAmount);
                ServerEconomy.deposit(player, player.getWorld().getName(), bitcoinManager.getBitcoinValue() * exchangeAmount);
                player.sendMessage(Messages.get("complete_exchange", Util.formatNumber(exchangeAmount), Util.formatRoundNumber(bitcoinManager.getBitcoinValue() * exchangeAmount)));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void buyCommand(Player player, String[] args) {
        if (!ServerEconomy.isPresent()) {
            player.sendMessage(Messages.NO_ECONOMY.toString());
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Messages.BUY_COMMAND_INVALID_ARG.toString());
            return;
        }
        if (buyDelays.containsKey(player.getUniqueId())) {
            player.sendMessage(Messages.get("buy_delay", buyDelays.get(player.getUniqueId())));
            return;
        }
        try {
            double buyAmount = Double.parseDouble(args[1]);
            long limit = playerDataManager.getBuyLimit(player);
            if (limit > 0 && buyAmount > limit) {
                player.sendMessage(Messages.get("buy_limit", limit));
            } else if (buyAmount > bitcoinManager.getAmountInBank()) {
                player.sendMessage(Messages.get("not_enough_in_bank", bitcoinManager.format(bitcoinManager.getAmountInBank())));
            } else if (buyAmount <= 0) {
                player.sendMessage(Messages.INVALID_NUMBER.toString());
            } else {
                double cost = (buyAmount * bitcoinManager.getBitcoinValue()) * (1 + bitcoinManager.getPurchaseTaxPercentage() / 100);
                if (cost > ServerEconomy.getBalance(player)) {
                    player.sendMessage(Messages.NOT_ENOUGH_MONEY.toString());
                } else {
                    playerDataManager.deposit(player.getUniqueId(), buyAmount);
                    bitcoinManager.removeFromBank(buyAmount);
                    ServerEconomy.withdraw(player, player.getWorld().getName(), cost);
                    long delay = playerDataManager.getBuyDelay(player);
                    if (delay > 0) {
                        UUID uuid = player.getUniqueId();
                        buyDelays.put(uuid, delay);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                long secsLeft = buyDelays.get(uuid) - 1;
                                buyDelays.put(uuid, secsLeft);
                                if (secsLeft == 0) {
                                    buyDelays.remove(uuid);
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 20, 20);
                    }
                    player.sendMessage(Messages.get("complete_purchase",
                            Util.formatNumber(buyAmount),
                            Util.formatRoundNumber(bitcoinManager.getBitcoinValue() * buyAmount),
                            Util.formatRoundNumber(bitcoinManager.getPurchaseTaxPercentage() / 100 * cost)));
                }
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void transferCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Messages.TRANSFER_COMMAND_INVALID_ARG.toString());
            return;
        }
        Player recipient = Bukkit.getPlayer(args[1]);
        if (recipient == null) {
            player.sendMessage(Messages.NOT_ONLINE.toString().replace("{PLAYER}", args[1]));
            return;
        }
        if (recipient.equals(player)) {
            player.sendMessage(Messages.CANNOT_TRANSFER_TO_SELF.toString());
            return;
        }
        try {
            double transferAmount = Double.parseDouble(args[2]);
            if (transferAmount > playerDataManager.getBalance(player.getUniqueId())) {
                player.sendMessage(Messages.get("not_enough_bitcoins", bitcoinManager.format(playerDataManager.getBalance(player.getUniqueId()))));
            } else if (transferAmount <= 0) {
                player.sendMessage(Messages.INVALID_NUMBER.toString());
            } else {
                playerDataManager.withdraw(player.getUniqueId(), transferAmount);
                playerDataManager.deposit(recipient.getUniqueId(), transferAmount);
                player.sendMessage(Messages.get("complete_transfer", Util.formatNumber(transferAmount), recipient.getDisplayName()));
                recipient.sendMessage(Messages.get("receive_bitcoins", Util.formatNumber(transferAmount), player.getDisplayName()));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void blackMarketCommand(Player player, String[] args) {
        BlackMarket blackMarket = BlackMarket.getInstance();
        if (args.length == 1) {
            blackMarket.open(player);
            return;
        }
        if (args.length < 4 || !args[1].equalsIgnoreCase("setslot")) {
            player.sendMessage(Messages.BLACK_MARKET_COMMAND_INVALID_ARG.toString());
            return;
        }
        if (!player.hasPermission("bitcoin.blackmarket.edit")) {
            player.sendMessage(Messages.NO_PERMISSION.toString());
            return;
        }
        try {
            int slot = Integer.parseInt(args[2]) - 1;
            double price = Double.parseDouble(args[3]);
            if (slot < 0 || slot > 53 || price < 0) {
                player.sendMessage(Messages.INVALID_NUMBER.toString());
                return;
            }
            blackMarket.setItem(slot, player.getInventory().getItemInMainHand(), price, args.length > 4 ?  Integer.parseInt(args[4]) : -1);
            player.sendMessage(Messages.BLACK_MARKET_SET_ITEM.toString());
        } catch (NumberFormatException e) {
            player.sendMessage(Messages.INVALID_NUMBER.toString());
        }
    }

    private void mineCommand(Player player) {
        if (computerManager.isEnabled()) {
            player.sendMessage(Messages.get("computer_help", computerManager.getRecipeString()));
        } else {
            MiningManager.getInstance().openInterface(player);
        }
    }
}
