package com.example.lulin.todolist.Utils;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 计时器
 */
public class CountUpTimer {


    public interface OnCountUpTickListener {

        /**
         * CountDownTimer 更改时触发的事件
         */
        void onCountUpTick(long millisUntilFinished);
        /**
         * CountDownTimer 完成时触发的事件
         */
        void onCountUpFinish();

    }

    /**
     * CountDownTimer 要执行的时间(单位毫秒), 此时间并不包含暂停的时间
     */
    private final long mMillisInFuture = Long.MAX_VALUE;

    /**
     * 间隔时间(单位毫秒)
     */
    private final long mCountupInterval;

    /**
     * 结束的时间(单位毫秒), 如果暂停会变化
     */
    private long mStopTimeInFuture;

    /**
     * 剩余时间(单位毫秒)
     */
    private long mMillisUntilFinished;

    /**
     * 暂停的状态
     */
    private boolean mPaused = false;

    /**
     * 是否正在倒计时
     */
    private boolean mIsRunning = false;

    /**
     * 倒计时开始的时间
     */
    private Date mStartTime;


    /**
     * 暂停时刻的时间(单位毫秒), 如果暂停会变化
     */
    private long mTpause;

    /**
     * 已经走过的时间(单位毫秒), 如果暂停会变化
     */
    private long mTspend;

    /**
     * 重新启动时的时刻。
     */
    private long mTrestart;



    //    private OnCountDownTickListener mOnCountDownTickListener;
    private OnCountUpTickListener mOnCountUpTickListener;

    public CountUpTimer(long millisInFuture) {
//        mMillisInFuture = millisInFuture;
        mCountupInterval = 1000;
    }

    public void setOnChronometerTickListener(OnCountUpTickListener listener) {
        mOnCountUpTickListener = listener;
    }

    public synchronized final CountUpTimer start() {
//        if (mMillisInFuture <= 0) {
//            onFinish();
//        } else {
            //
//            mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
            mStartTime = new Date();
        mTspend = 0;
        mTpause = mStartTime.getTime();
        mTrestart = mStartTime.getTime();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG), 1);
            mIsRunning = true;
            mPaused = false;
//        }

        return this;
    }

    public final void pause() {
        // 记录剩余时间
        mMillisUntilFinished = mStopTimeInFuture - SystemClock.elapsedRealtime();
        mIsRunning = false;
        mPaused = true;
        mTpause = System.currentTimeMillis();
        // 记录下当前暂停时刻的时间戳
        mTspend = mTspend +  mTpause - mTrestart;
    }

    public long resume() {
        // 结束的时间设置为当前时间加剩余时间
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisUntilFinished;
        mTrestart = System.currentTimeMillis();
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        mIsRunning = true;
        mPaused = false;
        return mMillisUntilFinished;
    }

    public final void cancel() {
        mHandler.removeMessages(MSG);
        mIsRunning = false;
    }

    private void onTick(long millisUntilFinished) {
        if (mOnCountUpTickListener != null) {
            mOnCountUpTickListener.onCountUpTick(millisUntilFinished);
        }
    }

    private void onFinish() {
        mIsRunning = false;

        if (mOnCountUpTickListener != null) {
            mOnCountUpTickListener.onCountUpFinish();
        }
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public Date getStartTime() {
        return mStartTime;
    }

    public long getMinutesInFuture() {
        return TimeUnit.MILLISECONDS.toMinutes(mMillisInFuture);
    }

    private static final int MSG = 1;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountUpTimer.this) {
                if (!mPaused) {
//                    final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();
//                    final long millisLeft = System.currentTimeMillis() - mStartTime.getTime();
                    final long millisLeft = System.currentTimeMillis() - mTrestart + mTspend;
//                    if (millisLeft <= 0) {
//                        onFinish();
//                    } else {
                        long lastTickStart = SystemClock.elapsedRealtime();
                        onTick(millisLeft);

                        // take into account user's onTick taking time to execute
                        long delay = lastTickStart + mCountupInterval - SystemClock.elapsedRealtime();

                        // special case: user's onTick took more than interval to
                        // complete, skip to next interval
                        while (delay < 0) delay += mCountupInterval;

                        sendMessageDelayed(obtainMessage(MSG), delay);
//                    }
                }
            }
        }
    };
}
