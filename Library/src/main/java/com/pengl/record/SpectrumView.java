package com.pengl.record;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pengl.record.R;

/**
 * 频谱视图,借用https://www.jianshu.com/p/76aceacbc243。优化改善
 */
public class SpectrumView extends View {
    private String TAG = getClass().getSimpleName();

    //画笔
    private Paint paint;

    //跳动指针的集合
    private List<Pointer> pointers;

    //跳动指针的数量
    private int pointerNum;

    //逻辑坐标 原点
    private float basePointX;
    private float basePointY;

    //指针间的间隙  默认5dp
    private float pointerPadding;

    //每个指针的宽度 默认3dp
    private float pointerWidth;

    //指针的颜色
    private int pointerColor = Color.RED;

    //控制开始/停止
    private boolean isPlaying;

    //指针波动速率
    private int pointerSpeed;

    private Runnable mAnimationRunnable;
    private ExecutorService mExecutorService;

    public SpectrumView(Context context) {
        super(context);
        init();
    }

    public SpectrumView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
    }

    public SpectrumView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        //取出自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpectrumView);
        pointerColor = ta.getColor(R.styleable.SpectrumView_rectangle_color, Color.RED);
        //指针的数量，默认为4
        pointerNum = ta.getInt(R.styleable.SpectrumView_rectangle_num, 4);
        //指针的宽度，默认5dp
        pointerWidth = ta.getDimension(R.styleable.SpectrumView_rectangle_width, 5f);
        pointerSpeed = ta.getInt(R.styleable.SpectrumView_rectangle_speed, 40);
        ta.recycle();
    }

    /**
     * 初始化画笔与指针的集合
     */
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(pointerColor);
        pointers = new ArrayList<>();

        mExecutorService = Executors.newSingleThreadExecutor();
        mAnimationRunnable = () -> {
            float i = 0;
            while (isPlaying) {
                try {
                    //循环改变每个指针高度
                    for (int j = 0; j < pointers.size(); j++) {
                        //利用正弦有规律的获取0~1的数。
                        float rate = (float) Math.abs(Math.sin(i + j));
                        //rate 乘以 可绘制高度，来改变每个指针的高度
                        pointers.get(j).setHeight((basePointY - getPaddingTop()) * rate);
                    }
                    //休眠一下下，可自行调节
                    Thread.sleep(pointerSpeed);
                    //控制开始/暂停
                    mHandler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i += 0.1;
            }
        };
    }


    /**
     * 在onLayout中做一些，宽高方面的初始化
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 获取逻辑原点的，也就是画布左下角的坐标。这里减去了paddingBottom的距离
        basePointY = getHeight() - getPaddingBottom();
        Random random = new Random();
        if (null == pointers)
            pointers = new ArrayList<>();
        pointers.clear();
        for (int i = 0; i < pointerNum; i++) {
            // 创建指针对象，利用0~1的随机数 乘以 可绘制区域的高度。作为每个指针的初始高度。
            pointers.add(new Pointer((float) (0.1 * (random.nextInt(10) + 1) * (getHeight() - getPaddingBottom() - getPaddingTop()))));
        }
        // 计算每个指针之间的间隔  总宽度 - 左右两边的padding - 所有指针占去的宽度  然后再除以间隔的数量
        pointerPadding = (getWidth() - getPaddingLeft() - getPaddingRight() - pointerWidth * pointerNum) / (pointerNum - 1);
    }

    /**
     * 开始绘画
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //将x坐标移动到逻辑原点，也就是左下角
        basePointX = 0f + getPaddingLeft();
        //循环绘制每一个指针。
        for (int i = 0; i < pointers.size(); i++) {
            //绘制指针，也就是绘制矩形
            canvas.drawRect(basePointX, basePointY - pointers.get(i).getHeight(), basePointX + pointerWidth, basePointY, paint);
            basePointX += (pointerPadding + pointerWidth);
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        if (!isPlaying) {
            //控制子线程中的循环
            isPlaying = true;
            mExecutorService.submit(mAnimationRunnable);
        }
    }

    /**
     * 停止子线程，并刷新画布
     */
    public void stop() {
        isPlaying = false;
        invalidate();
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<SpectrumView> view;

        MyHandler(SpectrumView mSpectrumView) {
            super(Looper.getMainLooper());
            view = new WeakReference<>(mSpectrumView);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            SpectrumView mSpectrumView = view.get();
            if (null == mSpectrumView) {
                return;
            }

            mSpectrumView.invalidate();
        }
    }

    /**
     * 指针类
     */
    public static class Pointer {
        private float height;

        Pointer(float height) {
            this.height = height;
        }

        float getHeight() {
            return height;
        }

        void setHeight(float height) {
            this.height = height;
        }
    }
}
