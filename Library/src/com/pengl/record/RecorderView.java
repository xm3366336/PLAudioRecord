package com.pengl.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 自定义麦克风录音控件，带录音时长
 */
public class RecorderView extends LinearLayout {

    private TextView mTvTime;
    private RecorderProgressView mRecorderProgressView;

    private long startTime = 0;
    private boolean isAnimationStart;
    private Runnable mTimeRunnable;
    private RecorderViewListener recorderViewListener;

    public RecorderView(Context context) {
        this(context, null);
    }

    public RecorderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setRecorderViewListener(RecorderViewListener recorderViewListener) {
        this.recorderViewListener = recorderViewListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        mTimeRunnable = () -> {
            while (isAnimationStart) {
                try {
                    mHandler.sendEmptyMessage(0);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        mTvTime = new TextView(getContext());
        mTvTime.setTextColor(Color.argb(255, 230, 85, 35));
        mTvTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mTvTime, params);

        mRecorderProgressView = new RecorderProgressView(getContext());
        addView(mRecorderProgressView, params);
        mRecorderProgressView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAnimation();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (event.getY() < 0) {
                        stopAnimation();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    stopAnimation();
                    return true;
                default:
                    break;
            }
            return false;
        });
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<RecorderView> view;

        MyHandler(RecorderView mRecorderView) {
            super(Looper.getMainLooper());
            view = new WeakReference<>(mRecorderView);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            RecorderView mRecorderView = view.get();
            if (null == mRecorderView) {
                return;
            }

            String seconds = Utils.toTime((int) (System.currentTimeMillis() - mRecorderView.startTime));
            mRecorderView.mTvTime.setText(seconds);
        }
    }

    private void startAnimation() {
        if (!isAnimationStart) {
            isAnimationStart = true;
            startTime = System.currentTimeMillis();
            mRecorderProgressView.startAnimation();
            mTvTime.setVisibility(VISIBLE);
            new Thread(mTimeRunnable).start();
            if (recorderViewListener != null) {
                recorderViewListener.onStart();
            }
        }
    }

    private void stopAnimation() {
        isAnimationStart = false;
        startTime = 0;
        mRecorderProgressView.stopAnimation();
        mTvTime.setVisibility(INVISIBLE);
        if (recorderViewListener != null) {
            recorderViewListener.onStop();
        }
    }

    public interface RecorderViewListener {
        void onStart();

        void onStop();
    }
}
