package com.kikatech.voicesdktester.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.ui.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanlin on 03/01/2018.
 */

public class PlayActivity extends AppCompatActivity {

    private static final String PATH = Environment.getExternalStorageDirectory() + "/kikaVoiceSDK/voiceTester/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        RecyclerView recyclerViewMic = (RecyclerView) findViewById(R.id.recycler_mic);
        recyclerViewMic.setAdapter(new FileAdapter(PATH, scanAvailableFile(PATH)));
        recyclerViewMic.setLayoutManager(new LinearLayoutManager(this));

//        RecyclerView recyclerViewLocal = (RecyclerView) findViewById(R.id.recycler_local);
//        recyclerViewLocal.setAdapter(new FileAdapter(LocalPlayBackActivity.PATH_FROM_LOCAL, scanAvailableFile(LocalPlayBackActivity.PATH_FROM_LOCAL)));
//        recyclerViewLocal.setLayoutManager(new LinearLayoutManager(this));
    }

    private List<String> scanAvailableFile(String path) {
        List<String> fileNames = new ArrayList<>();

        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            return fileNames;
        }

        for (final File file : folder.listFiles()) {
            if (file.isDirectory()
                    || file.getName().contains("wav")
                    || file.getName().contains("speex")
                    || file.getName().contains("txt")) {
                continue;
            }
            fileNames.add(file.getName());
        }
        return fileNames;
    }
}
