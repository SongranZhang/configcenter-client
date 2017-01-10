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

import com.linkedkeeper.configcenter.client.impl.BaseConfigCenterClient;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by frannk@linkedkeeper.com on 17/1/8.
 */
public class RecoveryFallWatch extends Thread {

    private BaseConfigCenterClient client = null;

    private final Logger log = Logger.getLogger(RecoveryFallWatch.class);

    private Map<String, DataCell> fallWatchQueue = new HashMap<String, DataCell>(10000);

    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    private final int sleepTime = 1 * 1000; // 1秒
    private final int expireTime = 10 * 60 * 1000; // 10分钟
    private final int maxCount = 3;

    public RecoveryFallWatch(BaseConfigCenterClient client) {
        log.error("ConfigCenterClient/RecoveryFallWatch init Thread -> " + this.toString());
        this.client = client;
    }

    public boolean writeData(String key, String value) {
        lock.lock();
        try {
            fallWatchQueue.put(key, new DataCell(value));
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
        return true;
    }

    public void run() {
        for (; ; ) {
            if (lock.tryLock()) {
                try {
                    if (fallWatchQueue.isEmpty()) {
                        notEmpty.await();
                    } else {
                        writeDataToZk();
                    }
                } catch (Exception e) {
                    log.error("RecoveryFallWatch run Thread (" + this.toString() + ")  failure, exception =>" + e.getMessage());
                } finally {
                    lock.unlock();
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    private void writeDataToZk() {
        Set<Map.Entry<String, DataCell>> allSet = fallWatchQueue.entrySet();
        for (Iterator<Map.Entry<String, DataCell>> it = allSet.iterator(); it.hasNext(); ) {
            Map.Entry<String, DataCell> me = it.next();
            DataCell cell = me.getValue();
            if (cell != null) {
                try {
                    client.writeDataToZk(me.getKey(), cell.getValue());
                    it.remove();
                } catch (Exception e) {
                    log.error("RecoveryFallWatch writeDataToZk key = {" + me.getKey() + "} exception =>" + e.getMessage());
                    // recovery
                    if (cell.getNum() > maxCount || diff(cell.getExpire())) {
                        try {
                            // 保存到本地
                            client.writeDataToRecovery(me.getKey(), cell.getValue());
                            it.remove();
                        } catch (Exception ex) {
                            log.error("RecoveryFallWatch writeDataToRecovery exception =>" + ex.getMessage());
                        }
                    }
                    cell.setNum(cell.getNum() + 1);
                }
            }
        }
    }

    private boolean diff(Date date) {
        Date now = new Date();
        Date expire = new Date(now.getTime() - expireTime);
        return date.before(expire);
    }

    class DataCell {

        private String value;
        private int num;
        private Date expire;

        public DataCell(String value) {
            this.value = value;
            this.num = 1;
            this.expire = new Date();
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public Date getExpire() {
            return expire;
        }

        public void setExpire(Date expire) {
            this.expire = expire;
        }
    }

}
