package com.example.frank.myverticalviewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 创建者     Frank
 * 创建时间   2016/6/9 16:55
 * 描述	      ${TODO}
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 * <p/>
 * <p/>
 * 这里之前用过一次getrawY(),获取到手指移动的距离，最后up的时候会出现bug,会留下一点缝隙，原因是因为mScorllEnd-mScorllStart的值太大了，
 * 之所以太大是因为这个取的是屏幕的值，包括状态栏，这个移动距离实际上是比画布移动的距离要稍微大一点的。可能是这个原因。
 * 所以(mScreenHeight-(mScorllEnd-mScorllStart)的值就变小了，
 * 所有要用getscrollY()来记录这些位置。
 * 手指在屏幕上移动多少距离，对应的改自定义控件就移动多少距离。也就是屏幕截取的部分往下移动。
 * <p/>
 * getScrollY获取的是当前的view的top的坐标值，就是这个view靠近屏幕状态栏的那根线。
 */
public class MyVerticleViewPager extends LinearLayout {


    private static final String TAG = "MyVerticleViewPager";
    private int mScreenHeight;

    private Scroller        mScroller;
    private VelocityTracker mVelocityTracker;
    private int             mChiledrencount;

    float lastY;

    private int     mScorllStart;
    private int     mScorllEnd;
    private boolean isScollring;


    //设置选中页面的监听

    public int currentPage=0;

    private OnPageChangeListener mOnPageChangeListener;

    public interface OnPageChangeListener {
        void onPageChange(int page);
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }


    public MyVerticleViewPager(Context context) {
        this(context, null);
    }

    public MyVerticleViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyVerticleViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();

        Display defaultDisplay = windowManager.getDefaultDisplay();

        defaultDisplay.getMetrics(displayMetrics);

        mScreenHeight = displayMetrics.heightPixels;

        mScroller = new Scroller(context);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        mChiledrencount = getChildCount();

        for (int i = 0; i < mChiledrencount; i++) {

            View childAt = getChildAt(i);
            measureChild(childAt, widthMeasureSpec, mScreenHeight);
        }


    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);


        if (changed) {

            MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
            params.height = mScreenHeight * mChiledrencount;
            setLayoutParams(params);

            for (int i = 0; i < mChiledrencount; i++) {

                View childAt = getChildAt(i);
                childAt.layout(l, i * mScreenHeight, r, (i + 1) * mScreenHeight);

            }
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isScollring) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        float dy;
        obtainVelocity(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastY = event.getY();
                mScorllStart = getScrollY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                dy = lastY - event.getY();

                /**
                 * dy>0手指往上滑，相对的图片往下显示图片，这个时候要判断最后一张图片是否滑到了底端，如果当前的scorllY加上将要移动
                 * 的dy距离超出，那么只移动scorllY的距离。
                 */

                int scorllY = getScrollY();
                if (dy > 0 && ((scorllY + dy) > (getHeight() - mScreenHeight))) {
                    dy = -(scorllY - ((getHeight() - mScreenHeight)));
                }

                if (dy < 0 && scorllY + dy < 0) {
                    dy = -scorllY;
                }
                scrollBy(0, (int) dy);
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mScorllEnd = getScrollY();

                if (wantScrollToNext()) {
                    if (shouldScrollToNext()) {
                        mScroller.startScroll(0, getScrollY(), 0, (mScreenHeight - (mScorllEnd - mScorllStart)));
                    } else {
                        mScroller.startScroll(0, getScrollY(), 0, (mScorllStart - mScorllEnd));
                    }
                }

                if (wantScrollToPre()) {
                    if (shouldeScrollToPre()) {
                        mScroller.startScroll(0, getScrollY(), 0, -(mScreenHeight - (mScorllStart - mScorllEnd)));
                    } else {
                        mScroller.startScroll(0, getScrollY(), 0, (mScorllStart - mScorllEnd));
                    }
                }

                isScollring = true;
                postInvalidate();
                recycleVelocity();
                break;
        }
        return true;
    }


    //手指往上滑动，这里必须用getscrollY，因为用gety获取的手指的坐标（这个坐标是相对于父容器的坐标，也就是本自定义控件的坐标）是不动的。
    public boolean wantScrollToNext() {
        return mScorllEnd > mScorllStart;
    }

    public boolean shouldScrollToNext() {

        if ((mScorllEnd - mScorllStart) > mScreenHeight / 2 || Math.abs(getVelocity()) > 600) {
            return true;
        }
        return false;
    }

    //手指往下移动，希望返回到上一个
    public boolean wantScrollToPre() {
        return mScorllEnd < mScorllStart;
    }


    public boolean shouldeScrollToPre() {
        if (mScorllStart - mScorllEnd > mScreenHeight / 2 || Math.abs(getVelocity()) > 600) {
            return true;
        }
        return false;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        } else {
            isScollring = false;

            int page = getScrollY() / mScreenHeight;

            if (page != currentPage) {
                if (mOnPageChangeListener != null) {
                    currentPage = page;
                    mOnPageChangeListener.onPageChange(currentPage);
                }
            }
        }
    }


    public void obtainVelocity(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    public void recycleVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    public int getVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        return (int) mVelocityTracker.getXVelocity();
    }


}
