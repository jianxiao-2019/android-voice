package com.kikatech.go.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.kikatech.go.eventbus.ToDFServiceEvent;
import com.kikatech.go.services.view.FloatingUiManager;
import com.kikatech.go.ui.KikaAlphaUiActivity;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.ui.dialog.KikaStopServiceDialogActivity;
import com.kikatech.go.util.AsyncThread;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.usb.util.ImageUtil;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;
import com.kikatech.voice.service.conf.AsrConfiguration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class DialogFlowForegroundService extends BaseForegroundService {
    private static final String TAG = "DialogFlowForegroundService";

    private static final long TTS_DELAY_ASR_RESUME = 500;

    private static FloatingUiManager mManager;

    private static WindowManager.LayoutParams mLayoutParam = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
    );

    private WindowManager.LayoutParams mLayoutParamsTipsView = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
    );

    private WindowManager.LayoutParams mLayoutParamsMsgView = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
    );


    private PowerManager.WakeLock mWakeLocker;
    private boolean isTipViewShown;

    private View mGMapView;
    private ImageView mStatusView;
    private View mStatusWrapperView;

    private View mTipView;

    private View mMsgView;
    private TextView mMsgViewText;

    private IDialogFlowService mDialogFlowService;
    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();


    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event sent to {@link com.kikatech.go.services.DialogFlowForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToServiceEvent(ToDFServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case ToDFServiceEvent.ACTION_ON_APP_FOREGROUND:
                if (mManager.isViewAdded(mGMapView)) {
                    mGMapView.setVisibility(View.GONE);
                    mTipView.setVisibility(View.GONE);
                    mMsgView.setVisibility(View.GONE);
                }
                break;
            case ToDFServiceEvent.ACTION_ON_APP_BACKGROUND:
                if (mManager.isViewAdded(mGMapView)) {
                    mGMapView.setVisibility(View.VISIBLE);
                    mTipView.setVisibility(View.VISIBLE);
                    mMsgView.setVisibility(View.VISIBLE);
                }
                break;
            case ToDFServiceEvent.ACTION_ON_STATUS_CHANGED:
                GoLayout.ViewStatus status = (GoLayout.ViewStatus) event.getExtras().getSerializable(ToDFServiceEvent.PARAM_STATUS);
                handleStatusChanged(status);
                break;
            case ToDFServiceEvent.ACTION_ON_MSG_CHANGED:
                String msg = event.getExtras().getString(ToDFServiceEvent.PARAM_TEXT);
                handleMsgChanged(msg);
                break;
            case ToDFServiceEvent.ACTION_ON_NAVIGATION_STARTED:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                pauseAsr();
                showGMap();
                break;
            case ToDFServiceEvent.ACTION_ON_NAVIGATION_STOPPED:
                removeGMap();
                break;
            case ToDFServiceEvent.ACTION_DIALOG_FLOW_TALK:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                String text = event.getExtras().getString(ToDFServiceEvent.PARAM_TEXT);
                pauseAsr();
                mDialogFlowService.talk(text);
                break;
            case ToDFServiceEvent.ACTION_DIALOG_FLOW_WAKE_UP:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                mDialogFlowService.wakeUp();
                break;
        }
    }


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
        removeGMap();
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
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onInitComplete");
                        }
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onWakeUp() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onWakeUp");
                        }
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_WAKE_UP);
                        sendDFServiceEvent(event);
                        resumeAsr();
                    }

                    @Override
                    public void onSleep() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSleep");
                        }
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_SLEEP);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onVadBos() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onVadBos");
                        }
                        pauseAsr();
                        mDialogFlowService.talkUncaught();
                    }

                    @Override
                    public void onASRPause() {
                        sendDFServiceEvent(new DFServiceEvent(DFServiceEvent.ACTION_ON_ASR_PAUSE));
                    }

                    @Override
                    public void onASRResume() {
                        sendDFServiceEvent(new DFServiceEvent(DFServiceEvent.ACTION_ON_ASR_RESUME));
                    }

                    @Override
                    public void onASRResult(final String speechText, String emojiUnicode, boolean isFinished) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("speechText: %1$s, emoji: %2%s, isFinished: %3$s", speechText, emojiUnicode, isFinished));
                        }
                        if (!asrActive) {
                            return;
                        } else if (isFinished) {
                            pauseAsr();
                        }

                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_ASR_RESULT);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, speechText);
                        //event.putExtra(DFServiceEvent.PARAM_EMOJI, emojiUnicode);
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
                    public void onStageActionStart() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onStageActionStart");
                        }
                        pauseAsr();
                    }

                    @Override
                    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("isInterrupted: %1$s, delayAsrResume: %2$s", isInterrupted, delayAsrResume));
                        }
                        if (delayAsrResume) {
                            AsyncThread.getIns().executeDelay(new Runnable() {
                                @Override
                                public void run() {
                                    resumeAsr();
                                }
                            }, TTS_DELAY_ASR_RESUME);
                        } else {
                            resumeAsr();
                        }
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
                    public void onSceneExit(boolean proactive) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSceneExit");
                        }
                        if (proactive) {
                            resumeAsr();
                        }
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_SCENE_EXIT);
                        event.putExtra(DFServiceEvent.PARAM_IS_PROACTIVE, proactive);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onAsrConfigChange(AsrConfiguration asrConfig) {
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_ASR_CONFIG);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, asrConfig.toJsonString());
                        sendDFServiceEvent(event);
                    }
                }, new IDialogFlowService.IAgentQueryStatus() {
                    @Override
                    public void onStart() {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onStart");
                        pauseAsr();
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_AGENT_QUERY_START);
                        sendDFServiceEvent(event);
                    }

                    @Override
                    public void onComplete(String[] dbgMsg) {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onComplete");
                        // dbgMsg[0] : scene - action
                        // dbgMsg[1] : parameters
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_AGENT_QUERY_COMPLETE);
                        event.putExtra(DFServiceEvent.PARAM_DBG_INTENT_ACTION, dbgMsg[0]);
                        event.putExtra(DFServiceEvent.PARAM_DBG_INTENT_PARMS, dbgMsg[1]);
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

    private synchronized void pauseAsr() {
        if (asrActive) {
            asrActive = false;
            mDialogFlowService.pauseAsr();
        }
    }

    private synchronized void resumeAsr() {
        if (!asrActive) {
            mDialogFlowService.resumeAsr();
            asrActive = true;
        }
    }


    private Runnable removeTipViewRunnable = new Runnable() {
        @Override
        public void run() {
            mManager.removeView(mTipView);
        }
    };

    private Runnable removeMsgViewRunnable = new Runnable() {
        @Override
        public void run() {
            mManager.removeView(mMsgView);
        }
    };

    private void showGMap() {
        if (mManager.isViewAdded(mGMapView)) {
            return;
        }

        mGMapView = mManager.inflate(R.layout.go_layout_gmap);

        mStatusWrapperView = mGMapView.findViewById(R.id.gmap_status_wrapper);
        mStatusView = (ImageView) mGMapView.findViewById(R.id.gmap_status);

        mGMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialogFlowService != null) {
                    mDialogFlowService.wakeUp();
                }
            }
        });

        mLayoutParam.gravity = Gravity.TOP | Gravity.RIGHT;
        mLayoutParam.x = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 14);
        mLayoutParam.y = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 14);

        mManager.addView(mGMapView, mLayoutParam);

        isTipViewShown = false;
    }

    private void removeGMap() {
        mManager.removeView(mGMapView);
        mManager.removeView(mTipView);
        mManager.removeView(mMsgView);
        mManager.removeCallbacks(removeTipViewRunnable);
        mManager.removeCallbacks(removeMsgViewRunnable);
    }

    private void showTipView() {
        if (mManager.isViewAdded(mTipView)) {
            mManager.removeView(mTipView);
            mManager.removeCallbacks(removeTipViewRunnable);
        }

        mTipView = mManager.inflate(R.layout.go_layout_gmap_tip);

        mLayoutParamsTipsView.gravity = Gravity.TOP | Gravity.RIGHT;
        mLayoutParamsTipsView.x = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 82);
        mLayoutParamsTipsView.y = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 78) - ResolutionUtil.getStatusBarHeight(DialogFlowForegroundService.this);
        mLayoutParamsTipsView.windowAnimations = android.R.style.Animation_Toast;

        mManager.addView(mTipView, mLayoutParamsTipsView);

        mManager.postDelay(removeTipViewRunnable, 3000);
    }

    private void showMsgView(String text) {
        if (mManager.isViewAdded(mMsgView)) {
            mManager.removeView(mMsgView);
            mManager.removeCallbacks(removeMsgViewRunnable);
        }

        mMsgView = mManager.inflate(R.layout.go_layout_gmap_msg);
        mMsgViewText = (TextView) mMsgView.findViewById(R.id.gmap_msg);

        mMsgViewText.setText(text);

        mLayoutParamsMsgView.gravity = Gravity.TOP | Gravity.RIGHT;
        mLayoutParamsMsgView.x = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 82);
        mLayoutParamsMsgView.y = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 78) - ResolutionUtil.getStatusBarHeight(DialogFlowForegroundService.this);
        mLayoutParamsMsgView.windowAnimations = android.R.style.Animation_Toast;

        mManager.addView(mMsgView, mLayoutParamsMsgView);

        mManager.postDelay(removeMsgViewRunnable, 2200);
    }


    private void registerReceiver() {
        unregisterReceiver();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceiver() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mManager = new FloatingUiManager.Builder()
                .setWindowManager((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .setLayoutInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .setConfiguration(getResources().getConfiguration())
                .setUiHandler(new Handler(Looper.getMainLooper()))
                .build();
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


    private void handleStatusChanged(GoLayout.ViewStatus status) {

        if (!mManager.isViewAdded(mGMapView) || status == null) {
            return;
        }

        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "handleStatusChanged: status: " + status.name());
        }

        if (mStatusWrapperView.getVisibility() == View.GONE) {
            mStatusWrapperView.setVisibility(View.VISIBLE);
        }

        Glide.with(DialogFlowForegroundService.this.getApplicationContext())
                .load(status.getSmallRes())
                .dontTransform()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusView);

        if (!isTipViewShown) {
            showTipView();
            isTipViewShown = true;
        }
    }

    private void handleMsgChanged(String text) {
        if (!mManager.isViewAdded(mGMapView) || TextUtils.isEmpty(text)) {
            return;
        }
        showMsgView(text);
    }


    public synchronized static void processOnAppForeground() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_APP_FOREGROUND);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processOnAppBackground() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_APP_BACKGROUND);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processStatusChanged(GoLayout.ViewStatus status) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_STATUS_CHANGED);
        event.putExtra(ToDFServiceEvent.PARAM_STATUS, status);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processMsgChanged(String text) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_MSG_CHANGED);
        event.putExtra(ToDFServiceEvent.PARAM_TEXT, text);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processNavigationStarted() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_NAVIGATION_STARTED);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processNavigationStopped() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_NAVIGATION_STOPPED);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processDialogFlowTalk(String text) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DIALOG_FLOW_TALK);
        event.putExtra(ToDFServiceEvent.PARAM_TEXT, text);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processDialogFlowWakeUp() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DIALOG_FLOW_WAKE_UP);
        sendToDFServiceEvent(event);
    }

    private synchronized static void sendToDFServiceEvent(ToDFServiceEvent event) {
        EventBus.getDefault().post(event);
    }


    @Override
    protected Notification getForegroundNotification() {
        Intent closeIntent = new Intent(DialogFlowForegroundService.this, DialogFlowForegroundService.class);
        closeIntent.setAction(Commands.STOP_FOREGROUND);
        PendingIntent closePendingIntent = PendingIntent.getService(DialogFlowForegroundService.this, getServiceId(), closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(DialogFlowForegroundService.this)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.app_icon))
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
