package com.linkedkeeper.configcenter.zookeeper;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ZkLock extends ReentrantLock {

    private static final long serialVersionUID = 1L;

    private Condition dataChangedCondition = newCondition();
    private Condition stateChangedCondition = newCondition();
    private Condition zNodeEventCondition = newCondition();

    /**
     * This condition will be signaled if a zookeeper event was processed and the event contains a data/child change.
     *
     * @return the condition.
     */

    public Condition getDataChangedCondition() {
        return dataChangedCondition;
    }

    /**
     * This condition will be signaled if a zookeeper event was processed and the event contains a state change
     * (connected, disconnected, session expired, etc ...).
     *
     * @return the condition.
     */
    public Condition getStateChangedCondition() {
        return stateChangedCondition;
    }

    /**
     * This condition will be signaled if any znode related zookeeper event was received.
     *
     * @return the condition.
     */
    public Condition getZNodeEventCondition() {
        return zNodeEventCondition;
    }
}
