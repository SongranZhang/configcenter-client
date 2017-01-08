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
