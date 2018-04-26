package com.kikatech.go.util.amazon.pollyGen;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.VoiceId;
import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.go.util.AsyncThreadPool;

import java.net.URL;

/**
 * Created by brad_chang on 2017/12/15.
 */

public class PollyUtil {
    private static final String AWS_COGNITO_POOL_ID = "us-east-1:64fc7318-6e53-47d0-b7c8-a6d01aa3c708";
    private static final Regions POLLY_TTS_REGION = Regions.US_EAST_1;

    private static final VoiceId mVoiceId = VoiceId.Salli;

    private static final String TAG_SPEAK = "<speak>";
    private static final String TAG_PICH = "<prosody pitch=\"+0%\">";
    private static final String TAG_EFFECT = "<amazon:effect vocal-tract-length=\"+0%\">";
    private static final String TAG_END_PICH = "</prosody>";
    private static final String TAG_END_EFFECT = "</amazon:effect>";
    private static final String TAG_END_SPEAK = "</speak>";
    private static final String TAG_SAMPLE_RATE = "22050";


    private static PollyUtil sIns;

    private AmazonPollyPresigningClient mClient;

    public static synchronized PollyUtil getIns() {
        if (sIns == null) {
            sIns = new PollyUtil();
        }
        return sIns;
    }

    private PollyUtil() {
        // Initialize the Amazon Cognito credentials provider.
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                KikaMultiDexApplication.getAppContext(), AWS_COGNITO_POOL_ID, POLLY_TTS_REGION
        );

        // Create a mClient that supports generation of presigned URLs.
        mClient = new AmazonPollyPresigningClient(credentialsProvider);
    }

    public void getUrl(final String ssml, final IPollySynthesizerListener listener) {
        AsyncThreadPool.getIns().execute(new Runnable() {
            @Override
            public void run() {
                final SynthesizeSpeechPresignRequest request =
                        new SynthesizeSpeechPresignRequest()
                                .withTextType(TextType.Ssml)
                                .withSampleRate(TAG_SAMPLE_RATE)
                                // Set text to synthesize.
                                .withText(ssml)
                                // Set voice selected by the user.
                                .withVoiceId(mVoiceId)
                                // Set format to MP3.
                                .withOutputFormat(OutputFormat.Mp3);
                URL url = mClient.getPresignedSynthesizeSpeechUrl(request);
                if (listener != null) {
                    listener.onUrlGet(url.toString());
                }
            }
        });
    }

    public String getDefaultSsmlText(String textToPlay) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TAG_SPEAK)
                .append(TAG_EFFECT)
                .append(TAG_PICH)
                .append(textToPlay)
                .append(TAG_END_PICH)
                .append(TAG_END_EFFECT)
                .append(TAG_END_SPEAK);
        return stringBuilder.toString();
    }

    public interface IPollySynthesizerListener {
        void onUrlGet(String url);
    }
}