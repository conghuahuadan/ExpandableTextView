package com.chhd.expandabletextview.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.DynamicLayout;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.chhd.expandabletextview.ExpandableTextView;

public class MyETV extends ExpandableTextView {

    private static final String TAG = MyETV.class.getSimpleName();

    TextView tvHeader;
    int tvHeaderWidth;
    int initMaxLines;

    public MyETV(Context context) {
        super(context);
    }

    public MyETV(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyETV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTvHeader(TextView tvHeader, int tvHeaderWidth) {
        this.tvHeader = tvHeader;
        this.tvHeaderWidth = tvHeaderWidth;
    }

    @Override
    protected void initAttr(Context context, AttributeSet attrs) {
        super.initAttr(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, com.chhd.expandabletextview.R.styleable.ExpandableTextView);
        initMaxLines = a.getInteger(com.chhd.expandabletextview.R.styleable.ExpandableTextView_etv_MaxLinesOnShrink,
                MAX_LINES_ON_SHRINK);
    }

    @Override
    public void setText(final CharSequence text, final int currState, final int viewWidth) {
        this.mCurrState = currState;
        this.mOrigText = text;
        this.mFutureTextViewWidth = viewWidth;

        if ((viewWidth <= 0 && (getWidth() == 0 || getMeasuredWidth() == 0)) || (tvHeaderWidth <= 0 && tvHeader != null && tvHeader.getWidth() == 0)) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setText(text, currState, viewWidth);
                }
            }, 5);
        } else {
            int line = initMaxLines;
            if (tvHeader != null && (tvHeaderWidth > 0 || tvHeader.getWidth() > 0)) {
                DynamicLayout mLayout = new DynamicLayout(tvHeader.getText(),
                        tvHeader.getPaint(), tvHeaderWidth > 0 ? tvHeaderWidth : tvHeader.getWidth(),
                        Layout.Alignment.ALIGN_NORMAL, tvHeader.getLineSpacingMultiplier(),
                        tvHeader.getLineSpacingExtra(), tvHeader.getIncludeFontPadding());
                line = initMaxLines - mLayout.getLineCount();
            }
            line = line <= 0 ? 1 : line;
            this.mMaxLinesOnShrink = line;
            Log.i(TAG, "setText: " + tvHeaderWidth + ", " + mMaxLinesOnShrink);
            setText(getNewTextByConfig());
        }
    }
}
