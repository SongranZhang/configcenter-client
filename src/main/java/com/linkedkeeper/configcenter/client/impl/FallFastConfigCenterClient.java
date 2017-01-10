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
