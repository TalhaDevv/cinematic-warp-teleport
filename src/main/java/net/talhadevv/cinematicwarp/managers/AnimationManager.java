package net.talhadevv.cinematicwarp.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimationManager {
    private final JavaPlugin plugin;
    private final Map<UUID, BukkitTask> activeAnimations;
    private final Map<UUID, GameMode> originalGameModes;

    public AnimationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeAnimations = new HashMap<>();
        this.originalGameModes = new HashMap<>();
    }

    public void startDeathRespawnAnimation(Player player, Location hospitalLocation) {
        UUID playerUUID = player.getUniqueId();
        
        if (activeAnimations.containsKey(playerUUID)) {
            return;
        }

        originalGameModes.put(playerUUID, player.getGameMode());
        
        player.sendTitle(
            ChatColor.RED + "WASTED", 
            "", 
            10,  
            40,  
            20   
        );
        
        BukkitTask deathAnimationTask = new BukkitRunnable() {
            private int stage = 0;
            private int ticks = 0;
            private final Location deathLocation = player.getLocation().clone();
            private final Location skyLocation = deathLocation.clone().add(0, 80, 0);
            private final Location hospitalSkyLocation = hospitalLocation.clone().add(0, 80, 0);
            
            @Override
            public void run() {
                ticks++;
                
                switch (stage) {
                    case 0: 
                        handleWastedAndRise(player);
                        if (ticks >= 70) { stage = 1; ticks = 0; }
                        break;
                    case 1: 
                        handleTimeSkip(player);
                        if (ticks >= 40) { stage = 2; ticks = 0; }
                        break;
                    case 2: 
                        handleHospitalTravel(player);
                        if (ticks >= 60) { stage = 3; ticks = 0; }
                        break;
                    case 3: 
                        handleHospitalDescent(player, hospitalLocation);
                        if (ticks >= 60) { stage = 4; ticks = 0; }
                        break;
                    case 4:
                        finishDeathAnimation(player, hospitalLocation);
                        this.cancel();
                        return;
                }
            }
            
            private void handleWastedAndRise(Player player) {
                if (ticks == 1) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 400, 1, true, false));
                    player.setGameMode(GameMode.SPECTATOR);
                    spawnDeathParticles(deathLocation);
                }
                
                if (ticks > 50) { 
                    double progress = (ticks - 50) / 20.0;
                    double easedProgress = easeOutQuart(Math.min(1.0, progress));
                    
                    Location currentPos = interpolateLocation(deathLocation, skyLocation, easedProgress);
                    currentPos.setPitch(90.0f);
                    player.teleport(currentPos);
                }
            }
            
            private void handleTimeSkip(Player player) {
                if (ticks == 1) {
                    long currentTime = player.getWorld().getTime();
                    long newTime = (currentTime + 8000) % 24000;
                    player.getWorld().setTime(newTime);
                    
                    player.sendMessage(ChatColor.GOLD + "8hours later");
                    spawnTimeSkipParticles(skyLocation);
                }
            }
            
            private void handleHospitalTravel(Player player) {
                double progress = (double) ticks / 60.0;
                double easedProgress = easeInOutCubic(progress);
                
                Location currentPos = interpolateLocation(skyLocation, hospitalSkyLocation, easedProgress);
                currentPos.setPitch(90.0f);
                player.teleport(currentPos);
                
                if (ticks % 8 == 0) {
                    spawnTravelParticles(currentPos);
                }
            }
            
            private void handleHospitalDescent(Player player, Location target) {
                double progress = (double) ticks / 60.0;
                double easedProgress = easeOutQuart(progress);
                
                Location currentPos = interpolateLocation(hospitalSkyLocation, target, easedProgress);
                
                float currentPitch = (float) (90.0f * (1.0 - progress));
                currentPos.setPitch(currentPitch);
                player.teleport(currentPos);
            }
            
            private void finishDeathAnimation(Player player, Location target) {
                player.teleport(target);
                
                GameMode originalMode = originalGameModes.get(player.getUniqueId());
                if (originalMode != null && originalMode != GameMode.SPECTATOR) {
                    player.setGameMode(originalMode);
                    originalGameModes.remove(player.getUniqueId());
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                }
                
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.setHealth(player.getMaxHealth());
                
                spawnHospitalParticles(player);
                
                player.sendMessage(ChatColor.GREEN + "Welcome Hospital");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                
                activeAnimations.remove(player.getUniqueId());
            }
            
        }.runTaskTimer(plugin, 0L, 1L);
        
        activeAnimations.put(playerUUID, deathAnimationTask);
    }

    public void startCinematicTeleport(Player player, Location targetLocation) {
        UUID playerUUID = player.getUniqueId();
        
        if (activeAnimations.containsKey(playerUUID)) {
            return;
        }

        originalGameModes.put(playerUUID, player.getGameMode());
        
        BukkitTask animationTask = new BukkitRunnable() {
            private int stage = 0;
            private int tick = 0;
            private Location originalLocation = player.getLocation().clone();
            private Location startLocation = player.getLocation().clone();
            private Location cameraLocation = startLocation.clone().add(0, 50, 0);
            private Location targetCameraLocation = targetLocation.clone().add(0, 50, 0);
            
            @Override
            public void run() {
                tick++;
                
                switch (stage) {
                    case 0:
                        handlePrepStage(player);
                        break;
                    case 1:
                        handleCameraRiseStage(player);
                        break;
                    case 2:
                        handleCinematicMovementStage(player);
                        break;
                    case 3:
                        handleDescentStage(player, targetLocation);
                        break;
                    case 4:
                        finishAnimation(player, targetLocation);
                        this.cancel();
                        return;
                }
            }
            
            private void handlePrepStage(Player player) {
                if (tick == 1) {
                    player.setWalkSpeed(0f);
                    player.setFlySpeed(0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 255, true, false));
                    
                    spawnFadeOutParticles(player);
                }
                
                if (tick >= 10) {
                    stage = 1;
                    tick = 0;
                }
            }
            
            private void handleCameraRiseStage(Player player) {
                if (tick == 1) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1, true, false));
                    player.setGameMode(GameMode.SPECTATOR);
                }
                
                double progress = Math.min(1.0, tick / 20.0);
                double smoothProgress = smoothStep(progress);
                
                Location currentCameraPos = interpolateLocation(startLocation, cameraLocation, smoothProgress);
                currentCameraPos.setPitch(90.0f);
                
                player.teleport(currentCameraPos);
                
                if (tick >= 20) {
                    stage = 2;
                    tick = 0;
                }
            }
            
            private void handleCinematicMovementStage(Player player) {
                double progress = Math.min(1.0, tick / 30.0);
                
                double acceleratedProgress = accelerationCurve(progress);
                
                Location currentPos = interpolateLocation(cameraLocation, targetCameraLocation, acceleratedProgress);
                currentPos.setPitch(90.0f);
                
                player.teleport(currentPos);
                
                if (tick % 3 == 0) {
                    spawnTravelParticles(currentPos);
                }
                
                if (tick >= 30) {
                    stage = 3;
                    tick = 0;
                }
            }
            
            private void handleDescentStage(Player player, Location target) {
                double progress = Math.min(1.0, tick / 20.0);
                double smoothProgress = smoothStep(progress);
                
                Location descentStart = targetCameraLocation;
                Location descentEnd = target.clone();
                
                Location currentPos = interpolateLocation(descentStart, descentEnd, smoothProgress);
                
                double pitchProgress = progress;
                float currentPitch = (float) (90.0f * (1.0 - pitchProgress));
                currentPos.setPitch(currentPitch);
                
                player.teleport(currentPos);
                
                if (tick >= 20) {
                    stage = 4;
                    tick = 0;
                }
            }
            
            private void finishAnimation(Player player, Location target) {
                player.teleport(target);
                
                GameMode originalMode = originalGameModes.get(player.getUniqueId());
                if (originalMode != null) {
                    player.setGameMode(originalMode);
                    originalGameModes.remove(player.getUniqueId());
                }
                
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                
                spawnFadeInParticles(player);
                
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                
                activeAnimations.remove(player.getUniqueId());
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        activeAnimations.put(playerUUID, animationTask);
    }
    
    private void spawnFadeOutParticles(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i < 30; i++) {
            double offsetX = (Math.random() - 0.5) * 3;
            double offsetY = Math.random() * 3;
            double offsetZ = (Math.random() - 0.5) * 3;
            
            loc.getWorld().spawnParticle(Particle.SQUID_INK, 
                loc.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0);
        }
    }
    
    private void spawnFadeInParticles(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 2;
            
            loc.getWorld().spawnParticle(Particle.PORTAL, 
                loc.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0.1);
        }
    }
    
    private void spawnTravelParticles(Location location) {
        location.getWorld().spawnParticle(Particle.CLOUD, location, 5, 1, 1, 1, 0.05);
    }
    
    private void spawnDeathParticles(Location location) {
        for (int i = 0; i < 50; i++) {
            double offsetX = (Math.random() - 0.5) * 5;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 5;
            
            location.getWorld().spawnParticle(Particle.SOUL, 
                location.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0.5, 0, 0.02);
        }
        
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 3;
            double offsetY = Math.random() * 1;
            double offsetZ = (Math.random() - 0.5) * 3;
            
            location.getWorld().spawnParticle(Particle.SMOKE, 
                location.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0.01);
        }
    }
    
    private void spawnTimeSkipParticles(Location location) {
        for (int i = 0; i < 30; i++) {
            double offsetX = (Math.random() - 0.5) * 8;
            double offsetY = (Math.random() - 0.5) * 8;
            double offsetZ = (Math.random() - 0.5) * 8;
            
            location.getWorld().spawnParticle(Particle.END_ROD, 
                location.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0.1);
        }
        
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 10;
            double offsetY = (Math.random() - 0.5) * 10;
            double offsetZ = (Math.random() - 0.5) * 10;
            
            location.getWorld().spawnParticle(Particle.ENCHANT, 
                location.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 1);
        }
    }
    
    private void spawnHospitalParticles(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i < 25; i++) {
            double offsetX = (Math.random() - 0.5) * 3;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 3;
            
            loc.getWorld().spawnParticle(Particle.HEART, 
                loc.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0);
        }
        
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 2;
            
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, 
                loc.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0);
        }
    }
    
    private Location interpolateLocation(Location start, Location end, double progress) {
        double x = start.getX() + (end.getX() - start.getX()) * progress;
        double y = start.getY() + (end.getY() - start.getY()) * progress;
        double z = start.getZ() + (end.getZ() - start.getZ()) * progress;
        
        float yaw = (float) (start.getYaw() + (end.getYaw() - start.getYaw()) * progress);
        float pitch = (float) (start.getPitch() + (end.getPitch() - start.getPitch()) * progress);
        
        return new Location(start.getWorld(), x, y, z, yaw, pitch);
    }
    
    private double smoothStep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }
    
    private double accelerationCurve(double t) {
        if (t < 0.3) {
            return t * t * 2.0;
        } else if (t < 0.7) {
            double adjustedT = (t - 0.3) / 0.4;
            return 0.18 + adjustedT * 0.64;
        } else {
            double adjustedT = (t - 0.7) / 0.3;
            return 0.82 + adjustedT * adjustedT * 0.18;
        }
    }
    
    private double easeOutQuart(double t) {
        return 1 - Math.pow(1 - t, 4);
    }

    private double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }
    
    public void cancelAnimation(Player player) {
        UUID playerUUID = player.getUniqueId();
        BukkitTask task = activeAnimations.get(playerUUID);
        
        if (task != null) {
            task.cancel();
            activeAnimations.remove(playerUUID);
            
            GameMode originalMode = originalGameModes.get(playerUUID);
            if (originalMode != null) {
                player.setGameMode(originalMode);
                originalGameModes.remove(playerUUID);
            }
            
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
        }
    }
    
    public boolean isInAnimation(Player player) {
        return activeAnimations.containsKey(player.getUniqueId());
    }
    
    public void cancelAllAnimations() {
        for (Map.Entry<UUID, BukkitTask> entry : activeAnimations.entrySet()) {
            entry.getValue().cancel();
            
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                GameMode originalMode = originalGameModes.get(entry.getKey());
                if (originalMode != null) {
                    player.setGameMode(originalMode);
                }
                
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
            }
        }
        
        activeAnimations.clear();
        originalGameModes.clear();
    }
}
