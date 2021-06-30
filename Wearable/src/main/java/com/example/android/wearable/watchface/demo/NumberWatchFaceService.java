package com.example.android.wearable.watchface.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.format.DateFormat;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 简单的数字键盘
 * 如何加载字体
 */
public class NumberWatchFaceService extends CanvasWatchFaceService {
    private static final int MSG_UPDATE_TIME = 0;
    private final static String FONT_NAME = "BITSUMISHI.TTF";

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    class MyEngine extends CanvasWatchFaceService.Engine {
        private Paint mPaint;
        private Paint mBackgroundPaint;
        private EngineHandler mHandler;
        private final long mUpdateRateMillis = TimeUnit.SECONDS.toMillis(1);

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mHandler = new EngineHandler(this);
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.parseColor("black"));
            mPaint = new Paint();
            new Thread(() ->
                    mPaint.setTypeface(Typeface.createFromAsset(
                            getAssets(), "font/" + FONT_NAME))
            ).start();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            //清除上次绘制
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            //另一种方式
//            canvas.drawColor(Color.BLACK);

            int width = canvas.getWidth();
            Calendar mCalendar = Calendar.getInstance();
            //time without second
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextAlign(Paint.Align.LEFT);
            mPaint.setTextSize(58 / getBaseSize() * width);
            canvas.drawText(getTimeString(mCalendar, ":", shouldShowAmPm()),
                    57 / getBaseSize() * width, 150 / getBaseSize() * width, mPaint);
            //Second
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextAlign(Paint.Align.LEFT);
            mPaint.setTextSize(38 / getBaseSize() * width);
            int second = mCalendar.get(Calendar.SECOND);
            String secondStr;
            if(second<10) {
                secondStr = ":0" + second;
            } else{
                secondStr = ":" + second;
            }
            canvas.drawText(secondStr, 255 / getBaseSize() * width, 130 / getBaseSize() * width, mPaint);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
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

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            updateTimer();
        }

    }

    private float getBaseSize() {
        return 400;
    }

    public String getTimeString(Calendar calendar, String connector, boolean hasAmPm) {
        if (connector == null) {
            connector = "";
        }
        return paddingSingleNumber(getHour(calendar, hasAmPm)) + connector
                + paddingSingleNumber(calendar.get(Calendar.MINUTE));
    }

    /**
     * 1位数字前面补0
     */
    public String paddingSingleNumber(int number) {
        return String.format(Locale.getDefault(), "%02d", number);
    }

    public int getHour(Calendar calendar, boolean hasAmPm) {
        boolean shouldShowAmPm = shouldShowAmPm();
        int hour;
        if (hasAmPm || shouldShowAmPm) {
            hour = calendar.get(Calendar.HOUR);
            if (hour == 0) {
                hour = 12;
            }
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        }
        return hour;
    }

    public boolean shouldShowAmPm() {
        return !is24HourFormat(this);
    }

    public static boolean is24HourFormat(Context context) {
        return DateFormat.is24HourFormat(context);
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
}
