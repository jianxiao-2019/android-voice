package com.kikatech.go.util.amazon;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * @author SkeeterWang Created on 2018/4/25.
 */

public class S3TransferUtil {
    private static final String TAG = "S3TransferUtil";

    private static final String KEY = "AKIAJB6CDTT6FK45X7GA";
    private static final String SECRET = "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";

    private static final String BUCKET_NAME = "kika-voicekeyboard";

    private static final long MAX_PROGRESS_TIME = 12000;

    private static S3TransferUtil sIns;

    private TransferUtility sTransferUtility;
    private ArrayList<TransferObserver> mObservers = new ArrayList<>();


    public static synchronized S3TransferUtil getIns() {
        if (sIns == null) {
            sIns = new S3TransferUtil();
        }
        return sIns;
    }

    private S3TransferUtil() {
        sTransferUtility = new TransferUtility(new AmazonS3Client(new BasicAWSCredentials(KEY, SECRET)), KikaMultiDexApplication.getAppContext());
    }


    /**
     * upload file in {@link #MAX_PROGRESS_TIME}
     **/
    public synchronized void uploadFile(final String filePath, final String key, final IUploadListener listener) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("uploadFile, key: %s, filePath: %s", key, filePath));
        }
        final File fileToUpload = new File(filePath);

        if (!fileToUpload.exists()) {
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }

        final TransferObserver transferObserver = uploadFile(key, fileToUpload);

        transferObserver.setTransferListener(new TransferListener() {
            private long startTime = -1;
            private Runnable counterRunnable = new Runnable() {
                @Override
                public void run() {
                    long current = System.currentTimeMillis();
                    long usedTime = current - startTime;
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, String.format("uploading file timer, usedTime: %s", usedTime / 1000));
                    }
                    if (usedTime >= MAX_PROGRESS_TIME) {
                        cancel(transferObserver.getId());
                    }
                    BackgroundThread.getHandler().postDelayed(this, 1000);
                }
            };

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, String.format("id: %s, state: %s", id, state.name()));
                }

                switch (state) {
                    case IN_PROGRESS:
                        if (startTime == -1) {
                            startTime = System.currentTimeMillis();
                            BackgroundThread.getHandler().post(counterRunnable);
                        }
                        break;
                    case COMPLETED:
                        long current = System.currentTimeMillis();
                        long usedTime = current - startTime;
                        long remainTime = MAX_PROGRESS_TIME - usedTime;
                        if (LogUtil.DEBUG) {
                            LogUtil.logv(TAG, String.format("uploading file timer, remainTime: %s", remainTime / 1000));
                        }
                        if (listener != null) {
                            listener.onUploaded(remainTime);
                        }
                        BackgroundThread.getHandler().removeCallbacks(counterRunnable);
                        removeTask(transferObserver);
                        break;
                    case WAITING_FOR_NETWORK:
                        cancel(transferObserver.getId());
                    case CANCELED:
                    case FAILED:
                        if (listener != null) {
                            listener.onFailed();
                        }
                        BackgroundThread.getHandler().removeCallbacks(counterRunnable);
                        removeTask(transferObserver);
                        break;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("File: %s %s/%s bytes.", fileToUpload.getName(), bytesCurrent, bytesTotal));
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                if (listener != null) {
                    listener.onFailed();
                }
                BackgroundThread.getHandler().removeCallbacks(counterRunnable);
                removeTask(transferObserver);
            }
        });
    }

    private synchronized TransferObserver uploadFile(String key, File file) {
        TransferObserver observer = sTransferUtility.upload(BUCKET_NAME, key, file);
        addTask(observer);
        return observer;
    }

    public synchronized void cancel(int id) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("cancel, id: %s", id));
        }
        sTransferUtility.cancel(id);
    }

    public synchronized TransferObserver downloadFile(String key, File file) {
        TransferObserver observer = sTransferUtility.download(BUCKET_NAME, key, file);
        addTask(observer);
        return observer;
    }

    private void addTask(TransferObserver observer) {
        mObservers.add(observer);
    }

    private void removeTask(TransferObserver observer) {
        mObservers.remove(observer);
    }

    public interface IUploadListener {
        void onUploaded(long remainTime);

        void onFailed();
    }
}
