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
