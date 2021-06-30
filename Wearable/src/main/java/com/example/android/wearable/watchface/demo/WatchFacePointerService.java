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

import com.example.android.wearable.watchface.R;
import com.example.android.wearable.watchface.mobvoi.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


/**
 * 如何实现时分秒指针，及其阴影
 */
public class WatchFacePointerService extends CanvasWatchFaceService {
    private static final String PNG_SUFFIX = ".png";
    private static final String ASSETS_FACES = "face";
    private static final String PREFIX_ASSETS = "assets:///";
    private static final int MSG_UPDATE_TIME = 0;

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
        Drawable drawableHour;
        Drawable drawableMinute;
        Drawable drawableSecond;
        Drawable drawableHourShadow;
        Drawable drawableMinuteShadow;
        Drawable drawableSecondShadow;
        private EngineHandler mHandler;
        private final long mUpdateRateMillis = TimeUnit.SECONDS.toMillis(1);

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            HandlerThread asyncThread = new HandlerThread("WatchFaceAsync");
            asyncThread.start();
            mAsyncHandler = new Handler(asyncThread.getLooper());
            mAsyncHandler.post(() -> {
                drawableHour = getAssetDrawable(null, "poi_hour");
                drawableMinute = getAssetDrawable(null, "poi_minute");
                drawableSecond = getAssetDrawable(null, "poi_second");
                drawableHourShadow = getAssetDrawable(null, "shadow_poi_hour");
                drawableMinuteShadow = getAssetDrawable(null, "shadow_poi_minute");
                drawableSecondShadow = getAssetDrawable(null, "shadow_poi_second");
            });
            mHandler = new EngineHandler(this);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            //背景图
            Drawable image = getDrawable(R.drawable.bg2);
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
            drawImage(canvas, image, 0.5f, 0.5f, 0, ratio);
            //时指针阴影
            drawImage(canvas, drawableHourShadow, 0.5f, 0.5f,
                    TimeUtil.calculateHourDegree(Calendar.getInstance()), 1);
            //时针
            drawImage(canvas, drawableHour, 0.5f, 0.5f,
                    TimeUtil.calculateHourDegree(Calendar.getInstance()), 1);
            //分指针阴影
            drawImage(canvas, drawableMinuteShadow, 0.5f, 0.5f,
                    TimeUtil.calculateMinuteDegree(Calendar.getInstance()), 1);
            //分针
            drawImage(canvas, drawableMinute, 0.5f, 0.5f,
                    TimeUtil.calculateMinuteDegree(Calendar.getInstance()), 1);
            //秒指针阴影
            drawImage(canvas, drawableSecondShadow, 0.5f, 0.5f,
                    TimeUtil.calculateSecondDegree(Calendar.getInstance()), 1);
            //秒针
            drawImage(canvas, drawableSecond, 0.5f, 0.5f,
                    TimeUtil.calculateSecondDegree(Calendar.getInstance()), 1);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            invalidate();   //需要刷新，避免显示空白
            updateTimer();
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
                return;
            }
            if (msg.what == MSG_UPDATE_TIME) {
                engine.handleUpdateTimeMessage();
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
