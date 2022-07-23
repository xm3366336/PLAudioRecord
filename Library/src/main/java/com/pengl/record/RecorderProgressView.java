package com.pengl.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.pengl.record.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 旋转的进度view
 */
class RecorderProgressView extends View {

    private static final int DEFAULT_MIN_WIDTH = 400; //View默认最小宽度
    private static final int RED = 230, GREEN = 85, BLUE = 35; //基础颜色，这里是橙红色
    private static final int MIN_ALPHA = 30; //最小不透明度
    private static final int MAX_ALPHA = 255; //最大不透明度
    private static final float doughnutRadiusPercent = 0.65f; //圆环外圆半径占View最大半径的百分比
    private static final float doughnutWidthPercent = 0.12f; //圆环宽度占View最大半径的百分比
    private static final float MIDDLE_WAVE_RADIUS_PERCENT = 0.9f; //第二个圆出现时，第一个圆的半径百分比
    private static final float WAVE_WIDTH = 5f; //波纹圆环宽度

    // 圆环颜色
    private static final int[] doughnutColors = new int[]{
            Color.argb(MAX_ALPHA, RED, GREEN, BLUE),
            Color.argb(MIN_ALPHA, RED, GREEN, BLUE),
            Color.argb(MIN_ALPHA, RED, GREEN, BLUE)};

    private final Paint paint = new Paint(); //画笔
    private SweepGradient mSweepGradient;
    private Bitmap bitmapMic;

    private float width; //自定义view的宽度
    private float height; //自定义view的高度
    private float currentAngle = 0f; //当前旋转角度
    private float radius; //自定义view的最大半径
    private float secondWaveRadius;

    private boolean isAnimationStart;
    private final ExecutorService esAnimation = Executors.newSingleThreadExecutor();
    private Future ftAnimation;

    public RecorderProgressView(Context context) {
        super(context);
        init();
    }

    public RecorderProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecorderProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSweepGradient = new SweepGradient(0, 0, doughnutColors, null);
        bitmapMic = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_mic_white);
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (!isAnimationStart) {
            isAnimationStart = true;
        }
        if (ftAnimation == null || ftAnimation.isDone()) {
            ftAnimation = esAnimation.submit(animationRunnable);
        }
    }

    public void stopAnimation() {
        isAnimationStart = false;
        ftAnimation.cancel(true);
    }

    private void resetParams() {
        width = getWidth();
        height = getHeight();
        radius = Math.min(width, height) / 2;
    }

    private void initPaint() {
        paint.reset();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        resetParams();

        // 将画布中心设为原点(0,0), 方便后面计算坐标
        canvas.translate(width / 2, height / 2);

        if (currentAngle >= 360f) {
            currentAngle = currentAngle - 360f;
        } else {
            currentAngle = currentAngle + 2f;
        }
        canvas.save();
        // 转起来
        canvas.rotate(-currentAngle, 0, 0);
        // 画渐变圆环
        float doughnutWidth = radius * doughnutWidthPercent;// 圆环宽度

        initPaint();
        paint.setStrokeWidth(doughnutWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(mSweepGradient);

        // 圆环外接矩形
        @SuppressLint("DrawAllocation") RectF rectF = new RectF(
                -radius * doughnutRadiusPercent,
                -radius * doughnutRadiusPercent,
                radius * doughnutRadiusPercent,
                radius * doughnutRadiusPercent);
        canvas.drawArc(rectF, 0, 360, false, paint);

        // 画旋转头部圆
        initPaint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(MAX_ALPHA, RED, GREEN, BLUE));
        canvas.drawCircle(radius * doughnutRadiusPercent, 0, doughnutWidth / 2, paint);
        canvas.restore();
        // 画圆背景
        initPaint();
        paint.setColor(Color.argb(MAX_ALPHA, RED, GREEN, BLUE));
        paint.setAntiAlias(true);
        canvas.drawCircle(0, 0, radius * doughnutRadiusPercent, paint);
        // 画图片
        initPaint();
        // 画麦克风
        if (bitmapMic != null) {
            canvas.drawBitmap(bitmapMic, -bitmapMic.getWidth() / 2f, -bitmapMic.getHeight() / 2f, paint);
        }
        // 实现类似水波涟漪效果
        initPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(WAVE_WIDTH);
        secondWaveRadius = calculateWaveRadius(secondWaveRadius);
        float firstWaveRadius = calculateWaveRadius(secondWaveRadius + radius * (MIDDLE_WAVE_RADIUS_PERCENT - doughnutRadiusPercent) - radius * doughnutWidthPercent / 2);
        paint.setColor(Color.argb(calculateWaveAlpha(secondWaveRadius), RED, GREEN, BLUE));
        // 画第二个圆（初始半径较小的）
        canvas.drawCircle(0, 0, secondWaveRadius, paint);

        initPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(WAVE_WIDTH);
        paint.setColor(Color.argb(calculateWaveAlpha(firstWaveRadius), RED, GREEN, BLUE));
        //画第一个圆（初始半径较大的）
        canvas.drawCircle(0, 0, firstWaveRadius, paint);
    }

    /**
     * 计算波纹圆的半径
     *
     * @param waveRadius 半径
     * @return 半径
     */
    private float calculateWaveRadius(float waveRadius) {
        if (waveRadius < radius * doughnutRadiusPercent + radius * doughnutWidthPercent / 2) {
            waveRadius = radius * doughnutRadiusPercent + radius * doughnutWidthPercent / 2;
        }
        if (waveRadius > radius * MIDDLE_WAVE_RADIUS_PERCENT + radius * (MIDDLE_WAVE_RADIUS_PERCENT - doughnutRadiusPercent) - radius * doughnutWidthPercent / 2) {
            waveRadius = waveRadius - (radius * MIDDLE_WAVE_RADIUS_PERCENT + radius * (MIDDLE_WAVE_RADIUS_PERCENT - doughnutRadiusPercent) - radius * doughnutWidthPercent / 2) + radius * doughnutWidthPercent / 2 + radius * doughnutRadiusPercent;
        }
        waveRadius += 0.6f;
        return waveRadius;
    }

    /**
     * 根据波纹圆的半径计算不透明度
     *
     * @param waveRadius 半径
     * @return 不透明度
     */
    private int calculateWaveAlpha(float waveRadius) {
        float percent = (waveRadius - radius * doughnutRadiusPercent - radius * doughnutWidthPercent / 2) / (radius - radius * doughnutRadiusPercent - radius * doughnutWidthPercent / 2);
        if (percent >= 1f) {
            return 0;
        } else {
            return (int) (MIN_ALPHA * (1f - percent));
        }
    }

    /**
     * 当布局为wrap_content时设置默认长宽
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int origin) {
        int result = DEFAULT_MIN_WIDTH;
        int specMode = MeasureSpec.getMode(origin);
        int specSize = MeasureSpec.getSize(origin);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private final Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            while (isAnimationStart) {

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                postInvalidate();
            }
        }
    };

}
