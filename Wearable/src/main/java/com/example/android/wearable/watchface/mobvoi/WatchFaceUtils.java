package com.example.android.wearable.watchface.mobvoi;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 常用方法工具类
 */
public class WatchFaceUtils {
    public static final float CENTER = 0.5f;

    protected float getBaseSize() {
        return 400.0f;
    }

    protected void drawImage(@NonNull Canvas canvas, @Nullable Drawable drawable,
                             float positionX, float positionY, float degree, float scale) {
        if (drawable == null) {
            return;
        }

        int width = canvas.getWidth();
        int height = canvas.getWidth();
        float drawableWidth = getDrawableWidth(drawable, width) * scale;
        float drawableHeight = getDrawableHeight(drawable, height) * scale;
        canvas.save();
        canvas.translate(width * positionX - drawableWidth / 2, height * positionY - drawableHeight / 2);
        if (degree != 0) {
            canvas.rotate(degree, drawableWidth / 2, drawableHeight / 2);
        }
        drawable.setBounds(0, 0, (int) drawableWidth, (int) drawableHeight);
        drawable.draw(canvas);
        canvas.restore();
    }

    protected float getDrawableWidth(Drawable drawable, int canvasWidth) {
        return drawable.getIntrinsicWidth() * canvasWidth / getBaseSize();
    }

    protected float getDrawableHeight(Drawable drawable, int canvasHeight) {
        return drawable.getIntrinsicHeight() * canvasHeight / getBaseSize();
    }
}
