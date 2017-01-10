/*
 * Copyright (c) 2016, LinkedKeeper
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of LinkedKeeper nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
