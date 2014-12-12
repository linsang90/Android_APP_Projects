package sl.rutgers.showdirection;

import java.text.DecimalFormat;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowDirection extends Activity implements SensorEventListener{

	private ImageView image;
	
	private float currentDegree = 0f;
	private float gravity[];
	private float linear_acceleration[];
	
	private SensorManager mSensorManager;
	
	TextView tvHeading;
	TextView accValue;
	TextView accGravity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_direction);
		linear_acceleration = new float[3];
		gravity = new float[3];
		
		image = (ImageView)findViewById(R.id.imageView1);
		tvHeading = (TextView) findViewById(R.id.directionValue);
		accValue = (TextView) findViewById(R.id.accValue);
		accGravity = (TextView) findViewById(R.id.accGravity);

		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
	}
	
	@Override
	protected void onPause() {
	super.onPause();
	// to stop the listener and save battery
	mSensorManager.unregisterListener(this);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		DecimalFormat df = new DecimalFormat("#.0");
		Sensor sensor = event.sensor;
		if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			
			final float alpha = (float) 0.8;

			  // Isolate the force of gravity with the low-pass filter.
			  gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			  gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			  gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
			  accValue.setText("accelerometer including the gravity:\nx = "+df.format(gravity[0])+" y = "+df.format(gravity[1])+" z = "+df.format(gravity[2]));

			  // Remove the gravity contribution with the high-pass filter.
			  linear_acceleration[0] = event.values[0] - gravity[0];
			  linear_acceleration[1] = event.values[1] - gravity[1];
			  linear_acceleration[2] = event.values[2] - gravity[2];
			  accGravity.setText("accelerometer: x = "+Math.round(linear_acceleration[0])+" y = "+Math.round(linear_acceleration[1])+" z = "+Math.round(linear_acceleration[2]));
		}
		else{
			float degree = Math.round(event.values[0]);
			tvHeading.setText("Heading: "+Float.toString(degree)+" degrees");
		
			RotateAnimation ra = new RotateAnimation(
					currentDegree,
					-degree,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
		
			ra.setDuration(210);
		
			ra.setFillAfter(true);
		
			image.startAnimation(ra);
			currentDegree = -degree;
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}
