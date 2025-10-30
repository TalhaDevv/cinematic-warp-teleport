package net.talhadevv.warptesttp;

import net.talhadevv.warptesttp.commands.SetWarpCommand;
import net.talhadevv.warptesttp.commands.WarpCommand;
import net.talhadevv.warptesttp.listeners.PlayerEventListener;
import net.talhadevv.warptesttp.managers.AnimationManager;
import net.talhadevv.warptesttp.managers.WarpManager;
import net.talhadevv.warptesttp.utils.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class Warptesttp extends JavaPlugin {
    
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
