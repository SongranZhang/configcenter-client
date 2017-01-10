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

package com.linkedkeeper.configcenter.client.impl;


import com.linkedkeeper.configcenter.client.ConfigCenter;
import com.linkedkeeper.configcenter.recovery.RecoveryFallWatch;
import com.linkedkeeper.configcenter.recovery.RecoveryStore;
import com.linkedkeeper.configcenter.snapshot.SnapshotFallWatch;
import com.linkedkeeper.configcenter.snapshot.SnapshotStore;
import com.linkedkeeper.configcenter.snapshot.SnapshotSyncWatch;
import com.linkedkeeper.configcenter.snapshot.impl.SnapshotStoreImpl;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ConfigCenterImpl implements ConfigCenter {

    private BaseConfigCenterClient client = null;

    public ConfigCenterImpl(BaseConfigCenterClient client) {
        this.client = client;
    }

    private SnapshotFallWatch snapshotFallWatch = null;
    private SnapshotStore snapshotStore = null;
    private SnapshotSyncWatch snapshotSyncWatch = null;
    private RecoveryFallWatch recoveryFallWatch = null;
    private RecoveryStore recoveryStore = null;

    public void initZookeeper() {
        this.client.initZookeeper();
        this.client.initZkStateListeners();
        this.client.initZkDataListeners();
        this.client.initZkChildListeners();
    }

    public synchronized SnapshotStore getSnapshotStore() {
        if (snapshotStore == null) {
            snapshotStore = new SnapshotStoreImpl();
        }
        return snapshotStore;
    }

    public synchronized SnapshotFallWatch getSnapshotFallWatch() {
        if (snapshotFallWatch == null) {
            snapshotFallWatch = new SnapshotFallWatch(client);
            snapshotFallWatch.setDaemon(true);
        }
        return snapshotFallWatch;
    }

    public synchronized SnapshotSyncWatch getSnapshotSyncWatch() {
        if (snapshotSyncWatch == null) {
            snapshotSyncWatch = new SnapshotSyncWatch(client, snapshotStore);
            snapshotSyncWatch.setDaemon(true);
        }
        return snapshotSyncWatch;
    }

    public RecoveryFallWatch getRecoveryFallWatch() {
        if (recoveryFallWatch == null) {
            recoveryFallWatch = new RecoveryFallWatch(client);
            recoveryFallWatch.setDaemon(true);
        }
        return recoveryFallWatch;
    }

    public RecoveryStore getRecoveryStore() {
        if (recoveryStore == null) {
            recoveryStore = new RecoveryStore();
        }
        return recoveryStore;
    }
}
