package com.udacity.nanodegree.nghianja.capstone.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.percent.PercentLayoutHelper;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * References:
 * [1] https://developer.android.com/reference/android/support/percent/PercentLayoutHelper.html?linkId=18501254
 * [2] https://android.googlesource.com/platform/frameworks/support/+/f9cabe2/percent/src/android/support/percent/PercentRelativeLayout.java
 * [3] https://github.com/JulienGenoud/android-percent-support-lib-sample/blob/master/app/src/main/java/com/juliengenoud/percentsamples/PercentLinearLayout.java
 * [4] http://stackoverflow.com/questions/37888749/how-to-use-percentrelativelayout-in-a-collapsingtoolbarlayout
 */
public class PercentCollapsingToolbarLayout extends CollapsingToolbarLayout {

    private final PercentLayoutHelper mHelper = new PercentLayoutHelper(this);

    public PercentCollapsingToolbarLayout(Context context) {
        super(context);
    }

    public PercentCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PercentCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHelper.adjustChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHelper.handleMeasuredStateTooSmall()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHelper.restoreOriginalParams();
    }

    public static class LayoutParams extends CollapsingToolbarLayout.LayoutParams
            implements PercentLayoutHelper.PercentLayoutParams {

        private PercentLayoutHelper.PercentLayoutInfo mPercentLayoutInfo;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            mPercentLayoutInfo = PercentLayoutHelper.getPercentLayoutInfo(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @Override
        public PercentLayoutHelper.PercentLayoutInfo getPercentLayoutInfo() {
            return mPercentLayoutInfo;
        }

        @Override
        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            PercentLayoutHelper.fetchWidthAndHeight(this, a, widthAttr, heightAttr);
        }
    }
}
