package com.linkedkeeper.configcenter.client.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public abstract class ConfigCenterClientCache {

    protected Map<String, String> localCache = new ConcurrentHashMap<String, String>();

    public Map<String, String> getSnapshot() {
        return new HashMap<String, String>(localCache);
    }

    public void put(String key, String value) {
        if (value != null) {
            localCache.put(key, value);
        }
    }
}
