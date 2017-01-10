/*
 * Copyright (c) 2016, LinkedKeeper
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of LinkedKeeper nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
