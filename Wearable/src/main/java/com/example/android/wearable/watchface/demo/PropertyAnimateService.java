package com.example.android.wearable.watchface.demo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.wearable.view.SimpleAnimatorListener;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;


/**
 * 如何实现属性动画
 */
public class PropertyAnimateService extends CanvasWatchFaceService {
    private static final String PNG_SUFFIX = ".png";
    private static final String ASSETS_FACES = "face";
    private static final String PREFIX_ASSETS = "assets:///";
    private static final float POSITION_STYLE[][] = new float[][]{{72, 165}, {70, 238}, {98, 252},
            {99, 312}, {160, 326}, {245, 340}, {273, 303}, {328, 235}, {317, 255}};

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    class MyEngine extends Engine {
        protected Handler mAsyncHandler;
        private ValueAnimator mValueAnimator;
        private IconState[] mStyleDrawables = new IconState[POSITION_STYLE.length];
        private int mCurrentDrawposition;
        private final float[] START_POSITION = new float[]{200, -100};
        private boolean mDrawableInited;
        private float mValue;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            HandlerThread asyncThread = new HandlerThread("WatchFaceAsync");
            asyncThread.start();
            mAsyncHandler = new Handler(asyncThread.getLooper());
            mAsyncHandler.post(() -> {
                for (int i = 0; i < mStyleDrawables.length; i++) {
                    mStyleDrawables[i] = new IconState();
                    mStyleDrawables[i].drawable = getAssetDrawable(null, String.format("style_%d", i + 1));
                    mStyleDrawables[i].targetPositionX = POSITION_STYLE[i][0];
                    mStyleDrawables[i].targetPositionY = POSITION_STYLE[i][1];
                }
                mDrawableInited = true;
            });
        }

        private void resetPosition() {
            mCurrentDrawposition = 0;
            if (!mDrawableInited) {
                return;
            }
            for (IconState iconState : mStyleDrawables) {
                iconState.positionX = START_POSITION[0];
                iconState.positionY = START_POSITION[1];
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            canvas.drawColor(Color.BLACK);
            if (!isInAmbientMode() && mDrawableInited) {
                for (int i = 0; i < mStyleDrawables.length; i++) {
                    if (mCurrentDrawposition > i) {
                        drawImage(canvas, mStyleDrawables[i].drawable, mStyleDrawables[i].targetPositionX / getBaseSize(),
                                mStyleDrawables[i].targetPositionY / getBaseSize(), 0,1);
                    } else if (mCurrentDrawposition == i) {
                        mStyleDrawables[i].positionX = (mStyleDrawables[i].targetPositionX - START_POSITION[0]) * mValue + START_POSITION[0];
                        mStyleDrawables[i].positionY = (mStyleDrawables[i].targetPositionY - START_POSITION[1]) * mValue + START_POSITION[1];
                        if (mStyleDrawables[i].positionX == mStyleDrawables[i].targetPositionX) {
                            continue;
                        }
                        drawImage(canvas, mStyleDrawables[i].drawable, mStyleDrawables[i].positionX / getBaseSize(),
                                mStyleDrawables[i].positionY / getBaseSize(), 0,1);
                    }
                }
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            invalidate();   //需要刷新，避免显示空白
            if (visible) {
                startAnim();
            } else {
                stopAnim();
            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (inAmbientMode) {
                stopAnim();
            } else {
                startAnim();
            }
        }

        private void startAnim() {
            if (mValueAnimator == null) {
                mValueAnimator = ValueAnimator.ofFloat(0, 1);
                mValueAnimator.setDuration(1000);
                mValueAnimator.setRepeatCount(mStyleDrawables.length);
                mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mValue = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                mValueAnimator.addListener(new SimpleAnimatorListener() {

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        super.onAnimationCancel(animator);
                        resetPosition();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                        super.onAnimationRepeat(animator);
                        mCurrentDrawposition++;
                    }
                });
            }
            if (!mValueAnimator.isRunning() && !mValueAnimator.isStarted()) {
                resetPosition();
                mCurrentDrawposition = 0;
                mValueAnimator.start();
            }
        }

        private void stopAnim() {
            if (mValueAnimator != null && mValueAnimator.isRunning()) {
                mValueAnimator.cancel();
            }
        }

        @Override
        public void onDestroy() {
            for (IconState mStyle1Drawable : mStyleDrawables) {
                recycleDrawable(mStyle1Drawable.drawable);
            }
            super.onDestroy();
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

        protected Drawable getAssetDrawable(String path, String name) {
            if (TextUtils.isEmpty(name)) {
                return null;
            } else if (path == null) {
                return getDrawableFromPath(PREFIX_ASSETS + getFaceResourceDir() + File.separator + name + PNG_SUFFIX);
            } else if (path.startsWith(PREFIX_ASSETS)) {
                path = path.replace(PREFIX_ASSETS, "");
                return getDrawableFromPath(PREFIX_ASSETS + getFaceResourceDir() + File.separator + path + PNG_SUFFIX);
            }
            return getDrawableFromPath(path);
        }

        private Drawable getDrawableFromAssets(String path) {
            if (path != null && path.startsWith(PREFIX_ASSETS)) {
                path = path.replace(PREFIX_ASSETS, "");
                InputStream is = null;
                try {
                    is = getApplicationContext().getAssets().open(path);
                    return Drawable.createFromResourceStream(getResources(), null, is, null);
                } catch (IOException e) {
                    Log.w("Manley", "Error get drawable from assets path = " + path);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
            return null;
        }

        private String getFaceResourceDir() {
            return ASSETS_FACES;
        }

        protected Drawable getDrawableFromPath(String path) {
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            if (path.startsWith(PREFIX_ASSETS)) {
                return getDrawableFromAssets(path);
            }
            return Drawable.createFromPath(path);
        }

        private class IconState {
            private float positionX;
            private float positionY;
            private float targetPositionX;
            private float targetPositionY;
            private Drawable drawable;
        }

        protected void recycleDrawable(Drawable drawable) {
            if (drawable != null) {
                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
        }
    }

    private float getBaseSize() {
        return 400;
    }

    protected float getDrawableWidth(Drawable drawable, int canvasWidth) {
        return drawable.getIntrinsicWidth() * canvasWidth / getBaseSize();
    }

    protected float getDrawableHeight(Drawable drawable, int canvasHeight) {
        return drawable.getIntrinsicHeight() * canvasHeight / getBaseSize();
    }
}
