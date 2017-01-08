package com.linkedkeeper.configcenter.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public interface IZkClient {

    void close() throws InterruptedException;

    void reconnect();

    String create(final String path, Object data, final CreateMode mode) throws Exception;

    boolean exists(final String path);

    boolean exists(final String path, final boolean watch);

    Stat writeData(final String path, final Object data);

    <T extends Object> T readData(String path) throws Exception;

    <T extends Object> T readData(String path, boolean returnNullIfPathNotExists) throws Exception;

    <T extends Object> T readData(String path, Stat stat) throws KeeperException;

    <T extends Object> T readData(final String path, final Stat stat, final boolean watch) throws KeeperException;
}
