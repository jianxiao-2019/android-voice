package com.kikatech.go;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.go.util.log.Logger;
import com.kikatech.go.engine.interfaces.IVoiceManager;
import com.kikatech.go.engine.interfaces.IVoiceView;
import com.kikatech.go.engine.websocket.VoiceManagerWs;

import java.util.Locale;

/**
 * Created by ryanlin on 18/09/2017.
 */

public class KikaVoiceService extends InputMethodService implements IVoiceView {

    public static final String PREF_KEY_DEBUG = "pref_debug";

    private IVoiceManager mVoiceManager;

    private TextView mHintText;
    private AppCompatImageView mButtonVoiceKey;
    private View mDebugInfoLayout;

    private TextView mServerSessionId;
    private TextView mClientRecordId;
    private TextView mServerResponseTime;
    private TextView mCommandToTtsTime;
    private TextView mTtsPrepareTime;

    private Boolean mIsStartWithSpace = null;

    private EditorInfo mEditorInfo;

    private Context mConfigurationContext;

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO : the creator should be abstract.
        mVoiceManager = VoiceManagerWs.getInstance();
        mVoiceManager.setVoiceView(this);

        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(new Locale("zh"));
        mConfigurationContext = createConfigurationContext(config);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mVoiceManager != null) {
            mVoiceManager.setVoiceView(null);
            mVoiceManager.stopListening();
        }
        mVoiceManager = null;
    }

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.main_keyboard_view, null);

        mHintText = (TextView) view.findViewById(R.id.voice_hint);
        mButtonVoiceKey = (AppCompatImageView) view.findViewById(R.id.voice_key);
        mButtonVoiceKey.setOnClickListener(mOnClickListener);
        mHintText.setText("");

        mDebugInfoLayout = view.findViewById(R.id.debug_info_layout);

        mServerSessionId = (TextView) mDebugInfoLayout.findViewById(R.id.debug_session_id);
        mClientRecordId = (TextView) mDebugInfoLayout.findViewById(R.id.debug_client_record_id);
        mServerResponseTime = (TextView) mDebugInfoLayout.findViewById(R.id.debug_server_response_time);
        mCommandToTtsTime = (TextView) mDebugInfoLayout.findViewById(R.id.debug_command_to_tts_time);
        mTtsPrepareTime = (TextView) mDebugInfoLayout.findViewById(R.id.debug_tts_prepare_time);

        return view;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            startListening();
        }
    };

    private void startListening() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        Logger.v("startListening permissionCheck = " + permissionCheck);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mVoiceManager.startListening();
        } else {
            Toast.makeText(this,
                    "You should accept the permission first in App.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        mEditorInfo = attribute;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        Logger.v("onStartInputView restarting = " + restarting);
        if (!restarting) {
            startListening();
        }

        if (mDebugInfoLayout != null) {
            boolean isDebug = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                    PREF_KEY_DEBUG, false);
            mDebugInfoLayout.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        Logger.v("onFinishInputView");
        if (mVoiceManager != null) {
            mVoiceManager.stopListening();
        }
    }

    @Override
    public void onStartListening() {
        if (mButtonVoiceKey != null) {
            mButtonVoiceKey.setBackgroundResource(R.drawable.mic_down);
            ((AnimationDrawable) mButtonVoiceKey.getBackground()).start();
            mButtonVoiceKey.setClickable(false);
        }
        if (mHintText != null) {
            mHintText.setText("");
        }
    }

    @Override
    public void onStopListening() {
        if (mButtonVoiceKey != null) {
            mButtonVoiceKey.setBackgroundResource(R.drawable.ic_mic_up);
            mButtonVoiceKey.setClickable(true);
        }
        if (mHintText != null) {
            mHintText.setText(R.string.tap_to_start);
        }
    }

    @Override
    public void onUpdateRecognizedResult(CharSequence result, int resultType) {
        if (result == null) {
            return;
        }

        InputConnection inputConnection = getCurrentInputConnection();
        if (mIsStartWithSpace == null) {
            mIsStartWithSpace = isCharSpaceBeforeCursor(inputConnection);
        }

        inputConnection.beginBatchEdit();
        if (resultType == RESULT_INTERMEDIATE) {
            String resultStr = (mIsStartWithSpace ? "" : " ") + result.toString();
            inputConnection.setComposingText(resultStr, 1);
        } else if (resultType == RESULT_FINAL) {
            String resultStr = (mIsStartWithSpace ? "" : " ") + result.toString();
            inputConnection.commitText(resultStr, 1);
            mIsStartWithSpace = null;
        } else if (resultType == RESULT_REPLACE_ALL) {
            // TODO : Find a better way to delete all.
            ExtractedText content = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
            inputConnection.deleteSurroundingText(content.text.length(), content.text.length());

            inputConnection.commitText(result, 1);
            mIsStartWithSpace = null;
        }
        inputConnection.endBatchEdit();
    }

    private boolean isCharSpaceBeforeCursor(InputConnection connection) {
        if (connection == null) {
            return false;
        }

        CharSequence textBeforeCursor = connection.getTextBeforeCursor(1, 0);
        return TextUtils.isEmpty(textBeforeCursor)
                || " ".equals(textBeforeCursor)
                || "\n".equals(textBeforeCursor);
    }

    @Override
    public CharSequence getTextOnEditor() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null) {
            ExtractedText exText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
            if (exText != null) {
                return exText.text;
            }
        }
        return null;
    }

    @Override
    public String getCurrentEditorPackageName() {
        if (mEditorInfo != null) {
            return mEditorInfo.packageName;
        }
        return null;
    }

    @Override
    public Context getContext() {
        return mConfigurationContext;
    }

    @Override
    public void updateHintStr(int hintStrResId) {
        if (mHintText != null) {
            if (hintStrResId == 0 || hintStrResId == -1) {
                mHintText.setText("");
            } else {
                String text = mConfigurationContext.getString(hintStrResId);
                mHintText.setText(text);
            }
        }
    }

    @Override
    public void sendKeyEvent(int keyCode) {
        sendDownUpKeyEvents(keyCode);
    }

    // Debug TextViews
    public void setClientRecordId(final String timeStr) {
        if (mClientRecordId != null) {
            mClientRecordId.post(new Runnable() {
                @Override
                public void run() {
                    mClientRecordId.setText(
                            String.format(getString(R.string.debug_client_record_id), timeStr));
                }
            });
        }
    }

    public void setServerSessionId(final String sessionId) {
        if (mServerSessionId != null) {
            mServerSessionId.post(new Runnable() {
                @Override
                public void run() {
                    mServerSessionId.setText(
                            String.format(getString(R.string.debug_session_id), sessionId));
                }
            });
        }
    }
}
