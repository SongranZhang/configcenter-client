package com.linkedkeeper.configcenter.snapshot;

import com.linkedkeeper.configcenter.client.impl.BaseConfigCenterClient;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class SnapshotFallWatch extends Thread {

    private final Logger log = Logger.getLogger(SnapshotFallWatch.class);

    private Set<String> fallWatchQueue = new HashSet<String>(10000);

    private BaseConfigCenterClient client = null;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    private final long sleepTime = 1L * 10L * 1000L; // 10 sec

    public SnapshotFallWatch(BaseConfigCenterClient client) {
        log.error("ConfigCenterClient/SnapshotFallWatch init Thread -> " + this.toString());
        this.client = client;
    }

    public boolean register(String key) {
        lock.lock();
        try {
            fallWatchQueue.add(key);
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
        return true;
    }

    public void run() {
        for (; ; ) {
            if (lock.tryLock()) {
                try {
                    if (fallWatchQueue.isEmpty()) {
                        notEmpty.await();
                    } else {
                        syncData();
                    }
                } catch (Exception e) {
                    log.error("SnapshotFallWatch run Thread (" + this.toString() + ") exception =>" + e.getMessage());
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

    private void syncData() throws InterruptedException {
        for (Iterator<String> it = fallWatchQueue.iterator(); it.hasNext(); ) {
            String key = it.next();
            try {
                String value = client.readDataFromZk(key);
                if (value != null) {
                    client.put(key, value);
                    it.remove();
                }
            } catch (Exception e) {
                log.error("SnapshotFallWatch syncData exception =>" + e.getMessage());
            }
        }
    }
}
