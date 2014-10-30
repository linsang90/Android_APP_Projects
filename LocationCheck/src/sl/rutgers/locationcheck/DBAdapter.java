package sl.rutgers.locationcheck;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	static final String KEY_DATE         = "date";
	static final String KEY_LAT          = "lat";
	static final String KEY_LNG          = "lng";
	static final String KEY_ACC         = "accuracy";
	static final String KEY_PRO         = "provider";
	static final String TAG              = "DBAdapter";
	
	static final String DATABASE_NAME    = "MyDB";
	static final String DATABASE_TABLE   = "Locations";
	static final String DATABASE_TABLE2  = "Sequence";
	static final int    DATABASE_VERSION = 1;
	
	static final String DATABASE_CREATE = 
			"create table Locations(date text not null,"
			+ "lat text not null,lng text not null)";
	static final String DATABASE_CREATE2 = "create table Sequence(provider text not null,accuracy text not null,"+
					"lat text not null,lng text not null)";
	
	final Context context;
	
	DatabaseHelper DBHelper;
	SQLiteDatabase db;
	Calendar currentDate;
	SimpleDateFormat formatter;
	
	public DBAdapter(Context ctx)
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
		currentDate = Calendar.getInstance();
		formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME,null,DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			try {
				db.execSQL(DATABASE_CREATE);
				db.execSQL(DATABASE_CREATE2);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) 
		{
			Log.w(TAG,"Upgrading databse from version "+oldVersion + "newVersion"
					+ newVersion +", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS Locations");
			onCreate(db);
			}
	}
	
	//--opens the database ---
	public DBAdapter open( ) throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	//---closes the database---
	public void close()
	{
		DBHelper.close();
	}
	
	public long insertSequence(String pro,String acc,String lat,String lng)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PRO, pro);
		initialValues.put(KEY_ACC, acc);
		initialValues.put(KEY_LAT, lat);
		initialValues.put(KEY_LNG, lng);
		return db.insert(DATABASE_TABLE2, null, initialValues);
	}
	//--insert a location into the database---
	public long insertLocation(String lat,String lng)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DATE, formatter.format(currentDate.getTime()));
		initialValues.put(KEY_LAT, lat);
		initialValues.put(KEY_LNG, lng);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	
	//--retrieves all the location---
	public Cursor getAllLocations()
	{
		return db.query(DATABASE_TABLE, new String[] {
				KEY_DATE,KEY_LAT,KEY_LNG},
				null,null,null,null,null);
	}
}
	
