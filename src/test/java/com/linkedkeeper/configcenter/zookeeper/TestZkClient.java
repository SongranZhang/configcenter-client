package com.linkedkeeper.configcenter.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class TestZkClient {

    private static ZkClient zk = null;

    private static String zkServers = "127.0.0.1:2181";
    private static int sessionTimeout = 30 * 1000;

    @BeforeClass
    public static void init() throws Exception {
        zk = new ZkClient(zkServers, sessionTimeout);
    }

    @Test
    public void create() throws Exception {
        String data = "--data--";
        zk.create("/test1", data.getBytes(), CreateMode.PERSISTENT);
    }
}
