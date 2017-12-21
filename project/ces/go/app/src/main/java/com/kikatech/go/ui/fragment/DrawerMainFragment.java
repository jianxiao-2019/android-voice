package com.kikatech.go.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kikatech.go.R;
import com.kikatech.go.ui.KikaDebugLogActivity;
import com.kikatech.go.util.LogUtil;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.go_layout_drawer_main, null);
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
    }

    public interface IDrawerMainListener {
        void onCloseDrawer();

        void onItemNavigationClick();

        void onItemImClicked();

        void onItemExitClicked();
    }
}
