package com.kikatech.voicesdktester.fragments;

import android.graphics.Color;
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
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.presenter.wakeup.AndroidWakeUpPresenter;
import com.kikatech.voicesdktester.presenter.wakeup.LocalWakeUpMonoPresenter;
import com.kikatech.voicesdktester.presenter.wakeup.LocalWakeUpNcPresenter;
import com.kikatech.voicesdktester.presenter.wakeup.UsbMonoInputWakeUpPresenter;
import com.kikatech.voicesdktester.presenter.wakeup.UsbNcInputWakeUpPresenter;
import com.kikatech.voicesdktester.presenter.wakeup.WakeUpPresenter;
import com.kikatech.voicesdktester.ui.FileAdapter;
import com.kikatech.voicesdktester.utils.FileUtil;
import ai.kikago.usb.AudioPlayBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.kikatech.voicesdktester.fragments.WakeUpVoiceFragment.FragmentType.LOCAL_MONO;
import static com.kikatech.voicesdktester.fragments.WakeUpVoiceFragment.FragmentType.LOCAL_NC;
import static com.kikatech.voicesdktester.fragments.WakeUpVoiceFragment.FragmentType.VOICE;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class WakeUpVoiceFragment extends Fragment implements
        WakeUpPresenter.PresenterCallback,
        FileAdapter.OnItemCheckedListener {

    public enum FragmentType {
        VOICE,
        LOCAL_NC,
        LOCAL_MONO,
    }

    private static final int WAKE_UP_MODE_ANDROID = 1;
    private static final int WAKE_UP_MODE_USB_NC = 2;
    private static final int WAKE_UP_MODE_USB_MONO = 3;
    private static final int WAKE_UP_MODE_LOCAL_NC = 4;
    private static final int WAKE_UP_MODE_LOCAL_MONO = 5;

    private FragmentType mFragmentType;

    private TextView mTextView;
    private TextView mResultText;

    private Button mStartButton;

    private RecyclerView mFileRecyclerView;
    private FileAdapter mFileAdapter;

    private WakeUpPresenter mWakeUpPresenter;

    public static WakeUpVoiceFragment getInstance(FragmentType fragmentType) {
        WakeUpVoiceFragment fragment = new WakeUpVoiceFragment();
        fragment.setFragmentType(fragmentType);
        return fragment;
    }

    private void setFragmentType(FragmentType fragmentType) {
        mFragmentType = fragmentType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice_wake_up_test, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextView = (TextView) view.findViewById(R.id.status_text);
        mResultText = (TextView) view.findViewById(R.id.result_text);



        mStartButton = (Button) view.findViewById(R.id.button_start);
        mStartButton.setOnClickListener(v -> {
            if (mWakeUpPresenter != null) {
                mWakeUpPresenter.start();
            }
            mResultText.setText("");
        });
        mStartButton.setText(isVoiceInput() ? "Start" : "PlayBack");

        view.findViewById(R.id.button_source_usb).setOnClickListener(v -> {
            mTextView.setText("Preparing...");

            mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_USB_NC);
            mWakeUpPresenter.prepare();
        });

        view.findViewById(R.id.button_source_usb_no_nc).setOnClickListener(v -> {
            mTextView.setText("Preparing...");

            mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_USB_MONO);
            mWakeUpPresenter.prepare();
        });

        view.findViewById(R.id.button_source_android).setOnClickListener(v -> {
            mTextView.setText("Preparing...");

            mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_ANDROID);
            mWakeUpPresenter.prepare();
        });

        view.findViewById(R.id.layout_select_source).setVisibility(
                isVoiceInput() ? View.VISIBLE : View.GONE);

        mFileRecyclerView = (RecyclerView) view.findViewById(R.id.files_recycler);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Logger.d("WakeUpVoiceFragment onViewCreated mFragmentType = " + mFragmentType);
        if (isVoiceInput()) {
            mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_ANDROID);
            mWakeUpPresenter.prepare();
        } else if (mFragmentType.equals(LOCAL_NC)) {
            mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_LOCAL_NC);
            mWakeUpPresenter.prepare();
        } else if (mFragmentType.equals(LOCAL_MONO)) {
            mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_LOCAL_MONO);
            mWakeUpPresenter.prepare();
        }
    }

    private boolean isVoiceInput() {
        return mFragmentType.equals(VOICE);
    }

    @Override
    public void onStart() {
        super.onStart();

        scanFiles();
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.i("onStop");


        if (mWakeUpPresenter != null) {
            mWakeUpPresenter.close();
        }
        AudioPlayBack.setListener(null);
    }

    public void scanFiles() {
        String path = FileUtil.getAudioFolder();
        Logger.d("WakeUpTestActivity scanFiles path = " + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        List<File> fileNames = new ArrayList<>();
        for (final File file : folder.listFiles()) {
            if (file.isDirectory() || fileFilter(file)) {
                continue;
            }
            fileNames.add(file);
        }

        if (mFileAdapter == null) {
            mFileAdapter = new FileAdapter(path, fileNames);
            mFileAdapter.setOnItemCheckedListener(this);
            mFileRecyclerView.setAdapter(mFileAdapter);
        } else {
            mFileAdapter.updateContent(fileNames);
            mFileAdapter.notifyDataSetChanged();
        }
    }

    private boolean fileFilter(File file) {
        if (mFragmentType == FragmentType.VOICE) {
            return (!file.getName().contains("USB") && !file.getName().contains("COMMAND"))
                    || file.getName().contains("wav");
        } else if (mFragmentType == FragmentType.LOCAL_NC) {
            return !file.getName().contains("USB") || file.getName().contains("wav");
        } else if (mFragmentType == FragmentType.LOCAL_MONO) {
            return !file.getName().contains("COMMAND") || file.getName().contains("wav");
        }
        return false;
    }

    @Override
    public void onUpdateStatus(String status) {
        mTextView.setText(status);
    }

    @Override
    public void onReadyStateChanged(boolean ready) {
        mStartButton.setEnabled(ready);
    }

    @Override
    public void onWakeUpResult(boolean success) {
        if (success) {
            mTextView.setText("");
            mResultText.setText("SUCCESS!");
            mResultText.setTextColor(Color.GREEN);
        } else {
            mTextView.setText("-end-");
            mResultText.setText("Fail!");
            mResultText.setTextColor(Color.RED);
        }
        new Handler().postDelayed(this::scanFiles, 500);
    }

    private WakeUpPresenter getWakeUpPresenter(int mode) {
        WakeUpPresenter presenter = null;
        switch (mode) {
            case WAKE_UP_MODE_ANDROID:
                presenter = new AndroidWakeUpPresenter(getContext());
                presenter.setPresenterCallback(this);
                break;
            case WAKE_UP_MODE_USB_NC:
                presenter = new UsbNcInputWakeUpPresenter(getContext());
                presenter.setPresenterCallback(this);
                break;
            case WAKE_UP_MODE_USB_MONO:
                presenter = new UsbMonoInputWakeUpPresenter(getContext());
                presenter.setPresenterCallback(this);
                break;
            case WAKE_UP_MODE_LOCAL_NC:
                presenter = new LocalWakeUpNcPresenter(getContext());
                presenter.setPresenterCallback(this);
                break;
            case WAKE_UP_MODE_LOCAL_MONO:
                presenter = new LocalWakeUpMonoPresenter(getContext());
                presenter.setPresenterCallback(this);
                break;
        }

        return presenter;
    }

    @Override
    public void onItemChecked(String itemStr) {
        if (mWakeUpPresenter != null) {
            String path = FileUtil.getAudioFolder();
            if (TextUtils.isEmpty(path)) {
                return;
            }
            mWakeUpPresenter.setFilePath(path + itemStr);
        }
    }

    @Override
    public void onItemUnchecked(String itemStr) {

    }

    @Override
    public void onNothingChecked() {
        if (mWakeUpPresenter != null) {
            mWakeUpPresenter.setFilePath(null);
        }
    }
}