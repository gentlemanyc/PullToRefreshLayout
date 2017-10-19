package com.cc.pulltorefreshlayout;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class CustomPullDownHeaderView extends PullDownHeaderView {
    private int colorStart = getResources().getColor(R.color.colorAccent);
    private int colorEnd = getResources().getColor(R.color.colorPrimary);
    private ArgbEvaluator evaluator = new ArgbEvaluator();

    public CustomPullDownHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPullDown(float overScrollTop) {
        float ratio = overScrollTop / pb.getHeight();
        pb.setScaleX(ratio);
        pb.setScaleY(ratio);
        if (ratio > 1f)
            ratio = 1f;
        int color = (int) evaluator.evaluate(ratio, colorStart, colorEnd);
        getChildAt(0).setBackgroundColor(color);
    }
}
