package com.kikatech.go.dialogflow.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.LongSparseArray;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.im.reply.SceneReplyIM;
import com.kikatech.go.dialogflow.im.send.SceneSendIM;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.message.im.IMManager;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class IMSceneManager extends BaseSceneManager {

    private static final String KIKA_PROCESS_RECEIVED_IM = "kika_process_received_im %d";

    private BroadcastReceiver mImReceiver = null;
    private LongSparseArray<BaseIMObject> mReceivedSmsList;

    public IMSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);

        mReceivedSmsList = new LongSparseArray<>();
        registerImReceiver();
    }

    private void registerImReceiver() {
        mImReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (IMManager.ACTION_IM_MESSAGE_UPDATED.equals(intent.getAction())) {
                    BaseIMObject imObjectData = intent.getParcelableExtra(IMManager.DATA_IM_OBJECT);
                    final BaseIMObject imObject = IMManager.getInstance().getReferenceIfExist(imObjectData);

                    if (LogUtil.DEBUG) {
                        LogUtil.log("IMSceneManager", imObject.getAppName() + ", " + imObject.getMsgContent());
                    }

                    long t = System.currentTimeMillis();
                    mReceivedSmsList.put(t, imObject);

                    DialogFlowForegroundService.processOnNewMsg(KIKA_PROCESS_RECEIVED_IM, t);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(IMManager.ACTION_IM_MESSAGE_UPDATED);
        mContext.registerReceiver(mImReceiver, filter);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneSendIM(mContext, mService.getTtsFeedback()));
        mSceneBaseList.add(new SceneReplyIM(mContext, mService.getTtsFeedback(), new SceneReplyIM.IImFunc() {
            @Override
            public BaseIMObject getReceivedIM(long timestamp) {
                BaseIMObject imo = mReceivedSmsList.get(timestamp);
                if (imo != null) mReceivedSmsList.remove(timestamp);
                return imo;
            }
        }));
    }

    @Override
    public void close() {
        super.close();
        try {
            mContext.unregisterReceiver(mImReceiver);
            mImReceiver = null;
            mReceivedSmsList.clear();
            mReceivedSmsList = null;
        } catch (Exception ignore) {
        }
    }
}