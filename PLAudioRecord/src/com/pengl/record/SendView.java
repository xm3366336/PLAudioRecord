package com.pengl.record;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 拍摄完成后，显示两个按钮：撤销和保存
 * Created by wanbo on 2017/1/20.
 */
public class SendView extends RelativeLayout {

    public RelativeLayout backLayout, selectLayout;

    public SendView(Context context) {
        super(context);
        init(context);
    }

    public SendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.view_send_btn, this);
        backLayout = findViewById(R.id.return_layout);
        selectLayout = findViewById(R.id.select_layout);
        setVisibility(View.INVISIBLE);
    }

    public void startAnim() {
        setVisibility(VISIBLE);
        int x = Utils.getPixels(getContext(), 96);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(backLayout, "translationX", 0, -x), //
                ObjectAnimator.ofFloat(selectLayout, "translationX", 0, x));
        set.setDuration(250).start();
    }

    public void stopAnim() {
        AnimatorSet set = new AnimatorSet();
        int x = Utils.getPixels(getContext(), 96);
        set.playTogether(ObjectAnimator.ofFloat(backLayout, "translationX", -x, 0),//
                ObjectAnimator.ofFloat(selectLayout, "translationX", x, 0));
        set.setDuration(250).start();
        setVisibility(INVISIBLE);
    }

}
