package com.kikatech.voice.core.webservice;

import android.text.TextUtils;

import com.kikatech.voice.core.webservice.data.SendingData;
import com.kikatech.voice.core.webservice.data.SendingDataByte;
import com.kikatech.voice.core.webservice.data.SendingDataString;
import com.kikatech.voice.core.webservice.message.Message;
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

import static com.kikatech.voice.core.webservice.WebSocket.SocketState.CONNECTED;
import static com.kikatech.voice.core.webservice.WebSocket.SocketState.CONNECTING;
import static com.kikatech.voice.core.webservice.WebSocket.SocketState.DISCONNECTED;

/**
 * Created by tianli on 17-10-28.
 */

public class WebSocket {
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

    private ConnectionConfiguration mConf;
    private Timer mTimer;

    private SocketState mSocketState = DISCONNECTED;
    enum SocketState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }

    public static WebSocket openConnection(OnWebSocketListener l) {
        return new WebSocket(l);
    }

    public interface OnWebSocketListener {
        void onMessage(Message message);

        void onWebSocketClosed();

        void onWebSocketError();
    }

    private WebSocket(OnWebSocketListener l) {
        mListener = l;
    }

    public void connect(final ConnectionConfiguration conf) {
        if (mReleased.get()) {
            Logger.e("WebSocket already released, can not connect again");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mClient != null && (mClient.isConnecting() || mClient.isOpen())) {
                    Logger.i("WebSocket is connecting.");
                    return;
                }
                mConf = conf;
                Map<String, String> httpHeaders = new HashMap<>();
                httpHeaders.put("version", VERSION);
                httpHeaders.put("sign", conf.sign);
                httpHeaders.put("User-Agent", conf.userAgent);
                httpHeaders.put("lang", conf.locale);
                httpHeaders.put("locale", conf.locale);
                httpHeaders.put("engine", conf.engine);
                httpHeaders.put("app-name", conf.appName);
                Logger.d("appName = " + conf.appName);

                AsrConfiguration asrConf = conf.getAsrConfiguration();
                if (asrConf != null) {
                    httpHeaders.put("spellingEnabled", String.valueOf(asrConf.getSpellingEnabled()));
                    httpHeaders.put("alterEnabled", String.valueOf(asrConf.getAlterEnabled()));
                    httpHeaders.put("emojiEnabled", String.valueOf(asrConf.getEmojiEnabled()));
                    httpHeaders.put("punctuationEnabled", String.valueOf(asrConf.getPunctuationEnabled()));
                    httpHeaders.put("vprEnabled", String.valueOf(asrConf.getVprEnabled()));
                    httpHeaders.put("eosPackets", String.valueOf(asrConf.getEosPackets()));
                    Logger.d("sign = " + conf.sign + " agent = " + conf.userAgent + " engine = " + conf.engine);
                    Logger.d("alterEnabled = " + asrConf.getAlterEnabled()
                            + " spellingEnabled = " + asrConf.getSpellingEnabled()
                            + " emojiEnabled = " + asrConf.getEmojiEnabled()
                            + " punctuationEnabled = " + asrConf.getPunctuationEnabled()
                            + " vprEnabled = " + asrConf.getVprEnabled()
                            + " eosPackets = " + asrConf.getEosPackets()
                    );
                }
                for (String key : conf.bundle.keySet()) {
                    httpHeaders.put(key, conf.bundle.getString(key));
                    Logger.d(key + " = " + conf.bundle.getString(key));
                }
                Draft draft = new Draft_6455();
                try {
                    Logger.i("WebSocket connect url = " + conf.url);
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

    public void release() {
        if (mReleased.compareAndSet(false, true)) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mClient != null) {
                        mClient.close();
                    }
                    mClient = null;
                    mSendBuffer.clear();

                    mSocketState = DISCONNECTED;
                }
            });
        }
    }

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

    public void sendCommand(final String command, final String payload) {
        if (mReleased.get()) {
            Logger.e("WebSocket already released, ignore data");
            if (mListener != null) {
                mListener.onWebSocketClosed();
            }
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String jsonCommand = genCommand(command, payload);
                Logger.i("1qaz sendCommand " + jsonCommand);
                if (TextUtils.isEmpty(jsonCommand)) {
                    Logger.e("Send command error : generate command failed.");
                    return;
                }
                checkConnectionAndSend(new SendingDataString(jsonCommand));
            }
        });
    }

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
            Message message = Message.create(json.optString("type", "NONE"));
            if (message != null) {
                message.fromJson(json);
            }
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.i("WebSocket parseMessage error: " + e.getMessage());
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
                connect(mConf);
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
            Logger.i("VoiceWebSocketClient onOpen mListener = " + mListener);
            changeState(CONNECTED);
            startHeartBeatTimer();
            sendRemindData();

            mReconnectTimes = 0;
        }

        @Override
        public void onMessage(String message) {
            Logger.i("VoiceWebSocketClient onMessage message = " + message);
            final Message msg = handleMessage(message);
            if (msg != null && mListener != null) {
                mListener.onMessage(msg);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Logger.i("VoiceWebSocketClient onClose code = [" + code + "]");
            changeState(DISCONNECTED);
            if (!reconnect() && !mReleased.get()) {
                if (mListener != null) {
                    mListener.onWebSocketClosed();
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            Logger.w("VoiceWebSocketClient onError ex = " + ex);
            ex.printStackTrace();
            changeState(DISCONNECTED);
            if (!reconnect() && !mReleased.get()) {
                if (mListener != null) {
                    mListener.onWebSocketError();
                }
            }
        }

        private boolean reconnect() {
            if (mReconnectTimes < MAX_RECONNECT_TIME && !mReleased.get()) {
                mReconnectTimes++;
                WebSocket.this.connect(mConf);
                return true;
            }
            return false;
        }
    }
}