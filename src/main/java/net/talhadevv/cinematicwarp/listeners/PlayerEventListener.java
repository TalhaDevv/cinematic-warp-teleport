package net.talhadevv.cinematicwarp.listeners;

import net.talhadevv.cinematicwarp.managers.AnimationManager;
import net.talhadevv.cinematicwarp.managers.WarpManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerEventListener implements Listener {
    private final AnimationManager animationManager;
    private final WarpManager warpManager;

    public PlayerEventListener(AnimationManager animationManager, WarpManager warpManager) {
        this.animationManager = animationManager;
        this.warpManager = warpManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (animationManager.isInAnimation(event.getPlayer())) {
            animationManager.cancelAnimation(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            if (animationManager.isInAnimation(player)) {
                event.setCancelled(true);
                return;
            }
            
            if (player.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true);
                player.setHealth(0.5);
                
                if (warpManager.warpExists("hastane")) {
                    Location hospitalLocation = warpManager.getWarpLocation("hastane");
                    if (hospitalLocation != null) {
                        animationManager.startDeathRespawnAnimation(player, hospitalLocation);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }
        
        if (animationManager.isInAnimation(event.getPlayer())) {
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN) {
                event.setCancelled(true);
            }
        }
    }
}
