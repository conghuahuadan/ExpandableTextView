package com.chhd.expandabletextview.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chhd.expandabletextview.ExpandableTextView;

import taobe.tec.jcc.JChineseConvertor;

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
//        Log.i(TAG, "setTvHeader: " + tvHeaderWidth);
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

        if ((viewWidth <= 0 && (getWidth() == 0 || getMeasuredWidth() == 0)) || (tvHeaderWidth <= 0 && tvHeader != null && tvHeader.getWidth() == 0 && tvHeader.getVisibility() == View.VISIBLE)) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setText(text, currState, viewWidth);
                }
            }, 5);
        } else {
            int line = initMaxLines;
            if (tvHeader != null && (tvHeaderWidth > 0 || tvHeader.getWidth() > 0) && tvHeader.getVisibility() == View.VISIBLE) {
                DynamicLayout mLayout = new DynamicLayout(tvHeader.getText(),
                        tvHeader.getPaint(), tvHeaderWidth > 0 ? tvHeaderWidth : tvHeader.getWidth(),
                        Layout.Alignment.ALIGN_NORMAL, tvHeader.getLineSpacingMultiplier(),
                        tvHeader.getLineSpacingExtra(), tvHeader.getIncludeFontPadding());
                line = initMaxLines - mLayout.getLineCount();
            }
            line = line <= 0 ? 1 : line;
            this.mMaxLinesOnShrink = line;
//            Log.i(TAG, "setText: " + tvHeaderWidth + ", " + mMaxLinesOnShrink + ", " + getWidth() + ", " + viewWidth);
            mFromNewText = true;
            setText(getNewTextByConfig());
        }
    }

    public void setToShrinkHint(String s) {
        mToShrinkHint = s;
    }

    @Override
    public CharSequence getNewTextByConfig() {
//        try {
//            if (mOrigText != null) {
//                JChineseConvertor jChineseConvertor = JChineseConvertor.getInstance();
//                mOrigText = jChineseConvertor.s2t(mOrigText.toString());
//                Log.i(TAG, "getNewTextByConfig: " + mOrigText);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return super.getNewTextByConfig();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            if (!TextUtils.isEmpty(text)) {
                String str = ((CharSequence) text).toString();
                JChineseConvertor jChineseConvertor = JChineseConvertor.getInstance();
                str = jChineseConvertor.s2t(str);
                if (text instanceof Spanned) {
                    Spanned spanned = (Spanned) text;
                    Object[] objects = spanned.getSpans(0, ((CharSequence) text).length(), Object.class);
                    if (objects != null) {
                        SpannableString ss = new SpannableString(str);
                        for (int i = 0; i < objects.length; ++i) {
                            int start = spanned.getSpanStart(objects[i]);
                            int end = spanned.getSpanEnd(objects[i]);
                            int flag = spanned.getSpanFlags(objects[i]);
                            ss.setSpan(objects[i], start, end, flag);
                        }

                        text = ss;
                    } else {
                        text = str;
                    }
                } else {
                    text = str;
                }

                Log.i(TAG, "setText: " + text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setText(text, type);
    }

}
