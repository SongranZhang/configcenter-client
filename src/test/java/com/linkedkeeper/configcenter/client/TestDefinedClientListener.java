package com.linkedkeeper.configcenter.client;

import com.linkedkeeper.configcenter.client.impl.DefinedConfigCenterClientListener;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class TestDefinedClientListener implements DefinedConfigCenterClientListener {

    public void definedHandleDataChange(String key, String value) {
        System.out.println("TestDefinedClientListener get key -> {" + key + "}, value -> {" + value + "}");
    }
}
