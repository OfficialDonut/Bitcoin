package us._donut_.bitcoin;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

class Messages {

    private Util util;
    private File messagesFile;
    private YamlConfiguration messagesConfig;
    private Map<String, String> messages = new HashMap<>();

    Messages(Bitcoin pluginInstance) {
        util = pluginInstance.getUtil();

        messagesFile = new File(pluginInstance.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        if (!messagesFile.exists()) {
            messagesConfig.options().header("Messages accept color codes." + System.lineSeparator() + "Any message can be multiple lines (see bottom messages for examples)." + System.lineSeparator() + "{VARIABLES} are filled in with their respective values, they can only be used in the messages they are in by default");
            pluginInstance.getLogger().info("Generated messages.yml!");
        }
        loadDefaults();
        loadAllMessages();
    }

    String getMessage(String message) {
        return messages.get(message);
    }

    private void loadDefaults() {
        if (!messagesConfig.contains("command_name")) { messagesConfig.set("command_name", "/bitcoin"); }
        if (!messagesConfig.contains("complete_transfer")) { messagesConfig.set("complete_transfer", "&aSuccessfully transferred {AMOUNT} bitcoins to &2{RECIPIENT}."); }
        if (!messagesConfig.contains("receive_bitcoins")) { messagesConfig.set("receive_bitcoins", "&aYou received {AMOUNT} bitcoins from &2{SENDER}."); }
        if (!messagesConfig.contains("invalid_number")) { messagesConfig.set("invalid_number", "&cInvalid number."); }
        if (!messagesConfig.contains("cannot_transfer_to_self")) { messagesConfig.set("cannot_transfer_to_self", "&cYou cannot transfer bitcoins to yourself."); }
        if (!messagesConfig.contains("not_enough_bitcoins")) { messagesConfig.set("not_enough_bitcoins", "&cYou only have {BALANCE} bitcoins."); }
        if (!messagesConfig.contains("not_online")) { messagesConfig.set("not_online", "&4{PLAYER} &cis not online."); }
        if (!messagesConfig.contains("invalid_entry")) { messagesConfig.set("invalid_entry", "&cInvalid entry."); }
        if (!messagesConfig.contains("complete_exchange")) { messagesConfig.set("complete_exchange", "&aSuccessfully exchanged {AMOUNT} bitcoins for {NEW_AMOUNT}."); }
        if (!messagesConfig.contains("no_economy")) { messagesConfig.set("no_economy", "&cNo economy plugin was detected."); }
        if (!messagesConfig.contains("cannot_use_commands")) { messagesConfig.set("cannot_use_commands", "&cYou cannot use commands at this time."); }
        if (!messagesConfig.contains("cancel_button")) { messagesConfig.set("cancel_button", "&c&l[Cancel]"); }
        if (!messagesConfig.contains("cancel_button_hover")) { messagesConfig.set("cancel_button_hover", "&cCancelled transfer."); }
        if (!messagesConfig.contains("cancelled_transfer")) { messagesConfig.set("cancelled_transfer", "&cCancelled transfer."); }
        if (!messagesConfig.contains("cancelled_exchange")) { messagesConfig.set("cancelled_exchange", "&cCancelled exchange."); }
        if (!messagesConfig.contains("nothing_to_cancel")) { messagesConfig.set("nothing_to_cancel", "&cNothing to cancel."); }
        if (!messagesConfig.contains("cannot_use_from_console")) { messagesConfig.set("cannot_use_from_console", "&cYou cannot use this command from console."); }
        if (!messagesConfig.contains("generating_puzzle")) { messagesConfig.set("generating_puzzle", "&aA new puzzle is being generated..."); }
        if (!messagesConfig.contains("generated_puzzle")) { messagesConfig.set("generated_puzzle", "&aPuzzle generated, be the first player to solve it to earn bitcoins!"); }
        if (!messagesConfig.contains("reward")) { messagesConfig.set("reward", "&aCongrats, you were rewarded {REWARD} bitcoins!"); }
        if (!messagesConfig.contains("menu_title")) { messagesConfig.set("menu_title", "&9&lBitcoin Menu"); }
        if (!messagesConfig.contains("statistic_item_name")) { messagesConfig.set("statistic_item_name", "&9&lStatistics"); }
        if (!messagesConfig.contains("transfer_item_name")) { messagesConfig.set("transfer_item_name", "&9&lTransfer Bitcoins"); }
        if (!messagesConfig.contains("transfer_item_lore")) { messagesConfig.set("transfer_item_lore", "&3Transfer bitcoins to another account"); }
        if (!messagesConfig.contains("exchange_item_name")) { messagesConfig.set("exchange_item_name", "&9&lExchange Bitcoins"); }
        if (!messagesConfig.contains("exchange_item_lore")) { messagesConfig.set("exchange_item_lore", "&3Exchange bitcoins for other currency"); }
        if (!messagesConfig.contains("mining_item_name")) { messagesConfig.set("mining_item_name", "&9&lBitcoin Mining"); }
        if (!messagesConfig.contains("mining_item_lore")) { messagesConfig.set("mining_item_lore", "&3Solve puzzles to earn bitcoins"); }
        if (!messagesConfig.contains("mining_menu_title")) { messagesConfig.set("mining_menu_title", "&9&lBitcoin Mining"); }
        if (!messagesConfig.contains("reset_item_name")) { messagesConfig.set("reset_item_name", "&4&lReset"); }
        if (!messagesConfig.contains("reset_item_lore")) { messagesConfig.set("reset_item_lore", "&cClick to reset the tiles"); }
        if (!messagesConfig.contains("solve_item_name")) { messagesConfig.set("solve_item_name", "&aClick when you think you solved the puzzle"); }
        if (!messagesConfig.contains("solve_item_lore")) { messagesConfig.set("solve_item_lore", "&cClick to reset the tiles"); }
        if (!messagesConfig.contains("exit_item_name")) { messagesConfig.set("exit_item_name", "&4&lExit"); }
        if (!messagesConfig.contains("white_tile")) { messagesConfig.set("white_tile", "&9&lWhite"); }
        if (!messagesConfig.contains("orange_tile")) { messagesConfig.set("orange_tile", "&9&lOrange"); }
        if (!messagesConfig.contains("magenta_tile")) { messagesConfig.set("magenta_tile", "&9&lMagenta"); }
        if (!messagesConfig.contains("light_blue_tile")) { messagesConfig.set("light_blue_tile", "&9&lLight Blue"); }
        if (!messagesConfig.contains("yellow_tile")) { messagesConfig.set("yellow_tile", "&9&lYellow"); }
        if (!messagesConfig.contains("lime_tile")) { messagesConfig.set("lime_tile", "&9&lLime"); }
        if (!messagesConfig.contains("pink_tile")) { messagesConfig.set("pink_tile", "&9&lPink"); }
        if (!messagesConfig.contains("gray_tile")) { messagesConfig.set("gray_tile", "&9&lGray"); }
        if (!messagesConfig.contains("light_gray_tile")) { messagesConfig.set("light_gray_tile", "&9&lLight Gray"); }
        if (!messagesConfig.contains("cyan_tile")) { messagesConfig.set("cyan_tile", "&9&lCyan"); }
        if (!messagesConfig.contains("purple_tile")) { messagesConfig.set("purple_tile", "&9&lPurple"); }
        if (!messagesConfig.contains("blue_tile")) { messagesConfig.set("blue_tile", "&9&lBlue"); }
        if (!messagesConfig.contains("brown_tile")) { messagesConfig.set("brown_tile", "&9&lBrown"); }
        if (!messagesConfig.contains("green_tile")) { messagesConfig.set("green_tile", "&9&lGreen"); }
        if (!messagesConfig.contains("red_tile")) { messagesConfig.set("red_tile", "&9&lRed"); }
        if (!messagesConfig.contains("black_tile")) { messagesConfig.set("black_tile", "&9&lBlack"); }
        if (!messagesConfig.contains("value_increase")) { messagesConfig.set("value_increase", Arrays.asList(" ", "&9<<< Daily Bitcoin Announcement >>>", "&3New bitcoin value: &b{VALUE}", "&aValue has increased by: &2{CHANGE}")); }
        if (!messagesConfig.contains("value_decrease")) { messagesConfig.set("value_decrease", Arrays.asList(" ", "&9<<< Daily Bitcoin Announcement >>>", "&3New bitcoin value: &b{VALUE}", "&cValue has decreased by: &4{CHANGE}")); }
        if (!messagesConfig.contains("solved")) { messagesConfig.set("solved", new ArrayList<>(Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3Puzzle solved by: &b{SOLVER}", "&3Reward: &b{REWARD} bitcoins", " "))); }
        if (!messagesConfig.contains("begin_transfer")) { messagesConfig.set("begin_transfer", Arrays.asList(" ", "&aYour balance: &2{BALANCE} bitcoins", "&aEnter the player and amount of bitcoins (e.g. Notch 5):")); }
        if (!messagesConfig.contains("begin_exchange")) { messagesConfig.set("begin_exchange", Arrays.asList(" ", "&aYour balance: &2{BALANCE} bitcoins", "&aCurrent bitcoin value: &2{VALUE}", "&aEnter the amount of bitcoins you would like to exchange:")); }
        if (!messagesConfig.contains("exit_item_lore")) { messagesConfig.set("exit_item_lore", Arrays.asList("&cProgress will be saved", "&c(as long as you don't leave the server)")); }
        if (!messagesConfig.contains("statistic_item_lore")) { messagesConfig.set("statistic_item_lore", Arrays.asList("&3Balance: &b{BALANCE} bitcoins", "&3Mining puzzles solved: &b{AMOUNT_SOLVED}", "&3Bitcoins mined: &b{AMOUNT_MINED}")); }
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