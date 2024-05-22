package me.plohn.gbank.gbankstorage;

import io.github.johnnypixelz.utilizer.file.storage.Storage;
import io.github.johnnypixelz.utilizer.file.storage.container.file.FileStorageContainer;
import me.plohn.gbank.GBankPlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonStorage extends GBankStorage {
    private final FileStorageContainer<Map<UUID, GBankPlayerProfile>> playerProfiles;
    public JsonStorage(String fileName) {


        this.playerProfiles = Storage.map(UUID.class, GBankPlayerProfile.class)
                .json(fileName)
                .container(HashMap::new);
    }

    @Override
    public Map<UUID, GBankPlayerProfile> getPlayerData() {
        return this.playerProfiles.get();
    }

    @Override
    public void setPlayerProfiles(HashMap<UUID, GBankPlayerProfile> playerData) {
        this.playerProfiles.set(playerData);
        this.playerProfiles.save();
    }
}
