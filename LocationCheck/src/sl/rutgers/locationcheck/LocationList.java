package sl.rutgers.locationcheck;

import java.util.ArrayList;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LocationList extends ListActivity {
	private ArrayList<String> results = new ArrayList<String>();
	TextView hi;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		openAndQueryDatabase();
		
		displayResultList();
	}
	
	private void displayResultList() {
		TextView tView = new TextView(this);
		tView.setText("This is all locations that checked in.");
		getListView().addHeaderView(tView);
		
		setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,results));
		getListView().setTextFilterEnabled(true);
	}
	
	private void openAndQueryDatabase() {
		try {
			DBAdapter db = new DBAdapter(this);
			db.open();
			Cursor c = db.getAllLocations();
			if(c != null) {
				if(c.moveToFirst()) {
					do {
						String date = c.getString(c.getColumnIndex("date"));
						String lat = c.getString(c.getColumnIndex("lat"));
						String lng = c.getString(c.getColumnIndex("lng"));
						results.add("date: "+date+", lat: "+lat+",lng: "+lng);
					} while (c.moveToNext());
				}
			}
			db.close();
		} catch (SQLiteException se) {
			Log.e(getClass().getSimpleName(),"Could not create or Open the database");
		} 
	}

}
