package com.kikatech.voicesdktester.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.activities.PlayActivity;
import com.kikatech.voicesdktester.source.LocalMonoVoiceSource;
import com.kikatech.voicesdktester.source.LocalNcVoiceSource;
import com.kikatech.voicesdktester.source.LocalVoiceSource;
import com.kikatech.voicesdktester.ui.FileAdapter;
import com.kikatech.voicesdktester.ui.ResultAdapter;
import com.kikatech.voicesdktester.utils.PreferenceUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ian.fan on 13/04/2018.
 */

public class LocalPlayBackFragment extends Fragment implements
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceWakeUpListener,
        LocalVoiceSource.EofListener,
        FileAdapter.OnItemCheckedListener {

    public enum FragmentType {
        LOCAL_USB,
        LOCAL_NC,
    }

    private LocalPlayBackFragment.FragmentType mFragmentType;

    private static final String DEBUG_FILE_PATH = "voiceTester";

    private TextView mTextView;

    private Button mStartButton;

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;

    private LocalNcVoiceSource mLocalNcVoiceSource;
    private LocalMonoVoiceSource mLocalMonoVoiceSource;

    private RecyclerView mResultRecyclerView;
    private ResultAdapter mResultAdapter;

    private RecyclerView mFileRecyclerView;
    private FileAdapter mFileAdapter;

    private Handler mUiHandler;

    private ArrayList<String> mItemList = null;
    private int totalItemsCount = 0;
    private String mItemStr = null;

    public static LocalPlayBackFragment getInstance(FragmentType fragmentType) {
        LocalPlayBackFragment fragment = new LocalPlayBackFragment();
        fragment.setFragmentType(fragmentType);
        return fragment;
    }

    private void setFragmentType(FragmentType fragmentType) {
        mFragmentType = fragmentType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_local_playback, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLocalVoiceSource(mFragmentType);

        mTextView = (TextView) view.findViewById(R.id.status_text);
        mFileRecyclerView = (RecyclerView) view.findViewById(R.id.files_recycler);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mResultAdapter = new ResultAdapter(getActivity());
        mResultRecyclerView = (RecyclerView) view.findViewById(R.id.result_recycler);
        mResultRecyclerView.setAdapter(mResultAdapter);
        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mStartButton = (Button) view.findViewById(R.id.button_start);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunNextPlayback();
            }
        });
        mStartButton.setEnabled(false);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (getActivity() != null && !getActivity().isDestroyed()) {
                    attachService();
                }
            }
        }, 2000);

//        attachService();

        mUiHandler = new Handler();

        view.findViewById(R.id.button_enter_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlayActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onRunNextPlayback() {
        if (mVoiceService != null && mItemList.size() > 0) {
            mItemStr = mItemList.get(0);
            mItemList.remove(mItemStr);

            String path = DebugUtil.getDebugFolderPath();
            if (TextUtils.isEmpty(path)) {
                showStatusInfo("folder path error.");
                onEndOfCurrentPlayback();
                return;
            }
            getLocalVoiceSource(mFragmentType).setTargetFile(path + mItemStr);

            mVoiceService.start();

            mResultAdapter.clearResults();
            mResultAdapter.notifyDataSetChanged();

            writeOriginalFileNameToFile();
            writeTimeToFile();

            showStatusInfo("starting.");
            mStartButton.setEnabled(false);
        }
    }

    public void scanFiles() {
        String path = DebugUtil.getDebugFolderPath();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        List<File> fileNames = new ArrayList<>();
        for (final File file : folder.listFiles()) {
            if (!isTargetFile(file, mFragmentType)) {
                continue;
            }
            fileNames.add(file);
        }

        if (mFileAdapter == null) {
            mFileAdapter = new FileAdapter(path, fileNames);
            mFileAdapter.setOnItemCheckedListener(this);
            mFileAdapter.setEnableMultipleCheck(true);
        } else {
            mFileAdapter.updateContent(fileNames);
            mFileAdapter.notifyDataSetChanged();
        }
        mFileRecyclerView.setAdapter(mFileAdapter);
    }

    private boolean isTargetFile(final File file, FragmentType fragmentType) {
        switch (fragmentType) {
            case LOCAL_NC:
                if (!file.isDirectory()
                        && !file.getName().contains("wav")
                        && (file.getName().contains("NC") || file.getName().contains("SRC"))) {
                    return true;
                }
                break;
            case LOCAL_USB:
            default:
                if (!file.isDirectory()
                        && !file.getName().contains("wav")
                        && file.getName().contains("USB")) {
                    return true;
                }
                break;
        }

        return false;
    }

    private void attachService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        // Debug
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
//                .setSpeechMode(((CheckBox) findViewById(R.id.check_one_shot)).isChecked()
//                        ? AsrConfiguration.SpeechMode.ONE_SHOT
//                        : AsrConfiguration.SpeechMode.CONVERSATION)
//                .setAlterEnabled(((CheckBox) findViewById(R.id.check_alter)).isChecked())
//                .setEmojiEnabled(((CheckBox) findViewById(R.id.check_emoji)).isChecked())
//                .setPunctuationEnabled(((CheckBox) findViewById(R.id.check_punctuation)).isChecked())
//                .setSpellingEnabled(((CheckBox) findViewById(R.id.check_spelling)).isChecked())
//                .setVprEnabled(((CheckBox) findViewById(R.id.check_vpr)).isChecked())
                .setEosPackets(9)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFileTag(DEBUG_FILE_PATH);
        conf.setIsDebugMode(true);
        conf.source(getLocalVoiceSource(mFragmentType));
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        getActivity(),
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        VoiceConfiguration.HostUrl.KIKAGO_SQ))
                .setLocale("en_US")
                .setSign(RequestManager.getSign(getActivity()))
                .setUserAgent(RequestManager.generateUserAgent(getActivity()))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        mVoiceService = VoiceService.getService(getActivity(), conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.create();
    }

    @Override
    public void onRecognitionResult(final Message message) {
        if (message instanceof IntermediateMessage) {
            if (mTextView != null) {
                mTextView.setText("Intermediate Result : " + ((IntermediateMessage) message).text);
            }
            return;
        }
        if (mTextView != null) {
            mTextView.setText("Final Result");
        }
        mResultAdapter.addResult(message);
        mResultAdapter.notifyDataSetChanged();
    }

    @Override
    public void onError(int reason) {

    }

    @Override
    public void onWakeUp() {

    }

    @Override
    public void onSleep() {

    }

    @Override
    public void onEndOfFile() {
        Logger.d("LocalPlayBackActivity onEndOfFile");

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                writeTimeToFile();
                mItemStr = null;
                mVoiceService.stop(VoiceService.StopType.NORMAL);

                onEndOfCurrentPlayback();
            }
        }, 1000);
    }

    private void onEndOfCurrentPlayback() {
        if (mLocalNcVoiceSource != null) {
            mLocalNcVoiceSource = null;
            attachService();
        }

        if (mItemList.size() == 0) {
            if (mTextView != null) {
                showStatusInfo("completed.");
            }
            scanFiles();
        } else {
            if (mTextView != null) {
                showStatusInfo("stopped.");
            }

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onRunNextPlayback();
                }
            }, 2000);
        }
    }

    @Override
    public void onItemChecked(String itemStr) {
        updateItemOjsAndInfo();

        if (getLocalVoiceSource(mFragmentType) != null) {
            String path = DebugUtil.getDebugFolderPath();
            if (TextUtils.isEmpty(path)) {
                showStatusInfo("folder path error.");
                return;
            }
        }
    }

    @Override
    public void onItemUnchecked(String itemStr) {
        updateItemOjsAndInfo();
    }

    @Override
    public void onNothingChecked() {
        updateItemOjsAndInfo();
    }

    private void updateItemOjsAndInfo() {
        mItemList = mFileAdapter.getCheckedList();
        totalItemsCount = mItemList.size();
        showStatusInfo("checked.");

        if (mStartButton != null) {
            if (totalItemsCount > 0) {
                mStartButton.setEnabled(true);
            } else {
                mStartButton.setEnabled(false);
            }
        }
    }

    private LocalVoiceSource getLocalVoiceSource(FragmentType fragmentType) {
        switch (fragmentType) {
            case LOCAL_NC:
                if (mLocalMonoVoiceSource == null) {
                    mLocalMonoVoiceSource = new LocalMonoVoiceSource();
                    mLocalMonoVoiceSource.setEofListener(this);
                }
                return mLocalMonoVoiceSource;
            case LOCAL_USB:
            default:
                if (mLocalNcVoiceSource == null) {
                    mLocalNcVoiceSource = new LocalNcVoiceSource();
                    mLocalNcVoiceSource.setEofListener(this);
                }
                return mLocalNcVoiceSource;
        }
    }

    private void writeTimeToFile(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());
        DebugUtil.logTextToFile("time", currentDateAndTime);
    }

    private void writeOriginalFileNameToFile(){
        if (mItemStr != null && mItemStr.length() > 0) {
            DebugUtil.logTextToFile("Original File", mItemStr);
        }
    }

    private void showStatusInfo(String info) {
        if (mTextView != null && info != null && info.length() > 0) {
            mTextView.setText(info + " " + String.valueOf(totalItemsCount - mItemList.size()) + "/" + String.valueOf(totalItemsCount));
        }
    }
}
