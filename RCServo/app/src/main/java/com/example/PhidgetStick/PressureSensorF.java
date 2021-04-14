package com.example.PhidgetStick;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.phidget22.*;

public class PressureSensorF extends Activity {

    PressureSensor ch;
    SeekBar dataIntervalBar;

    Toast errToast;

    int minDataInterval;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pressuresensor);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Hide device information and settings until one is attached
        LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
        settingsAndData.setVisibility(LinearLayout.GONE);

        //set data interval seek bar functionality
        dataIntervalBar = (SeekBar) findViewById(R.id.dataIntervalBar);
        dataIntervalBar.setOnSeekBarChangeListener(new dataIntervalChangeListener());

        try
        {
            ch = new PressureSensor();

            //Allow direct USB connection of Phidgets
            if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
                com.phidget22.usb.Manager.Initialize(this);

            //Enable server discovery to list remote Phidgets
            this.getSystemService(Context.NSD_SERVICE);
            Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);

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

            ch.addPressureChangeListener(new PressureSensorPressureChangeListener() {
                public void onPressureChange(PressureSensorPressureChangeEvent pressureChangeEvent) {
                    PressureSensorPressureChangeEventHandler handler = new PressureSensorPressureChangeEventHandler(ch, pressureChangeEvent);
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

    private class dataIntervalChangeListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            try {
                TextView dataIntervalTxt = (TextView) findViewById(R.id.dataIntervalTxt);
                int dataInterval = progress + minDataInterval;
                dataIntervalTxt.setText(String.valueOf(dataInterval));
                ch.setDataInterval(dataInterval);
            } catch (PhidgetException e) {
                e.printStackTrace();
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

                TextView dataIntervalTxt = (TextView) findViewById(R.id.dataIntervalTxt);
                dataIntervalTxt.setText(String.valueOf(((PressureSensor)ch).getDataInterval()));

                minDataInterval = ((PressureSensor)ch).getMinDataInterval();

                SeekBar dataIntervalBar = (SeekBar) findViewById(R.id.dataIntervalBar);
                dataIntervalBar.setProgress(((PressureSensor)ch).getDataInterval() - minDataInterval);

                //Limit the maximum dataInterval on the SeekBar to 5000 so it remains usable
                if(((PressureSensor)ch).getMaxDataInterval() >= 5000)
                    dataIntervalBar.setMax(5000 - minDataInterval);
                else
                    dataIntervalBar.setMax(((PressureSensor)ch).getMaxDataInterval() - minDataInterval);

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

            //clear data on detach
            ((TextView)findViewById(R.id.pressureTxt)).setText("");

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

    class PressureSensorPressureChangeEventHandler implements Runnable {
        Phidget ch;
        PressureSensorPressureChangeEvent pressureChangeEvent;

        public PressureSensorPressureChangeEventHandler(Phidget ch, PressureSensorPressureChangeEvent pressureChangeEvent) {
            this.ch = ch;
            this.pressureChangeEvent = pressureChangeEvent;
        }

        public void run() {
            TextView pressureTxt = (TextView)findViewById(R.id.pressureTxt);

            pressureTxt.setText(String.valueOf(pressureChangeEvent.getPressure()));
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

