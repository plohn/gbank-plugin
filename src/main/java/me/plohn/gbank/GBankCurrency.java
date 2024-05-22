package me.plohn.gbank;

import java.io.Serializable;

public class GBankCurrency {
    private String name;
    private String prefix;
    public GBankCurrency(String name, String prefix){
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}
