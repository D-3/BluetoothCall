package com.broadsense.newpine.bluetooth.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
    private boolean isCanScroll = true; //是否可以切换页面
    private boolean isCanTouch = false; //是否可以触摸

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public void setScanTouch(boolean isCanScroll) {
        this.isCanTouch = isCanScroll;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isCanScroll) {
            super.scrollTo(x, y);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        //return super.onTouchEvent(arg0);
        return isCanTouch && super.onTouchEvent(arg0);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item,false);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        //return super.onInterceptTouchEvent(arg0);
        return isCanTouch && super.onInterceptTouchEvent(arg0);
    }

}
