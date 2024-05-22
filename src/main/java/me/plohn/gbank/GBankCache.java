package me.plohn.gbank;

import it.unimi.dsi.fastutil.Hash;
import me.plohn.gbank.GBankPlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GBankCache <K, V> {
    private final Map<K, V> cache = new HashMap<>();

    public V get(K key) {
        return cache.get(key);
    }

    public V put(K key, V value) {
        return cache.put(key, value);
    }

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    public void invalidate(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}
