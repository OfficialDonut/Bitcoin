package us._donut_.bitcoin;

import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

class Sounds {

    private Bitcoin plugin;
    private Util util;
    private File soundsFile;
    private YamlConfiguration soundsConfig;
    private Map<String, Sound> sounds = new HashMap<>();

    Sounds(Bitcoin pluginInstance) {
        plugin = pluginInstance;
        util = pluginInstance.getUtil();
        reload();
    }

    void reload() {
        sounds.clear();
        soundsFile = new File(plugin.getDataFolder(), "sounds.yml");
        soundsConfig = YamlConfiguration.loadConfiguration(soundsFile);
        if (!soundsFile.exists()) {
            soundsConfig.options().header("1.9+ sounds: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html" + System.lineSeparator() + "1.8 sounds: http://docs.codelanx.com/Bukkit/1.8/org/bukkit/Sound.html");
            plugin.getLogger().info("Generated sounds.yml!");
        }
        loadDefaults();
        loadAllSounds();
    }

    Sound getSound(String sound) {
        return sounds.get(sound);
    }

    private void loadDefaults() {
        if (!soundsConfig.contains("cancelled_exchange")) { soundsConfig.set("cancelled_exchange", "ENTITY_BAT_TAKEOFF"); }
        if (!soundsConfig.contains("cancelled_transfer")) { soundsConfig.set("cancelled_transfer", "ENTITY_BAT_TAKEOFF"); }
        if (!soundsConfig.contains("value_change")) { soundsConfig.set("value_change", "ENTITY_PLAYER_LEVELUP"); }
        if (!soundsConfig.contains("click_transfer_item")) { soundsConfig.set("click_transfer_item", "ENTITY_EXPERIENCE_ORB_PICKUP"); }
        if (!soundsConfig.contains("click_buy_item")) { soundsConfig.set("click_buy_item", "ENTITY_EXPERIENCE_ORB_PICKUP"); }
        if (!soundsConfig.contains("click_exchange_item")) { soundsConfig.set("click_exchange_item", "ENTITY_EXPERIENCE_ORB_PICKUP"); }
        if (!soundsConfig.contains("click_mining_item")) { soundsConfig.set("click_mining_item", "ENTITY_EXPERIENCE_ORB_PICKUP"); }
        if (!soundsConfig.contains("complete_transfer")) { soundsConfig.set("complete_transfer", "ENTITY_PLAYER_LEVELUP"); }
        if (!soundsConfig.contains("complete_exchange")) { soundsConfig.set("complete_exchange", "ENTITY_PLAYER_LEVELUP"); }
        if (!soundsConfig.contains("complete_purchase")) { soundsConfig.set("complete_purchase", "ENTITY_PLAYER_LEVELUP"); }
        if (!soundsConfig.contains("no_economy")) { soundsConfig.set("no_economy", "ENTITY_BAT_TAKEOFF"); }
        if (!soundsConfig.contains("reset_tiles")) { soundsConfig.set("reset_tiles", "ENTITY_GENERIC_EXPLODE"); }
        if (!soundsConfig.contains("click_solve_when_not_solved")) { soundsConfig.set("click_solve_when_not_solved", "ENTITY_BAT_TAKEOFF"); }
        if (!soundsConfig.contains("puzzle_solved")) { soundsConfig.set("puzzle_solved", "ENTITY_PLAYER_LEVELUP"); }
        if (!soundsConfig.contains("exit_mining")) { soundsConfig.set("exit_mining", "ENTITY_BAT_TAKEOFF"); }
        util.saveYml(soundsFile, soundsConfig);
    }

    private void loadAllSounds() {
        for (String sound : soundsConfig.getKeys(false)) {
            sounds.put(sound, Sound.valueOf(soundsConfig.getString(sound)));
        }
    }
}