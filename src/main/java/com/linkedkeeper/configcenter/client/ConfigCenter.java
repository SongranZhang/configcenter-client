package com.linkedkeeper.configcenter.client;

import com.linkedkeeper.configcenter.recovery.RecoveryFallWatch;
import com.linkedkeeper.configcenter.recovery.RecoveryStore;
import com.linkedkeeper.configcenter.snapshot.SnapshotFallWatch;
import com.linkedkeeper.configcenter.snapshot.SnapshotStore;
import com.linkedkeeper.configcenter.snapshot.SnapshotSyncWatch;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public interface ConfigCenter {

    void initZookeeper();

    SnapshotStore getSnapshotStore();

    SnapshotFallWatch getSnapshotFallWatch();

    SnapshotSyncWatch getSnapshotSyncWatch();

    RecoveryFallWatch getRecoveryFallWatch();

    RecoveryStore getRecoveryStore();

}
