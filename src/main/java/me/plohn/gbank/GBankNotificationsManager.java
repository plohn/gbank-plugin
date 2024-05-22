package me.plohn.gbank;

import io.github.johnnypixelz.utilizer.config.Messages;
import io.github.johnnypixelz.utilizer.tasks.Tasks;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class GBankNotificationsManager {
    private final static HashMap<UUID, ArrayList<String>> notifications = new HashMap<>();

    public static void addNotification(OfflinePlayer player, String message) {
        ArrayList<String> messages = notifications.computeIfAbsent(player.getUniqueId(), uuid -> {
                    ArrayList<String> notifications = new ArrayList<>();
                    return notifications;
                }
        );
        messages.add(message);
    }

    public static void sendOnJoinNotifications(Player player) {
        if (notifications.containsKey(player.getUniqueId())) {
            ArrayList<String> messages = notifications.get(player.getUniqueId());
            messages.forEach(msg -> {
                //Delay bc of other plugins spamming messages so the player will see bank/pay notifications
                Tasks.sync().delayed(() -> {
                    Messages.send(player, msg);
                }, 20);
            });
            notifications.remove(player.getUniqueId());
        }
    }
}
