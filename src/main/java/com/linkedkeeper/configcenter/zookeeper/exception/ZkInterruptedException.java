package com.linkedkeeper.configcenter.zookeeper.exception;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ZkInterruptedException extends ZkException {

    public ZkInterruptedException() {
    }

    public ZkInterruptedException(String message) {
        super(message);
    }

    public ZkInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZkInterruptedException(Throwable cause) {
        super(cause);
    }
}
