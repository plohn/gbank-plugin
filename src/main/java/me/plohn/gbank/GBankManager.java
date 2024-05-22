package me.plohn.gbank;

import io.github.johnnypixelz.utilizer.config.Configs;
import io.github.johnnypixelz.utilizer.plugin.Logs;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class GBankManager {
    private static final ArrayList<GBankCurrency> serverCurrencies = new ArrayList<>();
    private static HashMap<UUID, GBankPlayerProfile> playerCache = new HashMap<>();


    public static GBankPlayerProfile getPlayerProfile(OfflinePlayer player) {

        if (playerCache.containsKey(player.getUniqueId())) {
            return playerCache.get(player.getUniqueId());
        }

        GBankPlayerProfile profile = new GBankPlayerProfile(player, serverCurrencies);
        playerCache.put(player.getUniqueId(), profile);
        return profile;
    }

    public static double getPlayerBalance(OfflinePlayer player, GBankCurrency currencyBalance) {
        return getPlayerProfile(player).getBalance(currencyBalance);
    }

    public static void updatePlayerBalance(OfflinePlayer player, GBankCurrency currency, double amount) {
        getPlayerProfile(player).setBalance(currency, amount);
    }

    public static ArrayList<GBankCurrency> getServerCurrencies() {
        return serverCurrencies;
    }

    public static Optional<GBankCurrency> getServerCurrency(String currencyName) {
        return serverCurrencies.stream()
                .filter(currency -> currencyName.equals(currency.getName()))
                .findFirst();
    }

    public static void setCache(Map<UUID, GBankPlayerProfile> userProfiles) {
        playerCache = (HashMap<UUID, GBankPlayerProfile>) userProfiles;
    }

    public static HashMap<UUID, GBankPlayerProfile> getCacheData() {
        return playerCache;
    }

    public static void loadCurrencies() {
        ConfigurationSection currenciesSection = Configs.get("config").getConfigurationSection("currencies");
        if (currenciesSection == null) {
            Logs.severe("No currencies have been configured");
            return;
        }
        currenciesSection.getKeys(false).forEach(currency -> {
                    ConfigurationSection currencySection = currenciesSection.getConfigurationSection(currency);
                    String name = currencySection.getString("name", "undefined");
                    String prefix = currencySection.getString("prefix", "undefined");
                    serverCurrencies.add(new GBankCurrency(name, prefix));
                }
        );
    }
}
