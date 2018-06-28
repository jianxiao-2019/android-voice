package com.kikatech.voice.core.webservice.impl;

import com.kikatech.voice.core.webservice.IWebSocket;
import com.kikatech.voice.core.webservice.data.SendingData;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SkeeterWang Created on 2018/6/14.
 */

public abstract class BaseWebSocket implements IWebSocket {

    protected final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    protected final LinkedList<SendingData> mSendBuffer = new LinkedList<>();

    protected OnWebSocketListener mListener;

    @Override
    public void setListener(OnWebSocketListener listener) {
        this.mListener = listener;
    }
}
