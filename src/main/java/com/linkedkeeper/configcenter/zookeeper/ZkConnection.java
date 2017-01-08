package com.linkedkeeper.configcenter.zookeeper;

import com.linkedkeeper.configcenter.zookeeper.exception.ZkException;
import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ZkConnection implements IZkConnection {

    private final static Logger log = Logger.getLogger(ZkConnection.class);

    /**
     * It is recommended to use quite large sessions timeouts for ZooKeeper.
     */
    private static final int DEFAULT_SESSION_TIMEOUT = 30000;

    /**
     * zookeeper instance
     */
    private ZooKeeper zk = null;
    private Lock zkLock = new ReentrantLock();

    private String zkServers;
    private int sessionTimeout;

    public ZkConnection(String zkServers) {
        this(zkServers, DEFAULT_SESSION_TIMEOUT);
    }

    public ZkConnection(String zkServers, int sessionTimeout) {
        this.zkServers = zkServers;
        this.sessionTimeout = sessionTimeout;
    }

    public void connect(Watcher watcher) throws IOException {
        zkLock.lock();
        try {
            if (zk != null) {
                throw new IllegalArgumentException("zk client has already been started.");
            }
            try {
                log.debug("Creating new Zookeeper instance to connect to " + zkServers + ".");
                zk = new ZooKeeper(zkServers, sessionTimeout, watcher);
            } catch (IOException e) {
                throw new ZkException("Unable to connect to " + zkServers, e);
            }
        } finally {
            zkLock.unlock();
        }
        log.info("connected zk! conf = " + zkServers);
    }

    public void close() throws InterruptedException {
        zkLock.lock();
        try {
            if (zk != null) {
                log.debug("Closing Zookeeper connected to " + zkServers);
                zk.close();
                zk = null;
            }
        } finally {
            zkLock.unlock();
        }
        log.info("closed zk!");
    }

    public String create(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException {
        return zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
    }

    public String create(String path, byte[] data, List<ACL> acl, CreateMode mode) throws KeeperException, InterruptedException {
        return zk.create(path, data, acl, mode);
    }

    public void delete(String path) throws InterruptedException, KeeperException {
        zk.delete(path, -1);
    }

    public boolean exists(String path, boolean watch) throws KeeperException, InterruptedException {
        return zk.exists(path, watch) != null;
    }

    public List<String> getChildren(final String path, final boolean watch) throws KeeperException, InterruptedException {
        return zk.getChildren(path, watch);
    }

    public byte[] readData(String path, Stat stat, boolean watch) throws KeeperException, InterruptedException {
        log.info("path -> {" + path + "}, stat -> {" + stat + "}, watch -> {" + watch + "}");
        return zk.getData(path, watch, stat);
    }

    public void writeData(String path, byte[] data) throws KeeperException, InterruptedException {
        zk.setData(path, data, -1);
    }

    public void writeData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
        zk.setData(path, data, version);
    }

    public Stat writeDataReturnStat(String path, byte[] data, int version) throws KeeperException, InterruptedException {
        return zk.setData(path, data, version);
    }

    public States getZookeeperState() {
        return zk != null ? zk.getState() : null;
    }

    public long getCreateTime(String path) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(path, false);
        if (stat != null) {
            return stat.getCtime();
        }
        return -1;
    }

    public String getServers() {
        return zkServers;
    }

}
