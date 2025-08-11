package com.zafar.escaperush3d.game.input;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Smooth tilt controller using GAME_ROTATION_VECTOR (fallback to ACCELEROMETER).
 * - Calibrates baseline so your natural hold is neutral.
 * - Outputs a normalized analog value in range [-1..1] (right tilt positive).
 */
public class TiltController implements SensorEventListener {
    public interface TiltListener { void onTilt(float analog); }

    private SensorManager sm;
    private Sensor rotSensor;   // preferred
    private Sensor accelSensor; // fallback
    private final TiltListener listener;

    private final float[] rotMatrix = new float[9];
    private final float[] orientation = new float[3];

    private boolean usingRotation = false;

    private boolean baselineSet = false;
    private float baseline = 0f;   // baseline analog value
    private float ema = 0f;        // smoothed analog
    private final float smoothAlpha = 0.2f; // 0..1 (higher = more responsive, less smooth)

    // For fallback
    private float accelEmaX = 0f;
    private final float accelAlpha = 0.15f;

    public TiltController(Context ctx, TiltListener l) {
        listener = l;

        // Prevent crash in Android Studio preview mode
        if (ctx != null && !isEditModeContext(ctx)) {
            sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
            if (sm != null) {
                rotSensor = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
                accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }
    }

    public void start() {
        if (sm == null) return; // No sensors in preview mode
        baselineSet = false; // force re-calibration on start
        ema = 0f;
        if (rotSensor != null) {
            usingRotation = true;
            sm.registerListener(this, rotSensor, SensorManager.SENSOR_DELAY_GAME);
        } else if (accelSensor != null) {
            usingRotation = false;
            sm.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        if (sm != null) {
            sm.unregisterListener(this);
        }
    }

    // Let UI ask for a fresh baseline (e.g., on restart)
    public void calibrate() {
        baselineSet = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sm == null) return; // Skip in preview mode

        float analog; // [-1..1], right tilt positive

        if (usingRotation && event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);
            SensorManager.getOrientation(rotMatrix, orientation);
            float rollRad = orientation[2]; // +right-down/-left-up (depends on device)
            float rollDeg = (float) Math.toDegrees(rollRad);

            // Map to normalized range. 32° ~= full right; invert so right tilt is positive.
            analog = clamp(-rollDeg / 32f, -1f, 1f);
        } else if (!usingRotation && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Fallback: low-pass filter X axis, normalize by 1g
            accelEmaX += accelAlpha * (event.values[0] - accelEmaX);
            analog = clamp(-accelEmaX / 9.81f, -1f, 1f);
        } else {
            return;
        }

        if (!baselineSet) {
            baseline = analog;   // capture your natural hold as neutral
            baselineSet = true;
        }

        analog -= baseline;
        analog = clamp(analog, -1f, 1f);

        // Smooth
        ema += smoothAlpha * (analog - ema);

        // Deliver to game
        listener.onTilt(ema);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    /**
     * Detect if we’re in Android Studio’s layout preview mode.
     */
    private static boolean isEditModeContext(Context ctx) {
        try {
            return (Boolean) ctx.getClass().getMethod("isInEditMode").invoke(ctx);
        } catch (Exception e) {
            return false;
        }
    }
}
