package thedavid.antiafk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
    public static JavaPlugin instance;
    Map<Player,Integer> playerTime = new HashMap<>();
    Map<Player,Boolean> playerNeedToClick = new HashMap<>();
    Map<Player,Integer> playerRandomCommand = new HashMap<>();
    Map<Player,Integer> playerClickLeftTIme = new HashMap<>();
    public final Integer detectAFKTime = 300;
    public final Integer minPlayer = 20;
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
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        if (Bukkit.getPluginCommand("i'mnot_afk") != null) {
            Bukkit.getPluginCommand("i'mnot_afk").setExecutor(this);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().getOnlinePlayers().forEach(p -> {
//                    玩家在線時間 +1
                    playerTime.putIfAbsent(p,0);
                    playerTime.put(p,playerTime.get(p) + 1);
//                    玩家人數 >= minPlayer && 玩家在線時間 >= detectAFKTime
                    if(playerTime.get(p) >= detectAFKTime && Bukkit.getOnlinePlayers().size() >= minPlayer && !Objects.equals(playerNeedToClick.get(p), true)){
                        //                                    隨機密碼(防止自動打指令)
                        int random = getRandom(0,9999);
//                                    隨機顏色(防止自動偵測顏色)
                        Random rand = new Random();
                        int randomColor = rand.nextInt(colors.size());
                        playerRandomCommand.put(p, random);
                        p.sendMessage(Component.text("                                                               ").decoration(TextDecoration.STRIKETHROUGH, true));
                        p.sendMessage(Component.text("我們偵測到您正在掛機 請於 120 秒內點擊按鈕來證明您還在線").color(NamedTextColor.RED));
//                                    隨機位置(防止直接自動點擊)
                        TextComponent text = Component.text().build();
                        int randomI = getRandom(0,40);
                        for(int i = 0;i<randomI;i++){
                            text = text.append(Component.text(" "));
                        }
                        p.sendMessage(
                                text.append(Component.text("我還在線阿").decoration(TextDecoration.UNDERLINED, true).clickEvent(ClickEvent.runCommand("/i'mnot_afk " + random)).color(colors.get(randomColor)).hoverEvent(HoverEvent.showText(Component.text("點擊我"))))
                        );
                        p.sendMessage(Component.text("                                                               ").decoration(TextDecoration.STRIKETHROUGH, true));
                        playerNeedToClick.put(p, true);
                        playerTime.put(p, detectAFKTime);
                        playerClickLeftTIme.put(p, 90);
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                if(Bukkit.getOnlinePlayers().contains(p) && playerNeedToClick.get(p).equals(true)){
                                    Random rand = new Random();
                                    int randomColor = rand.nextInt(colors.size());
                                    p.sendMessage(Component.text("                                                               ").decoration(TextDecoration.STRIKETHROUGH, true));
                                    p.sendMessage(
                                            Component.text("我們偵測到您正在掛機 請於 ").color(NamedTextColor.RED)
                                                    .append(Component.text(playerClickLeftTIme.get(p)).color(NamedTextColor.RED))
                                                    .append(Component.text(" 秒內點擊按鈕來證明您還在線").color(NamedTextColor.RED))
                                    );
//                                    隨機位置(防止直接自動點擊)
                                    TextComponent text = Component.text().build();
                                    int randomI = getRandom(0,40);
                                    for(int i = 0;i<randomI;i++){
                                        text = text.append(Component.text(" "));
                                    }
                                    p.sendMessage(
                                            text.append(Component.text("我還在線阿").decoration(TextDecoration.UNDERLINED, true).clickEvent(ClickEvent.runCommand("/i'mnot_afk " + playerRandomCommand.get(p))).color(colors.get(randomColor)).hoverEvent(HoverEvent.showText(Component.text("點擊我"))))
                                    );
                                    p.sendMessage(Component.text("                                                               ").decoration(TextDecoration.STRIKETHROUGH, true));
                                    playerClickLeftTIme.put(p, playerClickLeftTIme.get(p) - 30);
                                }else {
                                    playerClickLeftTIme.put(p, null);
                                    cancel();
                                }
                            }
                        }.runTaskTimer(instance, 20*30 , 20*30);
                    }
                    if(Objects.equals(playerNeedToClick.get(p), true) && playerTime.get(p) >= detectAFKTime + 120){
                        p.kick(
                                Component.text("已偵測到掛機")
                                        .appendNewline()
                                        .append(Component.text("因此先將您踢出"))
                        );
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
        if(args.length >= 1){
            try{
//                確認玩家需要驗證 && 密碼正確
                if(playerNeedToClick.get((Player) sender).equals(true) && playerRandomCommand.get((Player) sender).equals(Integer.parseInt(args[0]))){
                    playerTime.put((Player) sender,0);
                    playerNeedToClick.put((Player) sender, false);
                    playerRandomCommand.remove((Player) sender);
                    sender.sendMessage(
                            Component.text("已成功驗證").color(NamedTextColor.GREEN)
                    );
                }else{
                    sender.sendMessage(
                            Component.text("驗證失敗").color(NamedTextColor.RED)
                    );
                }
            } catch (Exception ex){
                sender.sendMessage(
                        Component.text("驗證失敗").color(NamedTextColor.RED)
                );
            }

        }
        return true;
    }
}
