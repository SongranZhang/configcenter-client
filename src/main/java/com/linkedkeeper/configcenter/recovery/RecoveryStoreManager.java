package com.linkedkeeper.configcenter.recovery;

/**
 * Created by sauronzhang on 15/11/15.
 */
public interface RecoveryStoreManager {

    void readFileData() throws Exception ;

    void reSendData() throws Exception;

}
