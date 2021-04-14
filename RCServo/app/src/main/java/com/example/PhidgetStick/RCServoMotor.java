// Search CSCM79 Advice for adding Network Server etc.
package com.example.PhidgetStick;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.phidget22.*;

import java.text.DecimalFormat;

public class RCServoMotor extends Activity {

    RCServo ch;
    Button engagedButton;
    SeekBar accelerationBar;
    SeekBar velocityLimitBar;
    SeekBar targetPositionBar;

    Toast errToast;

    double minAcceleration;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rcservo);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Hide device information and settings until one is attached
        LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
        settingsAndData.setVisibility(LinearLayout.GONE);

        //set button functionality
        engagedButton = (Button) findViewById(R.id.engagedButton);
        engagedButton.setOnClickListener(new engagedChangeListener());

        //set acceleration seek bar functionality
        accelerationBar = (SeekBar) findViewById(R.id.accelerationBar);
        accelerationBar.setOnSeekBarChangeListener(new accelerationChangeListener());

        //set velocity limit seek bar functionality
        velocityLimitBar = (SeekBar) findViewById(R.id.velocityLimitBar);
        velocityLimitBar.setOnSeekBarChangeListener(new velocityLimitChangeListener());

        targetPositionBar = (SeekBar) findViewById(R.id.targetPositionBar);
        targetPositionBar.setOnSeekBarChangeListener(new targetPositionChangeListener());

        //hide acceleration and velocity controls
        ((LinearLayout)findViewById(R.id.accelerationSection)).setVisibility(LinearLayout.GONE);
        ((LinearLayout)findViewById(R.id.velocityLimitSection)).setVisibility(LinearLayout.GONE);
        ((LinearLayout)findViewById(R.id.velocityInfo)).setVisibility(LinearLayout.GONE);

        ((TextView)findViewById(R.id.velocityTxt)).setText("");
        ((TextView)findViewById(R.id.positionTxt)).setText("");

        try
        {
            ch = new RCServo();

            //Allow direct USB connection of Phidgets
            if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
                com.phidget22.usb.Manager.Initialize(this);

            //Enable server discovery to list remote Phidgets
            this.getSystemService(Context.NSD_SERVICE);
            Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);

            //CSCM79 Advice
            //Add a specific network server to communicate with Phidgets remotely
            Net.addServer("ServerName", "192.168.1.18", 5661, "", 0);

            ch.addAttachListener(new AttachListener() {
                public void onAttach(final AttachEvent attachEvent) {
                    AttachEventHandler handler = new AttachEventHandler(ch);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            ch.addDetachListener(new DetachListener() {
                public void onDetach(final DetachEvent detachEvent) {
                    DetachEventHandler handler = new DetachEventHandler(ch);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            ch.addErrorListener(new ErrorListener() {
                public void onError(final ErrorEvent errorEvent) {
                    ErrorEventHandler handler = new ErrorEventHandler(ch, errorEvent);
                    runOnUiThread(handler);

                }
            });

            ch.addPositionChangeListener(new RCServoPositionChangeListener() {
                public void onPositionChange(RCServoPositionChangeEvent positionChangeEvent) {
                    RCServoPositionChangeEventHandler handler = new RCServoPositionChangeEventHandler(ch, positionChangeEvent);
                    runOnUiThread(handler);
                }
            });

            ch.addVelocityChangeListener(new RCServoVelocityChangeListener() {
                public void onVelocityChange(RCServoVelocityChangeEvent velocityChangeEvent) {
                    RCServoVelocityChangeEventHandler handler = new RCServoVelocityChangeEventHandler(ch, velocityChangeEvent);
                    runOnUiThread(handler);
                }
            });

            ch.open();
        } catch (PhidgetException pe) {
            pe.printStackTrace();
        }

    }
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Main.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
    private class engagedChangeListener implements Button.OnClickListener {
        public void onClick(View v) {
            try {
                if(engagedButton.getText() == "Engage") {
                    ch.setEngaged(true);
                    engagedButton.setText("Disengage");
                }
                else {
                    ch.setEngaged(false);
                    engagedButton.setText("Engage");
                }
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        }
    }

    private class accelerationChangeListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(fromUser) {
                try {
                    TextView accelerationTxt = (TextView) findViewById(R.id.accelerationTxt);
                    DecimalFormat numberFormat = new DecimalFormat("#.##");
                    double acceleration = ((double) progress / seekBar.getMax()) *
                            (ch.getMaxAcceleration() - ch.getMinAcceleration()) + ch.getMinAcceleration();

                    accelerationTxt.setText(numberFormat.format(acceleration));
                    ch.setAcceleration(acceleration);
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    private class velocityLimitChangeListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(fromUser) {
                try {
                    TextView velocityLimitTxt = (TextView) findViewById(R.id.velocityLimitTxt);
                    DecimalFormat numberFormat = new DecimalFormat("#.##");
                    double velocityLimit = ((double) progress / seekBar.getMax()) *
                            (ch.getMaxVelocityLimit() - ch.getMinVelocityLimit()) + ch.getMinVelocityLimit();
                    velocityLimitTxt.setText(numberFormat.format(velocityLimit));
                    ch.setVelocityLimit(velocityLimit);
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    private class targetPositionChangeListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(fromUser) {
                try {
                    TextView targetPositionTxt = (TextView) findViewById(R.id.targetPositionTxt);
                    double targetPosition = Math.round(((double) progress / seekBar.getMax()) *
                            (ch.getMaxPosition() - ch.getMinPosition()) + ch.getMinPosition());
                    targetPositionTxt.setText(String.valueOf(targetPosition));
                    ch.setTargetPosition(targetPosition);
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    class AttachEventHandler implements Runnable {
        Phidget ch;

        public AttachEventHandler(Phidget ch) {
            this.ch = ch;
        }

        public void run() {
            LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
            settingsAndData.setVisibility(LinearLayout.VISIBLE);

            TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);

            attachedTxt.setText("Attached");
            try {
                TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
                TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
                TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
                TextView channelTxt = (TextView) findViewById(R.id.channelTxt);
                TextView hubPortTxt = (TextView) findViewById(R.id.hubPortTxt);
                TextView labelTxt = (TextView) findViewById(R.id.labelTxt);

                nameTxt.setText(ch.getDeviceName());
                serialTxt.setText(Integer.toString(ch.getDeviceSerialNumber()));
                versionTxt.setText(Integer.toString(ch.getDeviceVersion()));
                channelTxt.setText(Integer.toString(ch.getChannel()));
                hubPortTxt.setText(Integer.toString(ch.getHubPort()));
                labelTxt.setText(ch.getDeviceLabel());

                SeekBar targetPositionBar = (SeekBar) findViewById(R.id.targetPositionBar);
                targetPositionBar.setProgress(targetPositionBar.getMax()/2);

                double targetPosition = (((RCServo)ch).getMaxPosition() - ((RCServo)ch).getMinPosition())/2
                        + ((RCServo)ch).getMinPosition();

                TextView targetPositionTxt = (TextView) findViewById(R.id.targetPositionTxt);
                targetPositionTxt.setText(String.valueOf(targetPosition));

                ((RCServo)ch).setTargetPosition(targetPosition);

                switch(ch.getDeviceID()) {
                    case PN_1066:
                    case PN_1061:
                        TextView accelerationTxt = (TextView) findViewById(R.id.accelerationTxt);
                        accelerationTxt.setText(String.valueOf(((RCServo)ch).getAcceleration()));

                        SeekBar accelerationBar = (SeekBar) findViewById(R.id.accelerationBar);
                        accelerationBar.setProgress((int) ((((RCServo)ch).getAcceleration() - ((RCServo)ch).getMinAcceleration())
                                / (((RCServo)ch).getMaxAcceleration() - ((RCServo)ch).getMinAcceleration()) * accelerationBar.getMax()));

                        TextView velocityLimitTxt = (TextView) findViewById(R.id.velocityLimitTxt);
                        velocityLimitTxt.setText(String.valueOf(((RCServo)ch).getVelocityLimit()));

                        SeekBar velocityLimitBar = (SeekBar) findViewById(R.id.velocityLimitBar);
                        velocityLimitBar.setProgress((int) ((((RCServo)ch).getVelocityLimit() - ((RCServo)ch).getMinVelocityLimit())
                                / (((RCServo)ch).getMaxVelocityLimit() - ((RCServo)ch).getMinVelocityLimit()) * velocityLimitBar.getMax()));

                        ((LinearLayout)findViewById(R.id.accelerationSection)).setVisibility(LinearLayout.VISIBLE);
                        ((LinearLayout)findViewById(R.id.velocityLimitSection)).setVisibility(LinearLayout.VISIBLE);
                        ((LinearLayout)findViewById(R.id.velocityInfo)).setVisibility(LinearLayout.VISIBLE);
                        break;
                    default:
                        break;
                }

                engagedButton.setText("Engage");
            } catch (PhidgetException e) {
                e.printStackTrace();
            }

            //notify that we're done
            synchronized(this)
            {
                this.notify();
            }
        }
    }

    class DetachEventHandler implements Runnable {
        Phidget ch;

        public DetachEventHandler(Phidget ch) {
            this.ch = ch;
        }

        public void run() {
            LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);

            settingsAndData.setVisibility(LinearLayout.GONE);

            TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);
            attachedTxt.setText("Detached");

            TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
            TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
            TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
            TextView channelTxt = (TextView) findViewById(R.id.channelTxt);
            TextView hubPortTxt = (TextView) findViewById(R.id.hubPortTxt);
            TextView labelTxt = (TextView) findViewById(R.id.labelTxt);

            nameTxt.setText(R.string.unknown_val);
            serialTxt.setText(R.string.unknown_val);
            versionTxt.setText(R.string.unknown_val);
            channelTxt.setText(R.string.unknown_val);
            hubPortTxt.setText(R.string.unknown_val);
            labelTxt.setText(R.string.unknown_val);

            //hide acceleration and velocity controls on detach
            ((LinearLayout)findViewById(R.id.accelerationSection)).setVisibility(LinearLayout.GONE);
            ((LinearLayout)findViewById(R.id.velocityLimitSection)).setVisibility(LinearLayout.GONE);
            ((LinearLayout)findViewById(R.id.velocityInfo)).setVisibility(LinearLayout.GONE);

            ((TextView)findViewById(R.id.velocityTxt)).setText("");
            ((TextView)findViewById(R.id.positionTxt)).setText("");

            //notify that we're done
            synchronized(this)
            {
                this.notify();
            }
        }
    }

    class ErrorEventHandler implements Runnable {
        Phidget ch;
        ErrorEvent errorEvent;

        public ErrorEventHandler(Phidget ch, ErrorEvent errorEvent) {
            this.ch = ch;
            this.errorEvent = errorEvent;
        }

        public void run() {
            if (errToast == null)
                errToast = Toast.makeText(getApplicationContext(), errorEvent.getDescription(), Toast.LENGTH_SHORT);

            //replace the previous toast message if a new error occurs
            errToast.setText(errorEvent.getDescription());
            errToast.show();
        }
    }

    class RCServoPositionChangeEventHandler implements Runnable {
        Phidget ch;
        RCServoPositionChangeEvent positionChangeEvent;

        public RCServoPositionChangeEventHandler(Phidget ch, RCServoPositionChangeEvent positionChangeEvent) {
            this.ch = ch;
            this.positionChangeEvent = positionChangeEvent;
        }

        public void run() {
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            TextView positionTxt = (TextView)findViewById(R.id.positionTxt);
            positionTxt.setText(numberFormat.format(positionChangeEvent.getPosition()));
        }
    }

    class RCServoVelocityChangeEventHandler implements Runnable {
        Phidget ch;
        RCServoVelocityChangeEvent velocityChangeEvent;

        public RCServoVelocityChangeEventHandler(Phidget ch, RCServoVelocityChangeEvent velocityChangeEvent) {
            this.ch = ch;
            this.velocityChangeEvent = velocityChangeEvent;
        }

        public void run() {
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            TextView velocityTxt = (TextView)findViewById(R.id.velocityTxt);
            velocityTxt.setText(numberFormat.format(velocityChangeEvent.getVelocity()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ch.close();

        } catch (PhidgetException e) {
            e.printStackTrace();
        }

        //Disable USB connection to Phidgets
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
            com.phidget22.usb.Manager.Uninitialize();
    }

}

