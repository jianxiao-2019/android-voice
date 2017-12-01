package com.kikatech.go.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.kikatech.go.eventbus.DFServiceEvent;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class DialogFlowForegroundService extends BaseForegroundService {
    private static final String TAG = "DialogFlowForegroundService";

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
    private PowerManager.WakeLock mWakeLocker;
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
        acquireWakeLock();
    }

    @Override
    protected void onStopForeground() {
        Toast.makeText(DialogFlowForegroundService.this, "KikaGo is closed", Toast.LENGTH_SHORT).show();
        releaseWakeLock();
        unregisterReceiver();
        removeView();
        for (BaseSceneManager bcm : mSceneManagers) {
            if (bcm != null) bcm.close();
        }
        if (mDialogFlowService != null) {
            mDialogFlowService.quitService();
        }
        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_EXIT_APP);
        sendDFServiceEvent(event);
    }

    @Override
    protected void onStopForegroundWithConfirm() {
        Intent showDialogIntent = new Intent(this, KikaStopServiceDialogActivity.class);
        showDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        IntentUtil.sendPendingIntent(DialogFlowForegroundService.this, showDialogIntent);
    }


    @SuppressLint("WakelockTimeout")
    @SuppressWarnings("deprecation")
    private void acquireWakeLock() {
        if (mWakeLocker == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm == null) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "PowerManager is null, return");
                }
                return;
            }
            mWakeLocker = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
            mWakeLocker.setReferenceCounted(false);
        }
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "acquireWakeLock");
        }
        mWakeLocker.acquire();
    }

    private void releaseWakeLock() {
        if (mWakeLocker != null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "releaseWakeLock");
            }
            mWakeLocker.release();
            if (!mWakeLocker.isHeld()) {
                mWakeLocker = null;
            }
        }
    }


    private void initDialogFlowService() {
        mDialogFlowService = DialogFlowService.queryService(this,
                DialogFlowConfig.queryDemoConfig(this),
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT);
                        sendDFServiceEvent(event);
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

                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_ASR_RESULT);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, speechText);
                        event.putExtra(DFServiceEvent.PARAM_IS_FINISHED, isFinished);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onText(String text, Bundle extras) {
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_TEXT);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, text);
                        event.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras) {
                        StringBuilder builder = new StringBuilder();
                        if (pairs != null && pairs.length > 0) {
                            for (Pair<String, Integer> pair : pairs) {
                                builder.append(pair.first);
                            }
                        }
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_TEXT_PAIRS);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, builder.toString());
                        event.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onStagePrepared(String scene, String action, SceneStage stage) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("scene: %1$s, action: %2$s, stage: %3$s", scene, action, stage.getClass().getSimpleName()));
                        }
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_STAGE_PREPARED);
                        event.putExtra(DFServiceEvent.PARAM_SCENE, scene);
                        event.putExtra(DFServiceEvent.PARAM_SCENE_ACTION, action);
                        event.putExtra(DFServiceEvent.PARAM_SCENE_STAGE, stage);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onStageActionDone(boolean isInterrupted) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("isInterrupted: %s", isInterrupted));
                        }
                        resumeAsr();
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_STAGE_ACTION_DONE);
                        event.putExtra(DFServiceEvent.PARAM_IS_INTERRUPTED, isInterrupted);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onStageEvent(Bundle extras) {
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_STAGE_EVENT);
                        event.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onSceneExit() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSceneExit");
                        }
                        resumeAsr();
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_SCENE_EXIT);
                        sendDFServiceEvent(event);
                    }
                }, new IDialogFlowService.IAgentQueryStatus() {
                    @Override
                    public void onStart() {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onStart");
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_AGENT_QUERY_START);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onComplete(String[] dbgMsg) {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onComplete");
                        // dbgMsg[0] : scene - action
                        // dbgMsg[1] : parameters
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_AGENT_QUERY_STOP);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onError" + e);
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_AGENT_QUERY_ERROR);
                        sendDFServiceEvent(event);
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

    private void sendDFServiceEvent(DFServiceEvent event) {
        EventBus.getDefault().post(event);
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
        closeIntent.setAction(Commands.STOP_FOREGROUND);
        PendingIntent closePendingIntent = PendingIntent.getService(DialogFlowForegroundService.this, getServiceId(), closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(DialogFlowForegroundService.this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("KikaGo is running in the background")
                .setContentText("Tap to close KikaGo")
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
