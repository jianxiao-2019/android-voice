package com.kikatech.voicesdktester.activities;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.LocalVoiceSource;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.utils.PreferenceUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class AutoTestActivity extends AppCompatActivity implements
        VoiceService.VoiceRecognitionListener,
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener,
        LocalVoiceSource.EofListener {

    private static final String DEBUG_FILE_PATH = "voiceTester";

    private TextView mTextView;

    private Button mStartButton;

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;

    private File mAudioFile;
    private File mAnswerFile;

    private LocalVoiceSource mLocalVoiceSource;

    private AutoTestingAdapter mAutoTestingAdapter;
    private RecyclerView mFileRecyclerView;

    private Handler mUiHandler;

    private final List<AutoTestItem> mAutoTestItems = new ArrayList<>();
    private int mResultIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_test);

        mLocalVoiceSource = new LocalVoiceSource();
        mLocalVoiceSource.setEofListener(this);

        mTextView = (TextView) findViewById(R.id.status_text);
        mFileRecyclerView = (RecyclerView) findViewById(R.id.auto_test_recycler_view);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFileRecyclerView.addItemDecoration(new SpacesItemDecoration(3));

        mStartButton = (Button) findViewById(R.id.button_start);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceService != null) {
                    mVoiceService.start();
                }
            }
        });
        attachService();

        mUiHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        scanFiles();
    }

    private void scanFiles() {
        String path = DebugUtil.getDebugFolderPath() + "/autoTest";
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        List<String> fileNames = new ArrayList<>();
        for (final File file : folder.listFiles()) {
            if (!file.isDirectory() && file.getName().contains("_USB")) {
                mAudioFile = file;
                mLocalVoiceSource.selectFile(mAudioFile.getPath());
            }
            if (!file.isDirectory() && file.getName().contains("_ANS")) {
                mAnswerFile = file;
            }
        }

        parseAnsFile();
        mAutoTestingAdapter = new AutoTestingAdapter();
        mFileRecyclerView.setAdapter(mAutoTestingAdapter);
    }

    private void parseAnsFile() {
        mAutoTestItems.clear();
        try {
            FileReader fr = new FileReader(mAnswerFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                Logger.d("parseAnsFile line = " + line);
                mAutoTestItems.add(new AutoTestItem(line));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AutoTestItem {

        AutoTestItem(String answer) {
            this.answer = answer.toLowerCase().trim();
        }

        String answer;
        String result;
    }

    private class AutoTestingAdapter extends RecyclerView.Adapter<AutoTestViewHolder> {

        @Override
        public AutoTestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AutoTestViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_auto_test, parent, false));
        }

        @Override
        public void onBindViewHolder(AutoTestViewHolder holder, int position) {
            AutoTestItem item = mAutoTestItems.get(position);

            holder.ansText.setText(item.answer);
            holder.resText.setText(item.result);

            int resId;
            if (TextUtils.isEmpty(item.result)) {
                resId = mResultIndex == position ? R.drawable.signal_point_yellow : R.drawable.signal_point_empty;
            } else {
                resId = item.answer.equals(item.result) ? R.drawable.signal_point_green : R.drawable.signal_point_red;
            }
            holder.signalImage.setImageResource(resId);
        }

        @Override
        public int getItemCount() {
            return mAutoTestItems.size();
        }
    }

    private class AutoTestViewHolder extends RecyclerView.ViewHolder {

        TextView ansText;
        TextView resText;
        ImageView signalImage;

        public AutoTestViewHolder(View itemView) {
            super(itemView);

            ansText = (TextView) itemView.findViewById(R.id.text_ans);
            resText = (TextView) itemView.findViewById(R.id.text_result);
            signalImage = (ImageView) itemView.findViewById(R.id.image_signal);
        }
    }

    private void attachService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        // Debug
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setAlterEnabled(false)
                .setEmojiEnabled(false)
                .setPunctuationEnabled(false)
                .setSpellingEnabled(false)
                .setVprEnabled(false)
                .setEosPackets(3)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFileTag(DEBUG_FILE_PATH);
        conf.setIsDebugMode(true);
        conf.source(mLocalVoiceSource);
        conf.setSupportWakeUpMode(false);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        AutoTestActivity.this,
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        MainActivity.WEB_SOCKET_URL_DEV))
                .setLocale("en_US")
                .setSign(RequestManager.getSign(this))
                .setUserAgent(RequestManager.generateUserAgent(this))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        mVoiceService = VoiceService.getService(this, conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.setVoiceStateChangedListener(this);
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

        if (mResultIndex >= 0 && mResultIndex < mAutoTestItems.size()) {
            AutoTestItem item = mAutoTestItems.get(mResultIndex);
            if (message instanceof TextMessage) {
                item.result = ((TextMessage) message).text[0].toLowerCase().trim();
//                    ((TextMessage) message).cid));
            }
            mResultIndex++;
        } else {
            mVoiceService.stop();
        }
        mAutoTestingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreated() {

    }

    @Override
    public void onStartListening() {
        Logger.d("LocalPlayBackActivity onStartListening");
        if (mTextView != null) {
            mTextView.setText("starting.");
        }
        mStartButton.setEnabled(false);
        mResultIndex = 0;
        mAutoTestingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStopListening() {
        Logger.d("LocalPlayBackActivity onStopListening");
        if (mTextView != null) {
            mTextView.setText("stopped.");
        }
        mStartButton.setEnabled(true);
    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public void onError(int reason) {

    }

    @Override
    public void onConnectionClosed() {

    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {

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
                mVoiceService.stop();
            }
        }, 500);
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }
}
