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

import com.linkedkeeper.configcenter.client.ConfigCenter;
import com.linkedkeeper.configcenter.zookeeper.ZkClient;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class BaseConfigCenterClient extends ConfigCenterClientListener {

    private final static Logger log = Logger.getLogger(BaseConfigCenterClient.class);

    private String zkServers = null;
    private String zNodePath = null;

    protected ConfigCenter configCenter = null;

    /**
     * zookeeper instance
     */
    protected ZkClient zkClient = null;
    private int sessionTimeout = 10 * 1000; // 10sec
    private final String SEPARATOR = "/";
    private final String NULL = "";

    //------------------- zk util --------------------

    public void initZookeeper() {
        try {
            zkClient = new ZkClient(zkServers, sessionTimeout);
            // 初始化节点
            initZNodePath();
        } catch (Exception e) {
            log.error("BaseConfigCenterClient initZookeeper failure, exception =>" + e + e.getMessage());
        }
    }

    private void initZNodePath() throws Exception {
        if (StringUtils.isBlank(zNodePath) || zkClient.exists(zNodePath)) {
            return;
        }
        String[] nodes = zNodePath.split(SEPARATOR);
        StringBuffer nodePath = new StringBuffer();
        for (String node : nodes) {
            if (StringUtils.isNotBlank(node)) {
                nodePath.append(SEPARATOR + node);
                if (!zkClient.exists(nodePath.toString())) {
                    zkClient.create(nodePath.toString(), NULL, CreateMode.PERSISTENT);
                }
            }
        }
    }

    public String readDataFromZk(String key) throws Exception {
        String path = generateZkPath(key);
        return zkClient.readData(path);
    }

    public void writeDataToZk(String key, String value) throws Exception {
        String path = generateZkPath(key);
        if (zkClient.exists(path)) {
            log.debug("writeDataToZk writeDate, key -> {" + key + "}");
            zkClient.writeData(path, value);
        } else {
            log.debug("writeDataToZk create, key -> {" + key + "}");
            zkClient.create(path, value, CreateMode.PERSISTENT);
        }
    }


    //------------------ recovery -------------------

    public String writeDataToRecovery(String key, String value) throws Exception {
        return configCenter.getRecoveryStore().save(key, value);
    }

    //------------------ listener -------------------

    public void initZkStateListeners() {
    }

    public void initZkDataListeners() {
        for (String key : localCache.keySet()) {
            zkClient.subscribeDataChanges(generateZkPath(key), this);
        }
    }

    public void initZkChildListeners() {
    }

    protected String generateZkPath(String key) {
        return zNodePath + SEPARATOR + key;
    }

    //-------------------- setter --------------------

    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    public void setzNodePath(String zNodePath) {
        this.zNodePath = zNodePath;
    }

}
