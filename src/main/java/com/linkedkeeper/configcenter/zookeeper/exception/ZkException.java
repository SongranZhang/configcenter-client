package com.linkedkeeper.configcenter.zookeeper.exception;

import org.apache.zookeeper.KeeperException;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ZkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZkException() {
        super();
    }

    public ZkException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZkException(String message) {
        super(message);
    }

    public ZkException(Throwable cause) {
        super(cause);
    }

    public static ZkException create(KeeperException e) {
        switch (e.code()) {
            case NONODE:
                return new ZkNoNodeException(e);
            default:
                return new ZkException(e);
        }
    }
}
