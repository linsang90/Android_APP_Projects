package bcby.rutgers.parkinghelper;

import java.io.IOException;
import java.io.OutputStreamWriter;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;  
import android.widget.AdapterView.OnItemSelectedListener; 
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends ActionBarActivity {
	private static final String[] permitTypes = {
		"Faculty/Staff","Resident","StudentA","StudentB",
		"StudentC","StudentD","StudentL","StudentH","Night Commuter"};
	private final String TAG = "SL";
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	String permit;
	private Button button;
	private int permit_id = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		spinner = (Spinner)findViewById(R.id.spinner);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,permitTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		spinner.setVisibility(View.VISIBLE);
		
		button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new 
						Intent("bcby.rutgers.parkinghelper.ParkingActivity"); 
				i.putExtra("permit_id", permit_id);
				startActivity(i);
			}
		});
	}
	
	class SpinnerSelectedListener implements OnItemSelectedListener{  
		  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {
        	permit_id = arg2;
            permit = permitTypes[arg2];  
            writeToFile(Integer.toString(permit_id));
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
        }  
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
		int id = item.getItemId();
		if (id == R.id.action_search) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void writeToFile(String data) {
	    try {
	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", Context.MODE_PRIVATE));
	        outputStreamWriter.write(data);
	        outputStreamWriter.close();
	    }
	    catch (IOException e) {
	        Log.e(TAG, "File write failed: " + e.toString());
	    } 
	}

}
