package com.linkedkeeper.configcenter.client.impl;


import com.linkedkeeper.configcenter.client.ConfigCenter;
import com.linkedkeeper.configcenter.recovery.RecoveryFallWatch;
import com.linkedkeeper.configcenter.recovery.RecoveryStore;
import com.linkedkeeper.configcenter.snapshot.SnapshotFallWatch;
import com.linkedkeeper.configcenter.snapshot.SnapshotStore;
import com.linkedkeeper.configcenter.snapshot.SnapshotSyncWatch;
import com.linkedkeeper.configcenter.snapshot.impl.SnapshotStoreImpl;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ConfigCenterImpl implements ConfigCenter {

    private BaseConfigCenterClient client = null;

    public ConfigCenterImpl(BaseConfigCenterClient client) {
        this.client = client;
    }

    private SnapshotFallWatch snapshotFallWatch = null;
    private SnapshotStore snapshotStore = null;
    private SnapshotSyncWatch snapshotSyncWatch = null;
    private RecoveryFallWatch recoveryFallWatch = null;
    private RecoveryStore recoveryStore = null;

    public void initZookeeper() {
        this.client.initZookeeper();
        this.client.initZkStateListeners();
        this.client.initZkDataListeners();
        this.client.initZkChildListeners();
    }

    public synchronized SnapshotStore getSnapshotStore() {
        if (snapshotStore == null) {
            snapshotStore = new SnapshotStoreImpl();
        }
        return snapshotStore;
    }

    public synchronized SnapshotFallWatch getSnapshotFallWatch() {
        if (snapshotFallWatch == null) {
            snapshotFallWatch = new SnapshotFallWatch(client);
            snapshotFallWatch.setDaemon(true);
        }
        return snapshotFallWatch;
    }

    public synchronized SnapshotSyncWatch getSnapshotSyncWatch() {
        if (snapshotSyncWatch == null) {
            snapshotSyncWatch = new SnapshotSyncWatch(client, snapshotStore);
            snapshotSyncWatch.setDaemon(true);
        }
        return snapshotSyncWatch;
    }

    public RecoveryFallWatch getRecoveryFallWatch() {
        if (recoveryFallWatch == null) {
            recoveryFallWatch = new RecoveryFallWatch(client);
            recoveryFallWatch.setDaemon(true);
        }
        return recoveryFallWatch;
    }

    public RecoveryStore getRecoveryStore() {
        if (recoveryStore == null) {
            recoveryStore = new RecoveryStore();
        }
        return recoveryStore;
    }
}
