package com.cc.pulltorefreshlayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class PullDownHeaderView extends FrameLayout implements PullToRefreshLayout.OnPullStateChangedListener {
    protected View pb;

    public PullDownHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_more, this, true);
        pb = findViewById(R.id.pb);
    }

    @Override
    public void onPullStateChanged(int state) {
        String text = "";
        switch (state) {
            case PullToRefreshLayout.STATE_NORMAL:
                text = "下拉刷新";
                break;
            case PullToRefreshLayout.STATE_READY_TO_REFRESHING:
                text = "松开刷新";
                break;
            case PullToRefreshLayout.STATE_REFRESHING:
                text = "正在刷新";
                break;
        }
        ((TextView) findViewById(R.id.tv_refresh)).setText(text);
    }

    @Override
    public float getReadyToRefreshOffset() {
        return pb.getHeight();
    }

    @Override
    public void onPullDown(float overScrollTop) {

    }
}
