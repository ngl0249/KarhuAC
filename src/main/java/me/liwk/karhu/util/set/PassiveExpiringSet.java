package me.liwk.karhu.util.set;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class PassiveExpiringSet<V> extends AbstractSetDecorator<V> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long timeToLiveMillis;
    private final Map<Object, Long> expirationMap = new HashMap<>();

    public PassiveExpiringSet(long timeToLiveMillis) {
        super(new HashSet<>());
        this.timeToLiveMillis = timeToLiveMillis;
    }

    public boolean add(V value) {
        this.removeAllExpired();
        this.expirationMap.put(value, now());
        return super.add(value);
    }

    public boolean remove(Object key) {
        this.expirationMap.remove(key);
        return super.remove(key);
    }

    public boolean contains(Object value) {
        this.removeAllExpired();
        return super.contains(value);
    }

    public int size() {
        this.removeAllExpired();
        return super.size();
    }

    public void clear() {
        this.expirationMap.clear();
        super.clear();
    }

    private void removeAllExpired() {
        Iterator<Map.Entry<Object, Long>> iterator = this.expirationMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Long> expirationEntry = iterator.next();
            if (this.expired(expirationEntry.getValue())) {
                super.remove(expirationEntry.getKey());
                iterator.remove();
            }
        }
    }

    private boolean expired(long insertTime) {
        return now() - insertTime >= timeToLiveMillis;
    }

    private long now() {
        return System.currentTimeMillis();
    }
}
