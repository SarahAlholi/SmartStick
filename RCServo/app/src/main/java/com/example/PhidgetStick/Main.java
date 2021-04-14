package com.example.PhidgetStick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.ActionBar;

public class Main extends Activity {

	TextView textView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		textView = (TextView) findViewById(R.id.text);
	}

	public void Ganesh(View View)
	{
		String button_text;
		button_text =((Button)View).getText().toString();
		if(button_text.equals("Distance"))
		{
			Intent distance = new Intent(this,DistanceSensorUS.class);
			startActivity(distance);
		}
		else if (button_text.equals("RCServoMotor"))
		{
			Intent rcmotor = new Intent(this,RCServoMotor.class);
			startActivity(rcmotor);

		}
        else if (button_text.equals("Pressure"))
        {
            Intent pressure = new Intent(this,PressureSensorF.class);
            startActivity(pressure);

        }
		else if (button_text.equals("gyrosensor"))
		{
			Intent gyro = new Intent(this,gyroSensor.class);
			startActivity(gyro);

		}
	}
}