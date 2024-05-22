package me.plohn.gbank.api;

import java.util.UUID;

public interface GBankAPI {
    double getBalance(UUID playerUuid, String currencyName);
}
