package com.linkedkeeper.configcenter.client.impl;

import com.linkedkeeper.configcenter.client.ConfigCenterClient;
import com.linkedkeeper.configcenter.zookeeper.exception.NonsupportException;
import org.apache.log4j.Logger;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class FallFastConfigCenterClient extends BaseConfigCenterClient implements ConfigCenterClient {

    private final static Logger log = Logger.getLogger(FallFastConfigCenterClient.class);

    public synchronized void init() {
        try {
            configCenter = new ConfigCenterImpl(this);
            configCenter.initZookeeper();
        } catch (Exception e) {
            log.error("FallFastDataSyncClient init failure, exception =>" + e.getMessage());
        }
    }

    public void restart() {
        try {
            log.info("FallFastDataSyncClient restart ...");
            zkClient.reconnect();
        } catch (Exception e) {
            log.error("FallFastDataSyncClient restart failure, exception =>" + e.getMessage());
        }
    }

    public void destroy() {
        try {
            log.info("FallFastDataSyncClient close ...");
            zkClient.close();
        } catch (InterruptedException e) {
            log.error("FallFastDataSyncClient destroy failure, exception =>" + e.getMessage());
        }
    }

    //----------------- client util ------------------

    public String get(String key) throws Exception {
        /**
         * one. get value from localCache.
         */
        String value = localCache.get(key);
        if (value != null) {
            return value;
        }
        /**
         * two. if localCache no exist. get from snapshot.
         */
        String path = generateZkPath(key);
        if (zkClient.exists(path)) {
            // if the path exist, read data from zk.
            zkClient.subscribeDataChanges(path, this);
            value = readDataFromZk(key);
            if (value != null) {
                localCache.put(key, value);
                return value;
            }
        }
        return value;
    }

    public boolean set(String key, String value) throws Exception {
        throw new NonsupportException("Cannot set value in 'FallFastDataSyncClient'");
    }
}
