package net.talhadevv.warptesttp.commands;

import net.talhadevv.warptesttp.managers.AnimationManager;
import net.talhadevv.warptesttp.managers.WarpManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final WarpManager warpManager;
    private final AnimationManager animationManager;

    public WarpCommand(WarpManager warpManager, AnimationManager animationManager) {
        this.warpManager = warpManager;
        this.animationManager = animationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "bu komutu oyuncular kullanabilir");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("warptesttp.use")) {
            player.sendMessage(ChatColor.RED + "bu komutu kullanmak için yetkiniz yok");
            return true;
        }

        if (args.length == 0) {
            sendWarpList(player);
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Kullanım: /warp <isim>");
            player.sendMessage(ChatColor.YELLOW + "Mevcut warpları görmek için: /warp");
            return true;
        }

        String warpName = args[0];

        if (animationManager.isInAnimation(player)) {
            player.sendMessage(ChatColor.RED + "warpa ışınlanıyorsun");
            return true;
        }

        if (warpManager.hasCooldown(player)) {
            long remainingTime = warpManager.getRemainingCooldown(player);
            int seconds = (int) (remainingTime / 1000) + 1;
            player.sendMessage(ChatColor.RED + "komutu kullanmak için " + seconds + " saniyye beklemelisin");
            return true;
        }

        if (!warpManager.warpExists(warpName)) {
            player.sendMessage(ChatColor.RED + "'" + warpName + "' warp bulunamadı");
            player.sendMessage(ChatColor.YELLOW + "warpları görmek için /warp");
            return true;
        }

        Location warpLocation = warpManager.getWarpLocation(warpName);
        if (warpLocation == null || warpLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "warp bulunamadı error1");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "'" + warpName + "' warpına gidiyorsun");
        
        warpManager.setCooldown(player);
        
        animationManager.startCinematicTeleport(player, warpLocation);

        return true;
    }

    private void sendWarpList(Player player) {
        var warpNames = warpManager.getWarpNames();
        
        if (warpNames.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "herhangi warp yok");
            player.sendMessage(ChatColor.GRAY + "warp oluşturmak için: /setwarp <isim>");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Warplar ===");
        
        StringBuilder warpList = new StringBuilder();
        warpList.append(ChatColor.GREEN);
        
        List<String> sortedWarps = new ArrayList<>(warpNames);
        sortedWarps.sort(String::compareToIgnoreCase);
        
        for (int i = 0; i < sortedWarps.size(); i++) {
            if (i > 0) {
                warpList.append(ChatColor.GRAY + ", " + ChatColor.GREEN);
            }
            warpList.append(sortedWarps.get(i));
        }
        
        player.sendMessage(warpList.toString());
        player.sendMessage(ChatColor.GRAY + "Kullanım: /warp <isim>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender instanceof Player && sender.hasPermission("warptesttp.use")) {
                String input = args[0].toLowerCase();
                
                for (String warpName : warpManager.getWarpNames()) {
                    if (warpName.toLowerCase().startsWith(input)) {
                        completions.add(warpName);
                    }
                }
            }
        }

        return completions;
    }
}
