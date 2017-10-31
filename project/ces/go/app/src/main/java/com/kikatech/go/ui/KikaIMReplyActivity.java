package com.kikatech.go.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.message.Message;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.message.im.IMManager;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.DeviceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jasonli Created on 2017/10/26.
 */

public class KikaIMReplyActivity extends BaseActivity {

    private TextView mPermissionStatus;
    private ArrayAdapter mAdapter;
    private List<BaseIMObject> mAllIMObjects;

    private AppInfo[] mSupportIM = new AppInfo[]{
            AppInfo.TELEGRAM,
            AppInfo.WECHAT,
            AppInfo.MESSENGER,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_im_reply);

        mPermissionStatus = (TextView) findViewById(R.id.text_reply_im_hint);
        TextView supportText = (TextView) findViewById(R.id.text_reply_im_support);
        String supportIMString = "Support IM: ";
        for (AppInfo appInfo : mSupportIM) {
            supportIMString += appInfo.getAppName() + ", ";
        }
        supportText.setText(supportIMString);

        Button notiPermissionButton = (Button) findViewById(R.id.button_check_notification_permission);
        notiPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
                    startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                } catch (Exception ignore) {
                }
            }
        });

        ListView listView = (ListView) this.findViewById(R.id.listview1);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mOnIMItemClickListener);
    }

    private AdapterView.OnItemClickListener mOnIMItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mAllIMObjects != null && DeviceUtil.overKitKat()) {

                final BaseIMObject imObject = mAllIMObjects.get(position);

                final Dialog dialog = new Dialog(KikaIMReplyActivity.this);
                dialog.setContentView(R.layout.dialog_message_edit);

                Window window = dialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);

                TextView title = (TextView) dialog.findViewById(R.id.dialog_title);
                title.setText(imObject.getUserName() + "  (" + imObject.getAppInfo().getAppName() + ")");

                final EditText messageEditText = (EditText) dialog.findViewById(R.id.dialog_edit_text);
                Button send = (Button) dialog.findViewById(R.id.dialog_button);
                send.setText("REPLY");
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String messageContent = messageEditText.getText().toString();
                        if (TextUtils.isEmpty(messageContent)) {
                            return;
                        }

                        boolean sent = IMManager.getInstance().sendMessage(KikaIMReplyActivity.this, imObject, messageContent);
                        if (!sent) {
                            showToast("Message send FAILED!");
                        } else {
                            showToast("Sent.");
                        }

                        dialog.dismiss();
                        updateData();
                    }
                });
                dialog.show();
            }
        }
    };

    private void updateData() {
        if (DeviceUtil.overKitKat()) {
            mAllIMObjects = IMManager.getInstance().getAllSortedIMObjects();

            List<String> imItemTitle = new ArrayList<>();
            for (BaseIMObject imObject : mAllIMObjects) {
                Message latestMessage = imObject.getLatestMessage();
                String itemText = "(" + imObject.getAppName() + ") "
                        + "[" + imObject.getTitle() + "]"
                        + (latestMessage.getSender() == null ? " (送出) " : " (收到) ")
                        + latestMessage.getContent();

                imItemTitle.add(itemText);
            }

            mAdapter.clear();
            mAdapter.addAll(imItemTitle.toArray());

            mAdapter.notifyDataSetChanged();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (IMManager.ACTION_IM_MESSAGE_UPDATED.equals(intent.getAction())) {
                updateData();

                BaseIMObject imObjectData = intent.getParcelableExtra(IMManager.DATA_IM_OBJECT);
                final BaseIMObject imObject = IMManager.getInstance().getReferenceIfExist(imObjectData);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] randomReply = new String[] {
                                "[自动回复] 北京天气晴",
                                "[自动回复] 我在开车呢",
                                "[自动回复] 这里是Kika总部",
                                "[自动回复] 我在慕田峪长城",
                                "[自动回复] 我觉得可以",
                                "[自动回复] 你有freestyle吗？",
                                "[自动回复] 在路上了",
                                "[自动回复] ⁽⁽ଘ( ˙꒳˙ )ଓ⁾⁾",
                                "[自动回复] (´・ω・`)",
                                "[自动回复] ヾ(´︶`*)ﾉ♬",
                                "[自动回复] 烤鸭好吃 (๑´ڡ`๑)",
                                "[自动回复] 是的老大马上办 ヽ(￣■￣)ゝ",
                        };

                        String reply = randomReply[new Random().nextInt(randomReply.length)];
                        boolean sent = IMManager.getInstance().sendMessage(context, imObject, reply);
                        if (!sent) {
                            showToast("Message send FAILED!");
                        } else {
                            showToast("Sent.");
                        }
                        updateData();
                    }
                }, 3500);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        updateData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(IMManager.ACTION_IM_MESSAGE_UPDATED);
        registerReceiver(mBroadcastReceiver, filter);

        boolean isPermissionNLOn = NotificationListenerUtil.isPermissionNLEnabled(this);
        mPermissionStatus.setText(isPermissionNLOn ? "Permission ON" : "No Permission!");
        mPermissionStatus.setTextColor(isPermissionNLOn ? Color.GREEN : Color.RED);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mBroadcastReceiver);
    }
}
