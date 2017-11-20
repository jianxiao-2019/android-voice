package com.kikatech.go.dialogflow.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.sms.reply.SceneReplySms;
import com.kikatech.go.dialogflow.sms.send.SceneSendSms;
import com.kikatech.go.message.sms.SmsManager;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsSceneManager extends BaseSceneManager {

    private static final String KIKA_PROCESS_RECEIVED_SMS = "kika_process_received_sms";

    private BroadcastReceiver mSmsReceiver = null;

    public SmsSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);

        registerSmsReceiver();
    }

    private void registerSmsReceiver() {
        mSmsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SmsManager.ACTION_SMS_MESSAGE_UPDATED.equals(intent.getAction())) {
                    SmsObject smsObject = intent.getParcelableExtra(SmsManager.KEY_DATA_SMS_OBJECT);
                    if (LogUtil.DEBUG)
                        LogUtil.log("SmsSceneManager", "Received sms object: " + smsObject.getMsgContent());

                    mService.talk(KIKA_PROCESS_RECEIVED_SMS);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmsManager.ACTION_SMS_MESSAGE_UPDATED);
        mContext.registerReceiver(mSmsReceiver, filter);
    }

    @Override
    public void close() {
        super.close();
        mContext.unregisterReceiver(mSmsReceiver);
        mSmsReceiver = null;
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneSendSms(mContext, mService.getTtsFeedback()));
        mSceneBaseList.add(new SceneReplySms(mContext, mService.getTtsFeedback(), new SceneReplySms.ISmsFunc() {
            @Override
            public SmsObject getReceivedSms(long t) {
                return null;
            }
        }));
    }
}