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
