package com.android.launcher3.icons;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * DJ(icon-tweaks): {@link ShadowGenerator} with a softer ambient shadow (alpha 10; upstream 25).
 *
 * The alpha is a private constant upstream, so both drawing methods are copied from
 * {@code ShadowGenerator} (submodule, 16-dev) with only that value changed. It lives in this package
 * because {@code addPathShadow}, the shadow of adaptive icons, is package-private.
 */
public class DjShadowGenerator extends ShadowGenerator {

    private static final int AMBIENT_SHADOW_ALPHA = 10;
    private static final int KEY_SHADOW_ALPHA = 7; // same as upstream

    private final int mIconSize;
    private final Paint mBlurPaint;
    private final Paint mDrawPaint;
    private final BlurMaskFilter mDefaultBlurMaskFilter;

    public DjShadowGenerator(int iconSize) {
        super(iconSize);
        mIconSize = iconSize;
        mBlurPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mDefaultBlurMaskFilter = new BlurMaskFilter(mIconSize * BLUR_FACTOR, Blur.NORMAL);
    }

    @Override
    public synchronized void drawShadow(Bitmap icon, Canvas out) {
        if (ENABLE_SHADOWS) {
            int[] offset = new int[2];
            mBlurPaint.setMaskFilter(mDefaultBlurMaskFilter);
            Bitmap shadow = icon.extractAlpha(mBlurPaint, offset);

            mDrawPaint.setAlpha(AMBIENT_SHADOW_ALPHA);
            out.drawBitmap(shadow, offset[0], offset[1], mDrawPaint);

            mDrawPaint.setAlpha(KEY_SHADOW_ALPHA);
            out.drawBitmap(shadow, offset[0], offset[1] + KEY_SHADOW_DISTANCE * mIconSize,
                    mDrawPaint);
        }
    }

    @Override
    synchronized void addPathShadow(Path path, Canvas out) {
        if (ENABLE_SHADOWS) {
            mDrawPaint.setMaskFilter(mDefaultBlurMaskFilter);

            mDrawPaint.setAlpha(AMBIENT_SHADOW_ALPHA);
            out.drawPath(path, mDrawPaint);

            int save = out.save();
            mDrawPaint.setAlpha(KEY_SHADOW_ALPHA);
            out.translate(0, KEY_SHADOW_DISTANCE * mIconSize);
            out.drawPath(path, mDrawPaint);
            out.restoreToCount(save);

            mDrawPaint.setMaskFilter(null);
        }
    }
}
