package net.talhadevv.cinematicwarp.commands;

import net.talhadevv.cinematicwarp.managers.WarpManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SetWarpCommand implements CommandExecutor, TabCompleter {
    private final WarpManager warpManager;
    private final JavaPlugin plugin;

    public SetWarpCommand(WarpManager warpManager, JavaPlugin plugin) {
        this.warpManager = warpManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "bu komutu oyuncular kullanabilir");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("warptesttp.set")) {
            player.sendMessage(ChatColor.RED + "bu komutu kullanmak için yetkin yok");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Kullanım: /setwarp <isim>");
            return true;
        }

        String warpName = args[0];

        if (warpName.length() > 16) {
            player.sendMessage(ChatColor.RED + "warp ismi 16 karakterden uzun olmamalı");
            return true;
        }

        if (!warpName.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage(ChatColor.RED + "warp ismi harf&rakam&altçizgi");
            return true;
        }

        if (warpManager.warpExists(warpName)) {
            player.sendMessage(ChatColor.YELLOW + "'" + warpName + "' warpı zaten mevcut, tekrar oluşturmak için komudu tekrar yazın");
            if (player.hasMetadata("confirmOverwrite") && 
                player.getMetadata("confirmOverwrite").get(0).asString().equals(warpName)) {
                
                player.removeMetadata("confirmOverwrite", plugin);
            } else {
                player.setMetadata("confirmOverwrite", new org.bukkit.metadata.FixedMetadataValue(plugin, warpName));
                return true;
            }
        }

        boolean success = warpManager.createWarp(warpName, player.getLocation());

        if (success) {
            player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' oluşturuldu");
            player.sendMessage(ChatColor.GRAY + "Konum: " + 
                String.format("%.1f, %.1f, %.1f", 
                    player.getLocation().getX(), 
                    player.getLocation().getY(), 
                    player.getLocation().getZ()));
        } else {
            player.sendMessage(ChatColor.RED + "warp oluştururken hata");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender instanceof Player && sender.hasPermission("warptesttp.set")) {
                String input = args[0].toLowerCase();
                
                for (String warpName : warpManager.getWarpNames()) {
                    if (warpName.toLowerCase().startsWith(input)) {
                        completions.add(warpName);
                    }
                }
                
                if (completions.isEmpty() && input.length() > 0) {
                    completions.add(args[0]);
                }
            }
        }

        return completions;
    }
}
