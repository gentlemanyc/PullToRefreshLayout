package com.cc.pulltorefreshlayout;

import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private String[] mTitles = new String[]{"普通用法", "使用tag", "自定义HeadView"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        ViewPager viewPager = (ViewPager) findViewById(R.id.vp);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return Fragment.instantiate(MainActivity.this, PullFragment.class.getName());
                    case 1:
                        return Fragment.instantiate(MainActivity.this, UseTagPullFragment.class.getName());
                    case 2:
                        return Fragment.instantiate(MainActivity.this, CustomPullFragment.class.getName());

                }
                return Fragment.instantiate(MainActivity.this, PullFragment.class.getName());
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles[position];
            }

            @Override
            public int getCount() {
                return mTitles.length;
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }
}
