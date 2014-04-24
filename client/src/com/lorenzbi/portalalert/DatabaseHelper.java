package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.lorenzbi.portalalert.Alerts.Alert;
import com.lorenzbi.portalalert.Alerts.AlertLocation;

public class DatabaseHelper extends SQLiteOpenHelper {
	Context context;
	private static final String DATABASE_NAME = "db";
	static final String ID = "id";
	static final String IMAGESRC = "imagesrc";
	static final String TITLE = "title";
	static final String MESSAGE = "message";
	static final String USERID = "userid";
	static final String TYPE = "type";
	static final String URGENCY = "urgency";
	static final String LATITUDE = "lat";
	static final String LONGITUDE = "lng";
	static final String EXPIRE = "expire";
	static final String DONE = "done";
	Double fudge = null;
	Double lng;
	Double lat;
	GeofenceRemover mGeofenceRemover;
	GeofenceRequester mGeofenceRequester;

	List<String> mGeofenceIdsToRemove = new ArrayList<String>();
	List<Geofence> mCurrentGeofences = new ArrayList<Geofence>();


	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
		this.context = context;
		mGeofenceRemover = new GeofenceRemover(context);
		mGeofenceRequester = new GeofenceRequester(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS alerts (_id INTEGER PRIMARY KEY AUTOINCREMENT, id TEXT UNIQUE ON CONFLICT REPLACE, imagesrc TEXT, title TEXT, message TEXT, userid TEXT, type INTEGER, urgency INTEGER, lat REAL, lng REAL, done BOOLEAN, expire REAL)");
	}
	public boolean addAlert(List<Alert> alerts, Double lng, Double lat){
		this.lng = lng;
		this.lat = lat;
		for (Alert alert : alerts) {
			addAlert(alert);
		}
		return false;
	}
	public boolean addAlert(Alert alert) {
		/*
		 * if(TextUtils.isEmpty(alert.getTitle())){ return false; }
		 */
		ContentValues row = new ContentValues();
		row.put(ID, alert.getId());
		row.put(IMAGESRC, alert.getImageSource());
		row.put(TITLE, alert.getTitle());
		row.put(MESSAGE, alert.getMessage());
		row.put(LONGITUDE, alert.getLocation().getLng());
		row.put(LATITUDE, alert.getLocation().getLat());
		row.put(EXPIRE, alert.getExpire());
		row.put(DONE, 0);
		SQLiteDatabase database = getWritableDatabase();
		database.insert("alerts", null, row);
		database.close();
		return true;
	}
	public void removeAlert(String id){
		SQLiteDatabase database = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("done", 1);
		database.update("alerts",cv, "id = ?", new String[] {id});
		removeFence(id);
		
	}
	public void undoRemove(String id){
		SQLiteDatabase database = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("done", 0);
		database.update("alerts",cv, "id = ?", new String[] {id});
		createFence(getAlert(id));
	}
	public String getId(Long listId) {
		Cursor cursor = getReadableDatabase().rawQuery(
				"select id from alerts where _id = ?",
				new String[] { listId.toString() });
		String id = null;
		if (cursor.moveToFirst()) {
			id = cursor.getString(cursor.getColumnIndex("id"));
		}
		return id;
	}
	public String getId(String title) {
		Cursor cursor = getReadableDatabase().rawQuery(
				"select id from alerts where title = ?",
				new String[] { title });
		String id = null;
		if (cursor.moveToFirst()) {
			id = cursor.getString(cursor.getColumnIndex("id"));
		}
		return id;

	}
	public Alert getAlert(String id) {
		Alert alert = null;
		Cursor cursor = getReadableDatabase().rawQuery(
				"select * from alerts where id = ?", new String[] { id });
		if (cursor.moveToFirst()) {
			String imagesrc = cursor.getString(cursor
					.getColumnIndex("imagesrc"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String message = cursor.getString(cursor.getColumnIndex("message"));
			AlertLocation alertLocation = new AlertLocation();
			Log.i("getlng portalalert",
					cursor.getDouble(cursor.getColumnIndex("lng")) + "");

			alertLocation
					.setLng(cursor.getDouble(cursor.getColumnIndex("lng")));
			alertLocation
					.setLat(cursor.getDouble(cursor.getColumnIndex("lat")));
			Float radius = (float) 200;//cursor.getFloat(cursor.getColumnIndex("radius"));
			Long expire = cursor.getLong(cursor.getColumnIndex("expire"))
					- System.currentTimeMillis();
			alert = new Alert(id, imagesrc, title, message, 0, 0,
					alertLocation, radius, "", expire);
		}
		return alert;

	}
	public void createFence(Alert alert){
			Long expire = alert.getExpire() - System.currentTimeMillis();
			SimpleGeofence mGeofence = new SimpleGeofence(alert.getId(), alert
					.getLocation().getLat(), alert.getLocation().getLng(),
					alert.getRadius(),
					// Set the expiration time
					expire, Geofence.GEOFENCE_TRANSITION_ENTER
							| Geofence.GEOFENCE_TRANSITION_EXIT);

			mCurrentGeofences.add(mGeofence.toGeofence());

			// Start the request. Fail if there's already a request in progress
			try {
				// Try to add geofences
				mGeofenceRequester.addGeofences(mCurrentGeofences);
			} catch (UnsupportedOperationException e) {
				// Notify user that previous request hasn't finished.
			}
		
	}
	public void removeFence(String id){
		mGeofenceIdsToRemove.add(id);
		mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
	}
	public void clearAll() {
		getReadableDatabase().rawQuery("delete from alerts", null);
	}

	public Cursor getNear(Double lng, Double lat) {
		fudge = Math.pow(Math.cos(Math.toRadians(lat)), 2);
		Long now = System.currentTimeMillis();
		String query = "SELECT _id, id, imagesrc, title, message, lng, lat, expire, done, ( "
				+ lat
				+ " - lat) * ( "
				+ lat
				+ "- lat) + ( "
				+ lng
				+ "- lng) * ( "
				+ lng
				+ "- lng) * "
				+ fudge
				+ " as distance "
				+ " from alerts WHERE expire >= "
				+ now
				+ " AND NOT done ORDER BY distance asc";
		Cursor cursor = getReadableDatabase().rawQuery(query, null);
		return cursor;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("Constants",
				"Upgrading database, which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS alerts");
		onCreate(db);
	}
	
}