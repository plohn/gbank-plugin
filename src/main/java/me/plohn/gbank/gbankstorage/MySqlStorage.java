package me.plohn.gbank.gbankstorage;

import me.plohn.gbank.GBankManager;
import me.plohn.gbank.GBankPlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySqlStorage extends GBankStorage {

    private String DATABASE_URL;
    private String DATABASE_NAME;
    private String USERNAME;
    private String PASSWORD;
    private String TABLE_NAME;

    public MySqlStorage(String databaseName,String databaseAddress, String databasePort, String username, String password, String tableName) {

        this.DATABASE_URL = "jdbc:mysql://%database_address%:%database_port%/"
                .replace("%database_address%", databaseAddress)
                .replace("%database_port%", databasePort);

        this.DATABASE_NAME = databaseName;
        this.USERNAME = username;
        this.PASSWORD = password;
        this.TABLE_NAME = tableName;

        initializeTables();
    }

    private void initializeTables(){
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL + DATABASE_NAME, USERNAME, PASSWORD);
            Statement statement = connection.createStatement();

            String createPlayerTable = """
                    CREATE TABLE IF NOT EXISTS %table_name% (
                        playerUuid VARCHAR(36) NOT NULL,
                        PRIMARY KEY (playerUuid)
                    );
                    """.replace("%table_name%", TABLE_NAME);

            String createBalanceTable = """
                    CREATE TABLE IF NOT EXISTS %table_name%_balances (
                        balanceId INT AUTO_INCREMENT PRIMARY KEY,
                        playerUuid CHAR(36),
                        balanceType VARCHAR(50),
                        balanceValue FLOAT,
                        FOREIGN KEY (playerUuid) REFERENCES %table_name%(playerUuid)
                    );
                    """.replace("%table_name%", TABLE_NAME);

            statement.executeUpdate(createPlayerTable);
            statement.executeUpdate(createBalanceTable);

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public HashMap<UUID, GBankPlayerProfile> fetchData() {
        HashMap<UUID, GBankPlayerProfile> playerProfiles = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL + DATABASE_NAME, USERNAME, PASSWORD);
            PreparedStatement playerStatement = connection.prepareStatement("SELECT * FROM %table_name%"
                    .replace("%table_name%", TABLE_NAME));

            ResultSet playerResultSet = playerStatement.executeQuery();

            while (playerResultSet.next()) {
                UUID playerUuid = UUID.fromString(playerResultSet.getString("playerUuid"));
                OfflinePlayer fetchedPlayer = Bukkit.getOfflinePlayer(playerUuid);
                GBankPlayerProfile profile = new GBankPlayerProfile(fetchedPlayer, GBankManager.getServerCurrencies());

                PreparedStatement balanceStatement = connection.prepareStatement("SELECT * FROM %table_name%_balances WHERE playerUuid = ?"
                        .replace("%table_name%", TABLE_NAME));

                balanceStatement.setString(1, playerUuid.toString());
                ResultSet balanceResultSet = balanceStatement.executeQuery();

                // Get balances for this player
                while (balanceResultSet.next()) {
                    String balanceType = balanceResultSet.getString("balanceType");
                    double balanceValue = balanceResultSet.getDouble("balanceValue");

                    profile.setBalance(balanceType, balanceValue);
                }

                playerProfiles.put(playerUuid, profile);
            }

            playerResultSet.close();
            playerStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerProfiles;
    }

    public void updateData(HashMap<UUID, GBankPlayerProfile> data) {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL + DATABASE_NAME, USERNAME, PASSWORD);
            for (GBankPlayerProfile profile : data.values()) {

                PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (playerUuid) VALUES (?) ON DUPLICATE KEY UPDATE playerUuid = ?");
                playerStatement.setString(1, profile.getPlayerUuid().toString());
                playerStatement.setString(2, profile.getPlayerUuid().toString());
                playerStatement.executeUpdate();

                for (String currencyName : profile.getBalances().keySet()) {
                    double balanceValue = profile.getBalances().get(currencyName);
                    PreparedStatement balanceStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + "_balances (playerUuid, balanceType, balanceValue) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE balanceValue = ?");
                    balanceStatement.setString(1, profile.getPlayerUuid().toString());
                    balanceStatement.setString(2, currencyName);
                    balanceStatement.setDouble(3, balanceValue);
                    balanceStatement.setDouble(4, balanceValue);
                    balanceStatement.executeUpdate();
                }
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<UUID, GBankPlayerProfile> getPlayerData() {
        return fetchData();
    }

    @Override
    public void setPlayerProfiles(HashMap<UUID, GBankPlayerProfile> playerData) {
        updateData(playerData);
    }
}
