package com.kikatech.go.view;

import android.os.Bundle;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.ui.MediaPlayerUtil;
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
                    LogUtil.log(TAG, "onLockRelease");
                }
                Task task = pollFromQueue();
                if (task == null) {
                    return;
                }
                Object obj = task.content;
                if (obj == null) {
                    return;
                }
                byte type = task.type;
                switch (type) {
                    case TaskType.TYPE_SCENE_STAGE:
                        SceneStage stage = (SceneStage) obj;
                        stage.action();
                        break;
                    case TaskType.TYPE_SPEECH:
                        String speech = (String) obj;
                        listen(speech);
                        break;

                }
            }
        });
    }


    public synchronized void dispatchTtsTask(String text, Bundle extras) {
        OptionList optionList = null;
        if (extras != null && extras.containsKey(BaseSceneStage.EXTRA_OPTIONS_LIST)) {
            optionList = extras.getParcelable(BaseSceneStage.EXTRA_OPTIONS_LIST);
        }
        if (optionList != null && !optionList.isEmpty()) {
            displayOptions(optionList);
        } else {
            speak(text);
        }
    }

    public synchronized void dispatchStageTask(SceneStage sceneStage) {
        if (isLayoutPerformTask()) {
            addToQueue(new Task(TaskType.TYPE_SCENE_STAGE, sceneStage));
        } else {
            sceneStage.action();
        }
    }

    public synchronized void dispatchSpeechTask(String speech) {
        if (isLayoutPerformTask()) {
            addToQueue(new Task(TaskType.TYPE_SPEECH, speech));
        } else {
            listen(speech);
        }
    }

    public synchronized void dispatchDefaultOptionsTask() {
        if (!isLayoutPerformTask()) {
            displayOptions(mDefaultOptionList);
            unlock();
        }
    }

    public synchronized void onStageActionDone() {
        unlock();
    }


    private boolean isLayoutPerformTask() {
        return mLayout.isViewLocking() || !mTaskQueue.isEmpty();
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


    private void speak(final String text) {
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                mLayout.speak(text);
            }
        });
    }

    private void listen(final String text) {
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                mLayout.listen(text);
            }
        });
    }

    private void displayOptions(final OptionList optionList) {
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                mLayout.displayOptions(optionList, new GoLayout.IOnOptionSelectListener() {
                    @Override
                    public void onSelected(byte requestType, int index, Option option) {
                        if (mFeedback != null) {
                            mFeedback.onOptionSelected(requestType, index, option);
                        }
                    }
                });
            }
        });
    }

    private void unlock() {
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                mLayout.unlock();
            }
        });
//        TODO: alert
//        if(withAlert) {
//            MediaPlayerUtil.playAlert(mLayout.getContext(), R.raw.alert_dot, null);
//        }
    }

    public interface IUiManagerFeedback {
        void onOptionSelected(byte requestType, int index, Option option);
    }
}
