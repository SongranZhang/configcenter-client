package com.linkedkeeper.configcenter.zookeeper.serialize;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public interface ZkSerializer {

    byte[] serialize(Object data);

    Object deserialize(byte[] bytes);

}
