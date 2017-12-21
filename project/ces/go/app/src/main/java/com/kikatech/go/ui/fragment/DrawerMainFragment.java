package com.kikatech.go.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.ui.KikaDebugLogActivity;
import com.kikatech.go.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author SkeeterWang Created on 2017/12/19.
 */

public class DrawerMainFragment extends Fragment {

    public static DrawerMainFragment newInstance(IDrawerMainListener listener) {
        DrawerMainFragment fragment = new DrawerMainFragment();
        fragment.setListener(listener);
        return fragment;
    }

    private IDrawerMainListener mListener;

    private void setListener(IDrawerMainListener listener) {
        mListener = listener;
    }


    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event from {@link com.kikatech.go.services.DialogFlowForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceEvent(DFServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE:
                String text = event.getExtras().getString(DFServiceEvent.PARAM_TEXT);
                updateMicStatus(text);
                break;
        }
    }

    private void updateMicStatus(String text) {
        if (mMicStatusView != null) {
            if (DialogFlowForegroundService.VOICE_SOURCE_USB.equals(text)) {
                mMicStatusView.setText(getString(R.string.drawer_item_mic_status_connected));
                mMicStatusView.setTextColor(getResources().getColor(R.color.drawer_subtitle_text));
            } else {
                mMicStatusView.setText(getString(R.string.drawer_item_mic_status_disconnected));
                mMicStatusView.setTextColor(getResources().getColor(R.color.drawer_subtitle_text_disable));
            }
        }
    }


    private TextView mMicStatusView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceivers();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.go_layout_drawer_main, null);

        mMicStatusView = (TextView) mView.findViewById(R.id.drawer_item_mic_status_text);

        mView.findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCloseDrawer();
                }
            }
        });
        mView.findViewById(R.id.drawer_item_navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemNavigationClick();
                }
            }
        });
        mView.findViewById(R.id.drawer_item_im).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemImClicked();
                }
            }
        });
        mView.findViewById(R.id.drawer_item_mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mView.findViewById(R.id.drawer_item_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemExitClicked();
                }
            }
        });

        if (LogUtil.DEBUG) {
            View mItemDebug = mView.findViewById(R.id.drawer_item_debug);
            View mItemDebugUnderline = mView.findViewById(R.id.drawer_item_debug_underline);
            mItemDebug.setVisibility(View.VISIBLE);
            mItemDebugUnderline.setVisibility(View.VISIBLE);
            mItemDebug.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), KikaDebugLogActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DialogFlowForegroundService.processPingVoiceSource();
    }

    @Override
    public void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }


    private void registerReceivers() {
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceivers() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }


    public interface IDrawerMainListener {
        void onCloseDrawer();

        void onItemNavigationClick();

        void onItemImClicked();

        void onItemExitClicked();
    }
}
