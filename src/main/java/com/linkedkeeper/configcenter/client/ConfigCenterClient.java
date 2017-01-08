package com.linkedkeeper.configcenter.client;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public interface ConfigCenterClient {

    void init();

    void destroy();

    String get(String key) throws Exception;

    boolean set(String key, String value) throws Exception;

}
