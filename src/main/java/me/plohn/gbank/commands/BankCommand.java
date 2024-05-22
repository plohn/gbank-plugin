package me.plohn.gbank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.github.johnnypixelz.utilizer.config.Messages;
import me.plohn.gbank.GBankCurrency;
import me.plohn.gbank.GBankManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("bank")
public class BankCommand extends BaseCommand {
    private String bankPermission;
    private String reloadPermission;

    public BankCommand(String bankPermission, String reloadPermission) {
        this.bankPermission = bankPermission;
        this.reloadPermission = reloadPermission;
    }

    @Subcommand("give")
    @CommandCompletion("@currencies 1 @players")
    @Syntax("<currency> <amount> <player>")
    public boolean onBankGive(Player sender, String currencyName, double amount, OfflinePlayer receiver) {

        if (!sender.hasPermission(bankPermission)) {
            Messages.cfg("language", "general.no_permission").send(sender);
            return false;
        }

        Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currencyName);
        if (serverCurrency.isEmpty()) {
            Messages.cfg("language", "general.invalid_currency").map("%currency%", currencyName).send(sender);
            return false;
        }
        GBankCurrency currency = serverCurrency.get();

        if (amount <= 0) {
            Messages.cfg("language", "general.invalid_amount").send(sender);
            return false;
        }

        double playerBalance = GBankManager.getPlayerBalance(receiver, currency);
        GBankManager.updatePlayerBalance(receiver, currency, playerBalance + amount);
        if (receiver.getPlayer() == null) {
            Messages.cfg("language", "bank.offline_notification").map("%player%", receiver.getName()).send(sender);
            return true;
        }
        Messages.cfg("language", "bank.give_success").map("%prefix%", currency.getPrefix()).map("%player%", receiver.getName()).map("%amount%", String.valueOf(amount)).map("%currency%", currency.getName()).send(sender);
        return true;
    }

    @Subcommand("take")
    @CommandCompletion("@currencies 1 @players")
    @Syntax("<currency> <amount> <player>")
    public boolean onBankTake(Player sender, String currencyName, double amount, OfflinePlayer receiver) {

        if (!sender.hasPermission(bankPermission)) {
            Messages.cfg("language", "general.no_permission").send(sender);
            return false;
        }

        Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currencyName);
        if (serverCurrency.isEmpty()) {
            Messages.cfg("language", "general.invalid_currency").map("%currency%", currencyName).send(sender);
            return false;
        }
        GBankCurrency currency = serverCurrency.get();

        if (amount <= 0) {
            Messages.cfg("language", "general.invalid_amount").send(sender);
            return false;
        }

        double playerBalance = GBankManager.getPlayerBalance(receiver, currency);
        if (playerBalance - amount < 0) {
            Messages.cfg("language", "general.not_enough_amount").map("%player%", receiver.getName()).map("%currency%", currency.getName()).send(sender);

            return false;
        }
        GBankManager.updatePlayerBalance(receiver, currency, playerBalance - amount);
        if (receiver.getPlayer() == null) {
            Messages.cfg("language", "bank.offline_notification").map("%player%", receiver.getName()).send(sender);
            return true;
        }
        Messages.cfg("language", "general.take_success").map("%prefix%", currency.getPrefix()).map("%player%", receiver.getName()).map("%amount%", String.valueOf(amount)).map("%currency%", currency.getName()).send(receiver.getPlayer());
        return false;
    }

    @Subcommand("set")
    @CommandCompletion("@currencies 1 @players")
    @Syntax("<currency> <amount> <player>")
    public boolean onBankSet(Player sender, String currencyName, double amount, OfflinePlayer receiver) {

        if (!sender.hasPermission(bankPermission)) {
            Messages.cfg("language", "general.no_permission").send(sender);
            return false;
        }

        Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currencyName);
        if (serverCurrency.isEmpty()) {
            Messages.cfg("language", "general.invalid_currency")
                    .map("%currency%", currencyName).send(sender);
            return false;
        }
        GBankCurrency currency = serverCurrency.get();

        if (amount < 0) {
            Messages.cfg("language", "general.invalid_amount").send(sender);
            return false;
        }

        GBankManager.updatePlayerBalance(receiver, currency, amount);
        if (receiver.getPlayer() == null) {
            Messages.cfg("language", "bank.offline_notification").map("%player%", receiver.getName()).send(sender);
            return true;
        }
        Messages.cfg("language", "bank.set_success")
                .map("%player%", receiver.getName())
                .map("%prefix%", currency.getPrefix())
                .map("%amount%", String.valueOf(amount))
                .map("%currency%", currency.getName()).send(sender);
        return true;
    }

    @Subcommand("reload")
    @Description("Reloads GBankPlugin plugin")
    public boolean onBankReload(Player sender) {

        if (!sender.hasPermission(reloadPermission)) {
            Messages.cfg("language", "general.no_permission").send(sender);
            return false;
        }
        //TODO
        return false;
    }
}
