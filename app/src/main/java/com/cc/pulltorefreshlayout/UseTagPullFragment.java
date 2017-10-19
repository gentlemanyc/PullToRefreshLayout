package com.cc.pulltorefreshlayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by YnanChao on 2017/8/31.
 */

public class UseTagPullFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pull_tag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.pr);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        pullToRefreshLayout.setScrollDownCallBack(new PullToRefreshLayout.OnChildScrollDownCallBack() {
            @Override
            public boolean canChildScrollUp(ViewGroup viewGroup, View targetView) {
                System.out.println("can Scroll:" + !ViewCompat.canScrollVertically(findViewById(R.id.sv), -1));
                return !ViewCompat.canScrollVertically(findViewById(R.id.sv), -1);
            }
        });
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }
}
