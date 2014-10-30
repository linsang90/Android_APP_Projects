package sl.rutgers.locationcheck;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import sl.rutgers.locationcheck.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationCheck extends FragmentActivity {
	
	//TAG for debugging
	private final String TAG = "SL";
	
	//All the components in UI
	private TextView locationText;
	private TextView addressText;
	private Button   update;
	private Button   list;
	private Button   checkin;
	
	//All about Location and Time
	private String Provider;
	private Criteria c;
	private LocationManager manager;
	private LocationListener locationListener;
	private Location initialLoc;
	private Location lastLoc;
	private long lastTime;
	
	//Database helper
	private DBAdapter db;
	
	//All about Google Maps
	private GoogleMap map;
	private Marker marker;
	private Hashtable<String, String> markers;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private Marker currentSpot;
	private boolean isSet = false;
	
	//LatLng of all friends
	private LatLng PosMickey = new LatLng(40.517838, -74.465297);
	private LatLng PosDonald = new LatLng(40.513189, -74.433849);
	private LatLng PosGoofy = new LatLng(40.495527, -74.467142);
	private LatLng PosGarfield = new LatLng(40.497127, -74.417056);
	
	//Used to create another thread
	ExecutorService mThreadPool = Executors.newSingleThreadExecutor();
	
	//---------------------UI--------------------------
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_location_check);
			
			//Create the map
			map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			if(map == null) {
				Toast.makeText(this, "Google maps not available", Toast.LENGTH_LONG).show();
			}
			else {
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
			}
			
			//Choose provider
			manager = (LocationManager)
					getSystemService(Context.LOCATION_SERVICE);
			//print out all the location providers
			List<String> locationProviers = manager.getAllProviders();
			for(String provider :locationProviers) {
				Log.d("LocationProviders",provider);
			}
			c = new Criteria();
		    c.setAccuracy(Criteria.ACCURACY_FINE);
		    c.setAltitudeRequired(false);
		    c.setBearingRequired(false);
		    c.setCostAllowed(true);
		    c.setPowerRequirement(Criteria.POWER_HIGH);
		    Provider = manager.getBestProvider(c, true);
		    
		    //Initialize the current point
			initialLoc = manager.getLastKnownLocation(Provider);
			if(initialLoc != null) {
				Log.i(TAG,"provider is "+Provider);
				lastLoc = initialLoc;
				LatLng initialPosition = new LatLng(initialLoc.getLatitude(), initialLoc.getLongitude());
				(new GetAddressTask(LocationCheck.this)).execute(initialLoc);
				currentSpot = map.addMarker(new MarkerOptions().position(initialPosition).title("You").snippet("Wow~Here I am~"));
				isSet = true;
			}

			//Initialize UI and location listener
			locationListener = new MyLocationListener();
			
			locationText = (TextView)findViewById(R.id.locText);
			addressText  = (TextView)findViewById(R.id.adrText);
			
			update = (Button)findViewById(R.id.update);
			update.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	Provider = manager.getBestProvider(c, true);
	                lastLoc = manager.getLastKnownLocation(Provider);
	                if(lastLoc != null) {
	                	lastTime = System.currentTimeMillis();
	                	LatLng lastPos = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
	                	locationText.setText("Current Location: \nLat: " + lastLoc.getLatitude() 
	                			+"\nLng: "+ lastLoc.getLongitude());
	                	(new GetAddressTask(LocationCheck.this)).execute(lastLoc);
	                	Toast.makeText(getBaseContext(), "(" + Provider + ")Location changed : Lat " + lastLoc.getLatitude() +
	                			" Lng: " + lastLoc.getLongitude(),Toast.LENGTH_SHORT).show();
	                	if(isSet == false) {
	                		currentSpot = map.addMarker(new MarkerOptions().position(lastPos).title("You").snippet("Wow~Here I am~"));
	                		isSet = true;
	                	}
	                	else {
	                		currentSpot.setPosition(lastPos);
	                	}
	                	map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 12));
	                	map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
	                }
	            }
	        });
			
			list = (Button)findViewById(R.id.list);
			list.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                startActivity(new Intent("sl.rutgers.locationcheck.LocationList"));
	            }
	        });
			
			checkin = (Button)findViewById(R.id.checkin);
			checkin.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//add a location into database
					String lat = String.valueOf(lastLoc.getLatitude());
					String lng = String.valueOf(lastLoc.getLongitude());
					db.open();
					if(db.insertLocation(lat,lng)>=0) {
						Toast.makeText(getBaseContext(), "Add successfully.", Toast.LENGTH_LONG).show();
						db.close();
					}
					addSequence();
				}
			});

			db = new DBAdapter(this);
			
			initImageLoader();
	        imageLoader = ImageLoader.getInstance();
	        options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.ic_launcher)		//	Display Stub Image
				.showImageForEmptyUri(R.drawable.ic_launcher)	//	If Empty image found
				.cacheInMemory()
				.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
			//Add friends and their urls.
	        markers = new Hashtable<String, String>();
			final Marker Mickey = map.addMarker(new MarkerOptions().position(PosMickey).title("Mickey")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			markers.put(Mickey.getId(), "http://winlab.rutgers.edu/~huiqing/mickey.png");
			final Marker Donald = map.addMarker(new MarkerOptions().position(PosDonald).title("Donald")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			markers.put(Donald.getId(), "http://winlab.rutgers.edu/~huiqing/donald.jpg");
			final Marker Goofy = map.addMarker(new MarkerOptions().position(PosGoofy).title("Goofy")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			markers.put(Goofy.getId(), "http://winlab.rutgers.edu/~huiqing/goofy.png");
			final Marker Garfield = map.addMarker(new MarkerOptions().position(PosGarfield).title("Garfield")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			markers.put(Garfield.getId(), "http://winlab.rutgers.edu/~huiqing/garfield.jpg");
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.location_check, menu);
			return true;
		}
	
	//---------------------LocationListener--------------------------
	private class MyLocationListener implements LocationListener {
		
		@Override
		public void onLocationChanged(final Location loc) {
			mThreadPool.execute(new Runnable() {
		        @Override
		        public void run() {
		            //Start another thread to run updating location
		        	if(loc != null) {
						int currentid1 = android.os.Process.getThreadPriority(android.os.Process.myTid());
			            Log.i(TAG,"The thread of update geocode is [" + String.valueOf(currentid1)
			            		+"]");
			            if(Looper.getMainLooper().getThread() == Thread.currentThread()) {
			            	Log.i(TAG,"Update is on the UI thread!");
			            } else {
			            	Log.i(TAG,"It's not on the UI thread.");
			            }
						lastLoc = loc;
						lastTime = System.currentTimeMillis();
						addSequence();
						//Update UI components should be done on UI thread			
						runOnUiThread(new Runnable() {
				            @Override
				            public void run() {
				                // This code will always run on the UI thread, therefore is safe to modify UI elements.
				            	locationText.setText("Current Location: \nLat: " + loc.getLatitude() 
										+"\nLng: "+ loc.getLongitude());
				            	Toast.makeText(getBaseContext(), "(" + Provider + ")Location changed : Lat " + loc.getLatitude() +
								" Lng: " + loc.getLongitude(),Toast.LENGTH_SHORT).show();
				            	
				            	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent() ) {
				            		(new GetAddressTask(LocationCheck.this)).execute(loc);
				            		LatLng currentPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
				            		if(isSet == false) {
				            			currentSpot = map.addMarker(new MarkerOptions().position(currentPosition).title("You").snippet("Wow~Here I am~"));
				            			isSet = true;
				            		}
				            		else {
				            			currentSpot.setPosition(currentPosition);
				            		}
				            		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 12));
				            		map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
				            	}
				            }
				        });
					}
		        }
		    });
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getBaseContext(), "Provider: " + provider +"disabled", 
					Toast.LENGTH_SHORT).show();
			if(Provider.equals("gps")){
				Provider = "network";
			}
			if(Provider.equals("network")){
				Provider = "gps";
			}
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getBaseContext(), "Provider: " + provider +"enabled", 
					Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			String statusString = "";
			switch (status) {
			case android.location.LocationProvider.AVAILABLE:
				statusString = "available";
			case android.location.LocationProvider.OUT_OF_SERVICE:
				statusString = "out of service";
			case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
				statusString = "temporarily unavailable";
			}
			Toast.makeText(getBaseContext(), provider + "" + statusString,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		manager.requestLocationUpdates(Provider,
				60000,
				0, 
				locationListener);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		manager.removeUpdates(locationListener);
	}
	
	
	//---------------------Get address--------------------------
	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		
		Context mContext;
		
		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}
		
		@Override
		protected String doInBackground(Location... params) {
			int currentid = android.os.Process.getThreadPriority(android.os.Process.myTid());
            Log.i(TAG,"The thread of update address is [" + String.valueOf(currentid)
            		+"]");
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
        protected void onPostExecute(String address) {
            // Display the results of the lookup.
            addressText.setText(address);
        }
	}
	
	//------------------- mark popup-------------------------------
	 private class CustomInfoWindowAdapter implements InfoWindowAdapter {

			private View view;

			public CustomInfoWindowAdapter() {
				view = getLayoutInflater().inflate(R.layout.custom_info_window,
						null);
			}

			@Override
			public View getInfoContents(Marker marker) {

				if (LocationCheck.this.marker != null
						&& LocationCheck.this.marker.isInfoWindowShown()) {
					LocationCheck.this.marker.hideInfoWindow();
					LocationCheck.this.marker.showInfoWindow();
				}
				return null;
			}

			@Override
			public View getInfoWindow(final Marker marker) {
				LocationCheck.this.marker = marker;

				String url = null;

				if (marker.getId() != null && markers != null && markers.size() > 0) {
					if ( markers.get(marker.getId()) != null &&
							markers.get(marker.getId()) != null) {
						url = markers.get(marker.getId());
					}
				}
				final ImageView image = ((ImageView) view.findViewById(R.id.badge));

				if (url != null && !url.equalsIgnoreCase("null")
						&& !url.equalsIgnoreCase("")) {
					imageLoader.displayImage(url, image, options,
							new SimpleImageLoadingListener() {
								@Override
								public void onLoadingComplete(String imageUri,
										View view, Bitmap loadedImage) {
									super.onLoadingComplete(imageUri, view,
											loadedImage);
									getInfoContents(marker);
								}
							});
				} else {
					image.setImageResource(R.drawable.ic_launcher);
				}

				final String title = marker.getTitle();
				final TextView titleUi = ((TextView) view.findViewById(R.id.title));
				if (title != null) {
					titleUi.setText(title);
				} else {
					titleUi.setText("");
				}

				LatLng position = marker.getPosition();
				Location temp = new Location("");
				temp.setLatitude(position.latitude);
				temp.setLongitude(position.longitude);
				float distance = temp.distanceTo(lastLoc);
				final String snippet = marker.getSnippet();
				final TextView snippetUi = ((TextView) view
						.findViewById(R.id.snippet));
				if (snippet != null) {
					snippetUi.setText(snippet);
				} else {
					snippetUi.setText("He was " + new StringBuilder(16)
					.append(distance).toString() + "m from you at " + new StringBuilder(16)
					.append((System.currentTimeMillis() - lastTime)/1000).toString() + " seconds ago");
				}

				return view;
			}
		}

	 //------------- Download image online -------------------------------
	    private void initImageLoader() {
			int memoryCacheSize;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
				int memClass = ((ActivityManager) 
						getSystemService(Context.ACTIVITY_SERVICE))
						.getMemoryClass();
				memoryCacheSize = (memClass / 8) * 1024 * 1024;
			} else {
				memoryCacheSize = 2 * 1024 * 1024;
			}

			final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
					this).threadPoolSize(5)
					.threadPriority(Thread.NORM_PRIORITY - 2)
					.memoryCacheSize(memoryCacheSize)
					.memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize-1000000))
					.denyCacheImageMultipleSizesInMemory()
					.discCacheFileNameGenerator(new Md5FileNameGenerator())
					.tasksProcessingOrder(QueueProcessingType.LIFO).enableLogging() 
					.build();

			ImageLoader.getInstance().init(config);
		}
	    //Every time get new fix, add it in sequence
	    public void addSequence() {
	    	String acc = String.valueOf(lastLoc.getAccuracy());
			String lat = String.valueOf(lastLoc.getLatitude());
			String lng = String.valueOf(lastLoc.getLongitude());
			db.open();
			db.insertSequence(Provider,acc, lat, lng);
			db.close();
	    }
}
