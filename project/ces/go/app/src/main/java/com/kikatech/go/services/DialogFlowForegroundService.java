package com.kikatech.go.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.common.CommonSceneManager;
import com.kikatech.go.dialogflow.im.IMSceneManager;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.sms.SmsSceneManager;
import com.kikatech.go.dialogflow.stop.SceneStopIntentManager;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.go.ui.KikaAlphaUiActivity;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.ui.dialog.KikaStopServiceDialogActivity;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.usb.util.ImageUtil;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class DialogFlowForegroundService extends BaseForegroundService {
    private static final String TAG = "DialogFlowForegroundService";

    public final class SendBroadcastInfos {
        public static final String ACTION_ON_DIALOG_FLOW_INIT = "action_on_dialog_flow_init";
        public static final String ACTION_ON_ASR_RESULT = "action_on_asr_result";
        public static final String ACTION_ON_TEXT = "action_on_text";
        public static final String ACTION_ON_TEXT_PAIRS = "action_on_text_pairs";
        public static final String ACTION_ON_STAGE_PREPARED = "action_on_stage_prepared";
        public static final String ACTION_ON_STAGE_ACTION_DONE = "action_on_stage_action_done";
        public static final String ACTION_ON_STAGE_EVENT = "action_on_stage_event";
        public static final String ACTION_ON_SCENE_EXIT = "action_on_scene_exit";
        public static final String ACTION_ON_AGENT_QUERY_START = "action_on_agent_query_start";
        public static final String ACTION_ON_AGENT_QUERY_STOP = "action_on_agent_query_stop";
        public static final String ACTION_ON_AGENT_QUERY_ERROR = "action_on_agent_query_error";

        public static final String PARAM_EXTRAS = "param_extras";
        public static final String PARAM_TEXT = "param_text";
        public static final String PARAM_IS_FINISHED = "param_is_finished";
        public static final String PARAM_SCENE = "param_scene";
        public static final String PARAM_SCENE_ACTION = "param_scene_action";
        public static final String PARAM_SCENE_STAGE = "param_scene_stage";
        public static final String PARAM_IS_INTERRUPTED = "param_is_interrupted";
    }

    private final class ReceiveBroadcastInfos {
        private static final String ACTION_ON_STATUS_CHANGED = "action_status_changed";
        private static final String ACTION_ON_NAVIGATION_STARTED = "action_navigation_started";
        private static final String ACTION_ON_NAVIGATION_STOPPED = "action_navigation_stopped";

        private static final String ACTION_DIALOG_FLOW_TALK = "action_dialog_flow_talk";

        private static final String PARAM_STATUS = "param_status";
        private static final String PARAM_TEXT = "param_text";
    }

    private static WindowManager mWindowManager;
    private static LayoutInflater mLayoutInflater;
    private static WindowManager.LayoutParams mLayoutParam = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
    );
    private View mView;
    private ImageView mStatusView;

    private IDialogFlowService mDialogFlowService;
    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceive(context, intent);
        }

        private void handleReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case ReceiveBroadcastInfos.ACTION_ON_STATUS_CHANGED:
                    if (isViewAdded() && asrActive) {
                        handleStatusChanged(intent);
                    }
                    break;
                case ReceiveBroadcastInfos.ACTION_ON_NAVIGATION_STARTED:
                    pauseAsr();
                    showGMap();
                    break;
                case ReceiveBroadcastInfos.ACTION_ON_NAVIGATION_STOPPED:
                    removeView();
                    break;
                case ReceiveBroadcastInfos.ACTION_DIALOG_FLOW_TALK:
                    String text = intent.getStringExtra(ReceiveBroadcastInfos.PARAM_TEXT);
                    pauseAsr();
                    mDialogFlowService.talk(text);
                    break;
            }
        }
    };


    @Override
    protected void onStartForeground() {
        registerReceiver();
        initDialogFlowService();
    }

    @Override
    protected void onStopForeground() {
        unregisterReceiver();
        removeView();
        for (BaseSceneManager bcm : mSceneManagers) {
            if (bcm != null) bcm.close();
        }
        if (mDialogFlowService != null) {
            mDialogFlowService.quitService();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onStopForegroundWithConfirm() {
        Intent showDialogIntent = new Intent(this, KikaStopServiceDialogActivity.class);
        showDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        IntentUtil.sendPendingIntent(DialogFlowForegroundService.this, showDialogIntent);
    }


    private void initDialogFlowService() {
        mDialogFlowService = DialogFlowService.queryService(this,
                DialogFlowConfig.queryDemoConfig(this),
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        sendLocalBroadcast(new Intent(SendBroadcastInfos.ACTION_ON_DIALOG_FLOW_INIT));
                        asrActive = true;
                    }

                    @Override
                    public void onASRResult(final String speechText, boolean isFinished) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("speechText: %1$s, isFinished: %2$s", speechText, isFinished));
                        }
                        if (!asrActive) {
                            return;
                        } else if (isFinished) {
                            pauseAsr();
                        }
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_ASR_RESULT);
                        intent.putExtra(SendBroadcastInfos.PARAM_TEXT, speechText);
                        intent.putExtra(SendBroadcastInfos.PARAM_IS_FINISHED, isFinished);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onText(String text, Bundle extras) {
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_TEXT);
                        intent.putExtra(SendBroadcastInfos.PARAM_TEXT, text);
                        intent.putExtra(SendBroadcastInfos.PARAM_EXTRAS, extras);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras) {
                        StringBuilder builder = new StringBuilder();
                        if (pairs != null && pairs.length > 0) {
                            for (Pair<String, Integer> pair : pairs) {
                                builder.append(pair.first);
                            }
                        }
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_TEXT_PAIRS);
                        intent.putExtra(SendBroadcastInfos.PARAM_TEXT, builder.toString());
                        intent.putExtra(SendBroadcastInfos.PARAM_EXTRAS, extras);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onStagePrepared(String scene, String action, SceneStage stage) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("scene: %1$s, action: %2$s, stage: %3$s", scene, action, stage.getClass().getSimpleName()));
                        }
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_STAGE_PREPARED);
                        intent.putExtra(SendBroadcastInfos.PARAM_SCENE, scene);
                        intent.putExtra(SendBroadcastInfos.PARAM_SCENE_ACTION, action);
                        intent.putExtra(SendBroadcastInfos.PARAM_SCENE_STAGE, stage);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onStageActionDone(boolean isInterrupted) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("isInterrupted: %s", isInterrupted));
                        }
                        resumeAsr();
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_STAGE_ACTION_DONE);
                        intent.putExtra(SendBroadcastInfos.PARAM_IS_INTERRUPTED, isInterrupted);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onStageEvent(Bundle extras) {
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_STAGE_EVENT);
                        intent.putExtra(SendBroadcastInfos.PARAM_EXTRAS, extras);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onSceneExit() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSceneExit");
                        }
                        resumeAsr();
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_SCENE_EXIT);
                        sendLocalBroadcast(intent);
                    }
                }, new IDialogFlowService.IAgentQueryStatus() {
                    @Override
                    public void onStart() {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onStart");
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_AGENT_QUERY_START);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onComplete(String[] dbgMsg) {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onComplete");
                        // dbgMsg[0] : scene - action
                        // dbgMsg[1] : parameters
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_AGENT_QUERY_STOP);
                        sendLocalBroadcast(intent);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onError" + e);
                        Intent intent = new Intent(SendBroadcastInfos.ACTION_ON_AGENT_QUERY_ERROR);
                        sendLocalBroadcast(intent);
                    }
                });

        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(this, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(this, mDialogFlowService, KikaAlphaUiActivity.class));
        mSceneManagers.add(new SmsSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new IMSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new CommonSceneManager(this, mDialogFlowService));
    }

    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).sendBroadcast(intent);
    }


    private boolean asrActive;

    private void pauseAsr() {
        asrActive = false;
        mDialogFlowService.pauseAsr();
    }

    private void resumeAsr() {
        mDialogFlowService.resumeAsr();
        asrActive = true;
    }

    private void showGMap() {
        if (isViewAdded()) {
            return;
        }

        mView = mLayoutInflater.inflate(R.layout.go_layout_gmap, null);

        mStatusView = (ImageView) mView.findViewById(R.id.gmap_status);

        mStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeAsr();
                Glide.with(DialogFlowForegroundService.this)
                        .load(R.drawable.kika_gmap_awake)
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(mStatusView);
            }
        });

        addView();
    }

    private void addView() {
        try {
            // mLayoutParam.softInputMode = LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_ADJUST_PAN;
            mLayoutParam.gravity = Gravity.TOP | Gravity.RIGHT;
            mLayoutParam.x = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 14);
            mLayoutParam.y = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 14);
            mWindowManager.addView(mView, mLayoutParam);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    private void removeView() {
        try {
            mWindowManager.removeView(mView);
        } catch (Exception ignore) {
        }
    }

    private boolean isViewAdded() {
        try {
            return mView != null && mView.getWindowToken() != null;
        } catch (Exception ignore) {
        }
        return true;
    }


    private void registerReceiver() {
        unregisterReceiver();
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).registerReceiver(mReceiver, new IntentFilter(ReceiveBroadcastInfos.ACTION_ON_STATUS_CHANGED));
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).registerReceiver(mReceiver, new IntentFilter(ReceiveBroadcastInfos.ACTION_ON_NAVIGATION_STARTED));
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).registerReceiver(mReceiver, new IntentFilter(ReceiveBroadcastInfos.ACTION_ON_NAVIGATION_STOPPED));
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).registerReceiver(mReceiver, new IntentFilter(ReceiveBroadcastInfos.ACTION_DIALOG_FLOW_TALK));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).unregisterReceiver(mReceiver);
        } catch (Exception ignore) {
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        onStopForeground();
        super.onDestroy();
    }

    private void handleStatusChanged(Intent intent) {
        GoLayout.ViewStatus status = (GoLayout.ViewStatus) intent.getSerializableExtra(ReceiveBroadcastInfos.PARAM_STATUS);
        Glide.with(DialogFlowForegroundService.this)
                .load(status.getSmallRes())
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusView);
    }


    public synchronized static void processStatusChanged(Context context, GoLayout.ViewStatus status) {
        Intent intent = new Intent(ReceiveBroadcastInfos.ACTION_ON_STATUS_CHANGED);
        intent.putExtra(ReceiveBroadcastInfos.PARAM_STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public synchronized static void processNavigationStarted(Context context) {
        Intent intent = new Intent(ReceiveBroadcastInfos.ACTION_ON_NAVIGATION_STARTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public synchronized static void processNavigationStopped(Context context) {
        Intent intent = new Intent(ReceiveBroadcastInfos.ACTION_ON_NAVIGATION_STOPPED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public synchronized static void processDialogFlowTalk(Context context, String text) {
        Intent intent = new Intent(ReceiveBroadcastInfos.ACTION_DIALOG_FLOW_TALK);
        intent.putExtra(ReceiveBroadcastInfos.PARAM_TEXT, text);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    @Override
    protected Notification getForegroundNotification() {
        Intent closeIntent = new Intent(DialogFlowForegroundService.this, DialogFlowForegroundService.class);
        closeIntent.setAction(Commands.STOP_FOREGROUND_WITH_CONFIRM);
        PendingIntent closePendingIntent = PendingIntent.getService(DialogFlowForegroundService.this, getServiceId(), closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(DialogFlowForegroundService.this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Kika Go 录音中")
                .setContentText("点击此处结束Kika Go")
                .setContentIntent(closePendingIntent)
                .setAutoCancel(true)
                // .setColor( appCtx.getResources().getColor( R.color.gela_green ) )
                .build();
    }

    @Override
    protected int getServiceId() {
        return ServiceIds.DIALOG_FLOW_SERVICE;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
