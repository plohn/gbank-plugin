package me.plohn.gbank.gbankstorage;

import me.plohn.gbank.GBankPlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class GBankStorage {

    public abstract Map<UUID, GBankPlayerProfile> getPlayerData();

    public abstract void setPlayerProfiles(HashMap<UUID, GBankPlayerProfile> playerData);
}
