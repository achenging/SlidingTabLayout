/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.achenging.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p/>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this layout is being used for.
 * <p/>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p/>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 */

/**
 * 修改来自google提供的sldingTabLayout 来适配需求
 */

public class SlidingTabLayout extends HorizontalScrollView {

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

        int getSelectedColor(int position);

        int getUnselectedTitleColors(int position);

    }


    private static final int TITLE_OFFSET_DIPS     = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 14;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 14;

    private static final int TAB_PDDING = 5;

    private static final int  DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 1;
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA    = 0x00;
    private static final int  SELECTED_INDICATOR_THICKNESS_DIPS    = 2;
    private static final int  DEFAULT_SELECTED_INDICATOR_COLOR     = 0xFF999999;
    private static final int  DEFAULT_DIVIDER_THICKNESS_DIPS       = 1;
    private static final byte DEFAULT_DIVIDER_COLOR_ALPHA          = 0x20;
    public static final  int  DEFAULT_TEXT_PADDING                 = 10;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;

    private int     mTabSelectedTextColor;
    private int     mTabTextColor;
    private int     mTextPadding;
    private boolean mAvgSplit;

    private ViewPager                      mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private final SlidingTabStrip mTabStrip;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mTabStrip = new SlidingTabStrip(context);
        int defaultBackgroundColor = mTabStrip.getSolidColor();


        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(true);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);
        //获取颜色
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);

        //选中与未选中的颜色
        mTabSelectedTextColor = typedArray.getColor(R.styleable.SlidingTabLayout_tabSelectedTextColor, 0);
        mTabTextColor = typedArray.getColor(R.styleable.SlidingTabLayout_tabTextColor, 0);

        //选中下划线的粗细
        int selectedIndicatorThickness = (int) typedArray.getDimension(R.styleable.SlidingTabLayout_tabIndicatorThickness, SELECTED_INDICATOR_THICKNESS_DIPS);
        //选中的下划线的颜色
        int tabIndicatorColor = typedArray.getColor(R.styleable.SlidingTabLayout_tabIndicatorColor, DEFAULT_SELECTED_INDICATOR_COLOR);

        //tab之间的中间分割线的粗细
        int tabDividerThickness = (int) typedArray.getDimension(R.styleable.SlidingTabLayout_dividerThickness, DEFAULT_DIVIDER_THICKNESS_DIPS);
        //tab之间的中间分割线颜色
        int tabDividerColor = typedArray.getColor(R.styleable.SlidingTabLayout_dividerColor, defaultBackgroundColor);

        //tab底部的下划线的背景后面的粗细与颜色
        int tabBottomBorderThickness = (int) typedArray.getDimension(R.styleable.SlidingTabLayout_bottomBorderThickness, DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS);
        int tabBottomBorderColor = typedArray.getColor(R.styleable.SlidingTabLayout_bottomBorderColor, defaultBackgroundColor);

        //是否显示tab中间的分割线，这里默认不显示
        boolean showAsDivider = typedArray.getBoolean(R.styleable.SlidingTabLayout_showAsDivider, false);

        typedArray.recycle();
        //


//        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTextPadding = (int) (DEFAULT_TEXT_PADDING * getResources().getDisplayMetrics().density);

        //设置颜色
        setSelectedTitleColors(mTabSelectedTextColor);
        setUnSelectedTitleColors(mTabTextColor);
        setDividerColors(tabDividerColor);
        setSelectedIndicatorColors(tabIndicatorColor);
        setTabBottomBorderColor(tabBottomBorderColor);
        setTabDividerColor(tabDividerColor);
        //设置粗细
        setSelectedIndicatorThickness(selectedIndicatorThickness);
        setTabBottomBorderThickness(tabBottomBorderThickness);
        setTabDividerThickness(tabDividerThickness);
        setShowAsDivider(showAsDivider);
        setShowTabDivider(false);

        LinearLayout.LayoutParams params
                = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        addView(mTabStrip);
    }


    public void setTabBottomBorderColor(int defaultBottomBorderColor) {
        mTabStrip.setBottomBorderColor(defaultBottomBorderColor);
    }


    public void setTabBottomBorderThickness(int bottomBorderThickness) {
        mTabStrip.setBottomBorderThickness(bottomBorderThickness);
    }

    public void setShowAsDivider(boolean showAsDivider) {
        mTabStrip.setShowAsDivider(showAsDivider);
    }


    public void setSelectedIndicatorThickness(int selectedIndicatorThickness) {
        mTabStrip.setSelectedIndicatorThickness(selectedIndicatorThickness);
    }

    public void setTabDividerThickness(float tabDividerThickness) {
        mTabStrip.setTabDividerThickness(tabDividerThickness);
    }

    public void setTabDividerColor(int tabDividerColor) {
        mTabStrip.setTabDividerColor(tabDividerColor);
    }


    /**
     * Set the custom {@link TabColorizer} to be used.
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        mTabStrip.setDividerColors(colors);
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    public void setSelectedTitleColors(int... colors) {
        mTabStrip.setSelectedTitleColors(colors);
    }

    public void setUnSelectedTitleColors(int... colors) {
        mTabStrip.setUnselectedTitleColors(colors);
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();
        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    public void setViewPager(ViewPager viewPager, boolean avgSplit) {
        mAvgSplit = avgSplit;
        setViewPager(viewPager);
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        AppCompatTextView textView = new AppCompatTextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.WHITE);
        textView.setPadding(mTextPadding, mTextPadding, mTextPadding, mTextPadding);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);

        if (mAvgSplit) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            textView.setWidth(screenWidth / mViewPager.getAdapter().getCount());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                    outValue, true);
            textView.setBackgroundResource(outValue.resourceId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
            textView.setAllCaps(true);
        }


        return textView;
    }

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();

        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView = null;
            TextView tabTitleView = null;

            if (mTabViewLayoutId != 0) {
                // If there is a custom tab view layout id set, try and inflate it
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip,
                        false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
            }

            if (tabView == null) {
                tabView = createDefaultTabView(getContext());
            }

            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }

            tabTitleView.setText(adapter.getPageTitle(i));
            tabView.setOnClickListener(tabClickListener);
            if (i == mViewPager.getCurrentItem()) {
                tabTitleView.setTextColor(mTabSelectedTextColor);
            } else {
                tabTitleView.setTextColor(mTabTextColor);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mTabStrip.addView(tabView, params);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);


        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }
            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mCurrentScrollState;
        private int mPrevScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }


            mTabStrip.onViewPagerPageChanged(position, positionOffset);
            if (mCurrentScrollState == ViewPager.SCROLL_STATE_DRAGGING ||
                    (mPrevScrollState == ViewPager.SCROLL_STATE_DRAGGING &&
                            mCurrentScrollState == ViewPager.SCROLL_STATE_SETTLING)) {
                TextView textView = (TextView) mTabStrip.getChildAt(position);
                TextView nextTextView = (TextView) mTabStrip.getChildAt(position + 1);
                if (textView != null && nextTextView != null) {
                    if (positionOffset <= 0.5) {
                        nextTextView.setTextColor(mTabTextColor);
                        textView.setTextColor(mTabSelectedTextColor);
                    } else {
                        nextTextView.setTextColor(mTabSelectedTextColor);
                        textView.setTextColor(mTabTextColor);
                    }
                }

            }

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mPrevScrollState = mCurrentScrollState;
            mCurrentScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mCurrentScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }

            int count = mTabStrip.getChildCount();
            for (int i = 0; i < count; i++) {
                if (position == i) {
                    TextView tabTitleView = (TextView) mTabStrip.getChildAt(position);
                    tabTitleView.setTextColor(mTabSelectedTextColor);
                } else {
                    ((TextView) mTabStrip.getChildAt(i)).setTextColor(mTabTextColor);
                }
            }


            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

    public void setShowTabDivider(boolean isShow) {
        if (isShow) return;
        mTabStrip.setShowDividers(SlidingTabStrip.SHOW_DIVIDER_NONE);
        Log.d("Tab Divider No Show", ">>>>>>>>>>>>>>>>");
    }

    public void setTabStripDividerColor(int color) {
        mTabStrip.setDividerColors(color);
    }


}
