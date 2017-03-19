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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


class SlidingTabStrip extends LinearLayout {

    private static final int  DEFAULT_ERROR_DIPS                   = 1;
    private static final int  DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 1;
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA    = 0x00;
    private static final int  SELECTED_INDICATOR_THICKNESS_DIPS    = 2;
    private static final int  DEFAULT_SELECTED_INDICATOR_COLOR     = 0xFF999999;

    private static final int   DEFAULT_DIVIDER_THICKNESS_DIPS = 1;
    private static final byte  DEFAULT_DIVIDER_COLOR_ALPHA    = 0x20;
    //    private static final int   DEFAULT_DIVIDER_COLOR          = 0xFF999999;
    private static final float DEFAULT_DIVIDER_HEIGHT         = 0.5f;

    private       int   mBottomBorderThickness;
    private final Paint mBottomBorderPaint;

    private       int   mSelectedIndicatorThickness;
    private final Paint mSelectedIndicatorPaint;
    private       Paint mTextPaint;

    private float mTabDividerThickness;
    private int   mTabDividerColor;

    private int mBottomBorderColor;

    private boolean mShowAsDivider;

    private final Paint mDividerPaint;

    private int mLastPosition = -1;
    private int   mSelectedPosition;
    private float mSelectionOffset;

    private final int mErrorDips;

    private       SlidingTabLayout.TabColorizer mCustomTabColorizer;
    private final SimpleTabColorizer            mDefaultTabColorizer;

    SlidingTabStrip(Context context) {
        this(context, null);
    }

    SlidingTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);
        final int themeForegroundColor = outValue.data;

        mBottomBorderColor = setColorAlpha(themeForegroundColor,
                DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);
        mDefaultTabColorizer = new SimpleTabColorizer();
        mBottomBorderPaint = new Paint();
        mSelectedIndicatorPaint = new Paint();
        mDividerPaint = new Paint();
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);

        mErrorDips = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ERROR_DIPS, getResources().getDisplayMetrics());
    }

    public void setBottomBorderColor(int bottomBorderColor) {
        mBottomBorderColor = bottomBorderColor;
        mBottomBorderPaint.setColor(mBottomBorderColor);
    }


    public void setBottomBorderThickness(int bottomBorderThickness) {
        final float density = getResources().getDisplayMetrics().density;
        mBottomBorderThickness = (int) (bottomBorderThickness * density);
    }

    public void setShowAsDivider(boolean showAsDivider) {
        mShowAsDivider = showAsDivider;
    }


    public void setSelectedIndicatorThickness(int selectedIndicatorThickness) {
        mSelectedIndicatorThickness = (int) (selectedIndicatorThickness);
    }

    public void setTabDividerThickness(float tabDividerThickness) {
        mTabDividerThickness = tabDividerThickness;
        mDividerPaint.setStrokeWidth((int) tabDividerThickness);
    }

    public void setTabDividerColor(int tabDividerColor) {
        mTabDividerColor = tabDividerColor;
    }


    void setCustomTabColorizer(SlidingTabLayout.TabColorizer customTabColorizer) {
        mCustomTabColorizer = customTabColorizer;
        invalidate();
    }

    void setSelectedIndicatorColors(int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setIndicatorColors(colors);
        invalidate();
    }

    void setDividerColors(int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setDividerColors(colors);

        invalidate();
    }

    void setSelectedTitleColors(int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setSelectedTitleColors(colors);
        invalidate();
    }

    void setUnselectedTitleColors(int[] colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setUnselectedTitleColors(colors);
        invalidate();
    }


    void onViewPagerPageChanged(int position, float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();
        final int dividerHeightPx = (int) (Math.min(Math.max(0f, DEFAULT_DIVIDER_HEIGHT), 1f) * height);
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;
        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);

            TextView textView = (TextView) selectedTitle;
            mTextPaint.setTextSize(textView.getTextSize());
            int textWidth = (int) mTextPaint.measureText(textView.getText().toString());
            int textViewWidth = textView.getRight() - textView.getLeft();
            int left = textView.getLeft() + (textViewWidth - textWidth) / 2;
            int right = left + textWidth;
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);

            if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
                int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
                if (color != nextColor) {
                    color = blendColors(nextColor, color, mSelectionOffset);
                }

                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mSelectedPosition + 1);
                TextView nextTextView = (TextView) nextTitle;
                int nextTextWidth = (int) mTextPaint.measureText(textView.getText().toString());

                int nextTextViewWidth = nextTextView.getRight() - nextTextView.getLeft();
                int nextLeft = (nextTextViewWidth - nextTextWidth) / 2 + nextTextView.getLeft();
                int nextRight = nextLeft + nextTextWidth;


                left = (int) (mSelectionOffset * nextLeft +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextRight +
                        (1.0f - mSelectionOffset) * right);
            }


            mSelectedIndicatorPaint.setColor(color);
            canvas.drawRect(left - mErrorDips,
                    height - mSelectedIndicatorThickness - mBottomBorderThickness,
                    right - mErrorDips,
                    height,
                    mSelectedIndicatorPaint);


        }

        // Thin underline along the entire bottom edge
        canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);

        if (!mShowAsDivider) return;
        // Vertical separators between the titles
        int separatorTop = (height - dividerHeightPx) / 2;
        for (int i = 0; i < childCount - 1; i++) {
            View child = getChildAt(i);
            mDividerPaint.setColor(tabColorizer.getDividerColor(i));
            canvas.drawLine(child.getRight(), separatorTop, child.getRight(),
                    separatorTop + dividerHeightPx, mDividerPaint);
        }
    }

    /**
     * Set the alpha value of the {@code color} to be the given {@code alpha} value.
     */
    private static int setColorAlpha(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
     *              0.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }


    private static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
        private int[] mIndicatorColors;
        private int[] mDividerColors;
        private int[] mTitleSelectedColors;
        private int[] mTitleUnselectedTitleColors;


        @Override
        public final int getIndicatorColor(int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        @Override
        public final int getDividerColor(int position) {
            return mDividerColors[position % mDividerColors.length];
        }

        @Override
        public int getSelectedColor(int position) {
            return mTitleSelectedColors[position % mTitleSelectedColors.length];
        }

        @Override
        public int getUnselectedTitleColors(int position) {
            return mTitleUnselectedTitleColors[position % mTitleUnselectedTitleColors.length];
        }


        void setIndicatorColors(int... colors) {
            mIndicatorColors = colors;
        }

        void setDividerColors(int... colors) {
            mDividerColors = colors;
        }

        void setSelectedTitleColors(int... colors) {
            mTitleSelectedColors = colors;
        }

        void setUnselectedTitleColors(int... colors) {
            mTitleUnselectedTitleColors = colors;
        }
    }
}