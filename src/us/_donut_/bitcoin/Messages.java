package us._donut_.bitcoin;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

class Messages {

    private Bitcoin plugin;
    private Util util;
    private File messagesFile;
    private YamlConfiguration messagesConfig;
    private Map<String, String> messages = new HashMap<>();

    Messages(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = pluginInstance.getUtil();
        reload();
    }

    void reload() {
        messages.clear();
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        if (!messagesFile.exists()) {
            messagesConfig.options().header("Messages accept color codes." + System.lineSeparator() + "Messages can be multiple lines." + System.lineSeparator() + "{VARIABLES} are filled in with their respective values, they can only be used in the messages that they are in by default");
            plugin.getLogger().info("Generated messages.yml!");
        }
        loadDefaults();
        loadAllMessages();
    }

    String getMessage(String message) {
        return messages.get(message);
    }

    private void loadDefaults() {
        messagesConfig.addDefault("bank_command", "&3Amount of bitcoins in bank: &b{AMOUNT}");
        messagesConfig.addDefault("begin_exchange", Arrays.asList(" ", "&aYour balance: &2{BALANCE} bitcoins", "&aCurrent bitcoin value: &2{VALUE}", "&aEnter the amount of bitcoins you would like to sell:"));
        messagesConfig.addDefault("begin_purchase", Arrays.asList(" ", "&aBitcoins in bank: &2{BANK} bitcoins", "&aBitcoin cost: &2{VALUE} per bitcoin", "&aTax: &2{TAX}", "&aEnter the amount of bitcoins you would like to buy:"));
        messagesConfig.addDefault("begin_transfer", Arrays.asList(" ", "&aYour balance: &2{BALANCE} bitcoins", "&aEnter the player and amount of bitcoins (e.g. Notch 5):"));
        messagesConfig.addDefault("black_market_command_invalid_arg", "&cInvalid argument.");
        messagesConfig.addDefault("black_market_item_cost", "&6Cost: &a{COST} bitcoins");
        messagesConfig.addDefault("black_market_item_lore", "&3Purchase items with bitcoins");
        messagesConfig.addDefault("black_market_item_name", "&9&lBlack Market");
        messagesConfig.addDefault("black_market_not_enough_bitcoins", "&cYou do not have enough bitcoins to buy this.");
        messagesConfig.addDefault("black_market_purchase", "&aSuccessfully bought item for {COST} bitcoins.");
        messagesConfig.addDefault("black_market_set_item", "&aSuccessfully set item in black market.");
        messagesConfig.addDefault("black_market_title", "&9&lBitcoin Black Market");
        messagesConfig.addDefault("black_tile", "&9&lBlack");
        messagesConfig.addDefault("blue_tile", "&9&lBlue");
        messagesConfig.addDefault("brown_tile", "&9&lBrown");
        messagesConfig.addDefault("buy_command_invalid_arg", "&cInvalid argument.");
        messagesConfig.addDefault("buy_item_lore", "&3Buy bitcoins from the bank");
        messagesConfig.addDefault("buy_item_name", "&9&lBuy Bitcoins");
        messagesConfig.addDefault("cancel_button", "&c&l[Cancel]");
        messagesConfig.addDefault("cancel_button_hover", "&cClick to cancel");
        messagesConfig.addDefault("cancelled_exchange", "&cCancelled exchange.");
        messagesConfig.addDefault("cancelled_purchase", "&cCancelled purchase.");
        messagesConfig.addDefault("cancelled_transfer", "&cCancelled transfer.");
        messagesConfig.addDefault("cannot_transfer_to_self", "&cYou cannot transfer bitcoins to yourself.");
        messagesConfig.addDefault("cannot_use_commands", "&cYou cannot use commands at this time.");
        messagesConfig.addDefault("cannot_use_from_console", "&cYou cannot use this command from console.");
        messagesConfig.addDefault("circulation_command", Arrays.asList("&3Amount of bitcoins in circulation: &b{AMOUNT}", "&3Circulation limit: &b{LIMIT}"));
        messagesConfig.addDefault("command_name", "/bitcoin");
        messagesConfig.addDefault("complete_exchange", "&aSuccessfully sold {AMOUNT} bitcoins for {NEW_AMOUNT}.");
        messagesConfig.addDefault("complete_purchase", "&aSuccessfully bought {AMOUNT} bitcoins for {COST} plus {TAX} in tax.");
        messagesConfig.addDefault("complete_transfer", "&aSuccessfully transferred {AMOUNT} bitcoins to &2{RECIPIENT}.");
        messagesConfig.addDefault("cyan_tile", "&9&lCyan");
        messagesConfig.addDefault("exceeds_limit", "&cThis amount would cause the number of bitcoins in circulation to exceed the limit of {LIMIT} bitcoins.");
        messagesConfig.addDefault("exchange_item_lore", "&3Sell bitcoins to the bank");
        messagesConfig.addDefault("exchange_item_name", "&9&lSell Bitcoins");
        messagesConfig.addDefault("exit_item_lore", Arrays.asList("&cProgress will be saved", "&c(as long as you don't leave the server)"));
        messagesConfig.addDefault("exit_item_name", "&4&lExit");
        messagesConfig.addDefault("generated_puzzle", "&aPuzzle generated, be the first player to solve it to earn bitcoins!");
        messagesConfig.addDefault("generating_puzzle", "&aA new puzzle is being generated...");
        messagesConfig.addDefault("give_command", "&aGave {AMOUNT} bitcoins to balance of &2{PLAYER}.");
        messagesConfig.addDefault("give_command_invalid_arg", "&cInvalid argument.");
        messagesConfig.addDefault("gray_tile", "&9&lGray");
        messagesConfig.addDefault("green_tile", "&9&lGreen");
        messagesConfig.addDefault("help_command", Arrays.asList(" ", "&9<<< Bitcoin Commands >>>", "&3/bitcoin help: &bDisplay this page", "&3/bitcoin value: &bView current bitcoin value", "&3/bitcoin stats [player]: &bView player stats", "&3/bitcoin bank: &bView amount of bitcoins in bank", "&3/bitcoin tax: &bView the current purchase tax", "&3/bitcoin circulation: &bView circulation info", "&3/bitcoin top: &bView players with the most bitcoins", "&3/bitcoin mine: &bOpen mining interface", "&3/bitcoin transfer <player> <amount>: &bTransfer bitcoins", "&3/bitcoin sell <amount>: &bSell bitcoins", "&3/bitcoin buy <amount>: &bBuy bitcoins", "&3/bitcoin blackmarket: &bOpen black market", "&3/bitcoin blackmarket setslot <slot> <price>: &bEdit black market", "&3/bitcoin give <player> <amount>: &bAdd to balance", "&3/bitcoin remove <player> <amount>: &bRemove from balance", "&3/bitcoin set <player> <amount>: &bSet balance", "&3/bitcoin reload: &bReload plugin", "&3/bitcoin cancel: &bCancel transfer/purchase/sell"));
        messagesConfig.addDefault("help_item_lore", "&3Click for list of commands");
        messagesConfig.addDefault("help_item_name", "&9&lHelp");
        messagesConfig.addDefault("inactive_balance_reset", "&cThe bank reclaimed {AMOUNT} bitcoins from {PLAYER} for inactivity.");
        messagesConfig.addDefault("invalid_command", "&cInvalid command.");
        messagesConfig.addDefault("invalid_entry", "&cInvalid entry.");
        messagesConfig.addDefault("invalid_number",  "&cInvalid number.");
        messagesConfig.addDefault("light_blue_tile", "&9&lLight Blue");
        messagesConfig.addDefault("light_gray_tile", "&9&lLight Gray");
        messagesConfig.addDefault("lime_tile", "&9&lLime");
        messagesConfig.addDefault("magenta_tile", "&9&lMagenta");
        messagesConfig.addDefault("menu_title", "&9&lBitcoin Menu");
        messagesConfig.addDefault("mining_item_lore", "&3Solve puzzles to earn bitcoins");
        messagesConfig.addDefault("mining_item_name", "&9&lBitcoin Mining");
        messagesConfig.addDefault("mining_menu_title", "&9&lBitcoin Mining");
        messagesConfig.addDefault("never_joined", "&4{PLAYER} &chas never joined the server.");
        messagesConfig.addDefault("no_economy", "&cNo economy plugin was detected.");
        messagesConfig.addDefault("no_permission", "&cYou do not have permission to use this command.");
        messagesConfig.addDefault("not_enough_bitcoins", "&cYou only have {BALANCE} bitcoins.");
        messagesConfig.addDefault("not_enough_in_bank", "&cThe bank only has {AMOUNT} bitcoins.");
        messagesConfig.addDefault("not_enough_money", "&cYou cannot afford this many bitcoins.");
        messagesConfig.addDefault("not_online", "&4{PLAYER} &cis not online.");
        messagesConfig.addDefault("nothing_to_cancel", "&cNothing to cancel.");
        messagesConfig.addDefault("orange_tile", "&9&lOrange");
        messagesConfig.addDefault("other_player_not_enough_bitcoins", "&c{PLAYER} only has {BALANCE} bitcoins.");
        messagesConfig.addDefault("pink_tile", "&9&lPink");
        messagesConfig.addDefault("purple_tile", "&9&lPurple");
        messagesConfig.addDefault("real_value_announcement", Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3Current bitcoin value: &b{VALUE}"));
        messagesConfig.addDefault("receive_bitcoins","&aYou received {AMOUNT} bitcoins from &2{SENDER}.");
        messagesConfig.addDefault("red_tile", "&9&lRed");
        messagesConfig.addDefault("reload_command", "&aSuccessfully reloaded bitcoin.");
        messagesConfig.addDefault("remove_command", "&aRemoved {AMOUNT} bitcoins from balance of &2{PLAYER}.");
        messagesConfig.addDefault("remove_command_invalid_arg", "&cInvalid argument.");
        messagesConfig.addDefault("reset_item_lore", "&cClick to reset the tiles");
        messagesConfig.addDefault("reset_item_name", "&4&lReset");
        messagesConfig.addDefault("reward", "&aCongrats, you were rewarded {REWARD} bitcoins!");
        messagesConfig.addDefault("sell_command_invalid_arg", "&cInvalid argument.");
        messagesConfig.addDefault("set_command", "&aSet balance of &2{PLAYER} &ato {AMOUNT} bitcoins.");
        messagesConfig.addDefault("set_command_invalid_arg", "&cInvalid argument.");
        messagesConfig.addDefault("solve_item_lore", "&aClick when you think you solved the puzzle");
        messagesConfig.addDefault("solve_item_name",  "&2&lSolve");
        messagesConfig.addDefault("solved", Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3Puzzle solved by: &b{SOLVER}", "&3Reward: &b{REWARD} bitcoins", "&3Time: &b{MIN} minutes {SEC} seconds"));
        messagesConfig.addDefault("statistic_command_self", Arrays.asList("&9<<< Your Stats >>>", "&3Balance: &b{BALANCE} bitcoins", "&3Mining puzzles solved: &b{AMOUNT_SOLVED}", "&3Bitcoins mined: &b{AMOUNT_MINED}", "&3Best puzzle time: &b{MIN} minutes {SEC} seconds"));
        messagesConfig.addDefault("statistic_command_other", Arrays.asList("&9<<< {PLAYER}'s Stats>>>", "&3Balance: &b{BALANCE} bitcoins", "&3Mining puzzles solved: &b{AMOUNT_SOLVED}", "&3Bitcoins mined: &b{AMOUNT_MINED}", "&3Best puzzle time: &b{MIN} minutes {SEC} seconds"));
        messagesConfig.addDefault("statistic_item_lore", Arrays.asList("&3Balance: &b{BALANCE} bitcoins", "&3Mining puzzles solved: &b{AMOUNT_SOLVED}", "&3Bitcoins mined: &b{AMOUNT_MINED}", "&3Best puzzle time: &b{MIN} minutes {SEC} seconds"));
        messagesConfig.addDefault("statistic_item_name", "&9&lStatistics");
        messagesConfig.addDefault("tax_command", "&3Purchase tax: &b{TAX}");
        messagesConfig.addDefault("top_command_format", "&3{PLACE}. {PLAYER}: &b{BALANCE} bitcoins");
        messagesConfig.addDefault("top_command_header", "&9<<< Bitcoin Top Players >>>");
        messagesConfig.addDefault("transfer_command_invalid_arg", "&cInvalid argument.");
        messagesConfig.addDefault("transfer_item_lore", "&3Transfer bitcoins to another account");
        messagesConfig.addDefault("transfer_item_name", "&9&lTransfer Bitcoins");
        messagesConfig.addDefault("value_command", "&3Current value of 1 bitcoin: &b{VALUE}");
        messagesConfig.addDefault("value_decrease", Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3New bitcoin value: &b{VALUE}", "&cValue has decreased by: &4{CHANGE}"));
        messagesConfig.addDefault("value_increase", Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3New bitcoin value: &b{VALUE}", "&aValue has increased by: &2{CHANGE}"));
        messagesConfig.addDefault("white_tile", "&9&lWhite");
        messagesConfig.addDefault("yellow_tile", "&9&lYellow");

        messagesConfig.options().copyDefaults(true);
        util.saveYml(messagesFile, messagesConfig);
    }

    private void loadAllMessages() {
        for (String configurableMessage : messagesConfig.getKeys(false)) {
            List<String> multipleLineMessage = messagesConfig.getStringList(configurableMessage);
            if (multipleLineMessage.isEmpty()) {
                messages.put(configurableMessage, util.colorMessage(messagesConfig.getString(configurableMessage)));
            } else {
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < multipleLineMessage.size(); i++) {
                    message.append(multipleLineMessage.get(i));
                    if (i != multipleLineMessage.size() - 1) {
                        message.append("\n");
                    }
                }
                messages.put(configurableMessage, util.colorMessage(message.toString()));
            }
        }
    }
}