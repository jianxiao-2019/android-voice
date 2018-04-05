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
import com.kikatech.voicesdktester.presenter.wakeup.UsbMonoInputWakeUpPresenter;
import com.kikatech.voicesdktester.presenter.wakeup.UsbNcInputWakeUpPresenter;
import com.kikatech.voicesdktester.presenter.wakeup.WakeUpPresenter;
import com.kikatech.voicesdktester.ui.FileAdapter;
import com.xiao.usbaudio.AudioPlayBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class WakeUpPlayBackWithNcFragment extends Fragment {

//        private TextView mTextView;
//        private TextView mResultText;
//
//        private Button mStartButton;
//
//        private RecyclerView mFileRecyclerView;
//        private FileAdapter mFileAdapter;
//
//        private WakeUpPresenter mWakeUpPresenter;
//
//        @Nullable
//        @Override
//        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//            return inflater.inflate(R.layout.fragment_voice_wake_up_test, container, false);
//        }
//
//        @Override
//        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//            super.onViewCreated(view, savedInstanceState);
//
//            mTextView = (TextView) view.findViewById(R.id.status_text);
//            mResultText = (TextView) view.findViewById(R.id.result_text);
//
//            mStartButton = (Button) view.findViewById(R.id.button_start);
//            mStartButton.setOnClickListener(v -> {
//                if (mWakeUpPresenter != null) {
//                    mWakeUpPresenter.start();
//                }
//                mResultText.setText("");
//            });
//
//            view.findViewById(R.id.button_source_usb).setOnClickListener(v -> {
//                mTextView.setText("Preparing...");
//
//                mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_USB_NC);
//                mWakeUpPresenter.prepare();
//            });
//
//            view.findViewById(R.id.button_source_usb_no_nc).setOnClickListener(v -> {
//                mTextView.setText("Preparing...");
//
//                mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_USB_MONO);
//                mWakeUpPresenter.prepare();
//            });
//
//            view.findViewById(R.id.button_source_android).setOnClickListener(v -> {
//                mTextView.setText("Preparing...");
//
//                mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_ANDROID);
//                mWakeUpPresenter.prepare();
//            });
//
//            mFileRecyclerView = (RecyclerView) view.findViewById(R.id.files_recycler);
//            mFileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//
//            mWakeUpPresenter = getWakeUpPresenter(WAKE_UP_MODE_ANDROID);
//            mWakeUpPresenter.prepare();
//            scanFiles();
//        }
//
//        @Override
//        public void onStop() {
//            super.onStop();
//            Logger.i("onStop");
//
//
//            if (mWakeUpPresenter != null) {
//                mWakeUpPresenter.close();
//            }
//            AudioPlayBack.setListener(null);
//        }
//
//        private void scanFiles() {
//            String path = DebugUtil.getDebugFolderPath();
//            Logger.d("WakeUpTestActivity scanFiles path = " + path);
//            if (TextUtils.isEmpty(path)) {
//                return;
//            }
//            File folder = new File(path);
//            if (!folder.exists() || !folder.isDirectory()) {
//                return;
//            }
//
//            List<File> fileNames = new ArrayList<>();
//            for (final File file : folder.listFiles()) {
//                if (file.isDirectory()
//                        || (!file.getName().contains("USB") && !file.getName().contains("COMMAND"))
//                        || file.getName().contains("wav")) {
//                    continue;
//                }
//                fileNames.add(file);
//            }
//
//            if (mFileAdapter == null) {
//                mFileAdapter = new FileAdapter(path, fileNames);
//                mFileRecyclerView.setAdapter(mFileAdapter);
//            } else {
//                mFileAdapter.updateContent(fileNames);
//                mFileAdapter.notifyDataSetChanged();
//            }
//        }
//
//        @Override
//        public void onUpdateStatus(String status) {
//            mTextView.setText(status);
//        }
//
//        @Override
//        public void onReadyStateChanged(boolean ready) {
//            mStartButton.setEnabled(ready);
//        }
//
//        @Override
//        public void onWakeUpResult(boolean success) {
//            if (success) {
//                mTextView.setText("");
//                mResultText.setText("SUCCESS!");
//                mResultText.setTextColor(Color.GREEN);
//            } else {
//                mTextView.setText("-end-");
//                mResultText.setText("Fail!");
//                mResultText.setTextColor(Color.RED);
//            }
//            new Handler().postDelayed(this::scanFiles, 500);
//        }
//
//        private static final int WAKE_UP_MODE_ANDROID = 1;
//        private static final int WAKE_UP_MODE_USB_NC = 2;
//        private static final int WAKE_UP_MODE_USB_MONO = 3;
//        private WakeUpPresenter getWakeUpPresenter(int mode) {
//            WakeUpPresenter presenter = null;
//            switch (mode) {
//                case WAKE_UP_MODE_ANDROID:
//                    presenter = new AndroidWakeUpPresenter(getContext());
//                    presenter.setPresenterCallback(this);
//                    break;
//                case WAKE_UP_MODE_USB_NC:
//                    presenter = new UsbNcInputWakeUpPresenter(getContext());
//                    presenter.setPresenterCallback(this);
//                    break;
//                case WAKE_UP_MODE_USB_MONO:
//                    presenter = new UsbMonoInputWakeUpPresenter(getContext());
//                    presenter.setPresenterCallback(this);
//                    break;
//            }
//
//            return presenter;
//        }
//    }
}
