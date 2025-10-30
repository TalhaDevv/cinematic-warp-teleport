package net.talhadevv.cinematicwarp;

import net.talhadevv.cinematicwarp.commands.SetWarpCommand;
import net.talhadevv.cinematicwarp.commands.WarpCommand;
import net.talhadevv.cinematicwarp.listeners.PlayerEventListener;
import net.talhadevv.cinematicwarp.managers.AnimationManager;
import net.talhadevv.cinematicwarp.managers.WarpManager;
import net.talhadevv.cinematicwarp.utils.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class CinematicWarp extends JavaPlugin {
    
    private ConfigUtil configUtil;
    private WarpManager warpManager;
    private AnimationManager animationManager;

    @Override
    public void onEnable() {
        getLogger().info("CinematicWarp başlıyor");
        
        try {
            setupConfig();
            setupManagers();
            setupCommands();
            setupListeners();
            
            getLogger().info("CinematicWarp yüklendi");
            getLogger().info("- cinematic warp aktif");
            getLogger().info("- Komutlar: /setwarp, /warp");
            getLogger().info("- cooldown süresi 5 saniye");
            
        } catch (Exception e) {
            getLogger().severe("plugin yüklenemedi " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupConfig() {
        getDataFolder().mkdirs();
        saveDefaultConfig();
        configUtil = new ConfigUtil(this);
    }

    private void setupManagers() {
        warpManager = new WarpManager(this, configUtil);
        animationManager = new AnimationManager(this);
    }

    private void setupCommands() {
        getCommand("setwarp").setExecutor(new SetWarpCommand(warpManager, this));
        getCommand("setwarp").setTabCompleter(new SetWarpCommand(warpManager, this));
        
        getCommand("warp").setExecutor(new WarpCommand(warpManager, animationManager));
        getCommand("warp").setTabCompleter(new WarpCommand(warpManager, animationManager));
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerEventListener(animationManager, warpManager), this);
    }

    @Override
    public void onDisable() {
        if (animationManager != null) {
            animationManager.cancelAllAnimations();
        }
        
        getLogger().info("CinematicWarp kapandı");
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }
}
