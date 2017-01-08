package com.linkedkeeper.configcenter.client.impl;

import com.linkedkeeper.configcenter.client.ConfigCenterClient;
import org.apache.log4j.Logger;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class FailoverConfigCenterClient extends BaseConfigCenterClient implements ConfigCenterClient {

    private final static Logger log = Logger.getLogger(FailoverConfigCenterClient.class);

    private String storeFilePath = null;
    private String recoveryFilePath = null;

    public synchronized void init() {
        try {
            configCenter = new ConfigCenterImpl(this);
            configCenter.initZookeeper();

            configCenter.getSnapshotStore().setStoreFilePath(storeFilePath);
            configCenter.getSnapshotFallWatch().start();
            configCenter.getSnapshotSyncWatch().start();
            configCenter.getRecoveryStore().setRecoveryFilePath(recoveryFilePath);
            configCenter.getRecoveryFallWatch().start();

        } catch (Exception e) {
            log.error("FailoverDataSyncClient init failure, exception =>" + e.getMessage());
        }
    }

    public void restart() {
        try {
            log.info("DataSyncClient restart ...");
            zkClient.reconnect();
        } catch (Exception e) {
            log.error("DataSyncClient restart failure, exception =>" + e.getMessage());
        }
    }

    public void destroy() {
        try {
            log.info("DataSyncClient close ...");
            zkClient.close();
        } catch (InterruptedException e) {
            log.error("DataSyncClient destroy failure, exception =>" + e.getMessage());
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
        zkClient.subscribeDataChanges(path, this);
        value = readDataFromZk(key);
        if (value != null) {
            localCache.put(key, value);
            return value;
        }
        /**
         * three. if zk failure. get from snapshot.
         */
        configCenter.getSnapshotFallWatch().register(key);
        value = configCenter.getSnapshotStore().readSnapshot(key);
        return value;
    }

    public boolean set(String key, String value) throws Exception {
        return configCenter.getRecoveryFallWatch().writeData(key, value);
    }

    //-------------------- setter --------------------

    public void setStoreFilePath(String storeFilePath) {
        this.storeFilePath = storeFilePath;
    }

    public void setRecoveryFilePath(String recoveryFilePath) {
        this.recoveryFilePath = recoveryFilePath;
    }
}
