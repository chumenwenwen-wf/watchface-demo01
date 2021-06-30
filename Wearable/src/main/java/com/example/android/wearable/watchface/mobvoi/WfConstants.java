package com.example.android.wearable.watchface.mobvoi;

import android.content.ComponentName;
import android.view.Gravity;

public interface WfConstants {
    /**
     * 健康应用提供的安全provider
     */
    ComponentName CALORIE_PROVIDER = new ComponentName("com.mobvoi.wear.health.aw",
            "com.mobvoi.ticwear.health.bg.complication.CalorieComplicationService");
    ComponentName STEP_PROVIDER = new ComponentName("com.mobvoi.wear.health.aw",
            "com.mobvoi.ticwear.health.bg.complication.StepComplicationService");
    ComponentName HEART_RATE_PROVIDER = new ComponentName("com.mobvoi.wear.health.aw",
            "com.mobvoi.ticwear.health.bg.complication.HeartRateComplicationService");
    ComponentName PRESSURE_PROVIDER = new ComponentName("com.mobvoi.wear.health.aw",
            "com.mobvoi.ticwear.health.bg.complication.PressureComplicationService");
    ComponentName NOISE_PROVIDER = new ComponentName("com.mobvoi.wear.health.aw",
            "com.mobvoi.ticwear.health.bg.complication.NoiseComplicationService");
    ComponentName BLOOD_OXYGEN_PROVIDER = new ComponentName("com.mobvoi.wear.health.aw",
            "com.mobvoi.ticwear.health.bg.complication.BloodOxygenComplicationService");
}