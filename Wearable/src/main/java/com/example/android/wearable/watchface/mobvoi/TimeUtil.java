package com.example.android.wearable.watchface.mobvoi;

import android.content.Context;

import com.example.android.wearable.watchface.R;

import java.util.Calendar;
import java.util.Locale;

public class TimeUtil {

    public static final String TIME_AM = "AM";
    public static final String TIME_PM = "PM";
    public static final String UNIT_KM = "KM";
    public static final String UNIT_MI = "MI";

    /**
     * 根据时间计算时针的角度
     *
     * @param calendar
     * @return
     */
    public static float calculateHourDegree(Calendar calendar) {
        return calculateHourDegree(calendar, true);
    }

    /**
     * 根据时间计算时针的角度
     *
     * @param calendar
     * @param delta    是否计算分针和秒针
     * @return
     */
    public static float calculateHourDegree(Calendar calendar, boolean delta) {
        if (delta) {
            return 360.0f * (calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0f) / 12.0f;
        }
        return 360.0f * calendar.get(Calendar.HOUR_OF_DAY) / 12.0f;
    }

    public static float calculatePedometerDegree(float start, float end, float degeree) {
        return (end - start) * degeree + start;
    }

    public static String getAmPmText(Calendar calendar) {
        return isAm(calendar) ? TIME_AM : TIME_PM;
    }

    /**
     * 根据时间计算分针的角度
     *
     * @param calendar
     * @return
     */
    public static float calculateMinuteDegree(Calendar calendar) {
        return 360.0f * (calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) / 60.0f) / 60.0f;
    }

    public static float calculateMinuteDegree(Calendar calendar, boolean delta) {
        if (!delta)
            return 360.0f * calendar.get(Calendar.MINUTE) / 60.0f;
        else
            return calculateMinuteDegree(calendar);
    }

    /**
     * 根据时间计算秒针的角度
     */
    public static float calculateSecondDegree(Calendar calendar) {
        return 360.0f * calendar.get(Calendar.SECOND) / 60.0f;
    }

    /**
     * 根据时间计算秒针的角度
     */
    public static float calculateSecondDegree(Calendar calendar, boolean delta) {
        long milliseconds = calendar.getTimeInMillis() % 1000;
        if (delta) {
            return 360.0f * (calendar.get(Calendar.SECOND) + milliseconds / 1000f) / 60f;
        }
        return 360.0f * calendar.get(Calendar.SECOND) / 60.0f;
    }

    public static float calculateWeekDegree(Calendar calendar, float startDegree, float endDegree) {
        return startDegree + (endDegree - startDegree) * (calendar.get(Calendar.DAY_OF_WEEK) - 1) / 6.0f;
    }

    public static float calculateBatteryDegree(float start, float end, float batteryLevel) {
        return (end - start) * batteryLevel / 100 + start;
    }

    public static float calculateTemperatureDegree(float start, float end, float startDegree,
                                                   float endDegree, float temperature) {
        float factor = (endDegree - startDegree) / (end - start);
        return factor * temperature + startDegree - factor * start;
    }

    public static int getHour(Calendar calendar, boolean hasAmPm) {
        int hour;
        if (hasAmPm) {
            hour = calendar.get(Calendar.HOUR);
            if (hour == 0) {
                hour = 12;
            }
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        }
        return hour;
    }

    public static String getHourStr(Calendar calendar) {
        return getHourStr(calendar);
    }

    public static String getHourStr(Calendar calendar, boolean hasAmPm) {
        return paddingSingleNumber(getHour(calendar, hasAmPm));
    }

    public static String getMinuteStr(Calendar calendar) {
        return paddingSingleNumber(calendar.get(Calendar.MINUTE));
    }

    public static boolean isAm(Calendar calendar) {
        //noinspection WrongConstant
        return calendar.get(Calendar.AM_PM) == Calendar.AM;
    }

    /**
     * 1位数字前面补0
     */
    public static String paddingSingleNumber(int number) {
        return String.format(Locale.getDefault(), "%02d", number);
    }

    public static String getDistance(int distanceInMeter, boolean isUnitMetric) {
        if (isUnitMetric) {
            return String.format(Locale.getDefault(), "%.2f", distanceInMeter / (float) 1000);
        } else {
            return String.format(Locale.getDefault(), "%.2f", distanceInMeter * 0.6214 / (float) 1000);
        }
    }

    /**
     * 小数点后保留一位有效数字
     */
    public static String getDistanceOne(int distanceInMeter, boolean isUnitMetric) {
        if (isUnitMetric) {
            return String.format(Locale.getDefault(), "%.1f", distanceInMeter / (float) 1000);
        } else {
            return String.format(Locale.getDefault(), "%.1f", distanceInMeter * 0.6214 / (float) 1000);
        }
    }

    public static String getDistanceUnit(boolean isMetric) {
        return isMetric ? UNIT_KM : UNIT_MI;
    }

    /**
     * 获取当前星期
     *
     * @param prefix 前缀，例如星期、周等
     */
    public static String getWeek(int weekDay, String prefix) {
        String weekStr = "日";
        switch (weekDay) {
            case 1:
                weekStr = "一";
                break;
            case 2:
                weekStr = "二";
                break;
            case 3:
                weekStr = "三";
                break;
            case 4:
                weekStr = "四";
                break;
            case 5:
                weekStr = "五";
                break;
            case 6:
                weekStr = "六";
                break;
            case 7:
                weekStr = "日";
                break;
        }
        if (prefix == null) {
            prefix = "星期";
        }
        return prefix + weekStr;
    }

    /**
     * 获取当前月份的英文简称
     */
    public static String getMonthEN(Calendar calendar, Context c) {
        String[] months = c.getResources().getStringArray(R.array.month_en);
        return months[calendar.get(Calendar.MONTH)];
    }
}