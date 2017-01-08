package com.linkedkeeper.configcenter.recovery;

import com.linkedkeeper.configcenter.client.ConfigCenterClient;
import com.linkedkeeper.configcenter.recovery.impl.RecoveryStoreManagerImpl;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class RecoveryStoreHandler {

    private ConfigCenterClient client = null;
    private String recoveryFilePath = null;

    public synchronized void recovery() {
        try {
            RecoveryStoreManager recoveryStoreManager = new RecoveryStoreManagerImpl(client, recoveryFilePath);
            recoveryStoreManager.readFileData();
            recoveryStoreManager.reSendData();
        } catch (Exception e) {

        }
    }

    public void setClient(ConfigCenterClient client) {
        this.client = client;
    }

    public void setRecoveryFilePath(String recoveryFilePath) {
        this.recoveryFilePath = recoveryFilePath;
    }
}
