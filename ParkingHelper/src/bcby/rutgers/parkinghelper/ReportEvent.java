package bcby.rutgers.parkinghelper;

import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ReportEvent extends Activity {

	private Button RepEve;
	private Button cancel;
	private double lat;
	private double lng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_text);
		
		lat = getIntent().getDoubleExtra("lat", 0);
		lng = getIntent().getDoubleExtra("lng", 0);
		
		RepEve = (Button)findViewById(R.id.ReportEvent);
		RepEve.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new CommunicateTask().execute("report "+lat+" "+lng);
				Toast.makeText(getApplicationContext(), "Reported!", Toast.LENGTH_LONG).show();
				ReportEvent.this.finish();
			}
		});
		cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			ReportEvent.this.finish();
				
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
	
}
