package us.donut.bitcoin.config;

import org.bukkit.configuration.file.YamlConfiguration;
import us.donut.bitcoin.Bitcoin;
import us.donut.bitcoin.Util;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Messages {

    BANK_COMMAND("bank_command", "&3Amount of bitcoins in bank: &b{AMOUNT}"),
    BEGIN_EXCHANGE("begin_exchange", Arrays.asList(" ", "&aYour balance: &2{BALANCE} bitcoins", "&aCurrent bitcoin value: &2${VALUE}", "&aEnter the amount of bitcoins you would like to sell:")),
    BEGIN_PURCHASE("begin_purchase", Arrays.asList(" ", "&aBitcoins in bank: &2{BANK} bitcoins", "&aBitcoin cost: &2${VALUE} per bitcoin", "&aTax: &2{TAX}", "&aEnter the amount of bitcoins you would like to buy:")),
    BEGIN_TRANSFER("begin_transfer", Arrays.asList(" ", "&aYour balance: &2{BALANCE} bitcoins", "&aEnter the player and amount of bitcoins (e.g. Notch 5):")),
    BLACK_MARKET_COMMAND_INVALID_ARG("black_market_command_invalid_arg", "&cInvalid argument."),
    BLACK_MARKET_ITEM_COST("black_market_item_cost", "&6Cost: &a{COST} bitcoins"),
    BLACK_MARKET_ITEM_LORE("black_market_item_lore", "&3Purchase items with bitcoins"),
    BLACK_MARKET_ITEM_NAME("black_market_item_name", "&9&lBlack Market"),
    BLACK_MARKET_ITEM_IN_STOCK("black_market_item_in_stock", "&3In Stock: &b{AMOUNT}"),
    BLACK_MARKET_ITEM_INFINITE_STOCK("black_market_item_infinite_stock", "&3INFINITE STOCK"),
    BLACK_MARKET_ITEM_OUT_OF_STOCK("black_market_item_out_of_stock", "&cOUT OF STOCK"),
    BLACK_MARKET_NOT_ENOUGH_BITCOINS("black_market_not_enough_bitcoins", "&cYou do not have enough bitcoins to buy this."),
    BLACK_MARKET_OUT_OF_STOCK("black_market_out_of_stock", "&cThis item is currently out of stock."),
    BLACK_MARKET_PURCHASE("black_market_purchase", "&aSuccessfully bought item for {COST} bitcoins."),
    BLACK_MARKET_SET_ITEM("black_market_set_item", "&aSuccessfully set item in black market."),
    BLACK_MARKET_TITLE("black_market_title", "&9&lBitcoin Black Market"),
    BLACK_TILE("black_tile", "&9&lBlack"),
    BLUE_TILE("blue_tile", "&9&lBlue"),
    BROWN_TILE("brown_tile", "&9&lBrown"),
    BUY_COMMAND_INVALID_ARG("buy_command_invalid_arg", "&cInvalid argument."),
    BUY_DELAY("buy_delay", "&cYou must wait {SEC} seconds before you can buy more bitcoins from the bank."),
    BUY_ITEM_LORE("buy_item_lore", "&3Buy bitcoins from the bank"),
    BUY_ITEM_NAME("buy_item_name", "&9&lBuy Bitcoins"),
    BUY_LIMIT("buy_limit", "&cYou can only buy {AMOUNT} bitcoins at one time."),
    CANCEL_BUTTON("cancel_button", "&c&l[Cancel]"),
    CANCEL_BUTTON_HOVER("cancel_button_hover", "&cClick to cancel"),
    CANCELLED_EXCHANGE("cancelled_exchange", "&cCancelled exchange."),
    CANCELLED_PURCHASE("cancelled_purchase", "&cCancelled purchase."),
    CANCELLED_TRANSFER("cancelled_transfer", "&cCancelled transfer."),
    CANNOT_TRANSFER_TO_SELF("cannot_transfer_to_self", "&cYou cannot transfer bitcoins to yourself."),
    CANNOT_USE_COMMANDS("cannot_use_commands", "&cYou cannot use commands at this time."),
    CANNOT_USE_FROM_CONSOLE("cannot_use_from_console", "&cYou cannot use this command from console."),
    CIRCULATION_COMMAND("circulation_command", Arrays.asList("&3Amount of bitcoins in circulation: &b{AMOUNT}", "&3Circulation limit: &b{LIMIT}")),
    COMMAND_NAME("command_name", "/bitcoin"),
    COMPLETE_EXCHANGE("complete_exchange", "&aSuccessfully sold {AMOUNT} bitcoins for ${NEW_AMOUNT}."),
    COMPLETE_PURCHASE("complete_purchase", "&aSuccessfully bought {AMOUNT} bitcoins for ${COST} plus ${TAX} in tax."),
    COMPLETE_TRANSFER("complete_transfer", "&aSuccessfully transferred {AMOUNT} bitcoins to &2{RECIPIENT}."),
    COMPUTER_BROKE("computer_broke", "&cYour computer broke!"),
    COMPUTER_COMMAND_INVALID_ARG("computer_command_invalid_arg", "&cInvalid argument."),
    COMPUTER_DISABLED("computer_disabled", "&cComputers are disabled, enable them in the config."),
    COMPUTER_HELP("computer_help", Arrays.asList("&3Craft a computer to mine bitcoins:", "&b{RECIPE}")),
    COMPUTER_ITEM_LORE("computer_item_lore", Arrays.asList("Right click to mine bitcoins", "&9Uses left: &b{USES}")),
    COMPUTER_ITEM_NAME("computer_item_name", "&9Computer"),
    CYAN_TILE("cyan_tile", "&9&lCyan"),
    EXCEEDS_LIMIT("exceeds_limit", "&cThis amount would cause the number of bitcoins in circulation to exceed the limit of {LIMIT} bitcoins."),
    EXCHANGE_ITEM_LORE("exchange_item_lore", "&3Sell bitcoins to the bank"),
    EXCHANGE_ITEM_NAME("exchange_item_name", "&9&lSell Bitcoins"),
    EXIT_ITEM_LORE("exit_item_lore", Arrays.asList("&cProgress will be saved", "&c(as long as you don't leave the server)")),
    EXIT_ITEM_NAME("exit_item_name", "&4&lExit"),
    GENERATED_PUZZLE("generated_puzzle", "&aPuzzle generated, be the first player to solve it to earn bitcoins!"),
    GENERATING_PUZZLE("generating_puzzle", "&aA new puzzle is being generated..."),
    GIVE_COMMAND("give_command", "&aGave {AMOUNT} bitcoins to balance of &2{PLAYER}."),
    GIVE_COMMAND_INVALID_ARG("give_command_invalid_arg", "&cInvalid argument."),
    GRAY_TILE("gray_tile", "&9&lGray"),
    GREEN_TILE("green_tile", "&9&lGreen"),
    HELP_COMMAND("help_command", Arrays.asList(" ", "&9<<< Bitcoin Commands >>>", "&3/bitcoin help: &bDisplay this page", "&3/bitcoin value: &bView current bitcoin value", "&3/bitcoin stats [player]: &bView player stats", "&3/bitcoin bank: &bView amount of bitcoins in bank", "&3/bitcoin tax: &bView the current purchase tax", "&3/bitcoin circulation: &bView circulation info", "&3/bitcoin top [bal/time/solved]: &bView players with the top stats", "&3/bitcoin mine: &bOpen mining interface", "&3/bitcoin transfer <player> <amount>: &bTransfer bitcoins", "&3/bitcoin sell <amount>: &bSell bitcoins", "&3/bitcoin buy <amount>: &bBuy bitcoins", "&3/bitcoin blackmarket: &bOpen black market", "&3/bitcoin blackmarket setslot <slot> <price> [stock]: &bEdit market", "&3/bitcoin give <player> <amount>: &bAdd to balance", "&3/bitcoin remove <player> <amount>: &bRemove from balance", "&3/bitcoin set <player> <amount>: &bSet balance", "&3/bitcoin reset <bal/mined/solved/time>: &bReset stats", "&3/bitcoin computer <player> [amount]: &bGive computer", "&3/bitcoin reload: &bReload plugin", "&3/bitcoin cancel: &bCancel transfer/purchase/sell")),
    HELP_ITEM_LORE("help_item_lore", "&3Click for list of commands"),
    HELP_ITEM_NAME("help_item_name", "&9&lHelp"),
    INACTIVE_BALANCE_RESET("inactive_balance_reset", "&cThe bank reclaimed {AMOUNT} bitcoins from {PLAYER} for inactivity."),
    INVALID_COMMAND("invalid_command", "&cInvalid command."),
    INVALID_ENTRY("invalid_entry", "&cInvalid entry."),
    INVALID_NUMBER("invalid_number",  "&cInvalid number."),
    LIGHT_BLUE_TILE("light_blue_tile", "&9&lLight Blue"),
    LIGHT_GRAY_TILE("light_gray_tile", "&9&lLight Gray"),
    LIME_TILE("lime_tile", "&9&lLime"),
    MAGENTA_TILE("magenta_tile", "&9&lMagenta"),
    MENU_TITLE("menu_title", "&9&lBitcoin Menu"),
    MINING_ITEM_LORE("mining_item_lore", "&3Solve puzzles to earn bitcoins"),
    MINING_ITEM_NAME("mining_item_name", "&9&lBitcoin Mining"),
    MINING_MENU_TITLE("mining_menu_title", "&9&lBitcoin Mining"),
    NEVER_JOINED("never_joined", "&4{PLAYER} &chas never joined the server."),
    NO_ECONOMY("no_economy", "&cNo economy plugin was detected."),
    NO_PERMISSION("no_permission", "&cYou do not have permission to use this command."),
    NOT_ENOUGH_BITCOINS("not_enough_bitcoins", "&cYou only have {BALANCE} bitcoins."),
    NOT_ENOUGH_IN_BANK("not_enough_in_bank", "&cThe bank only has {AMOUNT} bitcoins."),
    NOT_ENOUGH_MONEY("not_enough_money", "&cYou cannot afford this many bitcoins."),
    NOT_ONLINE("not_online", "&4{PLAYER} &cis not online."),
    NOTHING_TO_CANCEL("nothing_to_cancel", "&cNothing to cancel."),
    ORANGE_TILE("orange_tile", "&9&lOrange"),
    OTHER_PLAYER_NOT_ENOUGH_BITCOINS("other_player_not_enough_bitcoins", "&c{PLAYER} only has {BALANCE} bitcoins."),
    PINK_TILE("pink_tile", "&9&lPink"),
    PURPLE_TILE("purple_tile", "&9&lPurple"),
    RECEIVE_BITCOINS("receive_bitcoins","&aYou received {AMOUNT} bitcoins from &2{SENDER}."),
    RED_TILE("red_tile", "&9&lRed"),
    RELOAD_COMMAND("reload_command", "&aSuccessfully reloaded bitcoin."),
    REMOVE_COMMAND("remove_command", "&aRemoved {AMOUNT} bitcoins from balance of &2{PLAYER}."),
    REMOVE_COMMAND_INVALID_ARG("remove_command_invalid_arg", "&cInvalid argument."),
    RESET_COMMAND_BAL("reset_command_bal", "&aSuccessfully reset all balances."),
    RESET_COMMAND_INVALID_ARG("reset_command_invalid_arg", "&cInvalid argument."),
    RESET_COMMAND_MINED("reset_command_mined", "&aSuccessfully reset the bitcoins mined stat for all players."),
    RESET_COMMAND_SOLVED("reset_command_solved", "&aSuccessfully reset the puzzles solved stat for all players."),
    RESET_COMMAND_TIME("reset_command_time", "&aSuccessfully reset the best puzzle time stat for all players."),
    RESET_ITEM_LORE("reset_item_lore", "&cClick to reset the tiles"),
    RESET_ITEM_NAME("reset_item_name", "&4&lReset"),
    REWARD("reward", "&aCongrats, you were rewarded {REWARD} bitcoins!"),
    SELL_COMMAND_INVALID_ARG("sell_command_invalid_arg", "&cInvalid argument."),
    SET_COMMAND("set_command", "&aSet balance of &2{PLAYER} &ato {AMOUNT} bitcoins."),
    SET_COMMAND_INVALID_ARG("set_command_invalid_arg", "&cInvalid argument."),
    SOLVE_ITEM_LORE("solve_item_lore", "&aClick when you think you solved the puzzle"),
    SOLVE_ITEM_NAME("solve_item_name",  "&2&lSolve"),
    SOLVED("solved", Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3Puzzle solved by: &b{SOLVER}", "&3Reward: &b{REWARD} bitcoins", "&3Time: &b{MIN} minutes {SEC} seconds")),
    STATISTIC_COMMAND_SELF("statistic_command_self", Arrays.asList("&9<<< Your Stats >>>", "&3Balance: &b{BALANCE} bitcoins", "&3Mining puzzles solved: &b{AMOUNT_SOLVED}", "&3Bitcoins mined: &b{AMOUNT_MINED}", "&3Best puzzle time: &b{MIN} minutes {SEC} seconds")),
    STATISTIC_COMMAND_OTHER("statistic_command_other", Arrays.asList("&9<<< {PLAYER}'s Stats>>>", "&3Balance: &b{BALANCE} bitcoins", "&3Mining puzzles solved: &b{AMOUNT_SOLVED}", "&3Bitcoins mined: &b{AMOUNT_MINED}", "&3Best puzzle time: &b{MIN} minutes {SEC} seconds")),
    STATISTIC_ITEM_LORE("statistic_item_lore", Arrays.asList("&3Balance: &b{BALANCE} bitcoins", "&3Mining puzzles solved: &b{AMOUNT_SOLVED}", "&3Bitcoins mined: &b{AMOUNT_MINED}", "&3Best puzzle time: &b{MIN} minutes {SEC} seconds")),
    STATISTIC_ITEM_NAME("statistic_item_name", "&9&lStatistics"),
    TAX_COMMAND("tax_command", "&3Purchase tax: &b${TAX}"),
    TOP_BAL_COMMAND_FORMAT("top_bal_command_format", "&3{PLACE}. {PLAYER}: &b{BALANCE} bitcoins"),
    TOP_SOLVED_COMMAND_FORMAT("top_solved_command_format", "&3{PLACE}. {PLAYER}: &b{AMOUNT} puzzles"),
    TOP_TIME_COMMAND_FORMAT("top_time_command_format", "&3{PLACE}. {PLAYER}: &b{MIN} minutes {SEC} seconds"),
    TOP_BAL_COMMAND_HEADER("top_bal_command_header", "&9<<< Top Bitcoin Balances >>>"),
    TOP_SOLVED_COMMAND_HEADER("top_solved_command_header", "&9<<< Most Puzzles Solved >>>"),
    TOP_TIME_COMMAND_HEADER("top_time_command_header", "&9<<< Fastest Puzzle Times >>>"),
    TRANSFER_COMMAND_INVALID_ARG("transfer_command_invalid_arg", "&cInvalid argument."),
    TRANSFER_ITEM_LORE("transfer_item_lore", "&3Transfer bitcoins to another account"),
    TRANSFER_ITEM_NAME("transfer_item_name", "&9&lTransfer Bitcoins"),
    VALUE_COMMAND("value_command", "&3Current value of 1 bitcoin: &b${VALUE}"),
    VALUE_DECREASE("value_decrease", Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3New bitcoin value: &b${VALUE}", "&cValue has decreased by: &4${CHANGE}")),
    VALUE_INCREASE("value_increase", Arrays.asList(" ", "&9<<< Bitcoin Announcement >>>", "&3New bitcoin value: &b${VALUE}", "&aValue has increased by: &2${CHANGE}")),
    WHITE_TILE("white_tile", "&9&lWhite"),
    YELLOW_TILE("yellow_tile", "&9&lYellow");

    private static Map<Messages, String> messages = new HashMap<>();

    public static void reload() {
        messages.clear();
        File messagesFile = new File(Bitcoin.getInstance().getDataFolder(), "messages.yml");
        YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        if (!messagesFile.exists()) {
            messagesConfig.options().header("Messages accept color codes." + System.lineSeparator() + "Messages can be multiple lines." + System.lineSeparator() + "{VARIABLES} are filled in with their respective values, they can only be used in the messages that they are in by default");
            Bitcoin.getInstance().getLogger().info("Generated messages.yml!");
        }

        for (Messages message : values()) {
            messagesConfig.addDefault(message.key, message.defaultValue);
        }
        messagesConfig.options().copyDefaults(true);
        Util.saveYml(messagesFile, messagesConfig);

        for (Messages message : values()) {
            List<String> messageValue = messagesConfig.getStringList(message.key);
            messages.put(message, Util.color(messageValue.isEmpty() ? messagesConfig.getString(message.key) : String.join("\n", messageValue)));
        }
    }

    public static String get(String key, Object... args) {
        Messages messageEnum = Messages.valueOf(key.toUpperCase());
        List<String> vars = messageEnum.variables;
        String message = messageEnum.toString();
        for (int i = 0; i < vars.size(); i++) {
            if (i < args.length) {
                message = message.replace(vars.get(i), String.valueOf(args[i]));
            }
        }
        return message;
    }

    private String key;
    private Object defaultValue;
    private List<String> variables;
    private Pattern varPattern = Pattern.compile("\\{.+?}");

    Messages(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        variables = getVariables();
    }

    @SuppressWarnings("unchecked")
    private List<String> getVariables() {
        List<String> vars = new ArrayList<>();
        String message = defaultValue instanceof String ? (String) defaultValue : String.join("\n", (List<String>) defaultValue);
        Matcher matcher = varPattern.matcher(message);
        while (matcher.find()) {
            vars.add(matcher.group());
        }
        return vars;
    }

    @Override
    public String toString() {
        return Util.color(messages.get(this));
    }
}
