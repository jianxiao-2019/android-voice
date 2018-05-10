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

import com.kikatech.go.BuildConfig;
import com.kikatech.go.R;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.services.presenter.VoiceSourceHelper;
import com.kikatech.go.ui.activity.KikaDebugLogActivity;
import com.kikatech.go.util.FlavorUtil;
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
                String source = event.getExtras().getString(DFServiceEvent.PARAM_AUDIO_SOURCE);
                updateMicStatus(source);
                break;
            case DFServiceEvent.ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED:
                boolean isUsbDataCorrect = event.getExtras().getBoolean(DFServiceEvent.PARAM_IS_AUDIO_DATA_CORRECT);
                updateUsbDataStatus(isUsbDataCorrect);
                break;

        }
    }

    private void updateMicStatus(String source) {
        if (mMicStatusView != null) {
            if (VoiceSourceHelper.VOICE_SOURCE_USB.equals(source)) {
                mMicStatusView.setText(getString(R.string.drawer_item_mic_status_connected));
                mMicStatusView.setTextColor(getResources().getColor(R.color.drawer_subtitle_text));
            } else {
                mMicStatusView.setText(getString(R.string.drawer_item_mic_status_disconnected));
                mMicStatusView.setTextColor(getResources().getColor(R.color.drawer_subtitle_text_disable));
                updateUsbDataStatus(true);
            }
        }
    }

    private void updateUsbDataStatus(boolean isDataCorrect) {
        if (mUsbDataStatusErrorView != null) {
            mUsbDataStatusErrorView.setVisibility(isDataCorrect ? View.GONE : View.VISIBLE);
        }
    }


    private TextView mMicStatusView;
    private View mUsbDataStatusErrorView;

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
        mUsbDataStatusErrorView = mView.findViewById(R.id.drawer_item_usb_data_status_error_text);

        ((TextView) mView.findViewById(R.id.drawer_item_exit_app_version_text)).setText(String.format("V. %s", BuildConfig.VERSION_NAME));

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
        mView.findViewById(R.id.drawer_item_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemMusicClicked();
                }
            }
        });
        mView.findViewById(R.id.drawer_item_mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemMicClicked();
                }
            }
        });
        mView.findViewById(R.id.drawer_item_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemTipClicked();
                }
            }
        });
        mView.findViewById(R.id.drawer_item_advanced).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemAdvancedClicked();
                }
            }
        });
        mView.findViewById(R.id.drawer_item_faq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemFAQsClicked();
                }
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

        if (LogUtil.DEBUG && !FlavorUtil.isFlavorManufacturer()) {
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

        void onItemMusicClicked();

        void onItemMicClicked();

        void onItemTipClicked();

        void onItemAdvancedClicked();

        void onItemFAQsClicked();

        void onItemExitClicked();
    }
}
