package com.kikatech.go.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.apiai.ApiAiHelper;
import com.kikatech.go.intention.Intention;
import com.kikatech.go.intention.IntentionManager;
import com.kikatech.go.intention.handler.IntentionHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jasonli Created on 2017/10/27.
 */

public class KikaApiAiActivity extends BaseActivity {

    private TextView mSpeechUser;
    private TextView mSpeechBot;

    private TextView mActionName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = KikaApiAiActivity.this;

        setContentView(R.layout.activity_kika_apiai);

        mSpeechUser = (TextView) findViewById(R.id.text_user_speech);
        mSpeechBot = (TextView) findViewById(R.id.text_bot_speech);
        mActionName = (TextView) findViewById(R.id.text_action_name);

        final EditText editUserContacts = (EditText) findViewById(R.id.edit_user_contact);

        final Button speechButton = (Button) findViewById(R.id.button_send_speech);

        final EditText editSpeech = (EditText) findViewById(R.id.edit_user_speech);
        editSpeech.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    speechButton.performClick();
                    return true;
                }
                return false;
            }
        });
        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speech = editSpeech.getText().toString();
                if (TextUtils.isEmpty(speech)) {
                    return;
                }

                String userContacts = editUserContacts.getText().toString();
                if (!TextUtils.isEmpty(userContacts)) {
                    String[] userContactsArray = userContacts.split(",");
                    List<String> contactList = Arrays.asList(userContactsArray);

                    Map<String, List<String>> contactEntity = new HashMap<>();
                    contactEntity.put("contact", contactList);
                    ApiAiHelper.getInstance(context).queryIntention(null, speech, contactEntity);
                } else {
                    ApiAiHelper.getInstance(context).queryIntention(speech);
                }

                mSpeechUser.setText(speech);
                mSpeechBot.setText("");
                mActionName.setText("");
                editSpeech.setText("");
            }
        });

        findViewById(R.id.button_reset_context).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiAiHelper.getInstance(context).resetContext();
                IntentionManager.getInstance().resetIntention();
                mSpeechUser.setText("");
                mSpeechBot.setText("");
                mActionName.setText("");
                editSpeech.setText("");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentionManager.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IntentionManager.getInstance().unregister(this);
    }

    public void onHandleCallback(final IntentionHandler handler, final Intention intention) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String response = handler.getResponse();
                mSpeechBot.setText(response);

                String action = intention.getAction();
                if (!TextUtils.isEmpty(action)) {
                    mActionName.setText(action);
                    mActionName.setTextColor(Color.GRAY);
                } else {
                    mActionName.setText("No match intent action!");
                    mActionName.setTextColor(Color.RED);
                }
            }
        });
    }
}
