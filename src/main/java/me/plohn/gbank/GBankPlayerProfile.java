package me.plohn.gbank;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class GBankPlayerProfile {
    private UUID playerUuid;
    private HashMap<String, Double> balances = new HashMap<>();

    public GBankPlayerProfile(OfflinePlayer player, ArrayList<GBankCurrency> serverCurrencies) {
        this.playerUuid = player.getUniqueId();
        serverCurrencies.forEach(currency -> balances.put(currency.getName(), 0.0));
    }

    public GBankPlayerProfile(OfflinePlayer player, HashMap<String, Double> playerBalances) {

        this.playerUuid = player.getUniqueId();
        this.balances = playerBalances;
    }

    public UUID getPlayerUuid() {

        return playerUuid;
    }

    public HashMap<String, Double> getBalances() {
        return this.balances;
    }

    public double getBalance(GBankCurrency currency) {
        return balances.get(currency.getName());
    }

    public double setBalance(GBankCurrency currency, double amount) {

        balances.put(currency.getName(), amount);
        return balances.get(currency.getName());
    }
    public double setBalance(String currencyName, double amount) {

        balances.put(currencyName, amount);
        return balances.get(currencyName);
    }

    public void updateCurrencies(ArrayList<GBankCurrency> serverCurrencies) {

        balances.forEach((currency, integer) -> {
            Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currency);
            if (serverCurrency.isEmpty()) {
                balances.remove(currency);
            }
        });

        serverCurrencies.forEach(currency -> {
            balances.computeIfAbsent(currency.getName(), newCurrency -> 0.0);
        });
    }
}
