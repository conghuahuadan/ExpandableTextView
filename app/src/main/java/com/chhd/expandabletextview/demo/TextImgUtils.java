package com.chhd.expandabletextview.demo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

public class TextImgUtils {

    public TextImgUtils() {
    }

    public static SpannableString addSuffixImg(CharSequence text, Drawable drawImg) {
        SpannableString ss = new SpannableString(text + "  ");
        drawImg.setBounds(0, 0, drawImg.getIntrinsicWidth(), drawImg.getIntrinsicHeight());
        TextImgUtils.VerticalImageSpan spanImg = new TextImgUtils.VerticalImageSpan(drawImg);
        ss.setSpan(spanImg, ss.length() - 1, ss.length(), 17);
        return ss;
    }

    public static SpannableString addPrefixImg(CharSequence text, Drawable drawImg) {
        SpannableString ss = new SpannableString("  " + text);
        drawImg.setBounds(0, 0, drawImg.getIntrinsicWidth(), drawImg.getIntrinsicHeight());
        TextImgUtils.VerticalImageSpan spanImg = new TextImgUtils.VerticalImageSpan(drawImg);
        ss.setSpan(spanImg, 0, 1, 17);
        return ss;
    }

    public static class VerticalImageSpan extends ImageSpan {
        public VerticalImageSpan(Drawable drawable) {
            super(drawable);
        }

        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fontMetricsInt) {
            Drawable drawable = this.getDrawable();
            Rect rect = drawable.getBounds();
            if (fontMetricsInt != null) {
                Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
                int fontHeight = fmPaint.descent - fmPaint.ascent;
                int drHeight = rect.bottom - rect.top;
                int centerY = fmPaint.ascent + fontHeight / 2;
                fontMetricsInt.ascent = centerY - drHeight / 2;
                fontMetricsInt.top = fontMetricsInt.ascent;
                fontMetricsInt.bottom = centerY + drHeight / 2;
                fontMetricsInt.descent = fontMetricsInt.bottom;
            }

            return rect.right;
        }

        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Drawable drawable = this.getDrawable();
            canvas.save();
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.descent - fmPaint.ascent;
            int centerY = y + fmPaint.descent - fontHeight / 2;
            int transY = centerY - (drawable.getBounds().bottom - drawable.getBounds().top) / 2;
            canvas.translate(x, (float)transY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }
}
