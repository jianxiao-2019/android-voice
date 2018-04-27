package com.kikatech.go.ui.fragment;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.IMUtil;
import com.kikatech.go.util.ResolutionUtil;
import com.kikatech.go.ui.adapter.BaseAdapter;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.view.NoPredictiveAnimationManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/19.
 */

public class DrawerImFragment extends Fragment {
    private static final String TAG = "DrawerImFragment";

    public static DrawerImFragment newInstance(IDrawerImListener listener) {
        DrawerImFragment fragment = new DrawerImFragment();
        fragment.setListener(listener);
        return fragment;
    }

    private IDrawerImListener mListener;

    private void setListener(IDrawerImListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context mContext = getActivity();
        View mView = inflater.inflate(R.layout.go_layout_drawer_im, null);
        mView.findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackClicked();
                }
            }
        });
        RecyclerView mAppListView = (RecyclerView) mView.findViewById(R.id.drawer_im_app_list);
        AppAdapter mAdapter = new AppAdapter(mContext, Arrays.asList(IMUtil.SUPPORTED_IM));
        NoPredictiveAnimationManager mLayoutManager = new NoPredictiveAnimationManager(mContext);
        mAppListView.setAdapter(mAdapter);
        mAppListView.setLayoutManager(mLayoutManager);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public interface IDrawerImListener {
        void onBackClicked();
    }

    private final class AppAdapter extends BaseAdapter {

        private Context mContext;
        private List<AppInfo> mList;

        private AppAdapter(Context context, List<AppInfo> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AppHolder(LayoutInflater.from(mContext).inflate(R.layout.go_layout_drawer_im_app_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            if (holder == null) {
                return;
            }

            final AppHolder mAppHolder = (AppHolder) holder;
            final AppInfo mAppInfo = mList.get(position);

            if (mAppInfo == null) {
                return;
            }

            Glide.with(mContext.getApplicationContext())
                    .load(mAppInfo.getAppIcon())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontTransform()
                    .into(mAppHolder.mItemIcon);

            if (IMUtil.isIMAppSupported(mContext, mAppInfo.getPackageName())) {
                switch (UserSettings.getReplyMsgSetting(mAppInfo.getPackageName())) {
                    case UserSettings.SETTING_REPLY_MSG_READ:
                        mAppHolder.mItemText.setText(mContext.getString(R.string.drawer_item_im_popup_menu_item_yes));
                        break;
                    case UserSettings.SETTING_REPLY_MSG_IGNORE:
                        mAppHolder.mItemText.setText(mContext.getString(R.string.drawer_item_im_popup_menu_item_no));
                        break;
                    case UserSettings.SETTING_REPLY_MSG_ASK_USER:
                        mAppHolder.mItemText.setText(mContext.getString(R.string.drawer_item_im_popup_menu_item_ask));
                        break;
                }

                mAppHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopUpMenu(mAppHolder.mItemBtnMore, mAppInfo.getPackageName());
                    }
                });
            } else {
                mAppHolder.mItemText.setText("Not installed");
                mAppHolder.mItemBtnMore.setVisibility(View.GONE);
            }
        }

        private void showPopUpMenu(View anchorView, final String packageName) {
            final PopupWindow popupWindow = new PopupWindow(mContext);

            View menuView = LayoutInflater.from(mContext).inflate(R.layout.go_layout_drawer_im_popup_menu, null);
            menuView.findViewById(R.id.item_yes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserSettings.saveReplyMsgSetting(packageName, UserSettings.SETTING_REPLY_MSG_READ);
                    popupWindow.dismiss();
                    notifyDataSetChanged();
                }
            });
            menuView.findViewById(R.id.item_no).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserSettings.saveReplyMsgSetting(packageName, UserSettings.SETTING_REPLY_MSG_IGNORE);
                    popupWindow.dismiss();
                    notifyDataSetChanged();
                }
            });
            menuView.findViewById(R.id.item_ask).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserSettings.saveReplyMsgSetting(packageName, UserSettings.SETTING_REPLY_MSG_ASK_USER);
                    popupWindow.dismiss();
                    notifyDataSetChanged();
                }
            });

            popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setContentView(menuView);

            int[] location = new int[2];
            anchorView.getLocationOnScreen(location);

            menuView.measure(0, 0);

            popupWindow.showAsDropDown(anchorView, 0 - menuView.getMeasuredWidth(), ResolutionUtil.dp2px(mContext, 10), Gravity.RIGHT);
        }

        @Override
        public void resetHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AppHolder mAppHolder = (AppHolder) holder;
            mAppHolder.mItemText.setText("");
            Glide.clear(mAppHolder.mItemIcon);
            mAppHolder.mItemBtnMore.setVisibility(View.VISIBLE);
            mAppHolder.itemView.setOnClickListener(null);
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }

        private class AppHolder extends RecyclerView.ViewHolder {
            ImageView mItemIcon;
            TextView mItemText;
            View mItemBtnMore;

            private AppHolder(View itemView) {
                super(itemView);
                mItemIcon = (ImageView) itemView.findViewById(R.id.go_layout_drawer_im_app_list_item_icon);
                mItemText = (TextView) itemView.findViewById(R.id.go_layout_drawer_im_app_list_item_text);
                mItemBtnMore = itemView.findViewById(R.id.go_layout_drawer_im_app_list_item_btn_more);
            }
        }
    }
}
