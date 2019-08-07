package com.kikatech.voicesdktester.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kikago.speech.baidu.BaiduApi;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voice.webservice.tencent_cloud_speech.TencentApi;
import com.kikatech.voicesdktester.AudioPlayerTask;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.source.KikaGoUsbVoiceSourceWrapper;
import com.kikatech.voicesdktester.ui.ResultAdapter;
import com.kikatech.voicesdktester.utils.FileUtil;
import com.kikatech.voicesdktester.utils.PreferenceUtil;
import com.kikatech.voicesdktester.utils.VoiceConfig;

import java.io.File;
import java.text.SimpleDateFormat;

import ai.kikago.usb.UsbAudio;

import static com.kikatech.voicesdktester.utils.PreferenceUtil.KEY_ENABLE_DEBUG_APP;

/**
 * Created by ryanlin on 23/01/2018.
 */

public class RecorderFragment extends PageFragment implements
        View.OnClickListener,
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceWakeUpListener,
        TtsSource.TtsStateChangedListener {

    private static final String DEBUG_FILE_TAG = "voiceTesterUi";

    private View mStartRecordView;
    private View mStopRecordView;
    private TextView mRecordingTimerText;
    private View mUsingAndroid;
    private ImageView mKikagoSignal;
    private View mUsingKikaGo;
    private ImageView mAndroidSignal;
    private TextView mErrorHintText;
    private FrameLayout mRecentArea;
    private TextView mFirmwareinfoText;

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;
    private KikaGoUsbVoiceSourceWrapper mKikaGoVoiceSource;
    private UsbAudioService mUsbAudioService;

    private static final int MSG_TIMER = 0;
    private static final int MSG_CHECK_DEBUG = 1;
    private static final int MSG_VAD_TIMER = 2;
    private long mTimeInSec = 0;
    private long mNoAsrTimeInSec = 0;

    private int mDebugCount = 0;

    private TextView mPrevPlayingView = null;
    private AudioPlayerTask mTask;

//    private boolean mStartSpeech = false;

    private TextView mTextView;
    private RecyclerView mResultRecyclerView;
    private ResultAdapter mResultAdapter;

    @ServerType
    private int mServerType = ServerType.BAIDU;

    @IntDef
    @interface ServerType {
        int GOOGLE = 1;
        int TENCENT = 2;
        int BAIDU = 3;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recorder, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.server_google:
                mServerType = ServerType.GOOGLE;
                updateServer();
                Toast.makeText(getContext(), "Using Google", Toast.LENGTH_SHORT).show();
                break;
            case R.id.server_tencent:
                mServerType = ServerType.TENCENT;
                Toast.makeText(getContext(), "Using Tencent", Toast.LENGTH_SHORT).show();
                updateServer();
                break;
            case R.id.server_baidu:
                mServerType = ServerType.BAIDU;
                Toast.makeText(getContext(), "Using Baidu", Toast.LENGTH_SHORT).show();
                updateServer();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private ProgressDialog progressDialog;
    private void updateServer() {
        if (mKikaGoVoiceSource != null) {
            mKikaGoVoiceSource = null;
            attachService();
            progressDialog = ProgressDialog.show(getActivity(), "processing", null);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mUsbAudioService != null) {
                        mUsbAudioService.scanDevices();
                    }
                }
            }, 1000);
        } else {
            attachService();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStartRecordView = view.findViewById(R.id.start_record);
        mStartRecordView.setOnClickListener(this);
        mStopRecordView = view.findViewById(R.id.stop_record);
        mStopRecordView.setOnClickListener(this);

        mErrorHintText = (TextView) view.findViewById(R.id.error_hint);

        mFirmwareinfoText = (TextView) view.findViewById(R.id.firmware_info);
        int versioncode = 0;
        String versionname = "0";
        PackageManager pm = this.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(this.getContext().getPackageName(), 0);
            versioncode = packageInfo.versionCode;
            versionname = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        byte[] result = UsbAudio.checkDriverVersion();
        int fw =  result[1] & 0xFF | (result[0] & 0xFF) << 8;
        mFirmwareinfoText
                .setText(KikaGoVoiceSource.getNcVersion()+"."+fw+"."+versioncode+"\n"+versionname);

        mUsingAndroid = view.findViewById(R.id.device_button_right);
        mUsingAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mKikagoSignal != null) {
                    mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
                }
                if (mAndroidSignal != null) {
                    mAndroidSignal.setImageResource(R.drawable.signal_point_yellow);
                }
                mUsingKikaGo.setSelected(false);
                mUsingAndroid.setSelected(true);

                mKikaGoVoiceSource = null;
                attachService();
            }
        });
        mKikagoSignal = (ImageView) view.findViewById(R.id.signal_kikago);

        mUsingKikaGo = view.findViewById(R.id.device_button_left);
        mUsingKikaGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKikaGoVoiceSource == null) {
                    mUsingKikaGo.setSelected(true);
                    mUsingAndroid.setSelected(false);
                    if (mErrorHintText != null) {
                        mErrorHintText.setVisibility(View.GONE);
                    }

                    if (mUsbAudioService != null) {
                        mUsbAudioService.scanDevices();
                    }
                } else {
                    Toast.makeText(getContext(), "You'er already using kikiGO device.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAndroidSignal = (ImageView) view.findViewById(R.id.signal_phone);

        mRecordingTimerText = (TextView) view.findViewById(R.id.recording_timer_text);

        mRecentArea = (FrameLayout) view.findViewById(R.id.recent_area);

        view.findViewById(R.id.enter_debug_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("onClick mDebugCount = " + mDebugCount);
                Boolean isDebug = PreferenceUtil.getBoolean(getContext(), KEY_ENABLE_DEBUG_APP, false);
                if (isDebug) {
                    if (++mDebugCount >= 3) {
                        Toast.makeText(getContext(), "You'er already in the developer mode", Toast.LENGTH_SHORT).show();
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mDebugCount = 0;
                    } else {
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mTimerHandler.sendEmptyMessageDelayed(MSG_CHECK_DEBUG, 1000);
                    }
                } else {
                    if (++mDebugCount >= 10) {
                        PreferenceUtil.setBoolean(getContext(), KEY_ENABLE_DEBUG_APP, true);
                        Toast.makeText(getContext(), "You'er entering the developer mode", Toast.LENGTH_SHORT).show();
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mDebugCount = 0;
                    } else {
                        mTimerHandler.removeMessages(MSG_CHECK_DEBUG);
                        mTimerHandler.sendEmptyMessageDelayed(MSG_CHECK_DEBUG, 1000);
                    }
                }
            }
        });

        mTextView = (TextView) view.findViewById(R.id.text_recent);

        mResultAdapter = new ResultAdapter(getContext());
        mResultRecyclerView = (RecyclerView) view.findViewById(R.id.result_recycler);
        mResultRecyclerView.setAdapter(mResultAdapter);
        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        attachService();
        refreshRecentView();
    }

    private void onRestart() {
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.RESTART);
        }

        if (mVoiceService != null) {
            mVoiceService.restart();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mUsbAudioService = UsbAudioService.getInstance(getActivity());
        mUsbAudioService.setListener(mIUsbAudioListener);
        mUsbAudioService.scanDevices();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mVoiceService != null) {
            mVoiceService.destroy();
        }

        if (mUsbAudioService != null) {
            mUsbAudioService.closeDevice();
            mUsbAudioService.setListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void attachService() {
        if (mErrorHintText != null) {
            mErrorHintText.setVisibility(View.GONE);
        }

        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setAlterEnabled(false)
                .setEmojiEnabled(false)
                .setPunctuationEnabled(false)
                .setSpellingEnabled(false)
                .setVprEnabled(false)
                .setEosPackets(9)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setIsDebugMode(true);
        conf.setDebugFileTag(DEBUG_FILE_TAG);
        conf.source(mKikaGoVoiceSource);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(VoiceConfiguration.HostUrl.KIKAGO_SQ)
                .setLocale("en_US")
                .setSign(RequestManager.getSign(getActivity()))
                .setUserAgent(RequestManager.generateUserAgent(getActivity()))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        if (mServerType == ServerType.GOOGLE) {
            VoiceConfig.getVoiceConfig(getActivity(), conf, config -> {
                mVoiceService = VoiceService.getService(getActivity(), config);
                mVoiceService.setVoiceRecognitionListener(this);
                mVoiceService.create();
            });
        } else if (mServerType == ServerType.TENCENT) {
            conf.setWebSocket(new TencentApi(getContext()));
            mVoiceService = VoiceService.getService(getActivity(), conf);
            mVoiceService.setVoiceRecognitionListener(this);
            mVoiceService.create();
        } else {
            conf.setWebSocket(new BaiduApi(getContext()));
            mVoiceService = VoiceService.getService(getActivity(), conf);
            mVoiceService.setVoiceRecognitionListener(this);
            mVoiceService.create();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                onPressStartButton();
                break;
            case R.id.stop_record:
                onPressStopButton();
                break;
            case R.id.device_button_left:
                break;
            case R.id.device_button_right:
                break;
        }
    }

    private void onPressStartButton() {
        if (mVoiceService != null) {
            String folder = FileUtil.getAudioFolder();
            String fileName = FileUtil.getCurrentTimeFormattedFileName();
            mVoiceService.setAsrAudioFilePath(folder, fileName);
            if (mKikaGoVoiceSource != null) {
                mKikaGoVoiceSource.setAudioFilePath(folder, fileName);
            }
            mVoiceService.start();

            mResultAdapter.clearResults();
            mResultAdapter.notifyDataSetChanged();

            if (mStartRecordView != null) {
                mStartRecordView.setVisibility(View.GONE);
            }
            if (mStopRecordView != null) {
                mStopRecordView.setVisibility(View.VISIBLE);
            }

            mRecordingTimerText.setText("00:00");
            mTimerHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);

            if (mTextView != null) {
                mTextView.setText("Recent");
            }
            if (mRecordingTimerText != null) {
                mRecordingTimerText.setTextColor(Color.WHITE);
            }
        }
    }

    private void onPressStopButton() {
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.NORMAL);
        }
        updateViewForStopRecording();
    }

    private void updateViewForStopRecording() {
        if (mStartRecordView != null) {
            mStartRecordView.setVisibility(View.VISIBLE);
        }
        if (mStopRecordView != null) {
            mStopRecordView.setVisibility(View.GONE);
        }

        mTimerHandler.removeMessages(MSG_TIMER);
        mTimeInSec = 0;
        mNoAsrTimeInSec = 0;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshRecentView();
            }
        }, 1000);
    }

    @Override
    public void onTtsStart() {

    }

    @Override
    public void onTtsComplete() {

    }

    @Override
    public void onTtsInterrupted() {

    }

    @Override
    public void onTtsError() {

    }

    private long mLastCid = 0;

    @Override
    public void onRecognitionResult(Message message) {
        mNoAsrTimeInSec = 0;
        if (message instanceof IntermediateMessage) {
            if (mTextView != null) {
                mTextView.setText(((IntermediateMessage) message).text);
            }
            return;
        }
        if (((TextMessage) message).cid == 0 || mLastCid == ((TextMessage) message).cid) {
            return;
        }
        mLastCid = ((TextMessage) message).cid;

        if (mTextView != null) {
            mTextView.setText("");
        }
        mResultAdapter.addResult(message);
        mResultAdapter.notifyDataSetChanged();

        onRestart();
    }

    @Override
    public void onError(int reason) {
        Logger.e("onError reason = " + reason);
        // setViewToDisableState();
        if (mErrorHintText != null) {
            String str = "";
            if (reason == VoiceService.ERR_REASON_NOT_CREATED) {
                str = "ERR_REASON_NOT_CREATED";
            } else if (reason == VoiceService.ERR_CONNECTION_ERROR) {
                str = "ERR_CONNECTION_ERROR";
            } else if (reason == VoiceService.ERR_NO_SPEECH) {
                str = "ERR_NO_SPEECH";
            } else if (reason == VoiceService.ERR_RECORD_OPEN_FAIL) {
                str = "ERR_RECORD_OPEN_FAIL";
            } else if (reason == VoiceService.ERR_RECORD_DATA_FAIL) {
                str = "ERR_RECORD_DATA_FAIL";
            }
            mErrorHintText.setText(str);
            mErrorHintText.setVisibility(View.VISIBLE);
        }
        updateViewForStopRecording();
    }

    @Override
    public void onWakeUp() {

    }

    @Override
    public void onSleep() {

    }

    private IUsbAudioListener mIUsbAudioListener = new IUsbAudioListener() {

        @Override
        public void onDeviceAttached(KikaGoVoiceSource audioSource) {
            Logger.d("onDeviceAttached.");
            mKikaGoVoiceSource = new KikaGoUsbVoiceSourceWrapper(audioSource);
            attachService();

            if (mAndroidSignal != null) {
                mAndroidSignal.setImageResource(R.drawable.signal_point_empty);
            }
            if (mKikagoSignal != null) {
                mKikagoSignal.setImageResource(R.drawable.signal_point_yellow);
                mUsingKikaGo.setSelected(true);
                mUsingAndroid.setSelected(false);
            }
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");
            mKikaGoVoiceSource = null;
            attachService();

            if (mKikagoSignal != null) {
                mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
            }
            if (mAndroidSignal != null) {
                mAndroidSignal.setImageResource(R.drawable.signal_point_yellow);
                mUsingKikaGo.setSelected(false);
                mUsingAndroid.setSelected(true);
            }
        }

        @Override
        public void onDeviceError(int errorCode) {
            if (errorCode == ERROR_NO_DEVICES) {
                Logger.d("onDeviceError ERROR_NO_DEVICES");
                mKikaGoVoiceSource = null;
                attachService();
                Toast.makeText(getContext(), "KikaGo mic isn’t plugged-in.", Toast.LENGTH_SHORT).show();

                if (mKikagoSignal != null) {
                    mKikagoSignal.setImageResource(R.drawable.signal_point_empty);
                }
                if (mAndroidSignal != null) {
                    mAndroidSignal.setImageResource(R.drawable.signal_point_yellow);
                    mUsingKikaGo.setSelected(false);
                    mUsingAndroid.setSelected(true);
                }
            } else if (errorCode == ERROR_DRIVER_CONNECTION_FAIL) {
                Logger.d("onDeviceError ERROR_DRIVER_INIT_FAIL");
                if (mErrorHintText != null) {
                    mErrorHintText.setText("Device connection fail");
                    mErrorHintText.setVisibility(View.VISIBLE);
                }
                if (mKikagoSignal != null) {
                    mKikagoSignal.setImageResource(R.drawable.signal_point_red);
                }
            }
        }
    };

    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_TIMER) {
                mTimeInSec++;
                mNoAsrTimeInSec++;
                if (mRecordingTimerText != null) {
                    String result = String.format("%02d:%02d", mTimeInSec / 60, mTimeInSec % 60);
                    mRecordingTimerText.setText(result);
                    mTimerHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
                }
//                if (mTimeInSec >= 60) {
//                    if (mRecordingTimerText != null) {
//                        mRecordingTimerText.setTextColor(Color.RED);
//                    }
//                    if (mTimeInSec >= 65 && mRecordingTimerText != null) {
//                        onPressStopButton();
//                    }
//                }
                if (mNoAsrTimeInSec >= 30) {
                    onRestart();
                    mNoAsrTimeInSec = 0;
                }
            } else if (msg.what == MSG_CHECK_DEBUG) {
                mDebugCount = 0;
//            } else if (msg.what == MSG_VAD_TIMER) {
//                if (mVoiceService != null) {
//                    mVoiceService.stop();
            }
        }
    };

    private void refreshRecentView() {
        if (mRecentArea != null) {
            Activity activity = getActivity();
            if (activity == null || activity.isDestroyed()) {
                return;
            }
            RecognizeItem item = scanLatestFile(DebugUtil.getAsrAudioFilePath());
            if (item == null) {
                return;
            }
            mRecentArea.removeAllViews();

            View recentView = LayoutInflater.from(activity).inflate(R.layout.item_recorded, mRecentArea, false);

            recentView.findViewById(R.id.expanded_layout).setVisibility(View.GONE);
            ((ImageView) recentView.findViewById(R.id.source_icon)).setImageResource(item.isSourceUsb ?
                    R.drawable.ic_list_usbcable : R.drawable.ic_list_phone);
            ((TextView) recentView.findViewById(R.id.file_name)).setText(item.fileName);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
            long duration = item.file.length() / 2 / 16000;
            ((TextView) recentView.findViewById(R.id.file_time)).setText(sdf.format(item.file.lastModified()) + " | " + String.format("%02d:%02d", duration / 60, duration % 60));
            View controlNc = recentView.findViewById(R.id.control_nc);
            controlNc.getLayoutParams().width = item.isSourceUsb ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            controlNc.setOnClickListener(new PlayButtonClickListener(
                    item.filePath + "_NC",
                    R.drawable.ic_source_nc_play,
                    R.drawable.ic_source_nc_pause,
                    R.drawable.ic_source_tag_nc));
            recentView.findViewById(R.id.control_raw).setOnClickListener(new PlayButtonClickListener(
                    item.filePath + (item.isSourceUsb ? "_USB" : "_SRC"),
                    R.drawable.ic_source_raw_play,
                    R.drawable.ic_source_raw_pause,
                    R.drawable.ic_source_tag_raw));

            mRecentArea.addView(recentView);
        }
    }

    private RecognizeItem scanLatestFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory() || folder.listFiles() == null) {
            return null;
        }

        File latestFile = null;
        RecognizeItem latestItem = null;
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }

            if (file.getName().contains("_NC") || file.getName().contains("_SRC")) {
                if (latestFile == null || file.lastModified() > latestFile.lastModified()) {
                    String simpleFileName = file.getPath().substring(0, file.getPath().lastIndexOf("_"));
                    latestFile = file;
                    latestItem = new RecognizeItem();
                    latestItem.file = file;
                    latestItem.filePath = simpleFileName;
                    latestItem.fileName = simpleFileName.substring(simpleFileName.lastIndexOf("/") + 1);
                    latestItem.isSourceUsb = file.getName().contains("_NC");
                }
            }
        }

        return latestItem;
    }

    @Override
    public void onPagePause() {
        if (mTask != null && mTask.isPlaying()) {
            mTask.stop();
        }
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.NORMAL);
        }
    }

    @Override
    public void onPageResume() {
        refreshRecentView();
    }

    private class RecognizeItem {
        File file;
        String fileName;
        String filePath;
        boolean isSourceUsb;
    }

    private class PlayButtonClickListener implements View.OnClickListener {

        private String mFilePath;

        private int mDrawablePlay;
        private int mDrawableStop;
        private int mDrawableSource;

        public PlayButtonClickListener(String filePath,
                                       int drawablePlay, int drawableStop, int drawableSource) {
            mFilePath = filePath;

            mDrawablePlay = drawablePlay;
            mDrawableStop = drawableStop;
            mDrawableSource = drawableSource;
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof TextView)) {
                return;
            }
            TextView currentView = (TextView) v;
            if (v == mPrevPlayingView) {
                if (mTask == null || !mTask.isPlaying()) {
                    mTask = new AudioPlayerTask(mFilePath, currentView, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                } else {
                    if (mTask.isPlaying()) {
                        mTask.stop();
                        mTask = null;
                    }
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawablePlay, 0, mDrawableSource);
                }
            } else {
                if (mTask != null && mTask.isPlaying()) {
                    mTask.stop();
//                    mPrevPlayingView.setCompoundDrawablesWithIntrinsicBounds(
//                            0, mDrawablePlay, 0, mDrawableSource);
                    mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                } else {
                    mTask = new AudioPlayerTask(mFilePath, (TextView) v, mDrawablePlay, mDrawableSource);
                    mTask.execute();
                    currentView.setCompoundDrawablesWithIntrinsicBounds(
                            0, mDrawableStop, 0, mDrawableSource);
                }

                mPrevPlayingView = (TextView) v;
            }
        }
    }
}
