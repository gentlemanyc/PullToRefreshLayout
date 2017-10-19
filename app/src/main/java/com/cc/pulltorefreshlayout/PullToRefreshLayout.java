package com.cc.pulltorefreshlayout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * Created by YnanChao on 2017/8/30.
 * 下拉刷新布局，使用方法类似SwipeRefreshLayout
 * <p>
 * <br/>
 * 添加headView的3种方法(建议不要混合使用)：<ol><li>使用自定义属性： app:ptr_head_layout="@layout/pull_down_header",此时布局中第一个子View将是内容。</li>
 * <li> 直接将下拉头部View写在布局中，和FrameLayout一样的布局方式。注意，布局中第一个子View为下拉头，第二个为内容</li>
 * <li>在布局中直接指定headView和内容：分别将需要设置成headView和内容布局的tag设置为"headView"和"targetView"即可</li>
 * </ol>
 * <li>设置下拉刷新监听：{@link #setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener)}</li>
 * <li>展示、隐藏头部刷新布局{@link #setRefreshing(boolean)}</li>
 * <li>禁用下拉{@link #setEnabled(boolean)}</li>
 * </p>
 */

public class PullToRefreshLayout extends FrameLayout {

    private View mTargetView;
    private View mHeadView;
    private float mInitialMotionY;
    private static final float DRAG_RATE = .5f;
    private boolean mIsBeingDragged;
    private boolean mIsChildDraggedWhenRefreshing;
    private boolean mReturningToStart;
    private float mToRefreshingOffset = getResources().getDisplayMetrics().heightPixels;//回弹到正在刷新状态的距离
    private OnChildScrollDownCallBack mScrollDownCallBack;
    private int mState = STATE_NORMAL;
    int ANIMATION_DURATION = 300;
    private int mInitialPointId;
    private int mTotalConsumed;
    private boolean mIsAnimationEnd = true;
    private final int mVerticalOffset = 50;

    public PullToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
            int headViewLayout = typedArray.getResourceId(R.styleable.PullToRefreshLayout_ptr_head_layout, -1);
            if (headViewLayout != -1) {
                mHeadView = LayoutInflater.from(context).inflate(headViewLayout, this, false);
                addView(mHeadView);
            }
            typedArray.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureTargetView();
        post(new Runnable() {
            @Override
            public void run() {
                if (mHeadView != null)
                    mHeadView.setVisibility(INVISIBLE);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //当不可以下拉时，不拦截事件。
        if (!isEnabled() || mReturningToStart || mState == STATE_REFRESHING || !canPullDown()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialMotionY = event.getY();
                mInitialPointId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                int index = event.findPointerIndex(mInitialPointId);
                if (index < 0) {
                    return false;
                }
                mIsBeingDragged = event.getY() - mInitialMotionY > mVerticalOffset || event.getY() == mInitialMotionY;
                break;
            case MotionEvent.ACTION_UP:
                mInitialMotionY = 0;
                mIsBeingDragged = false;
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || mReturningToStart || mState == STATE_REFRESHING || !canPullDown()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialMotionY = event.getY();
                mInitialPointId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                int index = event.findPointerIndex(mInitialPointId);
                if (index < 0) {
                    return false;
                }
                float dy = event.getY(mInitialPointId) - mInitialMotionY;
                if (dy >= mVerticalOffset) {
                    float overScrollTop = (dy - mVerticalOffset) * DRAG_RATE;
                    startPullDown(overScrollTop < 0 ? 0 : overScrollTop);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged && mIsAnimationEnd) {
                    onActionUp();
                }
                mIsBeingDragged = false;
                break;
        }

        return true;
    }

    /**
     * 执行下拉动作。
     *
     * @param overScrollTop
     */
    private void startPullDown(float overScrollTop) {
        if (mIsAnimationEnd && overScrollTop >= 0 && !mIsChildDraggedWhenRefreshing) {
            mIsBeingDragged = true;

            int state = STATE_NORMAL;
            if (overScrollTop >= getReadyToRefreshOffset()) {
                state = STATE_READY_TO_REFRESHING;
            }
            if (mHeadView != null && mHeadView instanceof OnPullStateChangedListener && state != mState) {
                ((OnPullStateChangedListener) mHeadView).onPullStateChanged(state);
            }
            if (state != mState) {
                mState = state;
            }
            setTranslationY(overScrollTop);
        }
    }

    private float getReadyToRefreshOffset() {
        if (mHeadView != null && mHeadView instanceof OnPullStateChangedListener) {
            return ((OnPullStateChangedListener) mHeadView).getReadyToRefreshOffset();
        }
        return mToRefreshingOffset;
    }

    @Override
    public void setTranslationY(float translationY) {
        if (translationY > mToRefreshingOffset) {
            translationY = mToRefreshingOffset;
        }
        if (mHeadView != null && mTargetView != null) {
            mHeadView.setVisibility(VISIBLE);
            mTargetView.setTranslationY(translationY);
            if (mHeadView instanceof OnPullStateChangedListener) {
                ((OnPullStateChangedListener) mHeadView).onPullDown(translationY);
            }
            return;
        }
        super.setTranslationY(translationY);
    }

    @Override
    public float getTranslationY() {
        if (mTargetView != null)
            return mTargetView.getTranslationY();
        return super.getTranslationY();
    }

    public boolean isRefreshing() {
        return mState == STATE_REFRESHING;
    }

    private void onActionUp() {
        if (mOnRefreshListener != null && !isRefreshing()) {
            if (mState == STATE_READY_TO_REFRESHING) {
                mState = STATE_REFRESHING;
                if (mHeadView instanceof OnPullStateChangedListener) {
                    ((OnPullStateChangedListener) mHeadView).onPullStateChanged(mState);
                }
                if (mOnRefreshListener != null) {
                    mHeadView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mOnRefreshListener.onRefresh();
                        }
                    }, ANIMATION_DURATION);
                }
            }
        }
        mReturningToStart = !isRefreshing();
        if (isRefreshing()) {
            dealAnimation(getTranslationY(), getReadyToRefreshOffset());
        } else {
            dealAnimation(getTranslationY(), 0);
        }
    }

    private void dealAnimation(float... value) {
        ValueAnimator animator = ValueAnimator.ofFloat(value);
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                setTranslationY(value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (mAnimatorListener != null) {
                    mAnimatorListener.onAnimationStart(animator);
                }
                mIsAnimationEnd = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mReturningToStart)
                    mReturningToStart = false;
                if (getTranslationY() == 0) {
                    mIsAnimationEnd = true;
                    if (mHeadView != null)
                        mHeadView.setVisibility(INVISIBLE);
                    if (mAnimatorListener != null)
                        mAnimatorListener.onAnimationEnd(animator);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    /**
     * 展示、隐藏头部刷新布局
     *
     * @param isRefreshing
     */
    public void setRefreshing(boolean isRefreshing) {
        int state = isRefreshing ? STATE_REFRESHING : STATE_NORMAL;
        if (state == mState)
            return;
        else
            mState = state;
        if (isRefreshing)
            dealAnimation(0, getReadyToRefreshOffset());
        else
            dealAnimation(getReadyToRefreshOffset(), 0);

        if (mHeadView instanceof OnPullStateChangedListener) {
            ((OnPullStateChangedListener) mHeadView).onPullStateChanged(mState);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeadView != null) {
            measureChild(mHeadView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeadView != null) {
            mToRefreshingOffset = mHeadView.getMeasuredHeight();
        }
    }

    private void ensureTargetView() {
        mTargetView = findViewWithTag("targetView");
        mHeadView = findViewWithTag("headView");
        if (getChildCount() == 1) {
            if (mTargetView == null)
                mTargetView = getChildAt(0);
        } else if (getChildCount() > 1) {
            if (mHeadView == null) {
                mHeadView = getChildAt(0);
            }
            if (mTargetView == null)
                mTargetView = getChildAt(1);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        mTotalConsumed = 0;
        if (isRefreshing() && !mIsAnimationEnd)
            mIsChildDraggedWhenRefreshing = true;
        return true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (canPullDown() && dy < 0 || (mIsBeingDragged && dy >= 0)) {

            mTotalConsumed += dy;
            float overScrollTop = -mTotalConsumed;
            if (overScrollTop >= 0)
                consumed[1] = dy;
            else
                overScrollTop = 0;
            if (!isRefreshing())
                startPullDown(overScrollTop * DRAG_RATE);
        }

        if (dy > 0) {
            if (getTranslationY() == getReadyToRefreshOffset()) {
                mState = STATE_REFRESHING;
                setRefreshing(false);
            }
        }
    }

    boolean mIsBeingNestedScrolling;

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        mIsBeingNestedScrolling = true;
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        if (mIsBeingNestedScrolling && Math.abs(mTotalConsumed) > 0) {
            onActionUp();
        }
        mIsBeingDragged = false;
        if (!isRefreshing() && mIsAnimationEnd)
            mIsChildDraggedWhenRefreshing = false;
        mIsBeingNestedScrolling = false;
        mTotalConsumed = 0;
    }

    /**
     * 判断是否可以下滑
     *
     * @return
     */
    public boolean canPullDown() {
        if (mScrollDownCallBack != null) {
            return mScrollDownCallBack.canChildScrollUp(this, mTargetView);
        }
        return mTargetView != null && mTargetView.getMeasuredHeight() != 0 && !mTargetView.canScrollVertically(-1);
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener;

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    public interface OnChildScrollDownCallBack {
        /**
         * 自定义触发下拉条件
         *
         * @param viewGroup
         * @param targetView
         * @return
         */
        boolean canChildScrollUp(ViewGroup viewGroup, View targetView);
    }

    public void setScrollDownCallBack(OnChildScrollDownCallBack scrollDownCallBack) {
        this.mScrollDownCallBack = scrollDownCallBack;
    }

    /**
     * 正常状态
     */
    public static final int STATE_NORMAL = 0;

    /**
     * 松开即可刷新
     */
    public static final int STATE_READY_TO_REFRESHING = 1;

    /**
     * 正在刷新
     */
    public static final int STATE_REFRESHING = 2;

    public interface OnPullStateChangedListener {
        /**
         * 下拉状态改变
         * <li>{@link #STATE_NORMAL}</li>
         * <li>{@link #STATE_READY_TO_REFRESHING}</li>
         * <li>{@link #STATE_REFRESHING}</li>
         *
         * @param state
         */
        void onPullStateChanged(int state);

        /**
         * 设置松开刷新需要的距离
         *
         * @return
         */
        float getReadyToRefreshOffset();

        /**
         * 滑动的距离
         *
         * @param overScrollTop
         * @return
         */
        void onPullDown(float overScrollTop);
    }

    /**
     * 动画监听
     */
    private Animator.AnimatorListener mAnimatorListener;

    public void setAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.mAnimatorListener = animatorListener;
    }
}
