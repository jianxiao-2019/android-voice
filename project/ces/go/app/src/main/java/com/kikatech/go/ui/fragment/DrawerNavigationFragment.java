package com.kikatech.go.ui.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.model.SettingDestination;
import com.kikatech.go.ui.adapter.BaseAdapter;
import com.kikatech.go.util.PopupMenuUtil;
import com.kikatech.go.util.dialog.DialogKeys;
import com.kikatech.go.util.dialog.DialogUtil;
import com.kikatech.go.view.NoPredictiveAnimationManager;

import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/20.
 */

public class DrawerNavigationFragment extends Fragment {
    private static final String TAG = "DrawerNavigationFragment";

    public static DrawerNavigationFragment newInstance(IDrawerNavigationListener listener) {
        DrawerNavigationFragment fragment = new DrawerNavigationFragment();
        fragment.setListener(listener);
        return fragment;
    }


    private IDrawerNavigationListener mListener;

    private void setListener(IDrawerNavigationListener listener) {
        mListener = listener;
    }

    public interface IDrawerNavigationListener {
        void onBackClicked();
    }


    private RecyclerView mListView;
    private TextView mConfirmSubTv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.go_layout_drawer_navigation, null);
        mListView = (RecyclerView) mView.findViewById(R.id.drawer_navigation_list);
        mConfirmSubTv = (TextView) mView.findViewById(R.id.drawer_navigation_confirm_subtitle);

        mView.findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackClicked();
                }
            }
        });

        mView.findViewById(R.id.drawer_navigation_btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showDialogAddSettingDestination(getActivity(), new DialogUtil.IDialogListener() {
                    @Override
                    public void onApply(Bundle args) {
                        if (args == null) {
                            return;
                        }
                        String text = args.getString(DialogKeys.KEY_TEXT);
                        List<SettingDestination> list = UserSettings.getSettingDestinationList();
                        list.add(new SettingDestination(SettingDestination.TYPE_CUSTOMIZED, text));
                        UserSettings.saveSettingDestinationList(list);
                        setupListView();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        });
        mView.findViewById(R.id.drawer_navigation_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View mConfirmBtnMore = mView.findViewById(R.id.drawer_navigation_confirm_btn_more);
                PopupMenuUtil.showDrawerNavigationConfirmPopup(getActivity(), mConfirmBtnMore, new PopupMenuUtil.IPopupListener() {
                    @Override
                    public void onDismiss() {
                        updateConfirmSub();
                    }
                });
            }
        });
        setupListView();
        updateConfirmSub();
        return mView;
    }

    private void setupListView() {
        final Context mContext = getActivity();
        DestinationAdapter mAdapter = new DestinationAdapter(mContext, UserSettings.getSettingDestinationList());
        NoPredictiveAnimationManager mLayoutManager = new NoPredictiveAnimationManager(mContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setLayoutManager(mLayoutManager);
    }

    private void updateConfirmSub() {
        boolean toAsk = UserSettings.getSettingConfirmDestination();
        mConfirmSubTv.setText(getString(toAsk ? R.string.drawer_item_navigation_confirm_item_ask : R.string.drawer_item_navigation_confirm_item_skip));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private final class DestinationAdapter extends BaseAdapter<SettingDestination> {

        private DestinationAdapter(Context context, List<SettingDestination> list) {
            super(context, list);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DestinationHolder(LayoutInflater.from(mContext).inflate(R.layout.go_layout_drawer_navigation_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            if (holder == null) {
                return;
            }

            final DestinationHolder mDestinationHolder = (DestinationHolder) holder;
            final SettingDestination mDestination = mList.get(position);

            if (mDestination == null) {
                return;
            }

            mDestinationHolder.mItemIcon.setImageResource(mDestination.getTypeIconRes());

            String name = mDestination.getName();
            if (!TextUtils.isEmpty(name)) {
                mDestinationHolder.mItemName.setText(name);
            }

            final String address = mDestination.getAddress();
            if (!TextUtils.isEmpty(address)) {
                mDestinationHolder.mItemAddress.setText(address);
                mDestinationHolder.mItemBtnDelete.setVisibility(View.VISIBLE);
                mDestinationHolder.mItemBtnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDestination.canRemove()) {
                            mList.remove(mDestination);
                        } else {
                            mDestination.setAddress("");
                        }
                        saveSettings();
                    }
                });
            } else {
                mDestinationHolder.mItemAddress.setText("Tap to edit...");
                mDestinationHolder.mItemAddress.setTextColor(mContext.getResources().getColor(R.color.drawer_submenu_navigation_item_address_hint));
                mDestinationHolder.mItemBtnEdit.setVisibility(View.VISIBLE);
            }

            mDestinationHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtil.showDialogEditSettingDestination(mContext, address, new DialogUtil.IDialogListener() {
                        @Override
                        public void onApply(Bundle args) {
                            if (args == null) {
                                return;
                            }
                            String text = args.getString(DialogKeys.KEY_TEXT);
                            mDestination.setAddress(text);
                            saveSettings();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
            });
        }

        private void saveSettings() {
            UserSettings.saveSettingDestinationList(mList);
            notifyDataSetChanged();
        }

        @Override
        public void resetHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            DestinationHolder mDestinationHolder = (DestinationHolder) holder;
            Glide.clear(mDestinationHolder.mItemIcon);
            mDestinationHolder.mItemName.setText("");
            mDestinationHolder.mItemAddress.setText("");
            mDestinationHolder.mItemAddress.setTextColor(mContext.getResources().getColor(R.color.drawer_submenu_navigation_item_address));
            mDestinationHolder.mItemBtnEdit.setVisibility(View.GONE);
            mDestinationHolder.mItemBtnDelete.setVisibility(View.GONE);
            mDestinationHolder.mItemBtnDelete.setOnClickListener(null);
        }

        private class DestinationHolder extends RecyclerView.ViewHolder {
            ImageView mItemIcon;
            TextView mItemName;
            TextView mItemAddress;
            View mItemBtnEdit;
            View mItemBtnDelete;

            private DestinationHolder(View itemView) {
                super(itemView);
                mItemIcon = (ImageView) itemView.findViewById(R.id.go_layout_drawer_navigation_list_item_icon);
                mItemName = (TextView) itemView.findViewById(R.id.go_layout_drawer_navigation_list_item_name);
                mItemAddress = (TextView) itemView.findViewById(R.id.go_layout_drawer_navigation_list_item_address);
                mItemBtnEdit = itemView.findViewById(R.id.go_layout_drawer_navigation_list_item_btn_edit);
                mItemBtnDelete = itemView.findViewById(R.id.go_layout_drawer_navigation_list_item_btn_delete);
            }
        }
    }
}
