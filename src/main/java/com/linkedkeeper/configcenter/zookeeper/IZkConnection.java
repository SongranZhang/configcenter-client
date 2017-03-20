package com.linkedkeeper.configcenter.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.ZooKeeper.States;

import java.io.IOException;
import java.util.List;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public interface IZkConnection {

    void connect(Watcher watcher) throws IOException;

    void close() throws InterruptedException;

    String create(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException;

    String create(String path, byte[] data, List<ACL> acl, CreateMode mode) throws KeeperException, InterruptedException;

    void delete(String path) throws InterruptedException, KeeperException;

    boolean exists(final String path, final boolean watch) throws KeeperException, InterruptedException;

    List<String> getChildren(final String path, final boolean watch) throws KeeperException, InterruptedException;

    byte[] readData(String path, Stat stat, boolean watch) throws KeeperException, InterruptedException;

    void writeData(String path, byte[] data) throws KeeperException, InterruptedException;

    void writeData(String path, byte[] data, int version) throws KeeperException, InterruptedException;

    Stat writeDataReturnStat(String path, byte[] data, int version) throws KeeperException, InterruptedException;

    States getZookeeperState();

    long getCreateTime(String path) throws KeeperException, InterruptedException;

    String getServers();
}
