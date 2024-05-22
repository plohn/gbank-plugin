package me.plohn.gbank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import io.github.johnnypixelz.utilizer.config.Messages;
import me.plohn.gbank.GBankCurrency;
import me.plohn.gbank.GBankManager;
import me.plohn.gbank.GBankNotificationsManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("pay")
public class PayCommand extends BaseCommand {
    private String payCommandPermission;

    public PayCommand(String payCommandPermission){
        this.payCommandPermission = payCommandPermission;
    }
    @Default
    @CommandCompletion("@currencies @players 1")
    @Syntax("<currency> <player> <amount>")
    public void onPayCommand(Player sender, String currencyName, OfflinePlayer receiver, double amount) {

        if (!sender.hasPermission(payCommandPermission)){
            Messages.cfg("language","general.no_permission")
                    .send(sender);
            return;
        }

        Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currencyName);
        if (serverCurrency.isEmpty()) {
            Messages.cfg("language", "general.invalid_currency")
                    .map("%currency%", currencyName)
                    .send(sender);
            return;
        }
        GBankCurrency currency = serverCurrency.get();

        if (amount <= 0) {
            Messages.cfg("language", "general.invalid_amount")
                    .send(sender);
            return;
        }

        if (sender.equals(receiver.getPlayer())){
            Messages.cfg("language","general.invalid_player")
                    .send(sender);
            return;
        }

        if (GBankManager.getPlayerBalance(sender, currency) < amount) {
            Messages.cfg("language", "pay.insufficient_funds")
                    .map("%currency%", currency.getName())
                    .send(sender);
            return;
        }

        double senderAmount = GBankManager.getPlayerBalance(sender, currency);
        GBankManager.updatePlayerBalance(sender, currency, senderAmount - amount);

        if (receiver.getName() ==  null) {
            return;
        }

        Messages.cfg("language", "pay.payment_success")
                .map("%prefix%", currency.getPrefix())
                .map("%player%", receiver.getName())
                .map("%amount%", String.valueOf(amount))
                .map("%currency%", currency.getName())
                .send(sender);

        double receiverAmount = GBankManager.getPlayerBalance(receiver, currency);
        GBankManager.updatePlayerBalance(receiver, currency, receiverAmount + amount);
        if (!receiver.isOnline()) {

            Messages.cfg("language","pay.offline_notification")
                    .map("%player%",receiver.getName())
                    .send(sender);

            String receiverMessage = Messages.cfg("language", "pay.payment_received")
                    .map("%prefix%", currency.getPrefix())
                    .map("%sender%", sender.getName())
                    .map("%amount%", String.valueOf(amount))
                    .map("%currency%", currency.getName())
                    .getMessage();

            GBankNotificationsManager.addNotification(receiver,receiverMessage);
            return;
        }
        Messages.cfg("language", "pay.payment_received")
                .map("%prefix%", currency.getPrefix())
                .map("%sender%", receiver.getName())
                .map("%amount%", String.valueOf(amount))
                .map("%currency%", currency.getName())
                .send(receiver.getPlayer());
    }
}
