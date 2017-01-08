package com.linkedkeeper.configcenter.snapshot.impl;

import com.linkedkeeper.configcenter.snapshot.SnapshotStore;
import com.linkedkeeper.configcenter.utils.PropertiesUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by sauronzhang on 15/11/14.
 */
public class SnapshotStoreImpl implements SnapshotStore {

    private String storeFilePath = null;

    private final String STORE_FILENAME = "snapshot.properties";

    public void setStoreFilePath(String storeFilePath) {
        this.storeFilePath = storeFilePath;
    }

    public void storeSnapshot(Map<String, String> snapshot) throws IOException {
        // 先删除快照文件,再写入,为了防止长时间写入数据文件损坏
        File file = getDataFile();
        file.delete();

        PropertiesUtils.writeProperties(getDataFilePath(), snapshot);
    }

    public String readSnapshot(String key) throws IOException {
        return loadSnapshot().get(key);
    }

    private Map<String, String> loadSnapshot() throws IOException {
        return PropertiesUtils.readProperties(getDataFilePath());
    }

    private File getDataFile() throws IOException {
        createDirIfNecessary(storeFilePath);
        String filePath = getDataFilePath();
        File file = createFileIfNecessary(filePath);
        return file;
    }

    private void createDirIfNecessary(String path) {
        final File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private File createFileIfNecessary(String path) throws IOException {
        final File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private String getDataFilePath() {
        return storeFilePath.concat(File.separator).concat(STORE_FILENAME);
    }
}
