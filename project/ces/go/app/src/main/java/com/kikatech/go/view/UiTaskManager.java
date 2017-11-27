package com.kikatech.go.view;

import android.os.Bundle;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.SceneUtil;
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
                        stage.doAction();
                        break;
                    case TaskType.TYPE_SPEECH:
                        String speech = (String) obj;
                        listen(speech, true);
                        break;
                }
            }
        });
    }


    public synchronized void dispatchTtsTask(String text, Bundle extras) {
        OptionList optionList = null;
        if (extras != null && extras.containsKey(SceneUtil.EXTRA_OPTIONS_LIST)) {
            optionList = extras.getParcelable(SceneUtil.EXTRA_OPTIONS_LIST);
        }
        if (optionList != null && !optionList.isEmpty()) {
            displayOptions(optionList);
        } else {
            String uiText = extras != null ? extras.getString(SceneUtil.EXTRA_UI_TEXT, text) : text;
            speak(uiText);
        }
    }

    public synchronized void dispatchStageTask(SceneStage sceneStage) {
        if (isLayoutPerformTask()) {
            addToQueue(new Task(TaskType.TYPE_SCENE_STAGE, sceneStage));
        } else {
            sceneStage.doAction();
        }
    }

    public synchronized void dispatchSpeechTask(String speech) {
        dispatchSpeechTask(speech, true);
    }

    public synchronized void dispatchSpeechTask(String speech, boolean isFinished) {
        if (isLayoutPerformTask() && !GoLayout.ViewStatus.LISTEN.equals(mLayout.getCurrentStatus()) && isFinished) {
            addToQueue(new Task(TaskType.TYPE_SPEECH, speech));
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

    public synchronized void onStageActionDone(boolean isInterrupted) {
        unlock(!isInterrupted);
    }

    public synchronized void onSceneExit() {
        clearQueue();
        displayOptions(mDefaultOptionList);
        unlock(false);
    }

    public synchronized void release() {
        mTaskQueue.clear();
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


    private void sleep() {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.sleep();
            }
        });
    }

    private void speak(final String text) {
        final GoLayout layout = mLayout;
        if (layout == null) {
            return;
        }
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.speak(text);
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
                            mFeedback.onOptionSelected(requestType, index, option);
                        }
                    }
                });
            }
        });
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
            }
        });
    }

    public interface IUiManagerFeedback {
        void onOptionSelected(byte requestType, int index, Option option);
    }


    public enum DebugLogType {
        ASR_LISTENING("ASR listening"),
        ASR_STOP("ASR result"),
        API_AI_START("Api.ai start query"),
        API_AI_STOP("Api.ai stop query"),
        API_AI_ERROR("Api.ai query error");

        private String log;

        DebugLogType(String log) {
            this.log = log;
        }
    }

    public synchronized void writeDebugLog(final DebugLogType logType) {
        if (mLayout == null) {
            return;
        }
        final GoLayout layout = mLayout;
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.writeDebugInfo(logType.log);
            }
        });
    }
}
