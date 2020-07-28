package us.donut.bitcoin.config;

import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import us.donut.bitcoin.Bitcoin;
import us.donut.bitcoin.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Sounds {

    private static File soundsFile;
    private static YamlConfiguration soundsConfig;
    private static Map<String, Sound> sounds = new HashMap<>();

    public static void reload() {
        Bitcoin plugin = Bitcoin.getInstance();
        sounds.clear();
        soundsFile = new File(plugin.getDataFolder(), "sounds.yml");
        soundsConfig = YamlConfiguration.loadConfiguration(soundsFile);
        if (!soundsFile.exists()) {
            soundsConfig.options().header("1.9+ sounds: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html" + System.lineSeparator() + "1.8 sounds: http://docs.codelanx.com/Bukkit/1.8/org/bukkit/Sound.html");
            plugin.getLogger().info("Generated sounds.yml!");
        }
        loadDefaults();
        soundsConfig.getKeys(false).forEach(sound -> sounds.put(sound, Sound.valueOf(soundsConfig.getString(sound))));
    }

    private static void loadDefaults() {
        soundsConfig.addDefault("black_market_not_enough_bitcoins", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("black_market_out_of_stock", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("black_market_purchase", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundsConfig.addDefault("cancelled_exchange", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("cancelled_purchase", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("cancelled_transfer", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("click_black_market_item", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundsConfig.addDefault("click_buy_item", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundsConfig.addDefault("click_exchange_item", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundsConfig.addDefault("click_help_item", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundsConfig.addDefault("click_mining_item", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundsConfig.addDefault("click_solve_when_not_solved", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("click_transfer_item", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundsConfig.addDefault("complete_exchange", "ENTITY_PLAYER_LEVELUP");
        soundsConfig.addDefault("complete_purchase", "ENTITY_PLAYER_LEVELUP");
        soundsConfig.addDefault("complete_transfer", "ENTITY_PLAYER_LEVELUP");
        soundsConfig.addDefault("exit_mining", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("no_economy", "ENTITY_BAT_TAKEOFF");
        soundsConfig.addDefault("puzzle_solved", "ENTITY_PLAYER_LEVELUP");
        soundsConfig.addDefault("real_value_announcement", "ENTITY_PLAYER_LEVELUP");
        soundsConfig.addDefault("reset_tiles", "ENTITY_GENERIC_EXPLODE");
        soundsConfig.addDefault("value_change", "ENTITY_PLAYER_LEVELUP");
        soundsConfig.options().copyDefaults(true);
        Util.saveYml(soundsFile, soundsConfig);
    }

    public static Sound get(String sound) {
        return sounds.get(sound);
    }
}
