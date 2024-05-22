package me.plohn.gbank;

import co.aikar.commands.PaperCommandManager;
import io.github.johnnypixelz.utilizer.config.Configs;
import io.github.johnnypixelz.utilizer.config.Messages;
import io.github.johnnypixelz.utilizer.plugin.Logs;
import io.github.johnnypixelz.utilizer.plugin.UtilPlugin;
import io.github.johnnypixelz.utilizer.tasks.Tasks;
import me.plohn.gbank.commands.BalanceCommand;
import me.plohn.gbank.commands.BankCommand;
import me.plohn.gbank.commands.PayCommand;
import me.plohn.gbank.gbankstorage.GBankStorage;
import me.plohn.gbank.gbankstorage.JsonStorage;
import me.plohn.gbank.gbankstorage.MySqlStorage;
import me.plohn.gbank.listeners.GBankListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class GBank extends UtilPlugin {
    private static final ArrayList<GBankCurrency> currencies = new ArrayList<>();
    private static GBankStorage storage;

    @Override
    public void onEnable() {
        GBankManager.loadCurrencies();

        loadData();
        initializeCommands();
        initializeAutomatedRewards();
        registerListener(new GBankListener());
    }

    @Override
    public void onDisable() {
        Logs.info("Saving player data..");
        storage.setPlayerProfiles(GBankManager.getCacheData());
    }

    private void loadData() {
        Logs.info("Loading player data.");
        String storageOption = Configs.get("config").getString("storage.type", "JSON");
        switch (storageOption) {

            case "JSON":
                Logs.info("Storage option JSON");
                String fileName = Configs.get("config").getString("storage.json.file", "player_data.json");
                storage = new JsonStorage(fileName);
                break;

            case "MYSQL":
                Logs.info("Storage option MYSQL");
                ConfigurationSection dbProperties = Configs.get("config").getConfigurationSection("storage.mysql");

                if (dbProperties == null) {
                    Logs.severe("No mysql properties are configured");
                    break;
                }

                String databaseName = dbProperties.getString("database", "database");
                String databaseAddress = dbProperties.getString("address", "localhost");
                String databasePort = dbProperties.getString("port", "3306");
                String username = dbProperties.getString("username", "username");
                String password = dbProperties.getString("password", "password");
                String tableName = dbProperties.getString("table", "table");

                storage = new MySqlStorage(databaseName,databaseAddress, databasePort, username, password, tableName);
                break;
        }
        GBankManager.setCache(storage.getPlayerData());
        Logs.info("Loaded " + GBankManager.getCacheData().size() + " profiles.");
    }

    private void initializeCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        String bankPermission = Configs.get("config").getString("permissions.pay", "gbank.bank");
        String reloadPermission = Configs.get("config").getString("permissions.pay", "gbank.reload");
        BankCommand bankCommand = new BankCommand(bankPermission, reloadPermission);
        commandManager.registerCommand(bankCommand);

        String balancePermission = Configs.get("config").getString("permissions.bank", "gbank.balance");
        BalanceCommand balanceCommand = new BalanceCommand(balancePermission);
        commandManager.registerCommand(balanceCommand);

        String payPermission = Configs.get("config").getString("permissions.pay", "gbank.pay");
        PayCommand payCommand = new PayCommand(payPermission);
        commandManager.registerCommand(payCommand);


        commandManager.getCommandCompletions().registerAsyncCompletion("currencies", c -> {
            ArrayList<GBankCurrency> currencies = GBankManager.getServerCurrencies();
            return currencies.stream()
                    .map(GBankCurrency::getName)
                    .collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerCompletion("players", c -> {
            return getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        });
    }

    private void initializeAutomatedRewards() {

        ConfigurationSection currencyRewardsSection = Configs.get("config").getConfigurationSection("currency_rewards");
        int intervalSeconds = currencyRewardsSection.getInt("interval_seconds", 600);

        HashMap<GBankCurrency, Double> currency_rewards = new HashMap<>();
        ConfigurationSection rewardsSection = currencyRewardsSection.getConfigurationSection("rewards");
        Set<String> rewardsSectionKeys = rewardsSection.getKeys(false);
        if (rewardsSectionKeys.isEmpty()) {
            Logs.severe("No rewards are configured");
            return;
        }

        rewardsSectionKeys.forEach(currencyReward -> {
            Optional<GBankCurrency> serverCurrency = GBankManager.getServerCurrency(currencyReward);
            if (serverCurrency.isPresent()) {
                double amount = rewardsSection.getDouble(currencyReward, 1.0);
                currency_rewards.put(serverCurrency.get(), amount);
            }
        });

        if (currency_rewards.isEmpty()) {
            Logs.warn("No currencies are configured at currency_rewards section");
            return;
        }

        Tasks.sync().timer(() -> {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                currency_rewards.forEach((currency, amount) -> {

                    double previousAmount = GBankManager.getPlayerBalance(onlinePlayer, currency);
                    GBankManager.updatePlayerBalance(onlinePlayer, currency, previousAmount + amount);

                    Messages.cfg("language", "rewards.reward_received")
                            .map("%prefix%", currency.getPrefix())
                            .map("%amount%", String.valueOf(amount))
                            .map("%currency%", currency.getName())
                            .send(onlinePlayer);
                });
            });
        }, intervalSeconds);
    }
}
