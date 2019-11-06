package com.chhd.expandabletextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;

/**
 * 改自 Carbs0126 / ExpandableTextView
 *
 * · 新增展开图标、收起图标的属性
 * · 支持“\n”
 *
 * @author 陈伟强 (2019/7/29)
 */
public class ExpandableTextView extends android.support.v7.widget.AppCompatTextView {

    private final String TAG = this.getClass().getSimpleName();

    public static final int STATE_SHRINK = 0;
    public static final int STATE_EXPAND = 1;

    private static final String CLASS_NAME_VIEW = "android.view.View";
    private static final String CLASS_NAME_LISTENER_INFO = "android.view.View$ListenerInfo";
    private static final String ELLIPSIS_HINT = "..";
    private static final String GAP_TO_EXPAND_HINT = " ";
    private static final String GAP_TO_SHRINK_HINT = " ";
    private static final int MAX_LINES_ON_SHRINK = 2;
    private static final int TO_EXPAND_HINT_COLOR = 0xFF3498DB;
    private static final int TO_SHRINK_HINT_COLOR = 0xFFE74C3C;
    private static final boolean TOGGLE_ENABLE = true;
    private static final boolean SHOW_TO_EXPAND_HINT = true;
    private static final boolean SHOW_TO_SHRINK_HINT = true;

    /** 省略符号 */
    private String mEllipsisHint;
    /** 展开文本 */
    private String mToExpandHint;
    /** 收起文本 */
    private String mToShrinkHint;
    private Drawable mToExpandIcon;
    private Drawable mToShrinkIcon;
    private int mExpandIconVerticalAlign = AlignImageSpan.ALIGN_CENTER;
    private int mShrinkIconVerticalAlign = AlignImageSpan.ALIGN_CENTER;
    /** 左侧展开文本的间隔 */
    private String mGapToExpandHint = GAP_TO_EXPAND_HINT;
    /** 左侧收起文本的间隔 */
    private String mGapToShrinkHint = GAP_TO_SHRINK_HINT;
    private boolean mToggleEnable = TOGGLE_ENABLE;
    /** 显示展开文本 */
    private boolean mShowToExpandHint = SHOW_TO_EXPAND_HINT;
    /** 显示收起文本 */
    private boolean mShowToShrinkHint = SHOW_TO_SHRINK_HINT;
    private int mMaxLinesOnShrink = MAX_LINES_ON_SHRINK;
    private int mToExpandHintColor = TO_EXPAND_HINT_COLOR;
    private int mToShrinkHintColor = TO_SHRINK_HINT_COLOR;
    private int mCurrState = STATE_SHRINK;

    private TouchableSpan mTouchableSpan;
    private BufferType mBufferType = BufferType.NORMAL;
    private TextPaint mTextPaint;
    private Layout mLayout;
    private int mTextLineCount = -1;
    private int mLayoutWidth = 0;
    private int mFutureTextViewWidth = 0;

    private CharSequence mOrigText;

    private ExpandableClickListener mExpandableClickListener;
    private OnExpandListener mOnExpandListener;

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

    private void initAttr(Context context, AttributeSet attrs) {
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
        mToExpandHintColor = a.getInteger(R.styleable.ExpandableTextView_etv_ToExpandHintColor,
                TO_EXPAND_HINT_COLOR);
        mToShrinkHintColor = a.getInteger(R.styleable.ExpandableTextView_etv_ToShrinkHintColor,
                TO_SHRINK_HINT_COLOR);
//        mToExpandHintColorBgPressed =
//                a.getInteger(R.styleable.ExpandableTextView_etv_ToExpandHintColorBgPressed,
//                        TO_EXPAND_HINT_COLOR_BG_PRESSED);
//        mToShrinkHintColorBgPressed =
//                a.getInteger(R.styleable.ExpandableTextView_etv_ToShrinkHintColorBgPressed,
//                        TO_SHRINK_HINT_COLOR_BG_PRESSED);
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

    private void init() {
        mTouchableSpan = new TouchableSpan();
        if (TextUtils.isEmpty(mEllipsisHint)) {
            mEllipsisHint = ELLIPSIS_HINT;
        }
        if (TextUtils.isEmpty(mToExpandHint)) {
            mToExpandHint = getResources().getString(R.string.to_expand_hint);
        }
        if (TextUtils.isEmpty(mToShrinkHint)) {
            mToShrinkHint = getResources().getString(R.string.to_shrink_hint);
        }
        if (mGapToExpandHint == null) {
            mGapToExpandHint = GAP_TO_EXPAND_HINT;
        }
        if (mGapToShrinkHint == null) {
            mGapToShrinkHint = GAP_TO_SHRINK_HINT;
        }
        if (mToggleEnable) {
            mExpandableClickListener = new ExpandableClickListener();
            setOnClickListener(mExpandableClickListener);
        }
    }

    /* ----------------------------- ▼内部方法▼ -----------------------------  */

    private CharSequence getNewTextByConfig() {
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
        switch (mCurrState) {
            case STATE_SHRINK: {
                mLayout = new DynamicLayout(mOrigText, mTextPaint, mLayoutWidth,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f, 0.0f, false);
                mTextLineCount = mLayout.getLineCount();
                if (mTextLineCount <= mMaxLinesOnShrink) {
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
                if (mToExpandIcon == null) {
                    widthTailReplaced = mTextPaint.measureText(getContentOfString(mEllipsisHint)
                            + (mShowToExpandHint ? (getContentOfString(mToExpandHint)
                            + getContentOfString(mGapToExpandHint)) : ""));
                } else {
                    widthTailReplaced = mTextPaint.measureText(getContentOfString(mEllipsisHint)
                            + (mShowToExpandHint ? (getContentOfString(mGapToExpandHint)) : ""));
                    mToExpandIcon.setBounds(0, 0,
                            mToExpandIcon.getIntrinsicWidth(), mToExpandIcon.getIntrinsicHeight());
                    widthTailReplaced = widthTailReplaced + mToExpandIcon.getIntrinsicWidth();
                }

                int indexEndTrimmedRevised = indexEndTrimmed;
                // 剩余宽度 > 将被替换的宽度
                if (remainWidth > widthTailReplaced) {
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

                String fixText = removeEndLineBreak(mOrigText.subSequence(0,
                        indexEndTrimmedRevised));
                SpannableStringBuilder ssbShrink = new SpannableStringBuilder(fixText)
                        .append(mEllipsisHint);
                if (mShowToExpandHint) {
                    if (mToExpandIcon == null) {
                        ssbShrink.append(getContentOfString(mGapToExpandHint)
                                + getContentOfString(mToExpandHint));
                        ssbShrink.setSpan(mTouchableSpan, ssbShrink.length()
                                        - getLengthOfString(mToExpandHint), ssbShrink.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        ssbShrink.append(getContentOfString(mGapToExpandHint));
                        ssbShrink.append(" ");
                        AlignImageSpan sp = new AlignImageSpan(mToExpandIcon,
                                mExpandIconVerticalAlign);
                        ssbShrink.setSpan(sp, ssbShrink.length() - 1, ssbShrink.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                }
                return ssbShrink;
            }
            case STATE_EXPAND: {
                if (!mShowToShrinkHint) {
                    return mOrigText;
                }
                mLayout = new DynamicLayout(mOrigText, mTextPaint, mLayoutWidth,
                        Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                        false);
                mTextLineCount = mLayout.getLineCount();

                if (mTextLineCount <= mMaxLinesOnShrink) {
                    return mOrigText;
                }

                SpannableStringBuilder ssbExpand;
                if (mToShrinkIcon == null) {
                    ssbExpand = new SpannableStringBuilder(mOrigText)
                            .append(mGapToShrinkHint).append(mToShrinkHint);
                    ssbExpand.setSpan(mTouchableSpan, ssbExpand.length() -
                                    getLengthOfString(mToShrinkHint), ssbExpand.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    ssbExpand = new SpannableStringBuilder(mOrigText)
                            .append(mGapToShrinkHint);
                    ssbExpand.append(" ");
                    mToShrinkIcon.setBounds(0, 0,
                            mToShrinkIcon.getIntrinsicWidth(), mToShrinkIcon.getIntrinsicHeight());
                    AlignImageSpan sp = new AlignImageSpan(mToExpandIcon, mShrinkIconVerticalAlign);
                    ssbExpand.setSpan(sp, ssbExpand.length() - 1, ssbExpand.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return ssbExpand;
            }
        }
        return mOrigText;
    }

    private Layout getValidLayout() {
        return mLayout != null ? mLayout : getLayout();
    }

    private void toggle() {
        switch (mCurrState) {
            case STATE_SHRINK:
                mCurrState = STATE_EXPAND;
                if (mOnExpandListener != null) {
                    mOnExpandListener.onExpand(this);
                }
                break;
            case STATE_EXPAND:
                mCurrState = STATE_SHRINK;
                if (mOnExpandListener != null) {
                    mOnExpandListener.onShrink(this);
                }
                break;
        }
        setTextInternal(getNewTextByConfig(), mBufferType);
    }

    private void setTextInternal(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    private int getLengthOfString(String string) {
        if (string == null)
            return 0;
        return string.length();
    }

    private String getContentOfString(String string) {
        if (string == null)
            return "";
        return string;
    }

    private String removeEndLineBreak(CharSequence text) {
        String str = text.toString();
        while (str.endsWith("\n")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /* ----------------------------- ▼系统方法▼ -----------------------------  */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setTextInternal(getNewTextByConfig(), mBufferType);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOrigText = text;
        mBufferType = type;
        setTextInternal(getNewTextByConfig(), type);
    }

    /* ----------------------------- ▼对外方法▼ -----------------------------  */

    public int getExpandState() {
        return mCurrState;
    }

    public void setExpandState(int currState) {
        this.mCurrState = currState;
        setTextInternal(getNewTextByConfig(), mBufferType);
    }

    /* ----------------------------- ▼内部类▼ -----------------------------  */

    private class ExpandableClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            toggle();
        }
    }

    public View.OnClickListener getOnClickListener(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getOnClickListenerV14(view);
        } else {
            return getOnClickListenerV(view);
        }
    }

    private View.OnClickListener getOnClickListenerV(View view) {
        View.OnClickListener retrievedListener = null;
        try {
            Field field = Class.forName(CLASS_NAME_VIEW)
                    .getDeclaredField("mOnClickListener");
            field.setAccessible(true);
            retrievedListener = (View.OnClickListener) field.get(view);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retrievedListener;
    }

    private View.OnClickListener getOnClickListenerV14(View view) {
        View.OnClickListener retrievedListener = null;
        try {
            Field listenerField = Class.forName(CLASS_NAME_VIEW)
                    .getDeclaredField("mListenerInfo");
            Object listenerInfo = null;

            if (listenerField != null) {
                listenerField.setAccessible(true);
                listenerInfo = listenerField.get(view);
            }

            Field clickListenerField = Class.forName(CLASS_NAME_LISTENER_INFO)
                    .getDeclaredField("mOnClickListener");

            if (clickListenerField != null && listenerInfo != null) {
                clickListenerField.setAccessible(true);
                retrievedListener = (View.OnClickListener) clickListenerField.get(listenerInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retrievedListener;
    }

    private class TouchableSpan extends ClickableSpan {

        @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        @Override
        public void onClick(View widget) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                    && hasOnClickListeners()
                    && (getOnClickListener(ExpandableTextView.this) instanceof ExpandableClickListener)) {
            } else {
                toggle();
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            switch (mCurrState) {
                case STATE_SHRINK:
                    ds.setColor(mToExpandHintColor);
                    break;
                case STATE_EXPAND:
                    ds.setColor(mToShrinkHintColor);
                    break;
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
}
