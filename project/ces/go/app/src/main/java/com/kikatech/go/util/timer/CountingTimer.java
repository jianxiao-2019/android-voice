package com.kikatech.go.util.timer;

/**
 * @author SkeeterWang Created on 2017/3/6.
 */

public class CountingTimer implements Runnable {
    private long mMillisInFuture;
    private long mCountDownInterval;
    private ICountingListener mCountingListener;
    private boolean isCounting;
    private long mCountedTime = 0;


    public CountingTimer(long millisInFuture) {
        this(millisInFuture, 1000, null);
    }

    public CountingTimer(long millisInFuture, ICountingListener listener) {
        this(millisInFuture, 1000, listener);
    }

    public CountingTimer(long millisInFuture, long countDownInterval) {
        this(millisInFuture, countDownInterval, null);
    }

    public CountingTimer(long millisInFuture, long countDownInterval, ICountingListener listener) {
        this.mMillisInFuture = millisInFuture;
        this.mCountDownInterval = countDownInterval;
        this.mCountingListener = listener;
    }


    public void setCountingListener(ICountingListener listener) {
        this.mCountingListener = listener;
    }

    public void setMillisInFuture(long millisInFuture) {
        this.mMillisInFuture = millisInFuture;
        reset();
    }

    public long getMillisInFuture() {
        return this.mMillisInFuture;
    }


    @Override
    public void run() {
        if (mCountedTime < mMillisInFuture) {
            tick();
        } else {
            endStop();
        }
    }

    private void reset() {
        TimerThread.getHandler().removeCallbacks(this);
        isCounting = false;
        mCountedTime = 0;
    }

    private void tick() {
        long restTime = mMillisInFuture - mCountedTime;
        long countDownInterval = restTime == 0 || restTime > mCountDownInterval ? mCountDownInterval : restTime;
        mCountedTime = mCountedTime + countDownInterval;
        if (mCountingListener != null) mCountingListener.onTimeTick(mCountedTime);
        restTime = restTime - countDownInterval;
        countDownInterval = restTime == 0 || restTime > mCountDownInterval ? mCountDownInterval : restTime;
        TimerThread.getHandler().postDelayed(this, countDownInterval);
    }

    private void endStop() {
        if (mCountingListener != null) mCountingListener.onTimeTickEnd();
        reset();
    }


    public void start() {
        if (isCounting) reset();
        if (mCountedTime == 0 && mCountingListener != null)
            mCountingListener.onTimeTickStart();
        isCounting = true;
        TimerThread.getHandler().post(this);
    }

    public void stop() {
        if (mCountingListener != null) mCountingListener.onInterrupted(mCountedTime);
        reset();
    }


    public boolean isCounting() {
        return isCounting;
    }


    public interface ICountingListener {
        void onTimeTickStart();

        void onTimeTick(long millis);

        void onTimeTickEnd();

        void onInterrupted(long stopMillis);
    }
}