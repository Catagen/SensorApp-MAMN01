package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    ImageView compass_img;
    TextView txt_compass;
    int mAzimuth;
    String lastDirection;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt_azimuth);

        mediaPlayer = new MediaPlayer();

        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-mAzimuth);

        String where = "NW";

        if (mAzimuth >= 350 || mAzimuth <= 10) {
            where = "N";
            getWindow().getDecorView().setBackgroundColor(0xFFf5ef4c);
            if (lastDirection != where) playMedia(R.raw.s8);
        } else {
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        }
        if (mAzimuth < 350 && mAzimuth > 280) {
            where = "NW";
            if (lastDirection != where) playMedia(R.raw.s7);
        }
        if (mAzimuth <= 280 && mAzimuth > 260) {
            where = "W";
            if (lastDirection != where) playMedia(R.raw.s6);
        }
        if (mAzimuth <= 260 && mAzimuth > 190) {
            where = "SW";
            if (lastDirection != where) playMedia(R.raw.s5);
        }
        if (mAzimuth <= 190 && mAzimuth > 170) {
            where = "S";
            if (lastDirection != where) playMedia(R.raw.s4);
        }
        if (mAzimuth <= 170 && mAzimuth > 100) {
            where = "SE";
            if (lastDirection != where) playMedia(R.raw.s3);
        }
        if (mAzimuth <= 100 && mAzimuth > 80) {
            where = "E";
            if (lastDirection != where) playMedia(R.raw.s2);
        }
        if (mAzimuth <= 80 && mAzimuth > 10) {
            where = "NE";
            if (lastDirection != where) playMedia(R.raw.s1);
        }


        txt_compass.setText(mAzimuth + "° " + where);
        lastDirection = where;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            }
            else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                haveSensor2 = mSensorManager.registerListener((SensorEventListener) this, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
        else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener((SensorEventListener) this, mRotationV, SensorManager.SENSOR_DELAY_FASTEST);
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
        if (haveSensor) {
            mSensorManager.unregisterListener((SensorEventListener) this, mRotationV);
        }
        else {
            mSensorManager.unregisterListener((SensorEventListener) this, mAccelerometer);
            mSensorManager.unregisterListener((SensorEventListener) this, mMagnetometer);
        }

        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void playMedia(Integer source) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, source);
            mediaPlayer.start();
        }
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
