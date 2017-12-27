package com.kikatech.go.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.util.PopupMenuUtil;

/**
 * @author SkeeterWang Created on 2017/12/27.
 */

public class DrawerAdvancedFragment extends Fragment {
    private static final String TAG = "DrawerAdvancedFragment";

    public static DrawerAdvancedFragment newInstance(IDrawerAdvancedListener listener) {
        DrawerAdvancedFragment fragment = new DrawerAdvancedFragment();
        fragment.setListener(listener);
        return fragment;
    }


    private IDrawerAdvancedListener mListener;

    private void setListener(IDrawerAdvancedListener listener) {
        mListener = listener;
    }

    public interface IDrawerAdvancedListener {
        void onBackClicked();
    }


    private TextView mConfirmationSubTv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.go_layout_drawer_advanced, null);

        mConfirmationSubTv = (TextView) mView.findViewById(R.id.drawer_advanced_confirmation_subtitle);

        mView.findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackClicked();
                }
            }
        });
        mView.findViewById(R.id.drawer_advanced_confirmation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View mConfirmationBtnMore = mView.findViewById(R.id.drawer_advanced_confirmation_btn_more);
                PopupMenuUtil.showDrawerAdvancedConfirmationPopup(getActivity(), mConfirmationBtnMore, new PopupMenuUtil.IPopupListener() {
                    @Override
                    public void onDismiss() {
                        updateConfirmationSub();
                    }
                });
            }
        });

        updateConfirmationSub();

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private void updateConfirmationSub() {
        boolean isAuto = UserSettings.getSettingConfirmCounter();
        mConfirmationSubTv.setText(getString(isAuto ? R.string.drawer_item_advanced_confirmation_item_auto : R.string.drawer_item_advanced_confirmation_item_manually));
    }
}
