package com.kikatech.go.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.SettingDestination;
import com.kikatech.go.ui.adapter.BaseAdapter;
import com.kikatech.go.view.NoPredictiveAnimationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/28.
 */

public class DrawerTipFragment extends Fragment {
    private static final String TAG = "DrawerTipFragment";

    public static DrawerTipFragment newInstance(IDrawerTipListener listener) {
        DrawerTipFragment fragment = new DrawerTipFragment();
        fragment.setListener(listener);
        return fragment;
    }


    private IDrawerTipListener mListener;

    private void setListener(IDrawerTipListener listener) {
        mListener = listener;
    }

    public interface IDrawerTipListener {
        void onBackClicked();
    }


    private RecyclerView mListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.go_layout_drawer_tip, null);
        mListView = (RecyclerView) mView.findViewById(R.id.drawer_tip_list);
        mView.findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackClicked();
                }
            }
        });
        setupListView();
        return mView;
    }

    private void setupListView() {
        final Context mContext = getActivity();
        TipAdapter mAdapter = new TipAdapter(mContext, getTipList());
        NoPredictiveAnimationManager mLayoutManager = new NoPredictiveAnimationManager(mContext);
        mListView.setAdapter(mAdapter);
        mListView.setLayoutManager(mLayoutManager);
    }

    private List<TipBase> getTipList() {
        List<TipBase> list = new ArrayList<>();
        // --------------------------------------------------
        list.add(new TipTitle("Navigation"));
        list.add(new Tip("Take me <i>home</i>"));
        list.add(new Tip("Navigate to <i>Starbucks</i>"));
        list.add(new Tip("Find <i>gas stations</i>"));
        list.add(new Tip("Cancel navigation"));
        list.add(new Tip("<i>More natural commands...</i>"));
        // --------------------------------------------------
        list.add(new TipTitle("Message"));
        list.add(new Tip("Message <i>David</i> on <i>WhatsApp</i>."));
        list.add(new Tip("<i>WhatsApp</i> <i>Jason</i>."));
        list.add(new Tip("Send a <i>WhatsApp</i> to <i>Chris</i>."));
        list.add(new Tip("<i>More natural commands...</i>"));
        // --------------------------------------------------
        list.add(new TipTitle("Music"));
        list.add(new Tip("Search music"));
        list.add(new Tip("Play <i>See you again</i>."));
        list.add(new Tip("Play <i>Jazz music</i>."));
        list.add(new Tip("Play <i>Sorry</i> by <i>Justin Bieber</i>"));
        // --------------------------------------------------
        return list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private static final class TipAdapter extends BaseAdapter {

        private static final int TYPE_TITLE = 0;
        private static final int TYPE_TIP = 1;

        @IntDef({TYPE_TITLE, TYPE_TIP})
        private @interface ViewType {
        }

        private Context mContext;
        private List<TipBase> mList;

        private TipAdapter(Context context, List<TipBase> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ViewType int viewType) {
            switch (viewType) {
                case TYPE_TIP:
                    return new TipHolder(LayoutInflater.from(mContext).inflate(R.layout.go_layout_drawer_tip_list_item, parent, false));
                default:
                case TYPE_TITLE:
                    return new TipTitleHolder(LayoutInflater.from(mContext).inflate(R.layout.go_layout_drawer_tip_list_item_title, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            if (holder == null) {
                return;
            }

            switch (getItemViewType(position)) {
                case TYPE_TIP:
                    onBindTipHolder((TipHolder) holder, position);
                    break;
                case TYPE_TITLE:
                    onBindTipTitleHolder((TipTitleHolder) holder, position);
                    break;
            }
        }

        private void onBindTipHolder(TipHolder holder, int position) {
            Tip tip = (Tip) mList.get(position);
            if (tip == null) {
                return;
            }
            holder.mItemTip.setText(Html.fromHtml(tip.tip));
        }

        private void onBindTipTitleHolder(TipTitleHolder holder, int position) {
            TipTitle tipTitle = (TipTitle) mList.get(position);
            if (tipTitle == null) {
                return;
            }
            holder.mItemTitle.setText(tipTitle.title);
        }

        @Override
        public void resetHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case TYPE_TIP:
                    resetTipHolder((TipHolder) holder);
                    break;
                case TYPE_TITLE:
                    resetTitleHolder((TipTitleHolder) holder);
                    break;
            }
        }

        private void resetTipHolder(TipHolder holder) {
            holder.mItemTip.setText("");
        }

        private void resetTitleHolder(TipTitleHolder holder) {
            holder.mItemTitle.setText("");
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        @ViewType
        public int getItemViewType(int position) {
            TipBase object = mList.get(position);
            return object instanceof Tip
                    ? TYPE_TIP
                    : TYPE_TITLE;
        }

        private class TipTitleHolder extends RecyclerView.ViewHolder {
            private TextView mItemTitle;

            private TipTitleHolder(View itemView) {
                super(itemView);
                mItemTitle = (TextView) itemView.findViewById(R.id.go_layout_drawer_tip_list_item_title);
            }
        }

        private class TipHolder extends RecyclerView.ViewHolder {
            private TextView mItemTip;

            private TipHolder(View itemView) {
                super(itemView);
                mItemTip = (TextView) itemView.findViewById(R.id.go_layout_drawer_tip_list_item);
            }
        }
    }


    private class TipTitle extends TipBase {
        private String title;

        private TipTitle(String title) {
            this.title = title;
        }
    }

    private class Tip extends TipBase {
        private String tip;

        private Tip(String tip) {
            this.tip = tip;
        }
    }

    private abstract class TipBase {
    }
}
