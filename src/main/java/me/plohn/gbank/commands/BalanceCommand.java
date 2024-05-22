package me.plohn.gbank.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.github.johnnypixelz.utilizer.config.Messages;
import me.plohn.gbank.GBankCurrency;
import me.plohn.gbank.GBankManager;
import me.plohn.gbank.gui.PlayerBalanceProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("balance")
public class BalanceCommand extends BaseCommand {
    private String balancePermission;

    public BalanceCommand(String balancePermission) {
        this.balancePermission = balancePermission;
    }

    @Default
    @CommandCompletion("@currencies @players")
    @Syntax("<currency> [player]")
    public boolean onBalanceCommand(Player sender, @Optional String currencyName, @Optional OfflinePlayer player) {

        if (!sender.hasPermission(balancePermission)) {
            Messages.cfg("language", "general.no_permission")
                    .send(sender);
            return false;
        }

        if (currencyName == null) {
            // View all balances on server currencies of the sender
            new PlayerBalanceProvider(GBankManager.getPlayerProfile(sender),0).open(sender,0);
            return true;
        }

        java.util.Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currencyName);
        if (serverCurrency.isEmpty()) {

            Messages.cfg("language", "general.invalid_currency")
                    .map("%currency%", currencyName)
                    .send(sender);

            return false;
        }

        GBankCurrency currency = serverCurrency.get();
        if (player == null) {
            // Views sender's balance of a "currency"
            double playerBalance = GBankManager.getPlayerBalance(sender, currency);
            Messages.cfg("language", "balance.view_balance")
                    .map("%prefix%", currency.getPrefix())
                    .map("%amount%", String.valueOf(playerBalance))
                    .map("%currency%", currency.getName())
                    .send(sender);

            return true;
        }
        // Views receiver's balance of a "currency"
        double playerBalance = GBankManager.getPlayerBalance(player, currency);
        Messages.cfg("language", "balance.player_balance")
                .map("%prefix%", currency.getPrefix())
                .map("%amount%", String.valueOf(playerBalance))
                .map("%currency%", currency.getName())
                .map("%player%", player.getName())
                .send(sender);
        return true;
    }
}
