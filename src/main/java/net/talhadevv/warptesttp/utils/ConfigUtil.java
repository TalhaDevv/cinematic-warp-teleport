package net.talhadevv.warptesttp.utils;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigUtil {
    private final JavaPlugin plugin;
    private File warpsFile;
    private FileConfiguration warpsConfig;

    public ConfigUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        setupWarpsConfig();
    }

    private void setupWarpsConfig() {
        warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            warpsFile.getParentFile().mkdirs();
            try {
                warpsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("warps.yml oluşturulamadı: " + e.getMessage());
            }
        }
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    public void saveWarp(String name, Location location) {
        String path = "warps." + name;
        warpsConfig.set(path + ".world", location.getWorld().getName());
        warpsConfig.set(path + ".x", location.getX());
        warpsConfig.set(path + ".y", location.getY());
        warpsConfig.set(path + ".z", location.getZ());
        warpsConfig.set(path + ".yaw", location.getYaw());
        warpsConfig.set(path + ".pitch", location.getPitch());
        saveWarpsConfig();
    }

    public Location getWarp(String name) {
        String path = "warps." + name;
        if (!warpsConfig.contains(path)) {
            return null;
        }

        String worldName = warpsConfig.getString(path + ".world");
        double x = warpsConfig.getDouble(path + ".x");
        double y = warpsConfig.getDouble(path + ".y");
        double z = warpsConfig.getDouble(path + ".z");
        float yaw = (float) warpsConfig.getDouble(path + ".yaw");
        float pitch = (float) warpsConfig.getDouble(path + ".pitch");

        return new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
    }

    public Set<String> getWarpNames() {
        if (!warpsConfig.contains("warps")) {
            return Set.of();
        }
        return warpsConfig.getConfigurationSection("warps").getKeys(false);
    }

    public boolean warpExists(String name) {
        return warpsConfig.contains("warps." + name);
    }

    public void deleteWarp(String name) {
        warpsConfig.set("warps." + name, null);
        saveWarpsConfig();
    }

    private void saveWarpsConfig() {
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("warps.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public void reloadWarpsConfig() {
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }
}
