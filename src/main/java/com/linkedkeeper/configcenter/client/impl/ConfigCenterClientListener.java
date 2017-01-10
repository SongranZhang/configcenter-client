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

import com.linkedkeeper.configcenter.zookeeper.listener.IZkDataListener;
import org.apache.log4j.Logger;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ConfigCenterClientListener extends ConfigCenterClientCache implements IZkDataListener {

    private final Logger log = Logger.getLogger(ConfigCenterClientListener.class);

    private Object dataListener;

    public void setDataListener(Object dataListener) {
        this.dataListener = dataListener;
    }

    // IZkDataListener

    public void handleDataChange(String dataPath, Object data) {
        if (data == null) {
            log.error("ConfigCenterClientListener handleDataChange dataPath -> {" + dataPath + "} and data is null.");
            return;
        }
        log.info("ConfigCenterClientListener handleDataChange dataPath -> {" + dataPath + "}");

        String key = generateKey(dataPath);
        String value = (String) data;
        if (localCache.get(key) != null) {
            localCache.put(key, value);
            // 自定义监听
            definedDataChange(key, value);
        }
    }

    public void handleDataDeleted(String dataPath) throws Exception {
        log.debug("ConfigCenterClientListener handleDataDeleted dataPath -> {" + dataPath + "}");
        String key = generateKey(dataPath);
        localCache.remove(key);
    }

    // DefinedDataListener

    private void definedDataChange(String key, String value) {
        if (dataListener != null) {
            DefinedConfigCenterClientHandler handler = new DefinedConfigCenterClientHandler();
            DefinedConfigCenterClientListener listener = (DefinedConfigCenterClientListener) handler.newProxy(dataListener);
            listener.definedHandleDataChange(key, value);
        }
    }

    private String generateKey(String zkPath) {
        return zkPath.substring(zkPath.lastIndexOf("/") + 1);
    }
}
