package com.chhd.expandabletextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * 改自 Carbs0126 / ExpandableTextView
 * <p>
 * · 新增展开图标、收起图标的属性
 * · 支持“\n”
 *
 * @author 陈伟强 (2019/7/29)
 */
public class ExpandableTextView extends android.support.v7.widget.AppCompatTextView
        implements View.OnClickListener, View.OnLongClickListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final int STATE_SHRINK = 0;
    public static final int STATE_EXPAND = 1;

    protected static final String CLASS_NAME_VIEW = "android.view.View";
    protected static final String CLASS_NAME_LISTENER_INFO = "android.view.View$ListenerInfo";
    protected static final String ELLIPSIS_HINT = "...";
    protected static final String GAP_TO_EXPAND_HINT = " ";
    protected static final String GAP_TO_SHRINK_HINT = " ";
    protected static final int MAX_LINES_ON_SHRINK = 7;
    protected static final int TO_ELLIPSIS_HINT_COLOR = -1;
    protected static final int TO_EXPAND_HINT_COLOR = 0xFF3498DB;
    protected static final int TO_SHRINK_HINT_COLOR = 0xFFE74C3C;
    protected static final boolean TOGGLE_ENABLE = true;
    protected static final boolean SHOW_TO_EXPAND_HINT = true;
    protected static final boolean SHOW_TO_SHRINK_HINT = true;

    protected String mEllipsisHint;                                               // 省略符号
    protected String mToExpandHint;                                               // 展开文本
    protected String mToShrinkHint;                                               // 收起文本
    protected Drawable mToExpandIcon;
    protected Drawable mToShrinkIcon;
    protected int mExpandIconVerticalAlign = AlignImageSpan.ALIGN_CENTER;
    protected int mShrinkIconVerticalAlign = AlignImageSpan.ALIGN_CENTER;
    protected String mGapToExpandHint = GAP_TO_EXPAND_HINT;                       // 左侧展开文本的间隔
    protected String mGapToShrinkHint = GAP_TO_SHRINK_HINT;                       // 左侧收起文本的间隔
    protected boolean mToggleEnable = TOGGLE_ENABLE;
    protected boolean mShowToExpandHint = SHOW_TO_EXPAND_HINT;                    // 显示展开文本
    protected boolean mShowToShrinkHint = SHOW_TO_SHRINK_HINT;                    // 显示收起文本
    protected int mMaxLinesOnShrink = MAX_LINES_ON_SHRINK;
    protected int mToEllipsisHintColor = TO_ELLIPSIS_HINT_COLOR;
    protected int mToExpandHintColor = TO_EXPAND_HINT_COLOR;
    protected int mToShrinkHintColor = TO_SHRINK_HINT_COLOR;
    protected int mCurrState = STATE_SHRINK;

    protected TouchableSpan mTouchableSpan;
    protected BufferType mBufferType = BufferType.NORMAL;
    protected TextPaint mTextPaint;
    protected Layout mLayout;
    protected int mTextLineCount = -1;
    protected int mLayoutWidth = 0;
    protected int mFutureTextViewWidth = 0;
    protected boolean mIsToggleTrigger;

    protected CharSequence mOrigText;
    protected boolean mFromNewText = false;

    protected OnExpandListener mOnExpandListener;
    protected OnChildClickListener mOnChildClickListener;

    public ExpandableTextView(Context context) {
        super(context);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    /* ----------------------------- ▼初始化▼ -----------------------------  */

    protected void initAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mMaxLinesOnShrink = a.getInteger(R.styleable.ExpandableTextView_etv_MaxLinesOnShrink,
                MAX_LINES_ON_SHRINK);
        mEllipsisHint = a.getString(R.styleable.ExpandableTextView_etv_EllipsisHint);
        mToExpandHint = a.getString(R.styleable.ExpandableTextView_etv_ToExpandHint);
        mToShrinkHint = a.getString(R.styleable.ExpandableTextView_etv_ToShrinkHint);
        mToggleEnable = a.getBoolean(R.styleable.ExpandableTextView_etv_EnableToggle,
                TOGGLE_ENABLE);
        mShowToExpandHint = a.getBoolean(R.styleable.ExpandableTextView_etv_ToExpandHintShow,
                SHOW_TO_EXPAND_HINT);
        mShowToShrinkHint = a.getBoolean(R.styleable.ExpandableTextView_etv_ToShrinkHintShow,
                SHOW_TO_SHRINK_HINT);
        mToEllipsisHintColor = a.getInteger(R.styleable.ExpandableTextView_etv_ToEllipsisHintColor,
                mToEllipsisHintColor);
        mToExpandHintColor = a.getInteger(R.styleable.ExpandableTextView_etv_ToExpandHintColor,
                TO_EXPAND_HINT_COLOR);
        mToShrinkHintColor = a.getInteger(R.styleable.ExpandableTextView_etv_ToShrinkHintColor,
                TO_SHRINK_HINT_COLOR);
        mCurrState = a.getInteger(R.styleable.ExpandableTextView_etv_InitState, STATE_SHRINK);
        mGapToExpandHint = a.getString(R.styleable.ExpandableTextView_etv_GapToExpandHint);
        mGapToShrinkHint = a.getString(R.styleable.ExpandableTextView_etv_GapToShrinkHint);
        mToExpandIcon = a.getDrawable(R.styleable.ExpandableTextView_etv_ToExpandIcon);
        mToShrinkIcon = a.getDrawable(R.styleable.ExpandableTextView_etv_ToShrinkIcon);
        mExpandIconVerticalAlign =
                a.getInteger(R.styleable.ExpandableTextView_etv_ExpandIconVerticalAlign,
                        AlignImageSpan.ALIGN_CENTER);
        mShrinkIconVerticalAlign =
                a.getInteger(R.styleable.ExpandableTextView_etv_ShrinkIconVerticalAlign,
                        AlignImageSpan.ALIGN_CENTER);
        a.recycle();
    }

    protected void init() {
        mMaxLinesOnShrink = mMaxLinesOnShrink <= 0 ? 1 : mMaxLinesOnShrink;

        mTouchableSpan = new TouchableSpan();
        if (TextUtils.isEmpty(mEllipsisHint)) {
            mEllipsisHint = ELLIPSIS_HINT;
        }
        if (mGapToExpandHint == null) {
            mGapToExpandHint = GAP_TO_EXPAND_HINT;
        }
        if (mGapToShrinkHint == null) {
            mGapToShrinkHint = GAP_TO_SHRINK_HINT;
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                setTextInternal(getNewTextByConfig(), mBufferType);
            }
        });

        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);

        setSuperOnClickListener(this);
        setSuperOnLongClickListener(this);
    }

    /* ----------------------------- ▼内部方法▼ -----------------------------  */

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
                                                  mOnChildClickListener.onExpandClick(ExpandableTextView.this, mCurrState);
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
                                              mOnChildClickListener.onShrinkClick(ExpandableTextView.this, mCurrState);
                                          }
                                      }
                                  }, ssbExpand.length() - offset, ssbExpand.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return ssbExpand;
            }
        }
        return mOrigText;
    }

    protected Layout getValidLayout() {
        return mLayout != null ? mLayout : getLayout();
    }

    public void toggle() {
        switch (mCurrState) {
            case STATE_SHRINK:
                expand();
                break;
            case STATE_EXPAND:
                shrink();
                break;
        }
    }

    public void expand() {
        mCurrState = STATE_EXPAND;
        if (mOnExpandListener != null) {
            mOnExpandListener.onExpand(this);
        }
        setTextInternal(getNewTextByConfig(), mBufferType);
    }

    public void shrink() {
        mCurrState = STATE_SHRINK;
        if (mOnExpandListener != null) {
            mOnExpandListener.onShrink(this);
        }
        setTextInternal(getNewTextByConfig(), mBufferType);
    }

    protected void setTextInternal(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    protected int getLengthOfString(String string) {
        if (string == null)
            return 0;
        return string.length();
    }

    protected String getContentOfString(String string) {
        if (string == null)
            return "";
        return string;
    }

    protected CharSequence removeEndLineBreak(CharSequence text) {
        CharSequence str = text;
        while (str.toString().endsWith("\n")) {
            str = str.subSequence(0, str.length() - 1);
        }
        return str;
    }

    /* ----------------------------- ▼系统方法▼ -----------------------------  */

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mFromNewText) {
            mFromNewText = false;
        } else {
            mOrigText = text;
        }
        mBufferType = type;
        setTextInternal(getNewTextByConfig(), type);
    }

    OnClickListener mOnClickListener;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    public void setSuperOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    @Override
    public void onClick(View v) {
        if (mToggleEnable) {
            toggle();
        }
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
        if (!mIsToggleTrigger && mOnChildClickListener != null) {
            mOnChildClickListener.onContentClick(this, getExpandState());
        }
        mIsToggleTrigger = false;
    }

    protected OnLongClickListener mOnLongClickListener;
    protected boolean mIsLongClickTrigger = false;

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        mOnLongClickListener = l;
    }

    public void setSuperOnLongClickListener(@Nullable OnLongClickListener l) {
        super.setOnLongClickListener(l);
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnLongClickListener != null) {
            mOnLongClickListener.onLongClick(v);
        }
        mIsLongClickTrigger = true;
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean r = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsLongClickTrigger = false;
                break;
        }
        return r;
    }

    /* ----------------------------- ▼对外方法▼ -----------------------------  */

    public OnExpandListener getOnExpandListener(){
        return mOnExpandListener;
    }

    public OnChildClickListener getOnChildClickListener(){
        return mOnChildClickListener;
    }

    public void setMaxLinesOnShrink(int lines) {
        mMaxLinesOnShrink = lines;
        mMaxLinesOnShrink = mMaxLinesOnShrink <= 0 ? 1 : mMaxLinesOnShrink;
        setText(mOrigText, mCurrState, mFutureTextViewWidth);
    }

    public void setEllipsisHintColor(int color) {
        mToEllipsisHintColor = color;
        setText(mOrigText, getExpandState());
    }

    public void setExpandHintColor(int color) {
        mToExpandHintColor = color;
        setText(mOrigText, getExpandState());
    }

    public void setToExpandIcon(Drawable drawable) {
        mToExpandIcon = drawable;
        setText(mOrigText, getExpandState());
    }

    public void setToShrinkIcon(Drawable drawable) {
        mToShrinkIcon = drawable;
        setText(mOrigText, getExpandState());
    }

    public void setShrinkHintColor(int color) {
        mToShrinkHintColor = color;
        setText(mOrigText, getExpandState());
    }


    public void setText(final CharSequence text, final int currState) {
        setText(text, currState, 0);
    }

    public void setText(final CharSequence text, final int currState, final int viewWidth) {
        this.mCurrState = currState;
        this.mOrigText = text;
        this.mFutureTextViewWidth = viewWidth;
        if (viewWidth <= 0 && (getWidth() == 0 || getMeasuredWidth() == 0)) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setText(text, currState, viewWidth);
                }
            }, 5);
        } else {
            mFromNewText = true;
            setText(getNewTextByConfig());
        }
    }

    public void setExpandState(int currState) {
        this.mCurrState = currState;
        setTextInternal(getNewTextByConfig(), mBufferType);
    }

    public int getExpandState() {
        return mCurrState;
    }

    public CharSequence getOrigText() {
        return mOrigText;
    }

    /* ----------------------------- ▼内部类▼ -----------------------------  */

    protected class TouchableSpan extends ClickableSpan {

        int textColor = -1;

        public TouchableSpan() {
        }

        public TouchableSpan(int textColor) {
            this.textColor = textColor;
        }

        @Override
        public void onClick(View widget) {

        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            if (textColor != -1) {
                ds.setColor(textColor);
            }
            ds.setUnderlineText(false);
        }
    }

    /* ----------------------------- ▼事件回调▼ -----------------------------  */

    public void setExpandListener(OnExpandListener listener) {
        mOnExpandListener = listener;
    }

    public interface OnExpandListener {

        void onExpand(ExpandableTextView view);

        void onShrink(ExpandableTextView view);
    }

    public void setOnChildClickListener(OnChildClickListener listener) {
        mOnChildClickListener = listener;
    }

    public interface OnChildClickListener {

        void onContentClick(ExpandableTextView view, int state);

        void onExpandClick(ExpandableTextView view, int state);

        void onShrinkClick(ExpandableTextView view, int state);
    }
}
