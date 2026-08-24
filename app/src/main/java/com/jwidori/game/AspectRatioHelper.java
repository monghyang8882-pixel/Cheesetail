package com.jwidori.game;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Calculates the on-screen bounds of a drawable rendered with FIT_CENTER.
 */
public final class AspectRatioHelper {

    private AspectRatioHelper() {
    }

    public static float[] getFitCenterBounds(ImageView imageView) {
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();
        Drawable drawable = imageView.getDrawable();

        if (viewWidth <= 0 || viewHeight <= 0 || drawable == null) {
            return new float[] {0f, 0f, Math.max(1, viewWidth), Math.max(1, viewHeight)};
        }

        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new float[] {0f, 0f, viewWidth, viewHeight};
        }

        float scale = Math.min(
                viewWidth / (float) imageWidth,
                viewHeight / (float) imageHeight
        );
        float renderedWidth = imageWidth * scale;
        float renderedHeight = imageHeight * scale;
        float left = (viewWidth - renderedWidth) * 0.5f;
        float top = (viewHeight - renderedHeight) * 0.5f;

        return new float[] {left, top, renderedWidth, renderedHeight};
    }
}
