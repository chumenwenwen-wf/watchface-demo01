package com.example.android.wearable.watchface.demo;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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
 * 如何实现表盘动画
 */
public class AnimateService extends CanvasWatchFaceService {
    private static final String PNG_SUFFIX = ".png";
    private static final String ASSETS_FACES = "face";
    private static final String PREFIX_ASSETS = "assets:///";
    private static final int MSG_UPDATE_TIME = 0;
    private static final int MSG_ANIMATE = 1;
    private static final long ANIMATION_INTERVAL = 300L;    //动画总是长
    private static final String[] frameDrawable = new String[]{"frame1", "frame2", "frame3",
            "frame4", "frame5", "frame6"};

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    class MyEngine extends Engine {
        protected Handler mAsyncHandler;
        private EngineHandler mHandler;
        private final long mUpdateRateMillis = TimeUnit.SECONDS.toMillis(1);
        private Drawable[] mDrawables;
        private AnimationInfo mAnimationInfo;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            HandlerThread asyncThread = new HandlerThread("WatchFaceAsync");
            asyncThread.start();
            mAsyncHandler = new Handler(asyncThread.getLooper());
            mAsyncHandler.post(() -> {
                mDrawables = new Drawable[frameDrawable.length];
                for (int i = 0; i < frameDrawable.length; i++) {
                    mDrawables[i] = getAssetDrawable(null, frameDrawable[i]);
                }
            });
            mAnimationInfo = new AnimationInfo();
            mHandler = new EngineHandler(this);
            mHandler.sendEmptyMessageDelayed(MSG_ANIMATE, ANIMATION_INTERVAL);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            //背景图
            if (mDrawables != null && mDrawables[mAnimationInfo.currentIndex] != null
                    && !isInAmbientMode()) {
                drawImage(canvas, mDrawables[mAnimationInfo.currentIndex],
                        0.5f, 0.5f, 0, 1);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            invalidate();   //需要刷新，避免显示空白
            updateTimer();
            changeAnimation();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            changeAnimation();
        }

        private void changeAnimation() {
            mHandler.removeCallbacksAndMessages(null);
            if (shouldAnimationBeRunning()) {
                mHandler.sendEmptyMessage(MSG_ANIMATE);
            }
        }

        private boolean shouldAnimationBeRunning() {
            return isVisible() && !isInAmbientMode();
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

        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = mUpdateRateMillis
                        - (timeMs % mUpdateRateMillis);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void updateTimer() {
            mHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private class AnimationInfo {
            private int currentIndex;

            private void setCurrentIndex(int currentIndex) {
                this.currentIndex = currentIndex;
            }
        }
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyEngine> mWeakReference;

        public EngineHandler(MyEngine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyEngine engine = mWeakReference.get();
            if (engine == null) {
                removeCallbacksAndMessages(null);
                sendEmptyMessageDelayed(0, ANIMATION_INTERVAL);
                return;
            }
            if (msg.what == MSG_UPDATE_TIME) {
                engine.handleUpdateTimeMessage();
            } else if (msg.what == MSG_ANIMATE) {
                if (engine.mAnimationInfo.currentIndex == frameDrawable.length-1) {
                    engine.mAnimationInfo.setCurrentIndex(0);
                } else {
                    engine.mAnimationInfo.setCurrentIndex(engine.mAnimationInfo.currentIndex + 1);
                }
                engine.invalidate();
                sendEmptyMessageDelayed(MSG_ANIMATE, ANIMATION_INTERVAL);
            }
        }
    }

    private float getBaseSize() {
        return 456;
    }

    protected float getDrawableWidth(Drawable drawable, int canvasWidth) {
        return drawable.getIntrinsicWidth() * canvasWidth / getBaseSize();
    }

    protected float getDrawableHeight(Drawable drawable, int canvasHeight) {
        return drawable.getIntrinsicHeight() * canvasHeight / getBaseSize();
    }
}
