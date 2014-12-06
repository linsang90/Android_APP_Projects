package bcby.rutgers.parkinghelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.SearchManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ParkingActivity extends FragmentActivity implements LoaderCallbacks<Cursor>,SensorEventListener, OnMarkerClickListener{
	
	private final String TAG = "SL";
	
	private int userIcon;
	private int eventIcon;
	private int permitIcon;
	private int permit_id;
	
	private double latitude = 0;
    private double longitude = 0;
    private long lastUpdate;
    private String day;
    
	private LocationManager locMan;
	private Marker userMarker;
	private GoogleMap map;
	private List<Lot> listLot;
    private Marker marker;
    private SensorManager sensorManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acivity_parking);
		
		listLot = new ArrayList<Lot>();
		permit_id = Integer.parseInt(readFromFile());
		
		MapsInitializer.initialize(getApplicationContext());
		
		userIcon = R.drawable.yellow_point;
		eventIcon = R.drawable.event_point;
		switch(permit_id) {
		case 0: {permitIcon = R.drawable.red_point;break;}
		case 1: {permitIcon = R.drawable.green_point;break;}
		case 2: {permitIcon = R.drawable.blue_point;break;}
		case 3: {permitIcon = R.drawable.blue_point;break;}
		case 4: {permitIcon = R.drawable.blue_point;break;}
		case 5: {permitIcon = R.drawable.blue_point;break;}
		case 6: {permitIcon = R.drawable.blue_point;break;}
		case 7: {permitIcon = R.drawable.blue_point;break;}
		case 8: {permitIcon = R.drawable.purple_point;break;}
		}
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if(map == null) {
			Toast.makeText(this, "Google maps not available", Toast.LENGTH_LONG).show();
		}
		else {
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			map.setOnMarkerClickListener(this);
		}
		
		updatePlaces();
		
		handleIntent(getIntent());
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		String time = dateFormat.format(calendar.getTime()).toString();
		int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
		day = "weekday";
		if(day_of_week>5){
			day = "weekend";
		}
		if(permit_id != -1){
			new CommunicateTask().execute("request " + permit_id + " "+day+" "+time+" "+latitude+" "+longitude);
		}
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		lastUpdate = System.currentTimeMillis();
	}
	
	private void updatePlaces() {
		
		locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location lastLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		latitude = lastLoc.getLatitude();
		longitude = lastLoc.getLongitude();
		LatLng lastLatLng = new LatLng(latitude, longitude);
		if(userMarker!=null) userMarker.remove();
		userMarker = map.addMarker(new MarkerOptions()
	    .position(lastLatLng)
	    .title("You")
	    .icon(BitmapDescriptorFactory.fromResource(userIcon))
	    .snippet("Your last recorded location"));
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 12));
		map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
	}
	@Override
    public boolean onMarkerClick(final Marker marker) {

        if (!marker.equals(userMarker)) 
        {
        	Intent i = new 
					Intent("bcby.rutgers.parkinghelper.DetailActivity"); 
			i.putExtra("name", marker.getTitle());
			for(int j=0;j<listLot.size();j++) {
				if(listLot.get(j).name.equals(marker.getTitle()))	{
    			Lot lot = (Lot) listLot.get(j);
    			i.putExtra("mylat", latitude);
    			i.putExtra("mylng", longitude);
    			i.putExtra("Lot", lot);
    			i.putExtra("day", day);
    			startActivity(i);
    			overridePendingTransition  (R.anim.left_slide_in, R.anim.left_slide_out);
    			}
			}
        }
        return true;
    }
	
	 private void handleIntent(Intent intent){
	        if(intent.getAction().equals(Intent.ACTION_SEARCH)){
	            doSearch(intent.getStringExtra(SearchManager.QUERY));
	        }else if(intent.getAction().equals(Intent.ACTION_VIEW)){
	            getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
	        }
	    }
	 
	 private void doSearch(String query){
	        Bundle data = new Bundle();
	        data.putString("query", query);
	        getSupportLoaderManager().restartLoader(0, data, this);
	    }
	 
	 @Override
	    protected void onNewIntent(Intent intent) {
	        super.onNewIntent(intent);
	        setIntent(intent);
	        intent.putExtra("permit_id", permit_id);
	        handleIntent(intent);
	    }
	 
	 private void getPlace(String query){
	        Bundle data = new Bundle();
	        data.putString("query", query);
	        getSupportLoaderManager().restartLoader(1, data, this);
	    }
	 
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu
	        getMenuInflater().inflate(R.menu.parking_menu, menu);
	        return true;
	    }
	    
	 @Override
		public boolean onOptionsItemSelected(MenuItem item) {

			// Handle item selection
			switch (item.getItemId()) {

			// Toggle traffic overlay
			case R.id.action_search:
	            onSearchRequested();
	            return true;
	            
	        case R.id.report_event:
	        	Intent i = new Intent(this, ReportEvent.class);
				startActivity(i);
				i.putExtra("lat", latitude);
				i.putExtra("lng", longitude);
	        	return true;
	            
	        case R.id.report_ticket:
	        	Intent k = new Intent(this, ReportTicket.class);
				startActivity(k);
				k.putExtra("lat", latitude);
				k.putExtra("lng", longitude);
	            return true;
	        
	        case R.id.contact_us:
	        	Intent j = new Intent(this, ContactUs.class);
				startActivity(j);
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
	 
	    @Override
	    public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
	        CursorLoader cLoader = null;
	        if(arg0==0)
	            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.SEARCH_URI, null, null, new String[]{ query.getString("query") }, null);
	        else if(arg0==1)
	            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.DETAILS_URI, null, null, new String[]{ query.getString("query") }, null);
	        return cLoader;
	    }
	 
	    @Override
	    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
	        showLocations(c);
	    }
	 
	    @Override
	    public void onLoaderReset(Loader<Cursor> arg0) {
	    }
	 
	    private void showLocations(Cursor c){
	        MarkerOptions markerOptions = null;
	        LatLng position = null;
	        while(c.moveToNext()){
	            markerOptions = new MarkerOptions();
	            position = new LatLng(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)));
	            markerOptions.position(position);
	            markerOptions.title(c.getString(0));
	            map.addMarker(markerOptions);
	        }
	        if(position!=null){
	            CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(position);
	            map.animateCamera(cameraPosition);
	        }
	    }
	
	LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	latitude = location.getLatitude();
     		longitude = location.getLongitude();
     		LatLng currentPosition = new LatLng(latitude, longitude);
     		if(userMarker!=null) {
     			userMarker.setPosition(currentPosition);
     		}
     		else {
     			userMarker = map.addMarker(new MarkerOptions()
     		    .position(currentPosition)
     		    .title("You")
     		    .icon(BitmapDescriptorFactory.fromResource(userIcon))
     		    .snippet("Your last recorded location"));
     			map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 12));
     			map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
     		}
        }
        
        public void onStatusChanged(String provider, int status, Bundle extras) {
	  }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
      };
      
	public class CommunicateTask extends AsyncTask<String, String, String> {
		private final String HOST_IP = "54.148.79.131";
		private final int PORT_NUMBER = 10003;
	     
		protected String doInBackground(String... str) {
			Log.i(TAG,"yes,it comes here0....");
			String msg = "";
			String command = str[0];
			
				SocketClient client = new SocketClient(HOST_IP,PORT_NUMBER);
				try {
					Log.i(TAG, "Connecting: "+command);
					msg += client.communicate(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
	 		return msg;
	     }

	     protected void onProgressUpdate(String... progress) {
	     }

	     protected void onPostExecute(String result) {

		 		String ll = "Parking lots:\n";		 		
				try {
					JSONArray newJArray = new JSONArray(result);
					for(int i=0;i<newJArray.length();i++){
						int id = newJArray.getJSONObject(i).getInt("id");
						String name = newJArray.getJSONObject(i).getString("name");
						ll += name+", ";
						String location = newJArray.getJSONObject(i).getString("location");
						double longitude = newJArray.getJSONObject(i).getDouble("longitude");
						double latitude = newJArray.getJSONObject(i).getDouble("latitude");
						String type = newJArray.getJSONObject(i).getString("type");
						String startTime = newJArray.getJSONObject(i).getString("start_time");
						String endTime = newJArray.getJSONObject(i).getString("end_time");
						Lot lot = new Lot(id,name,location,longitude,latitude,type,startTime,endTime);
						listLot.add(lot);
					}
					AddMarker();
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
	     }
	     
	     public void AddMarker() {
	    	 if(!listLot.isEmpty()) {
	    		 Log.i(TAG,"there are "+listLot.size()+" lots");
	    		for(int i=0;i<listLot.size();i++) {
	    			Lot lot = (Lot) listLot.get(i);
	    			LatLng position = new LatLng(lot.latitude,lot.longitude);
	    			Marker marker= map.addMarker(new MarkerOptions().position(position).title(lot.name)
	    					.icon(BitmapDescriptorFactory.fromResource(permitIcon)));	    		
	    			if(lot.type.equals("0")) {
	    				marker.setIcon(BitmapDescriptorFactory.fromResource(eventIcon));
	    			}
	    		}
	    	 }
	    	 else Log.e(TAG,"no lots!");
	     }
	 }
	
	private String readFromFile() {

	    String ret = "";

	    try {
	        InputStream inputStream = openFileInput("config.txt");

	        if ( inputStream != null ) {
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = "";
	            StringBuilder stringBuilder = new StringBuilder();

	            while ( (receiveString = bufferedReader.readLine()) != null ) {
	                stringBuilder.append(receiveString);
	            }

	            inputStream.close();
	            ret = stringBuilder.toString();
	        }
	    }
	    catch (FileNotFoundException e) {
	        Log.e("login activity", "File not found: " + e.toString());
	    } catch (IOException e) {
	        Log.e("login activity", "Can not read file: " + e.toString());
	    }

	    return ret;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x )
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		if (accelationSquareRoot >= 2) //
		{
			if (actualTime - lastUpdate < 50) {
				return;
			}
			lastUpdate = actualTime;
			Intent i = new Intent(this, MainActivity.class);

			startActivity(i);
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				0,
				0, 
				locationListener);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		locMan.removeUpdates(locationListener);
	}
}
