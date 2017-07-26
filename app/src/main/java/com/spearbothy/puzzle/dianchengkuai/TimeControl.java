package com.spearbothy.puzzle.dianchengkuai;

import android.os.Handler;
import android.os.Message;

/**
 * Created by mahao on 17-7-25.
 */

public class TimeControl {

    private static final int MESSAGE_TIME = 2;

    private static final long REFRESH_INTERVAL = 16L;

    private static final long TIME_ADD_LEVEL = 5 * 1000L;

    private static final long TIME_ADD_POINT = 100L;

    private static TimeControl sInstance;

    private long mCurrentTime;

    private TimeListener listener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TIME:
                    mCurrentTime += REFRESH_INTERVAL;
                    mHandler.sendEmptyMessageDelayed(MESSAGE_TIME, REFRESH_INTERVAL);
                    notifyListener();
                    break;
                default:
                    break;
            }

        }
    };

    private void notifyListener() {
        if (listener == null) {
            return;
        }

        listener.onRefresh();

        if (mCurrentTime / TIME_ADD_LEVEL - (mCurrentTime - REFRESH_INTERVAL) / TIME_ADD_LEVEL == 1) {
            listener.onLevelUpdate();
        }

        if (mCurrentTime / TIME_ADD_POINT - (mCurrentTime - REFRESH_INTERVAL) / TIME_ADD_POINT == 1) {
            listener.onPointAdd();
        }
    }

    public void restart() {
        mCurrentTime = 0L;
        mHandler.sendEmptyMessageDelayed(MESSAGE_TIME, REFRESH_INTERVAL);
    }

    public void start() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_TIME, REFRESH_INTERVAL);
    }

    public void pause() {
        mHandler.removeMessages(MESSAGE_TIME);
    }

    public void end() {
        mCurrentTime = 0L;
        mHandler.removeMessages(MESSAGE_TIME);
    }

    private TimeControl() {

    }

    public static TimeControl getInstance() {
        if (sInstance == null) {
            sInstance = new TimeControl();
        }
        return sInstance;
    }

    public void setTimeListener(TimeListener listener) {
        this.listener = listener;
    }


    interface TimeListener {
        void onRefresh();

        void onLevelUpdate();

        void onPointAdd();
    }
}
