package com.linkedkeeper.configcenter.recovery.impl;

import com.linkedkeeper.configcenter.client.ConfigCenterClient;
import com.linkedkeeper.configcenter.recovery.RecoveryStoreManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sauronzhang on 15/11/15.
 */
public class RecoveryStoreManagerImpl implements RecoveryStoreManager {

    private final String MAP_KEY_FILE_PATH = "filePath";
    private final String MAP_KEY_FILE_CONTENT = "fileContent";
    private final int MAX_COUNT_ONCE = 10;

    public final String CONTENT_SEPARATOR = ":::";

    private ConfigCenterClient client = null;
    private String recoveryFilePath = null;
    private File file = null;
    private List<Map<String, String>> list = null;
    private Integer registerNum = null;
    boolean success = false;

    public RecoveryStoreManagerImpl(ConfigCenterClient client, String recoveryFilePath) {
        this.client = client;
        this.recoveryFilePath = recoveryFilePath;
        this.file = new File(recoveryFilePath);
        this.list = new ArrayList<Map<String, String>>();
        this.registerNum = 1;
    }

    public void readFileData() throws Exception {
        int loopCount = 0;
        do {
            loopCount++;
            if (file.exists()) {
                readFirstFolder();
            }
        } while (loopCount <= MAX_COUNT_ONCE && !CollectionUtils.isEmpty(list));
    }

    private void readFirstFolder() throws Exception {
        // 获取1级目录
        String[] firstFolderList = file.list();
        if (firstFolderList != null && firstFolderList.length > 0) {
            for (int i = 0, firstLen = firstFolderList.length; registerNum <= MAX_COUNT_ONCE && i < firstLen; i++) {
                readSecondFolder(recoveryFilePath + File.separator + firstFolderList[i]);
            }
        }
    }

    private void readSecondFolder(String secondFolder) throws Exception {
        // 获取2级目录
        File subFile = new File(secondFolder);
        String[] secondFolderList = subFile.list();
        if (secondFolderList != null && secondFolderList.length > 0) {
            for (int j = 0, secondLen = secondFolderList.length; registerNum < MAX_COUNT_ONCE && j < secondLen; j++) {
                readFile(new File(secondFolder + File.separator + secondFolderList[j]));
            }
        } else {
            subFile.delete();
        }
    }

    private void readFile(File readFile) throws Exception {
        // 获取文件
        if (readFile.exists()) {
            try {
                Map<String, String> map = new HashMap<String, String>(2);
                map.put(MAP_KEY_FILE_PATH, readFile.getAbsolutePath());
                map.put(MAP_KEY_FILE_CONTENT, readData(readFile));

                list.add(map);
                registerNum++;

                readFile.delete();
                success = true;
            } catch (Exception e) {
                success = false;
            }
        }
    }

    private String readData(File file) throws Exception {
        BufferedReader reader = null;
        StringBuffer rs = new StringBuffer();
        try {
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                String tmp;
                while ((tmp = reader.readLine()) != null) {
                    rs.append(tmp);
                }
            }
        } catch (Exception e) {

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return rs.toString();
    }

    public void reSendData() throws Exception {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (Map<String, String> map : list) {
            if (CollectionUtils.isEmpty(map)) {
                continue;
            }

            String path = map.get(MAP_KEY_FILE_PATH);
            String content = map.get(MAP_KEY_FILE_CONTENT);
            if (StringUtils.isNotBlank(content)) {
                try {
                    String[] valueArray = content.split(CONTENT_SEPARATOR);
                    String key = valueArray[0];
                    String value = valueArray[1];

                    client.set(key, value);
                    deleteFile(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteFile(String filePath) {
        try {
            if (StringUtils.isNotBlank(filePath)) {
                File file = new File(filePath);
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {

        }
    }
}
