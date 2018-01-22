package com.kikatech.go.music;

import android.support.annotation.IntDef;

import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.music.provider.StreamMusicProvider;
import com.kikatech.go.music.provider.YouTubeMusicProvider;
import com.kikatech.go.services.view.item.ItemYouTubePlayer;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class MusicManager {
    private static final String TAG = "MusicManager";

    private static MusicManager sIns;

    private static final int PROVIDER_STREAM = 0;
    private static final int PROVIDER_YOUTUBE = 1;

    @IntDef({PROVIDER_STREAM, PROVIDER_YOUTUBE})
    public @interface ProviderType {
        int STREAM = PROVIDER_STREAM;
        int YOUTUBE = PROVIDER_YOUTUBE;
    }

    private YouTubeMusicProvider mYouTubeMusicProvider;
    private StreamMusicProvider mStreamMusicProvider;

    public static synchronized MusicManager getIns() {
        if (sIns == null) {
            sIns = new MusicManager();
        }
        return sIns;
    }

    private MusicManager() {
        mYouTubeMusicProvider = YouTubeMusicProvider.getIns();
        mStreamMusicProvider = StreamMusicProvider.getIns();
    }

    public void setItemYouTubePlayer(ItemYouTubePlayer itemYouTubePlayer) {
        if (mYouTubeMusicProvider != null) {
            mYouTubeMusicProvider.setItemYouTubePlayer(itemYouTubePlayer);
        }
    }

    public void play(Object object) {
        int providerType = object instanceof YouTubeVideo ? PROVIDER_YOUTUBE : PROVIDER_STREAM;
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.play(null);
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.play((YouTubeVideo) object);
                }
                break;
        }
    }

    public void pause(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.pause();
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.pause();
                }
                break;
        }
    }

    public void resume(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.resume();
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.resume();
                }
                break;
        }
    }

    public void volumeUp(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.volumeUp();
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.volumeUp();
                }
                break;
        }
    }

    public void volumeDown(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.volumeDown();
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.volumeDown();
                }
                break;
        }
    }

    public void mute(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.mute();
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.mute();
                }
                break;
        }
    }

    public void unmute(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.unmute();
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.unmute();
                }
                break;
        }
    }

    public void stop(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                if (mStreamMusicProvider != null) {
                    mStreamMusicProvider.stop();
                }
                break;
            case ProviderType.YOUTUBE:
                if (mYouTubeMusicProvider != null) {
                    mYouTubeMusicProvider.stop();
                }
                break;
        }
    }

    public boolean isPlaying(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                return mStreamMusicProvider != null && mStreamMusicProvider.isPlaying();
            default:
            case ProviderType.YOUTUBE:
                return mYouTubeMusicProvider != null && mYouTubeMusicProvider.isPlaying();
        }
    }

    public boolean isPrepared(@ProviderType int providerType) {
        switch (providerType) {
            case ProviderType.STREAM:
                return mStreamMusicProvider != null && mStreamMusicProvider.isPrepared();
            default:
            case ProviderType.YOUTUBE:
                return mYouTubeMusicProvider != null && mYouTubeMusicProvider.isPrepared();
        }
    }
}
