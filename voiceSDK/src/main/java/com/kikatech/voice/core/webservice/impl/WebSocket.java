package com.kikatech.voice.core.webservice.impl;

import android.text.TextUtils;

import com.kikatech.voice.core.webservice.IWebSocket;
import com.kikatech.voice.core.webservice.data.SendingData;
import com.kikatech.voice.core.webservice.data.SendingDataByte;
import com.kikatech.voice.core.webservice.data.SendingDataString;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.helper.MsgHelper;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration.ConnectionConfiguration;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.kikatech.voice.core.webservice.impl.WebSocket.SocketState.CONNECTED;
import static com.kikatech.voice.core.webservice.impl.WebSocket.SocketState.CONNECTING;
import static com.kikatech.voice.core.webservice.impl.WebSocket.SocketState.DISCONNECTED;

/**
 * Created by tianli on 17-10-28.
 */

public class WebSocket implements IWebSocket {
    private static final String VERSION = "3";

    private static final int WEB_SOCKET_CONNECT_TIMEOUT = 5000;
    private static final int MAX_RECONNECT_TIME = 3;

    private static final int HEARTBEAT_DURATION = 10 * 1000;

    private VoiceWebSocketClient mClient;
    private OnWebSocketListener mListener;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private AtomicBoolean mReleased = new AtomicBoolean(false);
    private int mReconnectTimes = 0;

    private final LinkedList<SendingData> mSendBuffer = new LinkedList<>();

    private VoiceConfiguration mVoiceConfiguration;
    private Timer mTimer;

    private MsgHelper mMsgHelper = new MsgHelper();;

    private SocketState mSocketState = DISCONNECTED;
    enum SocketState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }

    public WebSocket(OnWebSocketListener l) {
        mListener = l;
    }

    @Override
    public void connect(VoiceConfiguration voiceConfiguration) {
        Logger.d("connect");
        if (mReleased.get()) {
            Logger.e("WebSocket already released, can not connect again");
            return;
        }
        mVoiceConfiguration = voiceConfiguration;
        mMsgHelper.registerMessage(mVoiceConfiguration);

        final ConnectionConfiguration conf = voiceConfiguration.getConnectionConfiguration();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mClient != null && (mClient.isConnecting() || mClient.isOpen())) {
                    Logger.w("WebSocket is connecting or it's already connected.");
                    return;
                }
                Map<String, String> httpHeaders = new HashMap<>();
                httpHeaders.put("version", VERSION);
                httpHeaders.put("sign", conf.sign);
                httpHeaders.put("User-Agent", conf.userAgent);
                httpHeaders.put("lang", conf.locale);
                httpHeaders.put("locale", conf.locale);
                httpHeaders.put("engine", conf.engine);
                httpHeaders.put("app-name", conf.appName);

                AsrConfiguration asrConf = conf.getAsrConfiguration();
                if (asrConf != null) {
                    httpHeaders.put("spellingEnabled", String.valueOf(asrConf.getSpellingEnabled()));
                    httpHeaders.put("emojiEnabled", String.valueOf(asrConf.getEmojiEnabled()));
                    httpHeaders.put("punctuationEnabled", String.valueOf(asrConf.getPunctuationEnabled()));
                    httpHeaders.put("vprEnabled", String.valueOf(asrConf.getVprEnabled()));
                    httpHeaders.put("eosPackets", String.valueOf(asrConf.getEosPackets()));
                }
                for (String key : conf.bundle.keySet()) {
                    httpHeaders.put(key, conf.bundle.getString(key));
                }
                Draft draft = new Draft_6455();
                try {
                    Logger.d("WebSocket connect url = " + conf.url);
                    logTheHttpHeaders(httpHeaders);
                    mClient = new VoiceWebSocketClient(new URI(conf.url), draft,
                            httpHeaders, WEB_SOCKET_CONNECT_TIMEOUT);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                mClient.connect();
                mSocketState = CONNECTING;
            }
        });
    }

    private void logTheHttpHeaders(Map<String, String> httpHeaders) {
        Logger.d("-------- http headers --------");
        for (String key : httpHeaders.keySet()) {
            Logger.d(key + " : " + httpHeaders.get(key));
        }
        Logger.d("------------------------------");
    }

    @Override
    public void release() {
        Logger.d("release");
        if (mReleased.compareAndSet(false, true)) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mClient != null) {
                        mClient.close();
                    }
                    mClient = null;

                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    mTimer = null;

                    mSendBuffer.clear();

                    mSocketState = DISCONNECTED;
                }
            });
            mMsgHelper.unregisterMessage();
        }
    }

    @Override
    public void startListening() {

    }

    @Override
    public void stopListening() {

    }

    @Override
    public void sendData(byte[] data) {
        if (mReleased.get()) {
            Logger.e("WebSocket already released, ignore data");
            if (mListener != null) {
                mListener.onWebSocketClosed();
            }
            return;
        }
        final byte[] sendingData = new byte[data.length];
        System.arraycopy(data, 0, sendingData, 0, data.length);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                checkConnectionAndSend(new SendingDataByte(sendingData));
            }
        });
    }

    @Override
    public void sendCommand(final String command, final String payload) {
        if (mReleased.get()) {
            Logger.e("WebSocket already released, ignore command ");
            if (mListener != null) {
                mListener.onWebSocketClosed();
            }
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String jsonCommand = genCommand(command, payload);
                Logger.d("Send command : " + jsonCommand);
                if (TextUtils.isEmpty(jsonCommand)) {
                    Logger.e("Send command error : generate command failed.");
                    return;
                }
                checkConnectionAndSend(new SendingDataString(jsonCommand));
            }
        });
    }

    @Override
    public boolean isConnected() {
        return mSocketState == CONNECTED;
    }

    private String genCommand(String command, String payload) {
        try {
            JSONObject cmdObj = new JSONObject();
            cmdObj.put("cmd", command);
            if (!TextUtils.isEmpty(payload)) {
                cmdObj.put("payload", payload);
            }
            return cmdObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Message handleMessage(String msg) {
        try {
            JSONObject json = new JSONObject(msg);
            Message message = mMsgHelper.create(json.optString("type", "NONE"));
            if (message != null) {
                message.fromJson(json);
            }
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.w("WebSocket parseMessage error: " + e.getMessage());
        }
        return null;
    }

    private void startHeartBeatTimer() {
        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                if (mSocketState == CONNECTED) {
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendCommand("HEART-BEAT", "");
                        }
                    }, HEARTBEAT_DURATION, HEARTBEAT_DURATION);
                }
            }
        });
    }

    private void changeState(final SocketState socketState) {
        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                mSocketState = socketState;

                if (mSocketState == DISCONNECTED) {
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }
        });
    }

    private void sendRemindData() {
        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                while (!mSendBuffer.isEmpty()) {
                    SendingData sendingData = mSendBuffer.getFirst();
                    boolean success = sendingData != null && sendingData.send(mClient);
                    if (success) {
                        mSendBuffer.pollFirst();
                    } else {
                        break;
                    }
                }
            }
        });
    }

    private void checkConnectionAndSend(SendingData data) {
        boolean success = false;
        if (mSocketState == CONNECTED) {
            success = data.send(mClient);
        }

        if (!success) {
            mSendBuffer.add(data);
            if (mSocketState == DISCONNECTED) {
                connect(mVoiceConfiguration);
            }
        }
    }

    private class VoiceWebSocketClient extends WebSocketClient {

        VoiceWebSocketClient(URI serverUri, Draft protocolDraft,
                             Map<String, String> httpHeaders, int connectTimeout) {
            super(serverUri, protocolDraft, httpHeaders, connectTimeout);
        }

        @Override
        public void onOpen(ServerHandshake handshakeData) {
            if (mReleased.get()) {
                Logger.v("onOpen, but has been released");
                return;
            }
            Logger.d("onOpen");
            changeState(CONNECTED);
            startHeartBeatTimer();
            sendRemindData();

            mReconnectTimes = 0;
        }

        @Override
        public void onMessage(String message) {
            if (mReleased.get()) {
                Logger.v("onMessage, but has been released");
                return;
            }
            Logger.d("onMessage message = " + message);
            final Message msg = handleMessage(message);
            if (msg != null && mListener != null) {
                mListener.onMessage(msg);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (mReleased.get()) {
                Logger.v("onClose, but has been released");
                return;
            }
            Logger.d("onClose code = [" + code + "]");
            changeState(DISCONNECTED);
            if (!reconnect() && !mReleased.get()) {
                if (mListener != null) {
                    mListener.onWebSocketClosed();
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            if (mReleased.get()) {
                Logger.v("onError, but has been released");
                return;
            }
            Logger.w("onError");
            ex.printStackTrace();
            changeState(DISCONNECTED);
            if (!reconnect() && !mReleased.get()) {
                if (mListener != null) {
                    mListener.onWebSocketError();
                }
            }
        }

        private boolean reconnect() {
            Logger.d("reconnect mReconnectTimes = " + mReconnectTimes);
            if (mReconnectTimes < MAX_RECONNECT_TIME && !mReleased.get()) {
                mReconnectTimes++;
                WebSocket.this.connect(mVoiceConfiguration);
                return true;
            }
            return false;
        }
    }
}