package com.kikatech.go.dialogflow.model;

import java.io.Serializable;

/**
 * @author SkeeterWang Created on 2017/12/29.
 */

public class DFServiceStatus implements Serializable {
    private boolean isInit;
    private boolean isAsrEnabled;
    private boolean isAwake;
    private byte connectionStatus;

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public boolean isAsrEnabled() {
        return isAsrEnabled;
    }

    public void setAsrEnabled(boolean asrEnabled) {
        isAsrEnabled = asrEnabled;
    }

    public boolean isAwake() {
        return isAwake;
    }

    public void setAwake(boolean awake) {
        isAwake = awake;
    }

    public byte getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(byte connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
}
