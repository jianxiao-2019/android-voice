package com.kikatech.go.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.kikatech.go.BuildConfig;
import com.kikatech.go.R;
import com.kikatech.go.databinding.GoLayoutDrawerMainBinding;
import com.kikatech.go.dialogflow.UserSettings;
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
    private static final String TAG = "DrawerMainFragment";

    public static DrawerMainFragment newInstance(IDrawerMainListener listener) {
        DrawerMainFragment fragment = new DrawerMainFragment();
        fragment.setListener(listener);
        return fragment;
    }

    private IDrawerMainListener mListener;

    private void setListener(IDrawerMainListener listener) {
        mListener = listener;
    }


    private GoLayoutDrawerMainBinding mBinding;

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
        if (mBinding.drawerItemMicStatusText != null) {
            if (VoiceSourceHelper.VOICE_SOURCE_USB.equals(source)) {
                mBinding.drawerItemMicStatusText.setText(getString(R.string.drawer_item_mic_status_connected));
                mBinding.drawerItemMicStatusText.setTextColor(getResources().getColor(R.color.drawer_subtitle_text));
            } else {
                mBinding.drawerItemMicStatusText.setText(getString(R.string.drawer_item_mic_status_disconnected));
                mBinding.drawerItemMicStatusText.setTextColor(getResources().getColor(R.color.drawer_subtitle_text_disable));
                updateUsbDataStatus(true);
            }
        }
    }

    private void updateUsbDataStatus(boolean isDataCorrect) {
        if (mBinding.drawerItemUsbDataStatusErrorText != null) {
            mBinding.drawerItemUsbDataStatusErrorText.setVisibility(isDataCorrect ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceivers();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.go_layout_drawer_main, null);

        mBinding = DataBindingUtil.bind(mView);

        mBinding.drawerItemExitAppVersionText.setText(String.format("V. %s", BuildConfig.VERSION_NAME));

        mBinding.drawerTitleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCloseDrawer();
                }
            }
        });
        initItemVolume();
        mBinding.itemVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar == null) {
                    return;
                }
                float MAX_VALUE = seekBar.getMax();
                float value = seekBar.getProgress();
                float newVolume = value / MAX_VALUE;
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, String.format("MAX_VALUE: %s, value: %s, newVolume: %s", MAX_VALUE, value, newVolume));
                }
                if (newVolume >= 0 && newVolume <= 1) {
                    UserSettings.saveSettingVolume(newVolume);
                    DialogFlowForegroundService.processSetTtsVolume(newVolume);
                }
            }
        });
        mBinding.drawerItemNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemNavigationClick();
                }
            }
        });
        mBinding.drawerItemIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemImClicked();
                }
            }
        });
        mBinding.drawerItemMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemMusicClicked();
                }
            }
        });
        mBinding.drawerItemMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemMicClicked();
                }
            }
        });
        mBinding.drawerItemTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemTipClicked();
                }
            }
        });
        mBinding.drawerItemAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemAdvancedClicked();
                }
            }
        });
        mBinding.drawerItemFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemFAQsClicked();
                }
            }
        });
        mBinding.drawerItemExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemExitClicked();
                }
            }
        });

        if (LogUtil.DEBUG && !FlavorUtil.isFlavorManufacturer()) {
            mBinding.drawerItemDebug.setVisibility(View.VISIBLE);
            mBinding.drawerItemDebugUnderline.setVisibility(View.VISIBLE);
            mBinding.drawerItemDebug.setOnClickListener(new View.OnClickListener() {
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


    private void initItemVolume() {
        float MAX_VALUE = mBinding.itemVolumeSeekBar.getMax();
        float volume = UserSettings.getSettingVolume();
        int value = (int) (volume * MAX_VALUE);
        mBinding.itemVolumeSeekBar.setProgress(value);
        mBinding.itemVolumeSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow Drawer to intercept touch events.
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Drawer to intercept touch events.
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle seekbar touch events.
                view.onTouchEvent(event);
                return true;
            }
        });
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


    public synchronized void updateVolume(float volume) {
        float MAX_VALUE = mBinding.itemVolumeSeekBar.getMax();
        int value = (int) (volume * MAX_VALUE);
        mBinding.itemVolumeSeekBar.setProgress(value);
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
