package com.example.myfirstapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        value_x = (TextView) findViewById(R.id.value_x);
        value_y = (TextView) findViewById(R.id.value_y);
        value_z = (TextView) findViewById(R.id.value_z);

        start();
    }

    public void buttonPressed(View view) {
        if (mLastAccelerometerSet) {
            generateTone(mLastAccelerometer[0] * 50 + 50, 2);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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

    public void generateTone(double frequency, double duration) {
        int sampleRate = 8000;
        int numSamples = (int) Math.ceil(duration * sampleRate);

        double wave[] = new double[numSamples];
        byte sound[] = new byte[2 * numSamples];

        for (int i = 0; i < numSamples; ++i) {
            wave[i] = Math.sin(frequency * 2 * Math.PI * i / sampleRate);
        }

        // convert to 16 bit PCM sound array
        int idx = 0;
        int ramp = numSamples / 20 ;

        // Attack
        for (int i = 0; i < ramp; i++) {
            double sample = wave[i];
            final short val = (short) (sample * 32767 * (i/ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            sound[idx++] = (byte) (val & 0x00ff);
            sound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        // Sustain
        for (int i = ramp; i < numSamples - ramp; i++) {
            double sample = wave[i];
            final short val = (short) (sample * 32767);
            sound[idx++] = (byte) (val & 0x00ff);
            sound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        // Release
        for (int i = numSamples - ramp; i < numSamples; i++) {                               // Ramp amplitude down
            double sample = wave[i];
            final short val = (short) (sample * 32767 * (numSamples - i)/ramp);
            sound[idx++] = (byte) (val & 0x00ff);
            sound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        int bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        AudioTrack audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .build(),
                bufferSize,
                AudioTrack.MODE_STREAM,
                0
        );


        audioTrack.play();
        audioTrack.write(sound, 0, sound.length);
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