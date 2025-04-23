package com.example.msway.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SensorManager implements SensorEventListener {
    private static final String TAG = "SensorManager";

    public interface OnCadenceMeasuredListener {
        void onCadenceMeasured(float cadence);
    }

    private Context context;
    private android.hardware.SensorManager systemSensorManager;
    private Sensor accelerometer;
    private OnCadenceMeasuredListener listener;

    private boolean isMeasuring = false;
    private List<Long> stepTimestamps = new ArrayList<>();
    private float currentCadence = 0;

    // Thresholds for step detection
    private static final float ACCELERATION_THRESHOLD = 10.0f;
    private static final long MIN_STEP_INTERVAL_MS = 200; // Minimum time between steps

    // Variables for step detection
    private float prevAcceleration = 0;
    private long lastStepTime = 0;
    private Handler cadenceUpdateHandler = new Handler();
    private Runnable cadenceUpdateRunnable;

    public SensorManager(Context context) {
        this.context = context;
        systemSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (systemSensorManager != null) {
            accelerometer = systemSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Runnable to calculate and update cadence every second
        cadenceUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isMeasuring) {
                    calculateCadence();
                    cadenceUpdateHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    public boolean checkSensorsAvailable() {
        return systemSensorManager != null && accelerometer != null;
    }

    public void startMeasuring(OnCadenceMeasuredListener listener) {
        if (!checkSensorsAvailable()) {
            Log.e(TAG, "Sensors not available");
            return;
        }

        this.listener = listener;

        // Reset variables
        stepTimestamps.clear();
        lastStepTime = 0;
        currentCadence = 0;

        // Register the sensor listener
        systemSensorManager.registerListener(this, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        isMeasuring = true;

        // Start regular cadence updates
        cadenceUpdateHandler.post(cadenceUpdateRunnable);

        Log.d(TAG, "Sensor measurement started");
    }

    public void stopMeasuring() {
        if (isMeasuring) {
            systemSensorManager.unregisterListener(this);
            cadenceUpdateHandler.removeCallbacks(cadenceUpdateRunnable);
            isMeasuring = false;
            Log.d(TAG, "Sensor measurement stopped");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            processAccelerometerData(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void processAccelerometerData(SensorEvent event) {
        // Calculate the total acceleration
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration = (float) Math.sqrt(x*x + y*y + z*z);

        // Basic step detection algorithm
        long currentTime = System.currentTimeMillis();

        // Look for a peak in acceleration that exceeds the threshold
        if (acceleration > ACCELERATION_THRESHOLD && prevAcceleration <= ACCELERATION_THRESHOLD
                && (currentTime - lastStepTime) > MIN_STEP_INTERVAL_MS) {

            // Record this as a step
            lastStepTime = currentTime;
            stepTimestamps.add(currentTime);

            // Maintain only the last 10 steps for cadence calculation
            while (stepTimestamps.size() > 10) {
                stepTimestamps.remove(0);
            }
        }

        prevAcceleration = acceleration;
    }

    private void calculateCadence() {
        if (stepTimestamps.size() < 2) {
            currentCadence = 0;
        } else {
            // Calculate time difference between first and last step (in milliseconds)
            long firstStep = stepTimestamps.get(0);
            long lastStep = stepTimestamps.get(stepTimestamps.size() - 1);
            long duration = lastStep - firstStep;

            if (duration > 0) {
                // Calculate steps per minute
                float stepsPerMs = (float) (stepTimestamps.size() - 1) / duration;
                currentCadence = stepsPerMs * 60000; // Convert to steps per minute
            } else {
                currentCadence = 0;
            }
        }

        // Notify the listener with the current cadence
        if (listener != null) {
            // Use Activity's runOnUiThread to ensure UI updates happen on the main thread
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onCadenceMeasured(currentCadence);
                    }
                });
            } else {
                listener.onCadenceMeasured(currentCadence);
            }
        }
    }
}
