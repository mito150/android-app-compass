package com.inalbyss.android.compass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

// TODO: add the ability to calibrate the compass but display a warning like:
/*
    if you are completely sure that your compass "is wrong" you can calibrate it.
    Take care, sometimes you have magnetic fields near that can alter north position.
    Calibrate your compass only if always give you wrong north position.
 */

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager sensorManager;

    private Window window;

    private ImageView pointerImage;
    private TextView textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pointerImage = (ImageView) findViewById(R.id.imageView2);
        textview = (TextView) findViewById(R.id.textView);

        // Starting sensor subsystem
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get window for keeping screen on
        window = getWindow();
    }


    /**
     * Query for sensor and register it to start receiving data
     */
    private void registerSensor() {
        // If we can start sensor subsystem
        if (sensorManager != null) {
            // Get sensor
            Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

            // If we have the required sensor
            if (sensorOrientation != null) {
                // Register the sensor listener to receive data updates
                sensorManager.registerListener(this, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
            }

            // If we don't have the sensor show error
            else textview.setText(R.string.error_no_sensor);
        }

        // Display an error if can't start sensor service
        else textview.setText(R.string.error_sensor_service);
    }


    /**
     * Add flag to window to keep screen on
     */
    private void startWakeLock() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Release the lock of the screen
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Unregister listener to "pause" sensor
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Restart receiving data from sensor after pause state
        registerSensor();

        // Re-create wake lock for screen
        startWakeLock();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // Synchronize access
        synchronized (this) {
            float angle = event.values[0];

            // Rotate pointer image using RotateAnimation
            RotateAnimation animation = new RotateAnimation(angle, angle, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(0);
            animation.setFillAfter(true);

            pointerImage.startAnimation(animation);

            textview.setText(decimalCut(angle) + "º");

            // Only update sensor info the first time
            /*if(textview.length() == 0) {
                textview.setText(event.sensor.getVendor() + " " + event.sensor.getName());
            }*/
        }
    }


    /**
     * Cut a float number to only 1 decimal
     * 2.3456 -> 2.3
     *
     * @param number to cut
     * @return cutted number
     */
    private String decimalCut(float number) {
        float decimalPart = number - (int) number;

        return String.valueOf((int) (number - decimalPart)) + "." + String.valueOf(decimalPart).charAt(2);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
