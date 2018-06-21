package com.kikatech.voice.core.webservice.impl;

import com.kikatech.voice.core.webservice.IWebSocket;
import com.kikatech.voice.core.webservice.data.SendingData;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SkeeterWang Created on 2018/6/14.
 */

abstract class BaseWebSocket implements IWebSocket {

    final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    final LinkedList<SendingData> mSendBuffer = new LinkedList<>();

    OnWebSocketListener mListener;

    @Override
    public void setListener(OnWebSocketListener listener) {
        this.mListener = listener;
    }
}
