package net.talhadevv.warptesttp.managers;

import net.talhadevv.warptesttp.utils.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WarpManager {
    private final JavaPlugin plugin;
    private final ConfigUtil configUtil;
    private final Map<UUID, Long> cooldowns;
    private final int cooldownTime = 5000;

    public WarpManager(JavaPlugin plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.cooldowns = new HashMap<>();
    }

    public boolean createWarp(String name, Location location) {
        try {
            configUtil.saveWarp(name, location);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("hata" + e.getMessage());
            return false;
        }
    }

    public Location getWarpLocation(String name) {
        return configUtil.getWarp(name);
    }

    public boolean warpExists(String name) {
        return configUtil.warpExists(name);
    }

    public Set<String> getWarpNames() {
        return configUtil.getWarpNames();
    }

    public boolean deleteWarp(String name) {
        if (!warpExists(name)) {
            return false;
        }
        configUtil.deleteWarp(name);
        return true;
    }

    public boolean hasCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        
        long lastUsed = cooldowns.get(uuid);
        return System.currentTimeMillis() - lastUsed < cooldownTime;
    }

    public long getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }
        
        long lastUsed = cooldowns.get(uuid);
        long timePassed = System.currentTimeMillis() - lastUsed;
        return Math.max(0, cooldownTime - timePassed);
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removeCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }
}
