package com.kikatech.go.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.music.YouTubeUtil;
import com.kikatech.go.ui.adapter.BaseAdapter;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.NoPredictiveAnimationManager;

import java.util.List;

/**
 * @author SkeeterWang Created on 2017/12/20.
 */

public class DrawerMusicFragment extends Fragment {
    private static final String TAG = "DrawerMusicFragment";

    public static DrawerMusicFragment newInstance(IDrawerMusicListener listener) {
        DrawerMusicFragment fragment = new DrawerMusicFragment();
        fragment.setListener(listener);
        return fragment;
    }

    private IDrawerMusicListener mListener;

    private void setListener(IDrawerMusicListener listener) {
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
        final View mView = inflater.inflate(R.layout.go_layout_drawer_music, null);
        mView.findViewById(R.id.drawer_title_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBackClicked();
                }
            }
        });
        RecyclerView mPlaylistListView = (RecyclerView) mView.findViewById(R.id.drawer_music_playlist_list);
        PlaylistAdapter mAdapter = new PlaylistAdapter(mContext, YouTubeUtil.RecommendPlayList.getAllPlaylist());
        NoPredictiveAnimationManager mLayoutManager = new NoPredictiveAnimationManager(mContext);
        mPlaylistListView.setAdapter(mAdapter);
        mPlaylistListView.setLayoutManager(mLayoutManager);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public interface IDrawerMusicListener {
        void onBackClicked();
    }

    private final class PlaylistAdapter extends BaseAdapter<YouTubeUtil.RecommendPlayList> {
        private static final String TAG = "PlaylistAdapter";

        private PlaylistAdapter(Context context, List<YouTubeUtil.RecommendPlayList> list) {
            super(context, list);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PlaylistHolder(LayoutInflater.from(mContext).inflate(R.layout.go_layout_drawer_music_playlist_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            if (holder == null) {
                return;
            }

            final PlaylistHolder mPlaylistHolder = (PlaylistHolder) holder;
            final YouTubeUtil.RecommendPlayList mPlayList = mList.get(position);

            mPlaylistHolder.mItemName.setText(mPlayList.getName());
            mPlaylistHolder.mItemCheckBox.setChecked(mPlayList.isEnable());

            mPlaylistHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isEnabled = mPlayList.isEnable();
                    boolean toEnable = !isEnabled;
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, String.format("name: %s, toEnable: %s", mPlayList.getName(), toEnable));
                    }
                    mPlaylistHolder.mItemCheckBox.setChecked(toEnable);
                    mPlayList.setEnable(toEnable);
                }
            });
        }

        @Override
        public void resetHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            PlaylistHolder playlistHolder = (PlaylistHolder) holder;
            playlistHolder.mItemName.setText("");
            playlistHolder.mItemCheckBox.setChecked(false);
            playlistHolder.itemView.setOnClickListener(null);
        }

        private class PlaylistHolder extends RecyclerView.ViewHolder {
            TextView mItemName;
            CheckBox mItemCheckBox;

            private PlaylistHolder(View itemView) {
                super(itemView);
                mItemName = (TextView) itemView.findViewById(R.id.go_layout_drawer_music_playlist_list_item_text);
                mItemCheckBox = (CheckBox) itemView.findViewById(R.id.go_layout_drawer_music_playlist_list_item_check_box);
            }
        }
    }

}
