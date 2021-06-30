package com.example.android.wearable.watchface.demo;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.SurfaceHolder;

import com.example.android.wearable.watchface.R;


/**
 * 如何设置表盘背景
 */
public class WatchFaceBgService extends CanvasWatchFaceService {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    class MyEngine extends CanvasWatchFaceService.Engine {
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            //背景图
            Drawable image = getDrawable(R.drawable.avatar);
            if (image == null) {
                return;
            }
            int rawWidth = image.getIntrinsicWidth();
            int rawHeight = image.getIntrinsicHeight();
            float ratio;
            if (rawHeight >= rawWidth) {
                ratio = getBaseSize() / rawWidth;
            } else {
                ratio = getBaseSize() / rawHeight;
            }
            int width = canvas.getWidth();
            int height = canvas.getWidth();
            float drawableWidth = getDrawableWidth(image, width) * ratio;
            float drawableHeight = getDrawableHeight(image, height) * ratio;
            canvas.save();
            canvas.translate(width * 0.5f - drawableWidth / 2, height * 0.5f - drawableHeight / 2);
            image.setBounds(0, 0, (int) drawableWidth, (int) drawableHeight);
            image.draw(canvas);
            canvas.restore();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            invalidate();   //需要刷新，避免显示空白
        }
    }

    private float getBaseSize() {
        return 360;
    }

    protected float getDrawableWidth(Drawable drawable, int canvasWidth) {
        return drawable.getIntrinsicWidth() * canvasWidth / getBaseSize();
    }

    protected float getDrawableHeight(Drawable drawable, int canvasHeight) {
        return drawable.getIntrinsicHeight() * canvasHeight / getBaseSize();
    }
}
