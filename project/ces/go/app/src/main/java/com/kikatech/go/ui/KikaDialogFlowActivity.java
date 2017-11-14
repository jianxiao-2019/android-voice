package com.kikatech.go.ui;

import android.app.PendingIntent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.navigation.NavigationCommand;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.go.navigation.NavigationManager;
import com.kikatech.go.navigation.provider.BaseNavigationProvider;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.SceneType;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;

import java.util.ArrayList;

/**
 * @author SkeeterWang Created on 2017/11/4.
 */
public class KikaDialogFlowActivity extends BaseActivity {

    private static final String TAG = "KikaDialogFlowActivity";

    private EditText mWordsInput;
    private View mBtnResetContexts;
    private View mBtnQuery;
    private View mBtnResetAll;
    private View mBtnClearInputs;
    private TextView mTvScene;
    private TextView mTvName;
    private TextView mTvAction;
    private TextView mTvExtras;
    private View[] mInteractiveViews;

    private IDialogFlowService mDialogFlowService;
    private TelephonySceneManager mTelephonySceneManager;
    private NaviSceneManager mNaviSceneManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_dialog_flow);
        bindView();
        setViewEnable(false);

        initDialogFlowService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTelephonySceneManager != null){
            mTelephonySceneManager.close();
        }
        if(mNaviSceneManager != null) {
            mNaviSceneManager.close();
        }
        if (mDialogFlowService != null) {
            mDialogFlowService.quitService();
        }
    }

    private void initDialogFlowService() {
        mDialogFlowService = DialogFlowService.queryService(this,
                DialogFlowConfig.queryDemoConfig(this),
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLongToast("Dialog Service Init Completed");
                                setViewEnable(true);
                            }
                        });
                    }

                    @Override
                    public void onCommand(SceneType scene, byte cmd, Bundle parameters) {
                        switch (scene) {
                            case NAVIGATION:
                                processNavigationCommand(cmd, parameters);
                                break;
                            case DEFAULT:
                                processGeneralCommand(cmd);
                                break;
                        }
                    }

                    @Override
                    public void onSpeechSpokenDone(final String speechText) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWordsInput.setText(speechText);
                            }
                        });
                    }
                });

        // Register all scenes from scene mangers
        mTelephonySceneManager = new TelephonySceneManager(this, mDialogFlowService);
        mNaviSceneManager = new NaviSceneManager(this, mDialogFlowService);
    }

    private void processGeneralCommand(byte cmd) {
        switch (cmd) {
            // TODO: 17-11-10
//            case GeneralCommand.GENERAL_CMD_UNKNOWN:
//                tts("I cannot get what you mean, What's the message ?");
//                break;
            default:
                break;
        }
    }

    private void processNavigationCommand(byte cmd, Bundle parameters) {
        String toast = "UNKNOWN";
        String log = "UNKNOWN";
        String address = parameters.getString(NavigationCommand.NAVI_CMD_ADDRESS);
        switch (cmd) {
            case NavigationCommand.NAVI_CMD_ERR:
                //
                log = "NAVI_CMD_ERR";
                toast = "Error occurs, please contact RD";
                break;
            case NavigationCommand.NAVI_CMD_ASK_ADDRESS:
                //
                log = "NAVI_CMD_ASK_ADDRESS";
                toast = "Please tell me your address";
                break;
            case NavigationCommand.NAVI_CMD_CONFIRM_ADDRESS:
                //
                log = "NAVI_CMD_CONFIRM_ADDRESS";
                toast = "Is your address '" + address + "' correct ?";
                break;
            case NavigationCommand.NAVI_CMD_START_NAVI:
                //
                log = "NAVI_CMD_START_NAVI";
                toast = "[Send Intent to Start Navigation to '" + address + "']";
                navigateToLocation(address);
                break;
            case NavigationCommand.NAVI_CMD_ASK_ADDRESS_AGAIN:
                //
                log = "NAVI_CMD_ASK_ADDRESS_AGAIN";
                toast = "Please tell me the address again";
                break;
            case NavigationCommand.NAVI_CMD_STOP_NAVIGATION:
                //
                log = "NAVI_CMD_STOP_NAVIGATION";
                toast = "Stop Navigation, bye bye";
                stopNavigation();
                break;
            case NavigationCommand.NAVI_CMD_DONT_UNDERSTAND:
                log = "NAVI_CMD_DONT_UNDERSTAND";
                toast = "Sorry I don't get it, would you say that again ?";
                break;
            default:
                //
                break;
        }

        if (LogUtil.DEBUG) LogUtil.log(TAG, log);
    }

    /**
     * This is a workaround ...
     */
    private void stopNavigation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NavigationManager.getIns().stopNavigation(KikaDialogFlowActivity.this);

//                tts("Stopping Navigation ...");

                mWordsInput.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            android.content.Intent intent = new android.content.Intent(KikaDialogFlowActivity.this, KikaDialogFlowActivity.class);
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(KikaDialogFlowActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            pendingIntent.send();
                        } catch (Exception ignore) {
                        }
                    }
                }, 4000);
            }
        });
    }

    private void navigateToLocation(String loc) {
        ArrayList<BaseNavigationProvider.NavigationAvoid> avoidList = new ArrayList<>();
        final BaseNavigationProvider.NavigationAvoid[] avoids = avoidList.toArray(new BaseNavigationProvider.NavigationAvoid[0]);
        NavigationManager.getIns().startNavigation(this, loc, BaseNavigationProvider.NavigationMode.DRIVE, avoids);
    }

    private void handleLog(final Intent intent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetLogs();
                if (intent != null) {
                    mTvScene.setText(intent.getScene());
                    mTvAction.setText(intent.getAction());
                    Bundle args = intent.getExtra();
                    if (args != null && !args.isEmpty()) {
                        String extras = "";
                        for (String key : args.keySet()) {
                            extras = extras + key + ": " + args.getString(key) + "\n";
                        }
                        mTvExtras.setText(extras);
                    }
                }
            }
        });
    }

    private void resetLogs() {
        mTvScene.setText("");
        mTvName.setText("");
        mTvAction.setText("");
        mTvExtras.setText("");
    }

    private void bindView() {
        mWordsInput = (EditText) findViewById(R.id.edit_words);

        mBtnResetContexts = findViewById(R.id.btn_reset_contexts);
        mBtnQuery = findViewById(R.id.btn_query);
        mBtnResetAll = findViewById(R.id.btn_reset_all);
        mBtnClearInputs = findViewById(R.id.btn_clear_words);

        mTvScene = (TextView) findViewById(R.id.log_tv_scene);
        mTvName = (TextView) findViewById(R.id.log_tv_name);
        mTvAction = (TextView) findViewById(R.id.log_tv_action);
        mTvExtras = (TextView) findViewById(R.id.log_tv_extras);

        mBtnResetContexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFlowService.resetContexts();
            }
        });

        mBtnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String words = mWordsInput.getText().toString();
                if (!TextUtils.isEmpty(words)) {
                    mDialogFlowService.talk(words);
                }
            }
        });

        mBtnClearInputs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWordsInput.setText("");
            }
        });

        mBtnResetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWordsInput.setText("");
                resetLogs();
                mDialogFlowService.resetContexts();
            }
        });

        mInteractiveViews = new View[]{mWordsInput, mBtnResetContexts, mBtnQuery, mBtnResetAll, mBtnClearInputs};
    }

    private void setViewEnable(boolean enable) {
        if (mInteractiveViews != null) {
            for (View v : mInteractiveViews) {
                v.setEnabled(enable);
            }
        }
    }
}