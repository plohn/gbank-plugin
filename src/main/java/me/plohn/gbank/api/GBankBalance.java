package me.plohn.gbank.api;

import me.plohn.gbank.GBankCurrency;
import me.plohn.gbank.GBankManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.UUID;

public class GBankBalance implements GBankAPI {
    @Override
    public double getBalance(UUID playerUuid, String currencyName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);

        Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currencyName);
        return serverCurrency.map(currency -> GBankManager.getPlayerBalance(offlinePlayer, currency))
                .orElse(0.0);
    }
}
