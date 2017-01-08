package com.linkedkeeper.configcenter.zookeeper.listener;

import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public interface IZkStateListener {

    /**
     * Called when the zookeeper connection state has changed.
     *
     * @param state The new state.
     * @throws Exception On any error.
     */
    void handleStateChanged(KeeperState state) throws Exception;

    /**
     * Called after the zookeeper session has expired and a new session has been created. You would have to re-create
     * any ephemeral nodes here.
     *
     * @throws Exception On any error.
     */
    void handleNewSession() throws Exception;

    /**
     * Called when a session cannot be re-established. This should be used to implement connection
     * failure handling e.g. retry to connect or pass the error up
     *
     * @param error The error that prevents a session from being established
     * @throws Exception On any error.
     */
    void handleSessionEstablishmentError(final Throwable error) throws Exception;

}
