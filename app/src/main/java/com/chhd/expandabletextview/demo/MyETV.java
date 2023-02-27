package com.chhd.expandabletextview.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chhd.expandabletextview.AlignImageSpan;
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

//    @Override
//    public CharSequence getNewTextByConfig() {
////        try {
////            if (mOrigText != null) {
////                JChineseConvertor jChineseConvertor = JChineseConvertor.getInstance();
////                mOrigText = jChineseConvertor.s2t(mOrigText.toString());
////                Log.i(TAG, "getNewTextByConfig: " + mOrigText);
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//        return super.getNewTextByConfig();
//    }

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

    public CharSequence getNewTextByConfig() {
        if (TextUtils.isEmpty(mOrigText)) {
            return mOrigText;
        }

        mLayout = getLayout();
        if (mLayout != null) {
            mLayoutWidth = mLayout.getWidth();
        }

        if (mLayoutWidth <= 0) {
            if (getWidth() == 0) {
                if (mFutureTextViewWidth == 0) {
                    return mOrigText;
                } else {
                    mLayoutWidth = mFutureTextViewWidth - getPaddingLeft() - getPaddingRight();
                }
            } else {
                mLayoutWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            }
        }

        mTextPaint = getPaint();

        mTextLineCount = -1;
//        Log.i(TAG, "getNewTextByConfig: " + mCurrState);
        switch (mCurrState) {
            case STATE_SHRINK: {
                mLayout = new DynamicLayout(mOrigText, mTextPaint, mLayoutWidth,
                        Layout.Alignment.ALIGN_NORMAL,
                        getLineSpacingMultiplier(), getLineSpacingExtra(), getIncludeFontPadding());
                mTextLineCount = mLayout.getLineCount();
                if (mTextLineCount <= mMaxLinesOnShrink) {
                    mCurrState = STATE_EXPAND;
                    return mOrigText;
                }
                // 计算MaxLines行文本末端的下标
                int indexEnd = getValidLayout().getLineEnd(mMaxLinesOnShrink - 1);
                // 计算MaxLines行文本开端的下标
                int indexStart = getValidLayout().getLineStart(mMaxLinesOnShrink - 1);

                // 被修剪过的文本末端下标
                int indexEndTrimmed = indexEnd
                        - getLengthOfString(mEllipsisHint)
                        - (mShowToExpandHint ? getLengthOfString(mToExpandHint)
                        + getLengthOfString(mGapToExpandHint) : 0);
                if (indexEndTrimmed <= indexStart) {
                    indexEndTrimmed = indexEnd;
                }

                // 剩余宽度
                int remainWidth = getValidLayout().getWidth() -
                        (int) (mTextPaint.measureText(mOrigText.subSequence(indexStart,
                                indexEndTrimmed).toString()) + 0.5);

                // 将被替换的宽度
                float widthTailReplaced;
                if (TextUtils.isEmpty(mToExpandHint)) {
                    widthTailReplaced = mTextPaint.measureText(getContentOfString(mEllipsisHint));
                } else {
                    widthTailReplaced = mTextPaint.measureText(getContentOfString(mEllipsisHint)
                            + (mShowToExpandHint ? (getContentOfString(mToExpandHint)
                            + getContentOfString(mGapToExpandHint)) : ""));
                }
                if (mToExpandIcon != null) {
                    mToExpandIcon.setBounds(0, 0,
                            mToExpandIcon.getIntrinsicWidth(), mToExpandIcon.getIntrinsicHeight());
                    widthTailReplaced = widthTailReplaced + mToExpandIcon.getIntrinsicWidth();
                }

                int indexEndTrimmedRevised = indexEndTrimmed;
                // 剩余宽度 > 将被替换的宽度
                if (remainWidth > widthTailReplaced) {
                    String endStr = mOrigText.subSequence(indexEndTrimmed - 1, indexEndTrimmed).toString();
                    if ("\n".endsWith(endStr)) {

                    } else { // 应用场景？感觉可以注释
                        int extraOffset = 0;
                        int extraWidth = 0;
                        while (remainWidth > widthTailReplaced + extraWidth) {
                            extraOffset++;
                            if (indexEndTrimmed + extraOffset <= mOrigText.length()) {
                                String tempText = mOrigText.subSequence(
                                        indexEndTrimmed,
                                        indexEndTrimmed + extraOffset).toString();
                                if (tempText.endsWith("\n")) {
                                    break;
                                }
                                extraWidth = (int) (mTextPaint.measureText(tempText) + 0.5);
                            } else {
                                break;
                            }
                        }
                        // 逐渐增加 被修剪过的文本末端下标
                        indexEndTrimmedRevised += extraOffset - 1;
                    }
                } else {
                    int extraOffset = 0;
                    int extraWidth = 0;
                    while (remainWidth + extraWidth < widthTailReplaced) {
                        extraOffset--;
                        if (indexEndTrimmed + extraOffset > indexStart) {
                            extraWidth = (int) (mTextPaint.measureText(
                                    mOrigText.subSequence(indexEndTrimmed + extraOffset,
                                            indexEndTrimmed).toString()) + 0.5);
                        } else {
                            break;
                        }
                    }
                    indexEndTrimmedRevised += extraOffset;
                }

                CharSequence fixText = removeEndLineBreak(mOrigText.subSequence(0,
                        indexEndTrimmedRevised));
                SpannableStringBuilder ssbShrink = new SpannableStringBuilder(fixText)
                        .append(mEllipsisHint);
                if (mToEllipsisHintColor != -1) {
                    ssbShrink.setSpan(new ForegroundColorSpan(mToEllipsisHintColor), ssbShrink.length()
                            - getLengthOfString(mEllipsisHint), ssbShrink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (mShowToExpandHint) {
                    int offset = 0;
                    if (TextUtils.isEmpty(mToExpandHint)) {

                    } else {
                        ssbShrink.append(getContentOfString(mGapToExpandHint)
                                + getContentOfString(mToExpandHint));
                        offset += getLengthOfString(mToExpandHint);
                    }
                    if (mToExpandIcon != null) {
                        ssbShrink.append(" ");
                        AlignImageSpan sp = new AlignImageSpan(mToExpandIcon,
                                mExpandIconVerticalAlign);
                        ssbShrink.setSpan(sp, ssbShrink.length() - 1, ssbShrink.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        offset += 1;
                    }
                    ssbShrink.setSpan(new TouchableSpan(mToExpandHintColor) {
                                          @Override
                                          public void onClick(View widget) {
                                              super.onClick(widget);
                                              mIsToggleTrigger = true;
                                              if (!mIsLongClickTrigger && mOnChildClickListener != null) {
                                                  mOnChildClickListener.onExpandClick(MyETV.this, mCurrState);
                                              }
                                          }
                                      }, ssbShrink.length() - offset, ssbShrink.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return ssbShrink;
            }
            case STATE_EXPAND: {
                if (!mShowToShrinkHint) {
                    return mOrigText;
                }
                mLayout = new DynamicLayout(mOrigText, mTextPaint, mLayoutWidth,
                        Layout.Alignment.ALIGN_NORMAL,
                        getLineSpacingMultiplier(), getLineSpacingExtra(), getIncludeFontPadding());
                mTextLineCount = mLayout.getLineCount();

                if (mTextLineCount <= mMaxLinesOnShrink) {
                    return mOrigText;
                }

                SpannableStringBuilder ssbExpand = new SpannableStringBuilder(mOrigText);
                int offset = 0;
                if (TextUtils.isEmpty(mToShrinkHint)) {

                } else {
                    ssbExpand.append(mGapToShrinkHint).append(mToShrinkHint);
                    offset += getLengthOfString(mToShrinkHint);
                }
                if (mToShrinkIcon != null) {
                    ssbExpand.append(" ");
                    mToShrinkIcon.setBounds(0, 0,
                            mToShrinkIcon.getIntrinsicWidth(), mToShrinkIcon.getIntrinsicHeight());
                    AlignImageSpan sp = new AlignImageSpan(mToShrinkIcon, mShrinkIconVerticalAlign);
                    ssbExpand.setSpan(sp, ssbExpand.length() - 1, ssbExpand.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    offset += 1;
                }
                ssbExpand.setSpan(new TouchableSpan(mToShrinkHintColor) {
                                      @Override
                                      public void onClick(View widget) {
                                          super.onClick(widget);
                                          mIsToggleTrigger = true;
                                          if (!mIsLongClickTrigger && mOnChildClickListener != null) {
                                              mOnChildClickListener.onShrinkClick(MyETV.this, mCurrState);
                                          }
                                      }
                                  }, ssbExpand.length() - offset, ssbExpand.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return ssbExpand;
            }
        }
        return mOrigText;
    }
}
