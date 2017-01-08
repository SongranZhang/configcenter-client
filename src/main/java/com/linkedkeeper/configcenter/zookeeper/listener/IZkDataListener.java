package com.linkedkeeper.configcenter.zookeeper.listener;

/**
 * An {@link IZkDataListener} can be registered at a {@ZkClient} for listening on zk data changes for a given path.
 * <p/>
 * Node: Also this listener re-subscribes it watch for the path on each zk event (zk watches are one-timers) is is not
 * guaranteed that events on the path are missing (see http://zookeeper.wiki.sourceforge.net/ZooKeeperWatches). An
 * implementation of this class should take that into account.
 * <p/>
 * Created by frank@linkedkeeper.com on 17/1/8.
 */

public interface IZkDataListener {

    void handleDataChange(String dataPath, Object data) throws Exception;

    void handleDataDeleted(String dataPath) throws Exception;

}
