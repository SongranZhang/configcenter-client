package com.linkedkeeper.configcenter.recovery;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class RecoveryStore {

    private final Logger log = Logger.getLogger(RecoveryStore.class);

    public final String CONTENT_SEPARATOR = ":::";
    private final int maxDirCount = 100;

    private String recoveryFilePath = null;

    public void setRecoveryFilePath(String recoveryFilePath) {
        this.recoveryFilePath = recoveryFilePath;
    }

    /**
     * 将数据保存到本地磁盘
     *
     * @param key
     * @param value
     */
    public String save(String key, String value) {
        // 声称文件内容
        StringBuffer sb = new StringBuffer();
        sb.append(key);
        sb.append(CONTENT_SEPARATOR);
        sb.append(value);

        String fileName = UUID.randomUUID().toString();
        createDirIfNecessary(recoveryFilePath);
        String filePath = recoveryFilePath + File.separator + generateNum(fileName); // 2级目录
        try {
            saveFile(filePath, fileName, sb.toString());
        } catch (Exception e) {
            log.error("DataSyncStore save exception =>" + e.getMessage());
        }
        return null;
    }

    private boolean saveFile(String filePath, String fileName, String message) throws Exception {
        FileWriter fw = null;
        boolean success = false;
        // 文件全路径
        String fullFilePath = filePath + File.separator + fileName;
        try {
            File file = createFileIfNecessary(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            fw = new FileWriter(fullFilePath);
            fw.write(message, 0, message.length());
            fw.flush();
            success = true;
        } catch (Exception e) {
            throw new Exception("saveFile failure, filePath = " + fullFilePath, e);
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
        return success;
    }

    private long generateNum(String fileName) {
        char[] chars = fileName.toCharArray();
        StringBuffer tmp = new StringBuffer();
        int i = 0;
        for (char ch : chars) {
            if (Character.isDigit(ch)) {
                tmp.append(ch);
                i++;
                if (i == 6) {
                    break;
                }
            }
        }
        long num = Long.parseLong(tmp.toString());
        return num % maxDirCount;
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
}
