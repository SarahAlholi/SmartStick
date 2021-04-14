package com.example.PhidgetStick;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import java.util.Locale;
//import static android.support.graphics.drawable.PathInterpolatorCompat.EPSILON;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;
import static java.lang.StrictMath.sqrt;

public class gyroSensor extends Activity {
    TextView textX, textY, textZ;
    SensorManager sensorManager;
    Sensor sensor;
    Sensor mSensorAccelerometer;
    Sensor mSensorMagnetometer;
    public Vibrator v;
    private float vibrateThreshold = 0;
    TextToSpeech tts;
    String text;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gyrosensor);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        vibrateThreshold = sensor.getMaximumRange() / 2;

        textX = (TextView) findViewById(R.id.textX);
        textY = (TextView) findViewById(R.id.textY);
        textZ = (TextView) findViewById(R.id.textZ);

        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        tts=new TextToSpeech(gyroSensor.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        text = "Start Walking no traffic";
                        tts.setLanguage(Locale.US);
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Main.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }


    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        text = "Please turn right there is traffic this side";
        tts.setLanguage(Locale.US);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(gyroListener);
    }

    public SensorEventListener gyroListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        private static final float NS2S = 1.0f / 1000000000.0f;
        private final float[] deltaRotationVector = new float[4];
        private float timestamp;

        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            textX.setText("X : " + (int) x + " rad/s");
            textY.setText("Y : " + (int) y + " rad/s");
            textZ.setText("Z : " + (int) z + " rad/s");

//            if (timestamp != 0) {
//                final float dT = (event.timestamp - timestamp) * NS2S;
//                // Axis of the rotation sample, not normalized yet.
//
//                // Calculate the angular speed of the sample
//                float omegaMagnitude = (float) sqrt(x*x + y*y + z*z);
//
//                // Normalize the rotation vector if it's big enough to get the axis
//                if (omegaMagnitude > EPSILON) {
//                    x /= omegaMagnitude;
//                    y /= omegaMagnitude;
//                    z /= omegaMagnitude;
//                }
//                // Integrate around this axis with the angular speed by the time step
//                // in order to get a delta rotation from this sample over the time step
//                // We will convert this axis-angle representation of the delta rotation
//                // into a quaternion before turning it into the rotation matrix.
//                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
//                float sinThetaOverTwo = (float) sin(thetaOverTwo);
//                float cosThetaOverTwo = (float) cos(thetaOverTwo);
//                deltaRotationVector[0] = sinThetaOverTwo * x;
//                deltaRotationVector[1] = sinThetaOverTwo * y;
//                deltaRotationVector[2] = sinThetaOverTwo * z;
//                deltaRotationVector[3] = cosThetaOverTwo;
//            }
//            timestamp = event.timestamp;
//            float[] deltaRotationMatrix = new float[9];
//            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
//            // User code should concatenate the delta rotation we computed with the current
//            // rotation in order to get the updated rotation.
//            // rotationCurrent = rotationCurrent * deltaRotationMatrix;
//
//            textX.setText("X : " + (int) deltaRotationVector[0] + " rad/s");
//            textY.setText("Y : " + (int) deltaRotationVector[1] + " rad/s");
//            textZ.setText("Z : " + (int) deltaRotationVector[2] + " rad/s");

            if(deltaRotationVector[1] > 1) {
                ConvertTextToSpeech();
                v.vibrate(50);
            }

        }

    };

}
