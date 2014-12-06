package bcby.rutgers.parkinghelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import bcby.rutgers.parkinghelper.MainActivity.SpinnerSelectedListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ReportTicket extends Activity {

	private Button RepTick;
	private Button cancel;
	private double lat;
	private double lng;
	private static final String[] hours = {
		"00:00 - 03:00","03:00 - 06:00","06:00 - 09:00","09:00 - 12:00","12:00 - 15:00","15:00 - 18:00","18:00 - 21:00"
		,"21:00 - 00:00"};
	private Spinner spinner;
	private ArrayAdapter<String> adapter;
	private String hour;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket_text);
		lat = getIntent().getDoubleExtra("lat", 0);
		lng = getIntent().getDoubleExtra("lng", 0);
		
		spinner = (Spinner)findViewById(R.id.spinner);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,hours);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		spinner.setVisibility(View.VISIBLE);
		
		RepTick = (Button)findViewById(R.id.ReportTicket);
		RepTick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new CommunicateTask().execute("ticket "+lat+" "+lng+" "+hour);
				Toast.makeText(getApplicationContext(), "Reported!", Toast.LENGTH_LONG).show();
				ReportTicket.this.finish();
			}
		});
		cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			ReportTicket.this.finish();
				
			}
		});
	}
	
	public class CommunicateTask extends AsyncTask<String, String, String> {
		private final String HOST_IP = "54.148.79.131";
		private final int PORT_NUMBER = 10003;
	     
		protected String doInBackground(String... str) {
			String msg = "";
			String command = str[0];
			
				
				SocketClient client = new SocketClient(HOST_IP,PORT_NUMBER);
				try {
					//Log.i(TAG, "Connecting: "+command);
					msg += client.communicate(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
	 		
	 		return msg;
		}

	     protected void onProgressUpdate(String... progress) {
	         
	     }

	     protected void onPostExecute(String result) {
	     }
	 }
	
	class SpinnerSelectedListener implements OnItemSelectedListener{  
		  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {
        	int inthour = arg2 *3;
        	if(inthour>10) {
            hour = Integer.toString(inthour);
        	}
        	else {
        		hour = "0"+ Integer.toString(inthour);
        	}
        	Log.i("SL",hour);
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
        }  
    }  
	

}
