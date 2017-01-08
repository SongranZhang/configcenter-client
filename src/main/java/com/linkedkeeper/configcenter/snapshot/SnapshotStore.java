package com.linkedkeeper.configcenter.snapshot;

import java.io.IOException;
import java.util.Map;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public interface SnapshotStore {

    /**
     * set store file path
     *
     * @param storeFilePath
     */
    void setStoreFilePath(String storeFilePath);

    /**
     * store snapshot from files
     *
     * @throws IOException
     */
    void storeSnapshot(Map<String, String> snapshot) throws IOException;

    /**
     * read snapshot from files
     *
     * @throws IOException
     */
    String readSnapshot(String key) throws IOException;

}
