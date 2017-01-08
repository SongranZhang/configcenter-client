package com.linkedkeeper.configcenter.client;

import com.linkedkeeper.configcenter.client.impl.DefinedConfigCenterClientListener;

/**
 * Created by zhangsongran on 2015/11/20.
 */
public class TestDefinedClientListener implements DefinedConfigCenterClientListener {

    public void definedHandleDataChange(String key, String value) {
        System.out.println("TestDefinedClientListener get key -> {" + key + "}, value -> {" + value + "}");
    }
}
