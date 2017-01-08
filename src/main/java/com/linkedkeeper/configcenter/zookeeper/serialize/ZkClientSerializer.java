package com.linkedkeeper.configcenter.zookeeper.serialize;

import com.linkedkeeper.configcenter.zookeeper.IZkClient;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public abstract class ZkClientSerializer implements IZkClient {

    protected ZkSerializer zkSerializer;

    protected byte[] serialize(Object data) {
        return zkSerializer.serialize(data);
    }

    protected <T extends Object> T deserialize(byte[] data) {
        return (data == null || data.length == 0) ? null : (T) zkSerializer.deserialize(data);
    }
}
