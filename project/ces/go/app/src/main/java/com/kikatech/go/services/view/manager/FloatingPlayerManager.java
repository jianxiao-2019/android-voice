package com.kikatech.go.services.view.manager;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.music.MusicSceneUtil;
import com.kikatech.go.music.MusicManager;
import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.music.model.YouTubeVideoList;
import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.go.services.presenter.YouTubeExtractorManager;
import com.kikatech.go.services.view.item.ItemYouTubePlayer;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.FlexibleOnTouchListener;
import com.kikatech.go.view.youtube.player.impl.SkVideoPlayerView;
import com.kikatech.go.view.youtube.playercontroller.impl.SkPlayerController;

/**
 * @author SkeeterWang Created on 2018/1/16.
 */

public class FloatingPlayerManager extends BaseFloatingManager {
    private static final String TAG = "FloatingPlayerManager";

    private final int MIN_WIDTH;
    private final int MIN_HEIGHT;

    private ItemYouTubePlayer mItemPlayer;


    private FlexibleOnTouchListener onYouTubePlayerTouchListener = new FlexibleOnTouchListener(100, new FlexibleOnTouchListener.ITouchListener() {
        private int[] viewOriginalXY = new int[2];
        private float[] eventOriginalXY = new float[2];
        private int[] deltaXY = new int[2];

        @Override
        public void onLongPress(View view, MotionEvent event) {
        }

        @Override
        public void onShortPress(View view, MotionEvent event) {
        }

        @Override
        public void onClick(View view, MotionEvent event) {
            mItemPlayer.onControllerViewClickEvent(event);
        }

        @Override
        public void onDown(View view, MotionEvent event) {
            viewOriginalXY = mItemPlayer.getViewXY();
            eventOriginalXY = new float[]{event.getRawX(), event.getRawY()};
        }

        @Override
        public void onMove(View view, MotionEvent event, long timeSpentFromStart) {
            deltaXY = new int[]{(int) (event.getRawX() - eventOriginalXY[0]), (int) (event.getRawY() - eventOriginalXY[1])};
            int targetX = getValidX(viewOriginalXY[0], deltaXY[0]);
            int targetY = getValidY(viewOriginalXY[1], deltaXY[1]);
            mContainer.moveItem(mItemPlayer, targetX, targetY);
        }

        @Override
        public void onUp(View view, MotionEvent event, long timeSpentFromStart) {
        }

        private int getValidX(int viewOriginalX, int deltaX) {
            int boundLeft = 0;
            int boundRight = getDeviceWidthByOrientation() - mItemPlayer.getMeasuredWidth();
            return (deltaX > 0)
                    ? (viewOriginalX + deltaX < boundRight) ? viewOriginalX + deltaX : boundRight
                    : (viewOriginalX + deltaX >= boundLeft) ? viewOriginalX + deltaX : boundLeft;
        }

        private int getValidY(int viewOriginalY, int deltaY) {
            int boundTop = 0;
            int boundBottom = getDeviceHeightByOrientation() - mItemPlayer.getMeasuredHeight();
            return (deltaY > 0)
                    ? (viewOriginalY + deltaY < boundBottom) ? viewOriginalY + deltaY : boundBottom
                    : (viewOriginalY + deltaY >= boundTop) ? viewOriginalY + deltaY : boundTop;
        }
    });

    private SkPlayerController.IControllerCallback.IVideoCallback mControllerVideoCallback = new SkPlayerController.IControllerCallback.IVideoCallback() {
        @Override
        public void onPrev() {
            prev();
        }

        @Override
        public void onNext() {
            next();
        }
    };

    private SkPlayerController.IControllerCallback.IPlayerCallback mControllerPlayerCallback = new SkPlayerController.IControllerCallback.IPlayerCallback() {
        @Override
        public void onScaleUp() {
            scaleUp();
        }

        @Override
        public void onScaleDown() {
            scaleDown();
        }

        @Override
        public void onClose() {
            MusicForegroundService.stopMusic(mContext);
        }

        @Override
        public void onShare() {
        }

        @Override
        public void onFavorite() {
        }

        @Override
        public void onYouTubeIconClick() {
        }
    };

    private FloatingPlayerManager(Context context, WindowManager manager, LayoutInflater inflater, Configuration configuration) {
        super(context, manager, inflater, configuration);
        MIN_WIDTH = context.getResources().getDimensionPixelSize(R.dimen.youtube_bar_player_view_width);
        MIN_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.youtube_bar_player_view_height);
        initItems();
        if (LogUtil.DEBUG) {
            switch (configuration.orientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    LogUtil.log(TAG, "ORIENTATION_LANDSCAPE");
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    LogUtil.log(TAG, "ORIENTATION_PORTRAIT");
                    break;
            }
        }
    }

    private void initItems() {
        mItemPlayer = new ItemYouTubePlayer(inflate(R.layout.youtube_player), onYouTubePlayerTouchListener);
        initPlayer();
        MusicManager.getIns().setItemYouTubePlayer(mItemPlayer);
    }

    private void initPlayer() {
        View mPlayerView = mItemPlayer.getPlayerView();
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlayerView.getLayoutParams();
        int deviceWidth = getDeviceWidthByOrientation();
        int deviceHeight = getDeviceHeightByOrientation();

        // Init player style according to current scale type
        switch (mItemPlayer.getPlayerSize()) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                int x = (deviceWidth - mItemPlayer.getMeasuredWidth()) / 2;
                int y = (deviceHeight - ResolutionUtil.dp2px(mContext, 186));
                layoutParams.width = MIN_WIDTH;
                layoutParams.height = MIN_HEIGHT;
                mItemPlayer.setViewWidth(MIN_WIDTH);
                mItemPlayer.setViewHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                mItemPlayer.setViewXY(x, y);
                mItemPlayer.getLayoutParams().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
                float scale = ((float) deviceWidth) / ((float) MIN_WIDTH);
                layoutParams.width = deviceWidth;
                layoutParams.height = (int) (MIN_HEIGHT * scale);
                mItemPlayer.setViewWidth(deviceWidth);
                mItemPlayer.setViewHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                if (mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mItemPlayer.setViewXY(0, 400);
                }
                // mLayoutParams.x = 0;
                // mLayoutParams.y = mLayoutParams.y - ( layoutParams.height - oldPlayerHeight ); // Fit Original Bottom
                mItemPlayer.getLayoutParams().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                break;
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mItemPlayer.setViewWidth(WindowManager.LayoutParams.MATCH_PARENT);
                mItemPlayer.setViewHeight(WindowManager.LayoutParams.MATCH_PARENT);
                mItemPlayer.setViewXY(0, 0);
                mItemPlayer.getLayoutParams().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
        }
        mPlayerView.setLayoutParams(layoutParams);
        mPlayerView.requestLayout();
        mItemPlayer.getItemView().requestLayout();
        mItemPlayer.setControllerVideoCallback(mControllerVideoCallback);
        mItemPlayer.setControllerPlayerCallback(mControllerPlayerCallback);
        mItemPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
    }


    public synchronized void showPlayer(final YouTubeVideoList listToPlay) {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "showPlayer");
        }
        if (!mContainer.isViewAdded(mItemPlayer)) {
            mContainer.addItem(mItemPlayer);
        } else {
            MusicManager.getIns().pause(MusicManager.ProviderType.YOUTUBE);
        }
        final YouTubeVideoList mClonedList = new YouTubeVideoList();
        mClonedList.setListType(listToPlay.getListType());
        mClonedList.addAll(listToPlay.getList());
        YouTubeExtractorManager.getIns().loadPlayList(mContext, mClonedList, new RetryableExtractListener() {
            @Override
            public void onLoaded(YouTubeVideo loadedVideo) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onLoaded");
                }
                MusicManager.getIns().play(loadedVideo);
            }

            @Override
            public void onError() {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onError");
                }
                if (mRetryCount > 0) {
                    YouTubeExtractorManager.getIns().loadPlayList(mContext, mClonedList, this);
                }
                mRetryCount--;
            }
        });
    }

    public synchronized void removePlayer() {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "removePlayer");
        }
        mContainer.removeItem(mItemPlayer);
        MusicManager.getIns().stop(MusicManager.ProviderType.YOUTUBE);
        YouTubeExtractorManager.getIns().clearTasks();
    }


    private void scaleUp() {
        switch (mItemPlayer.getPlayerSize()) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                scale(SkVideoPlayerView.PlayerSize.MEDIUM, mConfiguration);
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
                scale(SkVideoPlayerView.PlayerSize.FULLSCREEN, mConfiguration);
                break;
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                break;
        }
    }

    private void scaleDown() {
        switch (mItemPlayer.getPlayerSize()) {
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                scale(SkVideoPlayerView.PlayerSize.MEDIUM, mConfiguration);
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
                scale(SkVideoPlayerView.PlayerSize.MINIMUM, mConfiguration);
                break;
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                break;
        }
    }

    private void scale(@SkVideoPlayerView.PlayerSize int scaleType, Configuration configuration) {
        View mPlayerView = mItemPlayer.getPlayerView();
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlayerView.getLayoutParams();
        layoutParams.topMargin = 0;
        layoutParams.leftMargin = 0;

        int deviceWidth = getDeviceWidth();
        int oldPlayerWidth = mPlayerView.getWidth();
        int oldPlayerHeight = mPlayerView.getHeight();
        float scale;

        switch (scaleType) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                layoutParams.width = MIN_WIDTH;
                layoutParams.height = MIN_HEIGHT;
                layoutParams.leftMargin = 0;
                layoutParams.rightMargin = 0;
                mItemPlayer.setViewWidth(MIN_WIDTH);
                mItemPlayer.setViewHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                // mLayoutParams.x = ( FloatingPlayer.getDeviceWidthByOrientation() - MIN_WIDTH ) / 2;
                // mLayoutParams.y = mLayoutParams.y - ( layoutParams.height - oldPlayerHeight ); // Scale Down Align Original Bottom
                mItemPlayer.getLayoutParams().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
                scale = ((float) deviceWidth) / ((float) oldPlayerWidth);
                layoutParams.width = deviceWidth;
                layoutParams.height = (int) (oldPlayerHeight * scale);
                mItemPlayer.setViewWidth(deviceWidth);
                mItemPlayer.setViewHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mItemPlayer.setViewX(0);
                }
                // mLayoutParams.x = 0;
                // mLayoutParams.y = mLayoutParams.y - ( layoutParams.height - oldPlayerHeight ); // Fit Original Bottom
                mItemPlayer.getLayoutParams().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                break;
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mItemPlayer.setViewWidth(WindowManager.LayoutParams.MATCH_PARENT);
                mItemPlayer.setViewHeight(WindowManager.LayoutParams.MATCH_PARENT);
                mItemPlayer.setViewXY(0, 0);
                mItemPlayer.getLayoutParams().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
        }
        mPlayerView.setLayoutParams(layoutParams);
        mPlayerView.requestLayout();
        mItemPlayer.getItemView().requestLayout();
        mItemPlayer.scale(scaleType);
        mContainer.requestLayout(mItemPlayer);
    }


    public synchronized void pauseOrResume() {
        if (!mContainer.isViewAdded(mItemPlayer)) {
            return;
        }
        MusicManager musicManager = MusicManager.getIns();
        if (musicManager.isPlaying(MusicManager.ProviderType.YOUTUBE)) {
            musicManager.pause(MusicManager.ProviderType.YOUTUBE);
        } else {
            musicManager.resume(MusicManager.ProviderType.YOUTUBE);
        }
    }

    public synchronized void next() {
        if (!mContainer.isViewAdded(mItemPlayer)) {
            return;
        } else {
            MusicManager.getIns().pause(MusicManager.ProviderType.YOUTUBE);
        }
        YouTubeExtractorManager.getIns().next(mContext, new RetryableExtractListener() {
            @Override
            public void onLoaded(YouTubeVideo loadedVideo) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onLoaded");
                }
                MusicManager.getIns().play(loadedVideo);
            }

            @Override
            public void onError() {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onError");
                }
                if (mRetryCount > 0) {
                    YouTubeExtractorManager.getIns().next(mContext, this);
                }
                mRetryCount--;
            }
        });
    }

    public synchronized void prev() {
        if (!mContainer.isViewAdded(mItemPlayer)) {
            return;
        } else {
            MusicManager.getIns().pause(MusicManager.ProviderType.YOUTUBE);
        }
        YouTubeExtractorManager.getIns().prev(mContext, new RetryableExtractListener() {
            @Override
            public void onLoaded(YouTubeVideo loadedVideo) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onLoaded");
                }
                MusicManager.getIns().setItemYouTubePlayer(mItemPlayer);
                MusicManager.getIns().play(loadedVideo);
            }

            @Override
            public void onError() {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onError");
                }
                if (mRetryCount > 0) {
                    YouTubeExtractorManager.getIns().prev(mContext, this);
                }
                mRetryCount--;
            }
        });
    }

    public synchronized void volumeControl(@MusicSceneUtil.VolumeControlType int type) {
        if (mContainer.isViewAdded(mItemPlayer)) {
            switch (type) {
                case MusicSceneUtil.VolumeControlType.VOLUME_UP:
                    MusicManager.getIns().volumeUp(MusicManager.ProviderType.YOUTUBE);
                    break;
                case MusicSceneUtil.VolumeControlType.VOLUME_DOWN:
                    MusicManager.getIns().volumeDown(MusicManager.ProviderType.YOUTUBE);
                    break;
                case MusicSceneUtil.VolumeControlType.MUTE:
                    MusicManager.getIns().mute(MusicManager.ProviderType.YOUTUBE);
                    break;
                case MusicSceneUtil.VolumeControlType.UNMUTE:
                    MusicManager.getIns().unmute(MusicManager.ProviderType.YOUTUBE);
                    break;
            }
        }
    }

    public synchronized void pauseMusic() {
        if (!mContainer.isViewAdded(mItemPlayer)) {
            return;
        }
        MusicManager musicManager = MusicManager.getIns();
        if (musicManager.isPlaying(MusicManager.ProviderType.YOUTUBE)) {
            musicManager.pause(MusicManager.ProviderType.YOUTUBE);
        }
    }

    public synchronized void resumeMusic() {
        if (!mContainer.isViewAdded(mItemPlayer)) {
            return;
        }
        MusicManager musicManager = MusicManager.getIns();
        if (!musicManager.isPlaying(MusicManager.ProviderType.YOUTUBE)) {
            musicManager.resume(MusicManager.ProviderType.YOUTUBE);
        }
    }

    public synchronized String getCurrentVideoTitle() {
        return YouTubeExtractorManager.getIns().getCurrentVideoTitle();
    }


    public static final class Builder extends BaseFloatingManager.Builder<Builder> {
        public FloatingPlayerManager build(Context context) {
            return new FloatingPlayerManager(context, mWindowManager, mLayoutInflater, mConfiguration);
        }
    }


    private abstract class RetryableExtractListener implements YouTubeExtractorManager.IExtractListener {
        int mRetryCount;

        private RetryableExtractListener() {
            this(1);
        }

        private RetryableExtractListener(int retryCount) {
            mRetryCount = retryCount;
        }
    }
}
