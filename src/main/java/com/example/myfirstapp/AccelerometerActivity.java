package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    TextView value_x;
    TextView value_y;
    TextView value_z;
    boolean haveSensor = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float[] mLastAccelerometer = new float[3];
    private boolean mLastAccelerometerSet = false;

    float lastX = 0;
    float lastY = 0;
    float lastZ = 0;

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        value_x = (TextView) findViewById(R.id.value_x);
        value_y = (TextView) findViewById(R.id.value_y);
        value_z = (TextView) findViewById(R.id.value_z);

        mp = new MediaPlayer();

        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if (mLastAccelerometerSet) {

                float changeX = Math.round(event.values[0]) - Math.round(mLastAccelerometer[0]);
                float changeY = Math.round(event.values[1]) - Math.round(mLastAccelerometer[1]);
                float changeZ = Math.round(event.values[2]) - Math.round(mLastAccelerometer[2]);

                double thresh = 2;
                double largest = Math.max(Math.abs(changeX), Math.max(Math.abs(changeY), Math.abs(changeZ)));

                if (largest == Math.abs(changeX)) {
                    if (changeX > thresh) {
                        playMedia(R.raw.s1_h);
                    } else if (changeX < -thresh) {
                        playMedia(R.raw.s1_l);
                    }
                }

                if (largest == Math.abs(changeY)) {
                    if (changeY > thresh) {
                        playMedia(R.raw.s2_l);
                    } else if (changeY < -thresh) {
                        playMedia(R.raw.s2_h);
                    }
                }

                if (largest == Math.abs(changeZ)) {
                    if (changeZ > thresh) {
                        playMedia(R.raw.s3_h);
                    } else if (changeZ < -thresh) {
                        playMedia(R.raw.s3_l);
                    }
                }
            }

            float[] values = event.values;

            mLastAccelerometer[0] = values[0];
            mLastAccelerometer[1] = values[1];
            mLastAccelerometer[2] = values[2];
            mLastAccelerometerSet = true;
        }

        value_x.setText(String.valueOf(mLastAccelerometer[0]));
        value_y.setText(String.valueOf(mLastAccelerometer[1]));
        value_z.setText(String.valueOf(mLastAccelerometer[2]));
    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 0.25f * (input[i] - output[i]);
        }
        return output;
    }

    private void playMedia(Integer source) {
        if (!mp.isPlaying()) {
            mp.stop();
            mp.release();
            mp = MediaPlayer.create(this, source);
            mp.start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            noSensorsAlert();
        } else {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            haveSensor = mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    public void stop() {
        mSensorManager.unregisterListener((SensorEventListener) this, mAccelerometer);
        mp.stop();
        mp.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }
}