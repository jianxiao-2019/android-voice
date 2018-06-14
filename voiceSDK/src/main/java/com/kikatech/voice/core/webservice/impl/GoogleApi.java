package com.kikatech.voice.core.webservice.impl;

import android.text.TextUtils;

import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechGrpc;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.kikatech.voice.core.webservice.command.SocketCommand;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.BackgroundThread;
import com.kikatech.voice.util.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.okhttp.OkHttpChannelProvider;
import io.grpc.stub.StreamObserver;

/**
 * Created by ryanlin on 2018/5/24.
 */

public class GoogleApi extends BaseWebSocket {

    private static final String TAG = "GoogleApi";

    /**
     * We reuse an access token if its expiration time is longer than this.
     */
    private static final int ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000; // thirty minutes
    /**
     * We refresh the current access token before it expires.
     */
    private static final int ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000; // one minute

    private static final List<String> SCOPE = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");
    private static final String API_HOST = "speech.googleapis.com";
    private static final int API_HOST_PORT = 443;

    private SpeechGrpc.SpeechStub mApi;
    private StreamObserver<StreamingRecognizeRequest> mRequestObserver;
    private final StreamObserver<StreamingRecognizeResponse> mResponseObserver = new StreamObserver<StreamingRecognizeResponse>() {
        private long mCid = 0;

        @Override
        public void onNext(StreamingRecognizeResponse response) {
            if (isCanceled) {
                Logger.w(TAG, "recognition canceled");
                mCid = 0;
                return;
            }
            if (mListener == null) {
                Logger.w(TAG, "no listener available.");
                return;
            }
            ResultHolder holder = getResultHolder(response);
            if (holder == null) {
                if (Logger.DEBUG) {
                    Logger.w(TAG, "invalid result holder --- empty result");
                }
                mListener.onError(WebSocketError.EMPTY_RESULT);
                return;
            }
            if (Logger.DEBUG) {
                Logger.v(TAG, String.format("onNext, text: %s, isFinal: %s", holder.text, holder.isFinal));
            }
            Message msg;
            if (!holder.isFinal) { // Partial Result
                if (mCid == 0) {
                    mCid = System.currentTimeMillis();
                }
                msg = new IntermediateMessage(1, holder.text, "google", mCid);
            } else { // Final Result
                // TODO: process n-best result from #getResultHolder
                msg = new TextMessage(1, new String[]{holder.text}, "google", mCid);
                mCid = 0;
            }
            mListener.onMessage(msg);
        }

        /**
         * parsing recognized result from StreamingRecognizeResponse
         *
         * @return holder
         */
        private ResultHolder getResultHolder(StreamingRecognizeResponse response) {
            List<StreamingRecognitionResult> resultList = response != null ? response.getResultsList() : null;
            if (resultList != null && !resultList.isEmpty()) {
                for (StreamingRecognitionResult result : resultList) {
                    List<SpeechRecognitionAlternative> alternativeList = result.getAlternativesList();
                    if (alternativeList == null || alternativeList.isEmpty()) {
                        continue;
                    }
                    for (SpeechRecognitionAlternative alternative : alternativeList) {
                        String resultText = alternative.getTranscript();
                        if (TextUtils.isEmpty(resultText)) {
                            continue;
                        }
                        return new ResultHolder(resultText, result.getIsFinal());
                    }
                }
            }
            return null;
        }

        @Override
        public void onError(Throwable throwable) {
            if (Logger.DEBUG) {
                Logger.e(TAG, "Error calling the API.");
                Logger.printStackTrace(TAG, throwable.getMessage(), throwable);
            }
        }

        @Override
        public void onCompleted() {
            if (Logger.DEBUG) {
                Logger.i(TAG, "API completed.");
            }
        }

        class ResultHolder {
            private String text;
            private boolean isFinal;

            private ResultHolder(String text, boolean isFinal) {
                this.text = text;
                this.isFinal = isFinal;
            }
        }
    };

    private boolean isCanceled;

    public GoogleApi(OnWebSocketListener listener) {
        super(listener);
    }

    @Override
    public void connect(VoiceConfiguration voiceConfiguration) {
        fetchToken();
    }

    @Override
    public void release() {
        BackgroundThread.removeCallbacks(fetchTokenRunnable);
        // Release the gRPC channel.
        if (mApi != null) {
            final ManagedChannel channel = (ManagedChannel) mApi.getChannel();
            if (channel != null && !channel.isShutdown()) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Logger.e(TAG, "Error shutting down the gRPC channel.");
                    Logger.printStackTrace(TAG, e.getMessage(), e);
                }
            }
            mApi = null;
        }
    }

    @Override
    public void onStart() {
        if (mApi == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid api.");
            }
            return;
        }
        // Configure the API
        mRequestObserver = mApi.streamingRecognize(mResponseObserver);
        mRequestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(StreamingRecognitionConfig.newBuilder()
                        .setConfig(RecognitionConfig.newBuilder()
                                .setLanguageCode("en-US")
                                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                .setSampleRateHertz(16000)
                                .build())
                        .setInterimResults(true)
                        .setSingleUtterance(true)
                        .build())
                .build());
        isCanceled = false;
    }

    @Override
    public void onStop() {
        stop();
    }

    @Override
    public void sendCommand(String command, String payload) {
        if (Logger.DEBUG) {
            Logger.i(TAG, String.format("sendCommand, command: %s, payload: %s", command, payload));
        }
        switch (command) {
            case SocketCommand.STOP: // cancel
                isCanceled = true;
                stop();
                break;
            case SocketCommand.COMPLETE:
                stop();
                break;
        }
    }

    @Override
    public void sendData(byte[] data) {
        if (mRequestObserver == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid Request Observer");
            }
            return;
        }
        // Call the streaming recognition API
        mRequestObserver.onNext(StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(data, 0, data.length))
                .build());
    }

    @Override
    public boolean isConnected() {
        return mApi != null;
    }


    private void fetchToken() {
        if (Logger.DEBUG) {
            Logger.i(TAG, "fetchToken");
        }
        BackgroundThread.post(fetchTokenRunnable);
    }

    private Runnable fetchTokenRunnable = new Runnable() {
        @Override
        public void run() {
            doFetchToken();
        }

        private void doFetchToken() {
            AccessToken token = getAccessToken();
            if (token == null) {
                if (Logger.DEBUG) {
                    Logger.w(TAG, "invalid access token");
                }
                if (mListener != null) {
                    mListener.onError(WebSocketError.WEB_SOCKET_CLOSED);
                }
                return;
            }
            if (Logger.DEBUG) {
                Logger.d(TAG, String.format("token: %s", token));
            }
            initApi(token);
            // Schedule access token refresh before it expires
            long tokenExpiredTime = token.getExpirationTime().getTime() - System.currentTimeMillis() - ACCESS_TOKEN_FETCH_MARGIN;
            long fetchAgainDelayTime = Math.max(tokenExpiredTime, ACCESS_TOKEN_EXPIRATION_TOLERANCE);
            if (Logger.DEBUG) {
                Logger.d(TAG, String.format("fetch token again after %s ms", fetchAgainDelayTime));
            }
            BackgroundThread.postDelayed(this, fetchAgainDelayTime);
        }
    };

    private AccessToken getAccessToken() {
        try {
            // TODO: get auth json file from server
            final InputStream stream = new FileInputStream(new File("/sdcard/kikaVoiceSdk/google_speech"));
            final GoogleCredentials credentials = GoogleCredentials.fromStream(stream).createScoped(SCOPE);
            final AccessToken token = credentials != null ? credentials.refreshAccessToken() : null;
            if (Logger.DEBUG) {
                Logger.d(TAG, String.format("getAccessToken, token: %s", token));
            }
            return token;
        } catch (IOException e) {
            if (Logger.DEBUG) {
                Logger.e(TAG, "Failed to obtain access token.");
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    private void initApi(AccessToken token) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "initApi");
        }
        GoogleCredentials credentials = new GoogleCredentials(token).createScoped(SCOPE);
        GoogleCredentialsInterceptor interceptor = new GoogleCredentialsInterceptor(credentials);
        ManagedChannel channel = new OkHttpChannelProvider()
                .builderForAddress(API_HOST, API_HOST_PORT)
                .nameResolverFactory(new DnsNameResolverProvider())
                .intercept(interceptor)
                .build();
        mApi = SpeechGrpc.newStub(channel);
    }

    private void stop() {
        if (mRequestObserver == null) {
            return;
        }
        mRequestObserver.onCompleted();
        mRequestObserver = null;
    }

    /**
     * Authenticates the gRPC channel using the specified {@link GoogleCredentials}.
     */
    private static class GoogleCredentialsInterceptor implements ClientInterceptor {

        private final Credentials mCredentials;

        private Metadata mCached;

        private Map<String, List<String>> mLastMetadata;

        private GoogleCredentialsInterceptor(Credentials credentials) {
            mCredentials = credentials;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
                final Channel next) {
            return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(
                    next.newCall(method, callOptions)) {
                @Override
                protected void checkedStart(Listener<RespT> responseListener, Metadata headers)
                        throws StatusException {
                    Metadata cachedSaved;
                    URI uri = serviceUri(next, method);
                    synchronized (this) {
                        Map<String, List<String>> latestMetadata = getRequestMetadata(uri);
                        if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                            mLastMetadata = latestMetadata;
                            mCached = toHeaders(mLastMetadata);
                        }
                        cachedSaved = mCached;
                    }
                    headers.merge(cachedSaved);
                    delegate().start(responseListener, headers);
                }
            };
        }

        /**
         * Generate a JWT-specific service URI. The URI is simply an identifier with enough
         * information for a service to know that the JWT was intended for it. The URI will
         * commonly be verified with a simple string equality check.
         */
        private URI serviceUri(Channel channel, MethodDescriptor<?, ?> method)
                throws StatusException {
            String authority = channel.authority();
            if (authority == null) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Channel has no authority")
                        .asException();
            }
            // Always use HTTPS, by definition.
            final String scheme = "https";
            final int defaultPort = 443;
            String path = "/" + MethodDescriptor.extractFullServiceName(method.getFullMethodName());
            URI uri;
            try {
                uri = new URI(scheme, authority, path, null, null);
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI for auth")
                        .withCause(e).asException();
            }
            // The default port must not be present. Alternative ports should be present.
            if (uri.getPort() == defaultPort) {
                uri = removePort(uri);
            }
            return uri;
        }

        private URI removePort(URI uri) throws StatusException {
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), -1 /* port */,
                        uri.getPath(), uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI after removing port")
                        .withCause(e).asException();
            }
        }

        private Map<String, List<String>> getRequestMetadata(URI uri) throws StatusException {
            try {
                return mCredentials.getRequestMetadata(uri);
            } catch (IOException e) {
                throw Status.UNAUTHENTICATED.withCause(e).asException();
            }
        }

        private static Metadata toHeaders(Map<String, List<String>> metadata) {
            Metadata headers = new Metadata();
            if (metadata != null) {
                for (String key : metadata.keySet()) {
                    Metadata.Key<String> headerKey = Metadata.Key.of(
                            key, Metadata.ASCII_STRING_MARSHALLER);
                    for (String value : metadata.get(key)) {
                        headers.put(headerKey, value);
                    }
                }
            }
            return headers;
        }
    }
}
