package com.linkedkeeper.configcenter.zookeeper;

import com.linkedkeeper.configcenter.zookeeper.exception.ExceptionUtils;
import com.linkedkeeper.configcenter.zookeeper.exception.ZkException;
import com.linkedkeeper.configcenter.zookeeper.exception.ZkInterruptedException;
import com.linkedkeeper.configcenter.zookeeper.exception.ZkTimeoutException;
import com.linkedkeeper.configcenter.zookeeper.listener.ZkClientListener;
import com.linkedkeeper.configcenter.zookeeper.serialize.SerializableSerializer;
import com.linkedkeeper.configcenter.zookeeper.serialize.ZkSerializer;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.*;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ZkClient extends ZkClientListener implements Watcher {

    private final static Logger log = Logger.getLogger(ZkClient.class);

    private IZkConnection connection;
    private long operationRetryTimeoutInMillis;

    private final ZkLock _zkEventLock = new ZkLock();
    private Thread zookeeperEventThread;
    private volatile boolean closed;
    private Event.KeeperState currentState;

    public ZkClient(String zkServers, int sessionTimeout) {
        this(new ZkConnection(zkServers, sessionTimeout), new SerializableSerializer());
        this.operationRetryTimeoutInMillis = sessionTimeout;
    }

    private ZkClient(IZkConnection connection, ZkSerializer zkSerializer) {
        this.connection = connection;
        super.zkSerializer = zkSerializer;
        connect(this);
    }

    private void connect(Watcher watcher) throws IllegalStateException {
        acquireEventLock();
        log.info("try connect ...");
        try {
            initZkEventThread(connection.getServers());
            connection.connect(watcher);
            log.info("connection to Zookeeper server");
        } catch (Exception e) {
            log.error("connect error!", e);
        } finally {
            getEventLock().unlock();
        }
    }

    private void acquireEventLock() {
        try {
            getEventLock().lockInterruptibly();
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    /**
     * Returns a mutex all zookeeper events are synchronized aginst. So in case you need to do something without getting
     * any zookeeper event interruption synchronize against this mutex. Also all threads waiting on this mutex object
     * will be notified on an event.
     *
     * @return the mutex.
     */
    public ZkLock getEventLock() {
        return _zkEventLock;
    }

    public void close() throws InterruptedException {
        if (closed)
            return;

        if (connection == null) {
            return;
        }
        log.info("Closing ZkClient ..");
        getEventLock().lock();
        try {
            super.eventThread.interrupt();
            super.eventThread.join(2000);
            connection.close();
            connection = null;
            closed = true;
        } catch (ZkInterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
        log.info("Closing ZkClient .. done");
    }

    public void reconnect() {
        getEventLock().lock();
        log.warn("try reconnect ...");
        try {
            connection.close();
            connection.connect(this);
        } catch (Exception e) {
            log.error("reconnect error!", e);
        } finally {
            getEventLock().unlock();
        }
    }

    /**
     * 处理节点事件
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        log.info("Received event : " + watchedEvent);
        zookeeperEventThread = Thread.currentThread();

        boolean stateChanged = watchedEvent.getPath() == null;
        boolean zNodeChanged = watchedEvent.getPath() != null;
        boolean dataChanged = watchedEvent.getType() == Watcher.Event.EventType.NodeDataChanged
                || watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted
                || watchedEvent.getType() == Watcher.Event.EventType.NodeCreated
                || watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged;

        getEventLock().lock();
        try {
            // We might have to install child change event listener if a new node was created
            if (stateChanged) {
                processStateChanged(watchedEvent);
            }
            if (dataChanged) {
                processDataOrChildChange(watchedEvent);
            }
        } finally {
            if (stateChanged) {
                getEventLock().getStateChangedCondition().signalAll();
                // If the session expired we have to signal all conditions, because watches might have been removed and
                // there is no guarantee that those
                // conditions will be signaled at all after an Expired event
                if (watchedEvent.getState() == KeeperState.Expired) {
                    getEventLock().getZNodeEventCondition().signalAll();
                    getEventLock().getDataChangedCondition().signalAll();
                    // We also have to notify all listeners that something might have changed
                    fireAllEvents();
                }
            }
            if (zNodeChanged) {
                getEventLock().getZNodeEventCondition().signalAll();
            }
            if (dataChanged) {
                getEventLock().getDataChangedCondition().signalAll();
            }
            getEventLock().unlock();
            log.debug("Leaving process event");
        }
    }

    /**
     * 创建节点
     *
     * @param path
     * @param data
     * @param mode
     * @return
     * @throws Exception
     */
    public String create(final String path, Object data, final CreateMode mode) throws Exception {
        if (path == null) {
            throw new NullPointerException("path must not be null.");
        }
        final byte[] bytes = data == null ? null : serialize(data);

        return retryUntilConnected(new Callable<String>() {
            public String call() throws Exception {
                return connection.create(path, bytes, mode);
            }
        });
    }

    /**
     * 判断节点为是否存在
     *
     * @param path
     * @return
     */
    public boolean exists(String path) {
        return exists(path, hasListeners(path));
    }

    public boolean exists(final String path, final boolean watch) {
        return retryUntilConnected(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return connection.exists(path, watch);
            }
        });
    }

    /**
     * 写节点数据
     *
     * @param path
     * @param data
     * @return
     */
    public Stat writeData(final String path, final Object data) {
        if (path == null) {
            throw new NullPointerException("path must not be null.");
        }
        final byte[] bytes = data == null ? null : serialize(data);

        return retryUntilConnected(new Callable<Stat>() {
            public Stat call() throws Exception {
                return connection.writeDataReturnStat(path, bytes, -1);
            }
        });
    }

    /**
     * 取节点数据
     *
     * @param path
     * @param <T>
     * @return
     */
    public <T extends Object> T readData(String path) throws Exception {
        return readData(path, false);
    }

    public <T extends Object> T readData(String path, boolean returnNullIfPathNotExists) throws Exception {
        T data = null;
        try {
            data = readData(path, null);
        } catch (KeeperException e) {
            log.error("readData path = {" + path + "}, keeperException =>" + e.getMessage());
        } catch (Exception e) {
            log.error("readData path = {" + path + "}, exception =>" + e.getMessage());
        }
        return data;
    }

    public <T extends Object> T readData(String path, Stat stat) throws KeeperException {
        return readData(path, stat, hasListeners(path));
    }

    public <T extends Object> T readData(final String path, final Stat stat, final boolean watch) throws KeeperException {
        byte[] data = retryUntilConnected(new Callable<byte[]>() {
            public byte[] call() throws Exception {
                return connection.readData(path, stat, watch);
            }
        });
        return deserialize(data);
    }

    public List<String> getChildren(String path) {
        return getChildren(path, hasListeners(path));
    }

    public List<String> getChildren(final String path, final boolean watch) {
        return retryUntilConnected(new Callable<List<String>>() {
            public List<String> call() throws Exception {
                return connection.getChildren(path, watch);
            }
        });
    }

    /**
     * @param <T>
     * @param callable
     * @return result of Callable
     * @throws ZkInterruptedException   if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException              if any ZooKeeper exception occurred
     * @throws RuntimeException         if any other exception occurs from invoking the Callable
     */
    private <T> T retryUntilConnected(Callable<T> callable) throws RuntimeException {
        if (zookeeperEventThread != null && Thread.currentThread() == zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        final long operationStartTime = System.currentTimeMillis();
        int retryCount = 0;
        final int maxCount = 3;
        while (true) {
            if (closed) {
                throw new IllegalStateException("ZkClient already closed!");
            }
            try {
                return callable.call();
            } catch (ConnectionLossException e) {
                log.error("retryUntilConnected ConnectionLossException e -> ", e);
                // we give the event thread some time to update the status to 'Disconnected'
                Thread.yield();
                waitForRetry();
            } catch (SessionExpiredException e) {
                log.error("retryUntilConnected SessionExpiredException e -> ", e);
                // we give the event thread some time to update the status to 'Expired'
                Thread.yield();
                waitForRetry();
            } catch (KeeperException e) {
                log.error("retryUntilConnected KeeperException e -> ", e);
                throw ZkException.create(e);
            } catch (InterruptedException e) {
                log.error("retryUntilConnected InterruptedException e -> ", e);
                throw new ZkInterruptedException(e);
            } catch (Exception e) {
                log.error("retryUntilConnected Exception e -> ", e);
                throw ExceptionUtils.convertToRuntimeException(e);
            }
            // before attempting a retry, check whether retry timeout has elapsed
            log.info("retryUntilConnectedZ - operationRetryTimeoutInMillis -> {" + operationRetryTimeoutInMillis
                    + "}, operationStartTime -> {" + operationStartTime
                    + "}, System.currentTimeMillis() - operationStartTime -> { " + (System.currentTimeMillis() - operationStartTime)
                    + "}, (System.currentTimeMillis() - operationStartTime) >= this.operationRetryTimeoutInMillis -> {" + ((System.currentTimeMillis() - operationStartTime) >= this.operationRetryTimeoutInMillis) + "}");

            if ((this.operationRetryTimeoutInMillis > -1 && (System.currentTimeMillis() - operationStartTime) >= this.operationRetryTimeoutInMillis) || retryCount++ > maxCount) {
                throw new ZkTimeoutException("Operation cannot be retried because of retry timeout (" + this.operationRetryTimeoutInMillis + " milli seconds), retryCount (" + retryCount + ")");
            }
        }
    }

    public void setCurrentState(KeeperState currentState) {
        getEventLock().lock();
        try {
            this.currentState = currentState;
        } finally {
            getEventLock().unlock();
        }
    }

    private void waitForRetry() {
        if (this.operationRetryTimeoutInMillis < 0) {
            this.waitUntilConnected();
            return;
        }
        this.waitUntilConnected(this.operationRetryTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    public void waitUntilConnected() throws ZkInterruptedException {
        waitUntilConnected(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitUntilConnected(long time, TimeUnit timeUnit) throws ZkInterruptedException {
        return waitForKeeperState(KeeperState.SyncConnected, time, timeUnit);
    }

    public boolean waitForKeeperState(KeeperState keeperState, long time, TimeUnit timeUnit) throws ZkInterruptedException {
        if (zookeeperEventThread != null && Thread.currentThread() == zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        Date timeout = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));

        log.info("Waiting for keeper state " + keeperState);
        acquireEventLock();
        try {
            boolean stillWaiting = true;
            while (currentState != keeperState) {
                if (!stillWaiting) {
                    return false;
                }
                stillWaiting = getEventLock().getStateChangedCondition().awaitUntil(timeout);
            }
            log.debug("State is " + currentState);
            return true;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }
}
