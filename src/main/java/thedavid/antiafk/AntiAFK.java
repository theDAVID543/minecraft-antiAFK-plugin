package thedavid.antiafk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class AntiAFK extends JavaPlugin implements Listener, CommandExecutor {
    Map<Player,Integer> playerTime = new HashMap<>();
    Map<Player,Boolean> playerNeedToClick = new HashMap<>();
    Map<Player,Integer> playerRandomCommand = new HashMap<>();
    List<NamedTextColor> colors = Arrays.asList(
            NamedTextColor.AQUA,
            NamedTextColor.BLACK,
            NamedTextColor.BLUE,
            NamedTextColor.DARK_BLUE,
            NamedTextColor.GREEN,
            NamedTextColor.GOLD,
            NamedTextColor.GRAY,
            NamedTextColor.LIGHT_PURPLE,
            NamedTextColor.RED,
            NamedTextColor.WHITE,
            NamedTextColor.YELLOW
    );

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        if (Bukkit.getPluginCommand("i'mnot_afk") != null) {
            Bukkit.getPluginCommand("i'mnot_afk").setExecutor(this);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().getOnlinePlayers().forEach(p -> {
                    playerTime.putIfAbsent(p,0);
                    playerTime.put(p,playerTime.get(p) + 1);
                    if(playerTime.get(p) >= 3 && Bukkit.getOnlinePlayers().size() > 1 && !Objects.equals(playerNeedToClick.get(p), true)){
                        int random = getRandom(0,1000);
                        Random rand = new Random();
                        int randomColor = rand.nextInt(colors.size());
                        playerRandomCommand.put(p, random);
                        p.sendMessage(Component.text("---------------------"));
                        p.sendMessage(Component.text("我們偵測到您正在掛機 請於2分鐘內點擊按鈕來證明您還在線").color(NamedTextColor.RED));
                        TextComponent text = Component.text().build();
                        int randomI = getRandom(0,40);
                        for(int i = 0;i<randomI;i++){
                            text = text.append(Component.text(" "));
                        }
                        p.sendMessage(
                                text.append(Component.text("我還在線阿").decoration(TextDecoration.UNDERLINED, true).clickEvent(ClickEvent.runCommand("/i'mnot_afk " + random)).color(colors.get(randomColor)))
                        );
                        p.sendMessage(Component.text("---------------------"));
                        playerNeedToClick.put(p, true);
                    }
                    if(Objects.equals(playerNeedToClick.get(p), true) && playerTime.get(p) >= 30){
                        p.kick();
                    }
                });
            }
        }.runTaskTimer(this,0,20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public int getRandom(int lower, int upper) {
        Random random = new Random();
        return random.nextInt((upper - lower) + 1) + lower;
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        if(!Objects.equals(playerTime.get(e.getPlayer()),null)){
            playerTime.remove(e.getPlayer());
        }
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(playerNeedToClick.get((Player) sender).equals(true) && playerRandomCommand.get((Player) sender).equals(Integer.parseInt(args[0]))){
            playerTime.put((Player) sender,0);
            playerNeedToClick.put((Player) sender, false);
            playerRandomCommand.remove((Player) sender);
            sender.sendMessage(
                    Component.text("已成功驗證").color(NamedTextColor.GREEN)
            );
        }
        return true;
    }
}