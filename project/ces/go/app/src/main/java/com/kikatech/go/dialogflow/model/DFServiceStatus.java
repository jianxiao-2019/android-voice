package com.kikatech.go.dialogflow.model;

import java.io.Serializable;

/**
 * @author SkeeterWang Created on 2017/12/29.
 */

public class DFServiceStatus implements Serializable {
    private boolean isInit;
    private boolean isAsrEnabled;
    private boolean isAwake;
    private boolean isUsbDeviceAvailable;
    private Boolean isUsbDeviceDataCorrect = null; // using Boolean to check if the status set or not

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

    public boolean isUsbDeviceAvailable() {
        return isUsbDeviceAvailable;
    }

    public void setUsbDeviceAvailable(boolean isUsbDeviceAvailable) {
        this.isUsbDeviceAvailable = isUsbDeviceAvailable;
    }

    public Boolean isUsbDeviceDataCorrect() {
        return isUsbDeviceDataCorrect;
    }

    public void setUsbDeviceDataCorrect(boolean isCorrect) {
        this.isUsbDeviceDataCorrect = isCorrect;
    }
}
