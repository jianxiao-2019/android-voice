package com.kikatech.voicesdktester.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.source.LocalMonoVoiceSource;
import com.kikatech.voicesdktester.source.LocalVoiceSource;
import com.kikatech.voicesdktester.ui.FileAdapter;
import com.kikatech.voicesdktester.utils.PreferenceUtil;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class WakeUpPlayBackMonoFragment extends Fragment {
//        implements
//        VoiceService.VoiceRecognitionListener,
//        VoiceService.VoiceStateChangedListener,
//        VoiceService.VoiceActiveStateListener,
//        LocalVoiceSource.EofListener,
//        FileAdapter.OnItemCheckedListener  {
//
//    private VoiceService mLocalVoiceService;
//    private LocalMonoVoiceSource mLocalVoiceSource;
//    private Handler mUiHandler;
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_voice_wake_up_test, container, true);
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        mTextView = (TextView) view.findViewById(R.id.status_text);
//        mResultText = (TextView) view.findViewById(R.id.result_text);
//
//        mStartButton = (Button) view.findViewById(R.id.button_start);
//        mStartButton.setOnClickListener(v -> {
//            if (mVoiceService != null) {
//                mVoiceService.start();
//            }
//        });
//
//        mPlayBackButton = (Button) view.findViewById(R.id.button_playback);
//        mPlayBackButton.setOnClickListener(v -> {
//            if (mLocalVoiceService != null) {
//                mLocalVoiceService.start();
//            }
//        });
//        mPlayBackButton.setEnabled(false);
//
//        mLocalVoiceSource = new LocalMonoVoiceSource();
//        mLocalVoiceSource.setEofListener(this);
//        mUiHandler = new Handler();
//
//        attachLocalService();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        Logger.i("onStop");
//        if (mLocalVoiceService != null) {
//            mLocalVoiceService.destroy();
//            mLocalVoiceService = null;
//        }
//    }
//
//    private void attachLocalService() {
//        if (mLocalVoiceService != null) {
//            mLocalVoiceService.destroy();
//            mLocalVoiceService = null;
//        }
//        // Debug
//        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
//        mAsrConfiguration = builder
//                .setAlterEnabled(false)
//                .setEmojiEnabled(false)
//                .setPunctuationEnabled(false)
//                .setSpellingEnabled(false)
//                .setVprEnabled(false)
//                .setEosPackets(9)
//                .build();
//        VoiceConfiguration conf = new VoiceConfiguration();
//        conf.setDebugFileTag(DEBUG_FILE_PATH);
//        conf.setIsDebugMode(true);
//        conf.setSupportWakeUpMode(true);
//        conf.source(mLocalVoiceSource);
//        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
//                .setAppName("KikaGoTest")
//                .setUrl(PreferenceUtil.getString(
//                        getActivity(),
//                        PreferenceUtil.KEY_SERVER_LOCATION,
//                        VoiceConfiguration.HostUrl.DEV_MVP))
//                .setLocale("en_US")
//                .setSign(RequestManager.getSign(getActivity()))
//                .setUserAgent(RequestManager.generateUserAgent(getActivity()))
//                .setEngine("google")
//                .setAsrConfiguration(mAsrConfiguration)
//                .build());
//        mLocalVoiceService = VoiceService.getService(getActivity(), conf);
//        mLocalVoiceService.setVoiceRecognitionListener(this);
//        mLocalVoiceService.setVoiceStateChangedListener(this);
//        mLocalVoiceService.setVoiceActiveStateListener(this);
//        mLocalVoiceService.create();
//    }
//
//    @Override
//    public void onEndOfFile() {
//        mUiHandler.postDelayed(() -> mLocalVoiceService.stop(), 500);
//    }
//
//    @Override
//    public void onItemChecked(String itemStr) {
//        if (mLocalVoiceSource != null) {
//            String path = DebugUtil.getDebugFolderPath();
//            if (TextUtils.isEmpty(path)) {
//                return;
//            }
//            mLocalVoiceSource.setTargetFile(path + itemStr);
//        }
//        if (mPlayBackButton != null) {
//            mPlayBackButton.setEnabled(true);
//        }
//    }
//
//    @Override
//    public void onNothingChecked() {
//        if (mPlayBackButton != null) {
//            mPlayBackButton.setEnabled(false);
//        }
//    }
//
//    @Override
//    public void onRecognitionResult(Message message) {
//
//    }
//
//    @Override
//    public void onCreated() {
//
//    }
//
//    @Override
//    public void onStartListening() {
//
//    }
//
//    @Override
//    public void onStopListening() {
//
//    }
//
//    @Override
//    public void onDestroyed() {
//
//    }
//
//    @Override
//    public void onError(int reason) {
//
//    }
//
//    @Override
//    public void onConnectionClosed() {
//
//    }
//
//    @Override
//    public void onSpeechProbabilityChanged(float prob) {
//
//    }
//
//    @Override
//    public void onWakeUp() {
//        Logger.d("onWakeUp");
//        String path = DebugUtil.getDebugFilePath();
//        mVoiceService.stop();
//        mVoiceService.sleep();
//        mTextView.setText("");
//        mResultText.setText("SUCCESS!");
//        mResultText.setTextColor(Color.GREEN);
//
//        mHandler.removeMessages(MSG_WAKE_UP_BOS);
//        if (mUsbAudioSource != null) {
//            renameSuccessFile(path, "_USB");
//            renameSuccessFile(path, "_COMMAND");
//        }
//        if (mUsbAudioSource == null) {
//            renameSuccessFile(path, "_SRC");
//            renameSuccessFile(path, "_COMMAND");
//        }
//        scanFiles();
//    }
//
//    @Override
//    public void onSleep() {
//
//    }
}
