package com.chhd.expandabletextview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.text.style.ImageSpan;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

class AlignImageSpan extends ImageSpan {

    static final int ALIGN_TOP = 3; // 顶部对齐
    static final int ALIGN_CENTER = 4; // 垂直居中

    @IntDef({ALIGN_BOTTOM, ALIGN_BASELINE, ALIGN_TOP, ALIGN_CENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Alignment {
    }

    public AlignImageSpan(Drawable d) {
        this(d, ALIGN_CENTER);
    }

    public AlignImageSpan(Drawable d, @Alignment int verticalAlignment) {
        super(d, verticalAlignment);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Drawable d = getCachedDrawable();
        if (d == null) {
            return 0;
        }
        Rect rect = d.getBounds();
        if (fm != null) {
            Paint.FontMetrics fmPaint = paint.getFontMetrics();
            // 顶部 leading
            float topLeading = fmPaint.top - fmPaint.ascent;
            // 底部 leading
            float bottomLeading = fmPaint.bottom - fmPaint.descent;
            // drawable 的高度
            int drHeight = rect.height();

            switch (mVerticalAlignment) {
                case ALIGN_CENTER: { // drawable 的中间与 行中间对齐
                    // 当前行 的高度
                    float fontHeight = fmPaint.descent - fmPaint.ascent;
                    // 整行的 y方向上的中间 y 坐标
                    float center = fmPaint.descent - fontHeight / 2;

                    // 算出 ascent 和 descent
                    float ascent = center - drHeight / 2;
                    float descent = center + drHeight / 2;

                    fm.ascent = (int) ascent;
                    fm.top = (int) (ascent + topLeading);
                    fm.descent = (int) descent;
                    fm.bottom = (int) (descent + bottomLeading);
                    break;
                }
                case ALIGN_BASELINE: { // drawable 的底部与 baseline 对齐
                    // 所以 ascent 的值就是 负的 drawable 的高度
                    float ascent = -drHeight;
                    fm.ascent = -drHeight;
                    fm.top = (int) (ascent + topLeading);
                    break;
                }
                case ALIGN_TOP: { // drawable 的顶部与 行的顶部 对齐
                    // 算出 descent
                    float descent = drHeight + fmPaint.ascent;
                    fm.descent = (int) descent;
                    fm.bottom = (int) (descent + bottomLeading);
                    break;
                }
                case ALIGN_BOTTOM: // drawable 的底部与 行的底部 对齐
                default: {
                    // 算出 ascent
                    float ascent = fmPaint.descent - drHeight;
                    fm.ascent = (int) ascent;
                    fm.top = (int) (ascent + topLeading);
                }
            }
        }
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom,
                     Paint paint) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        Rect rect = drawable.getBounds();
        float transY;
        switch (mVerticalAlignment) {
            case ALIGN_BASELINE:
                transY = y - rect.height();
                break;
            case ALIGN_CENTER:
                transY = ((bottom - top) - rect.height()) / 2 + top;
                break;
            case ALIGN_TOP:
                transY = top;
                break;
            case ALIGN_BOTTOM:
            default:
                transY = bottom - rect.height();
        }
        canvas.save();
        // 这里如果不移动画布，drawable 就会在 Textview 的左上角出现
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<>(d);
        }

        return d;
    }

    private WeakReference<Drawable> mDrawableRef;
}
