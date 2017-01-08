package com.linkedkeeper.configcenter.zookeeper.serialize;

import com.linkedkeeper.configcenter.zookeeper.exception.ZkException;

import java.io.*;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class SerializableSerializer implements ZkSerializer {

    public byte[] serialize(Object data) {
        ObjectOutputStream stream = null;
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(byteArray);
            stream.writeObject(data);
            return byteArray.toByteArray();
        } catch (IOException e) {
            throw new ZkException(e);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
            }
        }
    }

    public Object deserialize(byte[] bytes) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = inputStream.readObject();
            return object;
        } catch (ClassNotFoundException e) {
            throw new ZkException("Unable to find object class.", e);
        } catch (IOException e) {
            throw new ZkException(e);
        }
    }
}
