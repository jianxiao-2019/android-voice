package com.kikatech.go.view;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.ContactOptionList;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.model.UserInfo;
import com.kikatech.go.dialogflow.model.UserMsg;
import com.kikatech.go.util.MediaPlayerUtil;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author SkeeterWang Created on 2017/11/17.
 */
public class UiTaskManager {
    private static final String TAG = "UiTaskManager";

    private static final OptionList mDefaultOptionList = OptionList.getDefaultOptionList();

    private static final String PARAM_SPEECH_TEXT = "param_speech_text";
    private static final String PARAM_IS_FINISHED = "param_is_finished";

    private final class TaskType {
        private static final byte TYPE_TTS = 0x01;
        private static final byte TYPE_SPEECH = 0x02;
        private static final byte TYPE_OPTIONS = 0x03;
        private static final byte TYPE_SCENE_STAGE = 0x04;
        private static final byte TYPE_DEFAULT_OPTIONS = 0x05;
    }

    private class Task {
        private byte type;
        private Object content;

        private Task(byte type, Object content) {
            this.type = type;
            this.content = content;
        }
    }


    private GoLayout mLayout;
    private IUiManagerFeedback mFeedback;
    private Queue<Task> mTaskQueue = new LinkedList<>();


    public UiTaskManager(GoLayout layout, IUiManagerFeedback feedback) {
        mLayout = layout;
        mFeedback = feedback;
        mLayout.setOnModeChangedListener(new GoLayout.IOnModeChangedListener() {
            @Override
            public void onChanged(GoLayout.DisplayMode mode) {
                if (mFeedback != null) {
                    mFeedback.onLayoutModeChanged(mode);
                }
                switch (mode) {
                    case AWAKE:
                        displayOptions(mDefaultOptionList);
                        unlock(true);
                        break;
                    case SLEEP:
                        break;
                }
            }
        });
        mLayout.setOnLockStateChangeListener(new GoLayout.IOnLockStateChangeListener() {
            @Override
            public void onLocked() {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "onLock");
                }
            }

            @Override
            public void onLockReleased() {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "onLockRelease:");
                }
                Task task = pollFromQueue();
                if (task == null) {
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, "UI Task Queue is empty.");
                    }
                    return;
                }
                Object obj = task.content;
                if (obj == null) {
                    return;
                }
                byte type = task.type;
                switch (type) {
                    case TaskType.TYPE_SCENE_STAGE:
                        final SceneStage stage = (SceneStage) obj;
                        doStageAction(stage);
                        break;
                    case TaskType.TYPE_SPEECH:
                        final Bundle extras = (Bundle) obj;
                        String speech = extras.getString(PARAM_SPEECH_TEXT);
                        boolean isFinished = extras.getBoolean(PARAM_IS_FINISHED);
                        listen(speech, isFinished);
                        break;
                }
            }
        });
    }


    public synchronized void dispatchWakeUp() {
        wakeUp();
    }

    public synchronized void dispatchSleep() {
        sleep();
    }

    public synchronized void dispatchConnectionStatusChanged(boolean connected) {
        onConnectionStatusChanged(connected);
    }

    public synchronized void dispatchAsrStart() {
        onStatusChanged(GoLayout.ViewStatus.ANALYZE);
        playAlert(R.raw.alert_stop);
    }

    public synchronized void dispatchTtsTask(String text, Bundle extras) {
        if (extras != null) {
            String uiText = extras.getString(SceneUtil.EXTRA_UI_TEXT, text);
            if (extras.containsKey(SceneUtil.EXTRA_CONTACT_OPTIONS_LIST)) {
                ContactOptionList contactOptionList = extras.getParcelable(SceneUtil.EXTRA_CONTACT_OPTIONS_LIST);
                if (contactOptionList != null && !contactOptionList.isEmpty()) {
                    displayContactOptions(contactOptionList);
                }
            }
            if (extras.containsKey(SceneUtil.EXTRA_OPTIONS_LIST)) {
                OptionList optionList = extras.getParcelable(SceneUtil.EXTRA_OPTIONS_LIST);
                if (optionList != null && !optionList.isEmpty()) {
                    displayOptions(optionList);
                }
            } else if (extras.containsKey(SceneUtil.EXTRA_USR_INFO)) {
                UserInfo userInfo = extras.getParcelable(SceneUtil.EXTRA_USR_INFO);
                if (userInfo != null) {
                    displayUsrInfo(userInfo.getAvatar(), userInfo.getName(), userInfo.getAppInfo());
                }
            } else if (extras.containsKey(SceneUtil.EXTRA_USR_MSG)) {
                UserMsg userMsg = extras.getParcelable(SceneUtil.EXTRA_USR_MSG);
                if (userMsg != null) {
                    displayUsrMsg(userMsg.getAvatar(), userMsg.getName(), userMsg.getMsg(), userMsg.getAppInfo());
                }
            } else if (extras.containsKey(SceneUtil.EXTRA_TTS_TEXT)) {
                TtsText ttsText = extras.getParcelable(SceneUtil.EXTRA_TTS_TEXT);
                speak(ttsText);
            }
        } else {
            speak(new TtsText(text));
        }
    }

    public synchronized void dispatchEventTask(Bundle extras) {
        if (extras != null) {
            String event = extras.getString(SceneUtil.EXTRA_EVENT, null);
            int alertRes = extras.getInt(SceneUtil.EXTRA_ALERT, 0);
            if (!TextUtils.isEmpty(event)) {
                switch (event) {
                    case SceneUtil.EVENT_DISPLAY_MSG_SENT:
                        if (alertRes > 0) {
                            playAlert(alertRes);
                        }
                        boolean isSentSuccess = extras.getBoolean(SceneUtil.EXTRA_SEND_SUCCESS, true);
                        displayMsgSent(isSentSuccess);
                        break;
                    case SceneUtil.EVENT_OUTGOING_CALL:
                        onStatusChanged(GoLayout.ViewStatus.ANALYZE);
                        break;
                }
            }
        }
    }

    public synchronized void dispatchStageTask(SceneStage sceneStage) {
        final boolean isLayoutPerformTask = isLayoutPerformTask();
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("isLayoutPerformTask: %s", isLayoutPerformTask));
        }
        if (isLayoutPerformTask) {
            addToQueue(new Task(TaskType.TYPE_SCENE_STAGE, sceneStage));
        } else {
            doStageAction(sceneStage);
        }
    }

    private synchronized void doStageAction(final SceneStage stage) {
        onStatusChanged(GoLayout.ViewStatus.ANALYZE_TO_TTS, new GoLayout.IGifStatusListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onStop(Exception e) {
                stage.doAction();
            }
        });
    }

    public synchronized void dispatchSpeechTask(String speech) {
        dispatchSpeechTask(speech, true);
    }

    public synchronized void dispatchSpeechTask(String speech, boolean isFinished) {
        if (isLayoutPerformTask() &&
                !(GoLayout.ViewStatus.LISTEN_1.equals(mLayout.getCurrentStatus())
                        || GoLayout.ViewStatus.LISTEN_2.equals(mLayout.getCurrentStatus()))) {
            Bundle extras = new Bundle();
            extras.putString(PARAM_SPEECH_TEXT, speech);
            extras.putBoolean(PARAM_IS_FINISHED, isFinished);
            addToQueue(new Task(TaskType.TYPE_SPEECH, extras));
        } else {
            listen(speech, isFinished);
        }
    }

    public synchronized void dispatchDefaultOptionsTask() {
        if (!isLayoutPerformTask()) {
            displayOptions(mDefaultOptionList);
            unlock(true);
        }
    }

    public synchronized void onStageActionDone(boolean isInterrupted, int bosDuration) {
        unlock(!isInterrupted);
        if (bosDuration > 0) {
            startOptionProgress(bosDuration);
        }
    }

    public synchronized void onSceneExit(boolean proactive) {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "onSceneExit");
        }
        clearQueue();
        if (proactive) {
            displayOptions(mDefaultOptionList);
            unlock(false);
        }
    }

    public synchronized void release() {
        mTaskQueue.clear();
        mLayout.clear();
        mLayout = null;
    }

    private boolean isLayoutPerformTask() {
        return mLayout != null && (mLayout.isViewLocking() || !mTaskQueue.isEmpty());
    }

    private synchronized void clearQueue() {
        mTaskQueue.clear();
    }

    private synchronized Task pollFromQueue() {
        return mTaskQueue.poll();
    }

    private synchronized void addToQueue(Task task) {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "addToQueue");
        }
        mTaskQueue.offer(task);
    }


    private void wakeUp() {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.disableTouchWakeUpPanel();
                layout.wakeUp();
            }
        });
    }

    private void sleep() {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.sleep();
                layout.enableTouchWakeUpPanel();
            }
        });
    }

    private void onConnectionStatusChanged(final boolean connected) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.onConnectionStatusChanged(connected);
            }
        });
    }

    private void speak(final TtsText ttsText) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.speak(ttsText);
            }
        });
    }

    private void listen(final String text, final boolean isFinished) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.listen(text, isFinished);
            }
        });
    }

    private void displayOptions(final OptionList optionList) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.displayOptions(optionList, new GoLayout.IOnOptionSelectListener() {
                    @Override
                    public void onSelected(byte requestType, int index, Option option) {
                        if (mFeedback != null) {
                            clearQueue();
                            mFeedback.onOptionSelected(requestType, index, option);
                        }
                    }
                });
            }
        });
    }

    private void startOptionProgress(final int bosDuration) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.startOptionProgress(bosDuration);
            }
        });
    }

    private void displayContactOptions(final ContactOptionList contactOptionList) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.displayContactOptions(contactOptionList, new GoLayout.IOnOptionSelectListener() {
                    @Override
                    public void onSelected(byte requestType, int index, Option option) {
                        if (mFeedback != null) {
                            clearQueue();
                            mFeedback.onOptionSelected(requestType, index, option);
                        }
                    }
                });
            }
        });
    }

    private void displayUsrInfo(final String userAvatar, final String userName, final AppInfo appInfo) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.displayUsrInfo(userAvatar, userName, appInfo);
            }
        });
    }

    private void displayUsrMsg(final String usrAvatar, final String usrName, final String msgContent, final AppInfo appInfo) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.displayUsrMsg(usrAvatar, usrName, msgContent, appInfo);
            }
        });
    }

    private void displayMsgSent(final boolean success) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.displayMsgSent(success);
            }
        });
    }

    private void onStatusChanged(final GoLayout.ViewStatus status) {
        onStatusChanged(status, null);
    }

    private void onStatusChanged(final GoLayout.ViewStatus status, GoLayout.IGifStatusListener listener) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.onStatusChanged(status, listener);
    }

    private void unlock(final boolean withAlert) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.unlock();
                if (withAlert) {
                    MediaPlayerUtil.playAlert(layout.getContext(), R.raw.alert_dot, null);
                }
                onStatusChanged(GoLayout.ViewStatus.LISTEN_1);
            }
        });
    }

    private void playAlert(final int alertRes) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                MediaPlayerUtil.playAlert(layout.getContext(), alertRes, null);
            }
        });
    }

    public interface IUiManagerFeedback {
        void onOptionSelected(byte requestType, int index, Option option);

        void onLayoutModeChanged(GoLayout.DisplayMode mode);
    }
}