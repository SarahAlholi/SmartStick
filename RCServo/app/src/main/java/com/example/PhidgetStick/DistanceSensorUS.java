package com.example.PhidgetStick;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.phidget22.*;

public class DistanceSensorUS extends Activity {

    DistanceSensor ch;
    SeekBar dataIntervalBar;
    CheckBox quietModeBox;

    Toast errToast;

    int minDataInterval;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distancesensor);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Hide device information and settings until one is attached
        LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
        settingsAndData.setVisibility(LinearLayout.GONE);

        LinearLayout sonarSection = (LinearLayout) findViewById(R.id.sonarSection);
        sonarSection.setVisibility(LinearLayout.GONE);

        //set data interval seek bar functionality
        dataIntervalBar = (SeekBar) findViewById(R.id.dataIntervalBar);
        dataIntervalBar.setOnSeekBarChangeListener(new dataIntervalChangeListener());

        //set quiet mode functionality for sonar
        quietModeBox = (CheckBox) findViewById(R.id.quietModeBox);
        quietModeBox.setOnCheckedChangeListener(new quietModeChangeListener());

        try
        {
            ch = new DistanceSensor();

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

            ch.addDistanceChangeListener(new DistanceSensorDistanceChangeListener() {
                public void onDistanceChange(DistanceSensorDistanceChangeEvent distanceChangeEvent) {
                    DistanceSensorDistanceChangeEventHandler handler = new DistanceSensorDistanceChangeEventHandler(ch, distanceChangeEvent);
                    runOnUiThread(handler);
                }
            });

            ch.addSonarReflectionsUpdateListener(new DistanceSensorSonarReflectionsUpdateListener() {
                public void onSonarReflectionsUpdate(DistanceSensorSonarReflectionsUpdateEvent sonarReflectionsUpdateEvent) {
                    DistanceSensorSonarReflectionsUpdateEventHandler handler =
                            new DistanceSensorSonarReflectionsUpdateEventHandler(ch, sonarReflectionsUpdateEvent);
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

    private class quietModeChangeListener implements CheckBox.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            try {
                ch.setSonarQuietMode(isChecked);
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        }
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
                dataIntervalTxt.setText(String.valueOf(((DistanceSensor)ch).getDataInterval()));

                minDataInterval = ((DistanceSensor)ch).getMinDataInterval();

                SeekBar dataIntervalBar = (SeekBar) findViewById(R.id.dataIntervalBar);
                dataIntervalBar.setProgress(((DistanceSensor)ch).getDataInterval() - minDataInterval);

                //Limit the maximum dataInterval on the SeekBar to 5000 so it remains usable
                if(((DistanceSensor)ch).getMaxDataInterval() >= 5000)
                    dataIntervalBar.setMax(5000 - minDataInterval);
                else
                    dataIntervalBar.setMax(((DistanceSensor)ch).getMaxDataInterval() - minDataInterval);

                //if the ch is a sonar, display the relevant information and controls
                if(ch.getDeviceID() == DeviceID.PN_DST1200) {
                    quietModeBox.setChecked(((DistanceSensor)ch).getSonarQuietMode());

                    ((TextView)findViewById(R.id.distance0Txt)).setText("");
                    ((TextView)findViewById(R.id.distance1Txt)).setText("");
                    ((TextView)findViewById(R.id.distance2Txt)).setText("");
                    ((TextView)findViewById(R.id.distance3Txt)).setText("");
                    ((TextView)findViewById(R.id.distance4Txt)).setText("");
                    ((TextView)findViewById(R.id.distance5Txt)).setText("");
                    ((TextView)findViewById(R.id.distance6Txt)).setText("");
                    ((TextView)findViewById(R.id.distance7Txt)).setText("");

                    ((TextView)findViewById(R.id.amplitude0Txt)).setText("");
                    ((TextView)findViewById(R.id.amplitude1Txt)).setText("");
                    ((TextView)findViewById(R.id.amplitude2Txt)).setText("");
                    ((TextView)findViewById(R.id.amplitude3Txt)).setText("");
                    ((TextView)findViewById(R.id.amplitude4Txt)).setText("");
                    ((TextView)findViewById(R.id.amplitude5Txt)).setText("");
                    ((TextView)findViewById(R.id.amplitude6Txt)).setText("");
                    ((TextView)findViewById(R.id.amplitude7Txt)).setText("");

                    ((LinearLayout)findViewById(R.id.sonarSection)).setVisibility(LinearLayout.VISIBLE);
                }
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

            ((TextView)findViewById(R.id.distanceTxt)).setText("");
            ((LinearLayout)findViewById(R.id.sonarSection)).setVisibility(LinearLayout.GONE);

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

    class DistanceSensorDistanceChangeEventHandler implements Runnable {
        Phidget ch;
        DistanceSensorDistanceChangeEvent distanceChangeEvent;

        public DistanceSensorDistanceChangeEventHandler(Phidget ch, DistanceSensorDistanceChangeEvent distanceChangeEvent) {
            this.ch = ch;
            this.distanceChangeEvent = distanceChangeEvent;
        }

        public void run() {
            TextView distanceTxt = (TextView)findViewById(R.id.distanceTxt);

            distanceTxt.setText(String.valueOf(distanceChangeEvent.getDistance()));
        }
    }

    class DistanceSensorSonarReflectionsUpdateEventHandler implements Runnable {
        Phidget ch;
        DistanceSensorSonarReflectionsUpdateEvent e;

        public DistanceSensorSonarReflectionsUpdateEventHandler(DistanceSensor ch,
                                                                DistanceSensorSonarReflectionsUpdateEvent e) {
            this.ch = ch;
            this.e = e;
        }

        public void run() {
            TextView reflectionCountTxt = (TextView)findViewById(R.id.reflectionCountTxt);
            TextView distance0Txt = (TextView)findViewById(R.id.distance0Txt);
            TextView distance1Txt = (TextView)findViewById(R.id.distance1Txt);
            TextView distance2Txt = (TextView)findViewById(R.id.distance2Txt);
            TextView distance3Txt = (TextView)findViewById(R.id.distance3Txt);
            TextView distance4Txt = (TextView)findViewById(R.id.distance4Txt);
            TextView distance5Txt = (TextView)findViewById(R.id.distance5Txt);
            TextView distance6Txt = (TextView)findViewById(R.id.distance6Txt);
            TextView distance7Txt = (TextView)findViewById(R.id.distance7Txt);

            TextView amplitude0Txt = (TextView)findViewById(R.id.amplitude0Txt);
            TextView amplitude1Txt = (TextView)findViewById(R.id.amplitude1Txt);
            TextView amplitude2Txt = (TextView)findViewById(R.id.amplitude2Txt);
            TextView amplitude3Txt = (TextView)findViewById(R.id.amplitude3Txt);
            TextView amplitude4Txt = (TextView)findViewById(R.id.amplitude4Txt);
            TextView amplitude5Txt = (TextView)findViewById(R.id.amplitude5Txt);
            TextView amplitude6Txt = (TextView)findViewById(R.id.amplitude6Txt);
            TextView amplitude7Txt = (TextView)findViewById(R.id.amplitude7Txt);

            int count = e.getCount();

            reflectionCountTxt.setText(String.valueOf(count));
            distance0Txt.setText((0 < count) ? String.valueOf(e.getDistances()[0]) : "");
            distance1Txt.setText((1 < count) ? String.valueOf(e.getDistances()[1]) : "");
            distance2Txt.setText((2 < count) ? String.valueOf(e.getDistances()[2]) : "");
            distance3Txt.setText((3 < count) ? String.valueOf(e.getDistances()[3]) : "");
            distance4Txt.setText((4 < count) ? String.valueOf(e.getDistances()[4]) : "");
            distance5Txt.setText((5 < count) ? String.valueOf(e.getDistances()[5]) : "");
            distance6Txt.setText((6 < count) ? String.valueOf(e.getDistances()[6]) : "");
            distance7Txt.setText((7 < count) ? String.valueOf(e.getDistances()[7]) : "");

            amplitude0Txt.setText((0 < count) ? String.valueOf(e.getAmplitudes()[0]) : "");
            amplitude1Txt.setText((1 < count) ? String.valueOf(e.getAmplitudes()[1]) : "");
            amplitude2Txt.setText((2 < count) ? String.valueOf(e.getAmplitudes()[2]) : "");
            amplitude3Txt.setText((3 < count) ? String.valueOf(e.getAmplitudes()[3]) : "");
            amplitude4Txt.setText((4 < count) ? String.valueOf(e.getAmplitudes()[4]) : "");
            amplitude5Txt.setText((5 < count) ? String.valueOf(e.getAmplitudes()[5]) : "");
            amplitude6Txt.setText((6 < count) ? String.valueOf(e.getAmplitudes()[6]) : "");
            amplitude7Txt.setText((7 < count) ? String.valueOf(e.getAmplitudes()[7]) : "");
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

