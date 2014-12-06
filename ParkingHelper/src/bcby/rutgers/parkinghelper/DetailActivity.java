package bcby.rutgers.parkinghelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

		/**  Detail of clicked lot  **/
public class DetailActivity extends Activity{
	
	public double mylat;
	public double mylng;
	
	public String name;
	private String day;
	
	public float x1,x2;
    public float y1, y2;
    
	private TextView lotname;
	private TextView campus;
	private TextView address;
	private TextView time;
	private Button direction;
	private Lot lot;
	private Location location;
	private ImageView image;
	


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		/**  get lot information from former activity  **/
		location = new Location("dummyprovider");;
		name = getIntent().getStringExtra("name");
		mylat = getIntent().getDoubleExtra("mylat", 0);
		mylng = getIntent().getDoubleExtra("mylng", 0);
		lot = (Lot) getIntent().getSerializableExtra("Lot");
		day = getIntent().getStringExtra("day");
		
		/**  initialization  **/
		lotname = (TextView)findViewById(R.id.name);
		campus = (TextView)findViewById(R.id.campus);
		address = (TextView)findViewById(R.id.address);
		time = (TextView)findViewById(R.id.time);
		location.setLatitude(lot.latitude);
		location.setLongitude(lot.longitude);
		(new GetAddressTask(this)).execute(location);
		lotname.setText("LOT "+lot.name);
		campus.setText("Campus: "+lot.location);
		if(lot.type.equals("0")) {
			time.setText("Parking time :Arranged by event");
		}
		else {
			if(day.equals("weekeend")) {
				time.setText("All day");
			}
			else {
				time.setText("Pakring time: "+lot.startTime+" - "+lot.endTime);
			}
		}
		direction = (Button)findViewById(R.id.direction);
		direction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", mylat, mylng, lot.latitude, lot.longitude);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(intent);
			}
		});
		image = (ImageView) findViewById(R.id.imageView1);
		new GetImageTask().execute(lot.id);
		
	}
	
	/**  get ticket image **/
	public class GetImageTask extends AsyncTask<Integer, String, Bitmap> {
		private final String HOST_IP = "54.148.79.131";
		private final int PORT_NUMBER = 10003;
	     
		protected Bitmap doInBackground(Integer... param) {
			String msg = "";
			String command = "getImg "+param[0];
			Bitmap img = null;
				
				SocketClient client = new SocketClient(HOST_IP,PORT_NUMBER);
				try {
					img = client.getBitMap(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
	 		return img;
	     }

	     protected void onProgressUpdate(String... progress) {
	     }

	     protected void onPostExecute(Bitmap img) {
	    	 	image.setImageBitmap(img);
	     }
	 }
	
	public boolean onTouchEvent(MotionEvent touchevent) 
    {
                 switch (touchevent.getAction())
                 {
                        // when user first touches the screen we get x and y coordinate
                         case MotionEvent.ACTION_DOWN: 
                         {
                             x1 = touchevent.getX();
                             y1 = touchevent.getY();
                             break;
                        }
                         case MotionEvent.ACTION_UP: 
                         {
                             x2 = touchevent.getX();
                             y2 = touchevent.getY(); 

                             //if left to right sweep event on screen
                             if (x1 < x2) 
                             {
                            	 this.finish();
                         	    overridePendingTransition  (R.anim.right_slide_in, R.anim.right_slide_out);
                              }
                             break;
                         }
                 }
                 return false;
    }

	/**  Event after clicking back  **/
	@Override
	public void onBackPressed() 
	{
	    this.finish();
	    overridePendingTransition  (R.anim.right_slide_in, R.anim.right_slide_out);
	}
	
	/**  get address **/
	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		
		Context mContext;
		
		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}
		
		@Override
		protected String doInBackground(Location... params) {
			int currentid = android.os.Process.getThreadPriority(android.os.Process.myTid());
			Geocoder geocoder = 
					new Geocoder(mContext,Locale.US);
			Location loc = params[0];
			List<Address>addresses = null;
			try {
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch(IOException e1) {
				Log.e("LocationSampleActivity",
	                    "IO Exception in getFromLocation()");
	            e1.printStackTrace();
	            return ("IO Exception trying to get address");
			}catch (IllegalArgumentException e2) {
	            // Error message to post in the log
	            String errorString = "Illegal arguments " +
	                    Double.toString(loc.getLatitude()) +
	                    " , " +
	                    Double.toString(loc.getLongitude()) +
	                    " passed to address service";
	            Log.e("LocationSampleActivity", errorString);
	            e2.printStackTrace();
	            return errorString;
			}
			if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
		}
		@Override
        protected void onPostExecute(String ads) {
            // Display the results of the lookup.
            address.setText("Address: " + ads);
        }
	}

}
