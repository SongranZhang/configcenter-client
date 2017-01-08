package com.linkedkeeper.configcenter.snapshot;

import com.linkedkeeper.configcenter.client.impl.BaseConfigCenterClient;
import org.apache.log4j.Logger;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class SnapshotSyncWatch extends Thread {

    private final Logger log = Logger.getLogger(SnapshotSyncWatch.class);

    private BaseConfigCenterClient client = null;
    private SnapshotStore snapshotStore = null;

    private final ReentrantLock lock = new ReentrantLock();

    private final int sleepTime = 60 * 1000;

    public SnapshotSyncWatch(BaseConfigCenterClient client, SnapshotStore snapshotStore) {
        log.error("ConfigCenterClient/SnapshotSyncWatch init Thread -> " + this.toString());
        this.snapshotStore = snapshotStore;
        this.client = client;
    }

    public void run() {
        for (; ; ) {
            if (lock.tryLock()) {
                try {
                    snapshotStore.storeSnapshot(client.getSnapshot());
                } catch (Exception e) {
                    log.error("SnapshotSyncWatch run Thread (" + this.toString() + ") exception =>" + e.getMessage());
                } finally {
                    lock.unlock();
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
