package com.linkedkeeper.configcenter.zookeeper.listener;

import com.linkedkeeper.configcenter.zookeeper.exception.ZkNoNodeException;
import com.linkedkeeper.configcenter.zookeeper.listener.ZkEventThread.ZkEvent;
import com.linkedkeeper.configcenter.zookeeper.serialize.ZkClientSerializer;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public abstract class ZkClientListener extends ZkClientSerializer {

    private final static Logger log = Logger.getLogger(ZkClientListener.class);

    protected ZkEventThread eventThread;
    private Watcher.Event.KeeperState currentState;

    protected final Map<String, Set<IZkChildListener>> childListener = new ConcurrentHashMap<String, Set<IZkChildListener>>();
    protected final Map<String, Set<IZkDataListener>> dataListener = new ConcurrentHashMap<String, Set<IZkDataListener>>();
    protected final Set<IZkStateListener> stateListener = new CopyOnWriteArraySet<IZkStateListener>();

    protected void initZkEventThread(String name) {
        eventThread = new ZkEventThread(name);
        eventThread.start();
    }

    //------------------------ public --------------------------

    public void subscribeChildChanges(String path, IZkChildListener listener) {
        synchronized (this.childListener) {
            Set<IZkChildListener> listeners = this.childListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkChildListener>();
                this.childListener.put(path, listeners);
            }
            listeners.add(listener);
        }
    }

    public void unsubscribeChildChanges(String path, IZkChildListener childListener) {
        synchronized (this.childListener) {
            final Set<IZkChildListener> listeners = this.childListener.get(path);
            if (listeners != null) {
                listeners.remove(childListener);
            }
        }
    }

    public void subscribeDataChanges(String path, IZkDataListener listener) {
        synchronized (this.dataListener) {
            Set<IZkDataListener> listeners = this.dataListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkDataListener>();
                this.dataListener.put(path, listeners);
            }
            listeners.add(listener);
        }
        log.debug("Subscribed data changes for " + path);
    }

    public void unsubscribeDataChanges(String path, IZkDataListener dataListener) {
        synchronized (this.dataListener) {
            final Set<IZkDataListener> listeners = this.dataListener.get(path);
            if (listeners != null) {
                listeners.remove(dataListener);
            }
            if (listeners == null || listeners.isEmpty()) {
                this.dataListener.remove(path);
            }
        }
    }

    public void subscribeStateChanges(final IZkStateListener listener) {
        synchronized (this.stateListener) {
            this.stateListener.add(listener);
        }
    }

    public void unsubscribeStateChanges(IZkStateListener stateListener) {
        synchronized (this.stateListener) {
            this.stateListener.remove(stateListener);
        }
    }

    public void unsubscribeAll() {
        synchronized (childListener) {
            childListener.clear();
        }
        synchronized (this.dataListener) {
            this.dataListener.clear();
        }
        synchronized (this.stateListener) {
            this.stateListener.clear();
        }
    }

    //------------------------ protected --------------------------

    protected void processStateChanged(WatchedEvent event) {
        log.info("zookeeper state changed (" + event.getState() + ")");
        setCurrentState(event.getState());

        fireStateChangedEvent(event.getState());
        if (event.getState() == Watcher.Event.KeeperState.Expired) {
            try {
                reconnect();
                fireNewSessionEvents();
            } catch (final Exception e) {
                log.info("Unable to re-establish connection. Notifying consumer of the following exception: ", e);
                fireSessionEstablishmentError(e);
            }
        }
    }

    protected void processDataOrChildChange(WatchedEvent event) {
        final String path = event.getPath();

        if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged
                || event.getType() == Watcher.Event.EventType.NodeCreated
                || event.getType() == Watcher.Event.EventType.NodeDeleted) {
            Set<IZkChildListener> childListeners = this.childListener.get(path);
            if (childListeners != null && !childListeners.isEmpty()) {
                fireChildChangedEvents(path, childListeners);
            }
        }

        if (event.getType() == Watcher.Event.EventType.NodeDataChanged
                || event.getType() == Watcher.Event.EventType.NodeDeleted
                || event.getType() == Watcher.Event.EventType.NodeCreated) {
            Set<IZkDataListener> listeners = this.dataListener.get(path);
            if (listeners != null && !listeners.isEmpty()) {
                fireDataChangedEvents(event.getPath(), listeners);
            }
        }
    }

    /**
     * 判断节点上是否有监听
     *
     * @param path
     * @return
     */
    protected boolean hasListeners(String path) {
        Set<IZkDataListener> dataListeners = this.dataListener.get(path);
        if (dataListeners != null && dataListeners.size() > 0) {
            return true;
        }
        Set<IZkChildListener> childListeners = childListener.get(path);
        if (childListeners != null && childListeners.size() > 0) {
            return true;
        }
        return false;
    }

    //------------------------ private --------------------------

    protected void fireAllEvents() {
        for (Map.Entry<String, Set<IZkChildListener>> entry : childListener.entrySet()) {
            fireChildChangedEvents(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Set<IZkDataListener>> entry : dataListener.entrySet()) {
            fireDataChangedEvents(entry.getKey(), entry.getValue());
        }
    }

    private void fireStateChangedEvent(final Watcher.Event.KeeperState state) {
        for (final IZkStateListener _stateListener : stateListener) {
            eventThread.send(new ZkEvent("State changed to " + state + " sent to " + _stateListener) {
                public void run() throws Exception {
                    _stateListener.handleStateChanged(state);
                }
            });
        }
    }

    private void fireNewSessionEvents() {
        for (final IZkStateListener _stateListener : stateListener) {
            eventThread.send(new ZkEvent("New session event sent to " + _stateListener) {
                public void run() throws Exception {
                    _stateListener.handleNewSession();
                }
            });
        }
    }

    private void fireSessionEstablishmentError(final Throwable error) {
        for (final IZkStateListener _stateListener : stateListener) {
            eventThread.send(new ZkEvent("Session establishment error(" + error + ") sent to " + _stateListener) {
                public void run() throws Exception {
                    _stateListener.handleSessionEstablishmentError(error);
                }
            });
        }
    }

    private void fireChildChangedEvents(final String path, Set<IZkChildListener> childListeners) {
        try {
            // reinstall the watch
            for (final IZkChildListener listener : childListeners) {
                eventThread.send(new ZkEvent("Children of " + path + " changed sent to " + listener) {
                    @Override
                    public void run() throws Exception {
                        try {
                            // if the node doesn't exist we should listen for the root node to reappear
                            exists(path);
                            List<String> children = getChildren(path);
                            listener.handleChildChange(path, children);
                        } catch (ZkNoNodeException e) {
                            listener.handleChildChange(path, null);
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to fire child changed event. Unable to getChildren. exception =>" + e.getMessage());
        }
    }

    private void fireDataChangedEvents(final String path, Set<IZkDataListener> listeners) {
        for (final IZkDataListener listener : listeners) {
            eventThread.send(new ZkEvent("Data of " + path + " changed sent to " + listener) {
                public void run() throws Exception {
                    // reInstall watch
                    exists(path, true);
                    try {
                        Object data = readData(path, null, true);
                        listener.handleDataChange(path, data);
                    } catch (Exception e) {
                        listener.handleDataDeleted(path);
                    }
                }
            });
        }
    }

    abstract public List<String> getChildren(String path);

    private void setCurrentState(Watcher.Event.KeeperState currentState) {
        this.currentState = currentState;
    }

}
