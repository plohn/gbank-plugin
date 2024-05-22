package me.plohn.gbank.listeners;

import me.plohn.gbank.GBankNotificationsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GBankListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        GBankNotificationsManager.sendOnJoinNotifications(player);
    }
}
