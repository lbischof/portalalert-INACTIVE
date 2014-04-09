package com.lorenzbi.portalalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lorenzbi.portalalert.Alerts.Alert;
import com.lorenzbi.portalalert.Alerts.AlertLocation;

public class DatabaseHelper extends SQLiteOpenHelper {
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
	Double fudge = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS alerts (_id INTEGER PRIMARY KEY AUTOINCREMENT, id TEXT UNIQUE ON CONFLICT REPLACE, imagesrc TEXT, title TEXT, message TEXT, userid TEXT, type INTEGER, urgency INTEGER, lat REAL, lng REAL, expire REAL)");
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
		SQLiteDatabase database = getWritableDatabase();
		database.insert("alerts", null, row);
		database.close();
		Log.i("portalalert Lorenz", "db inserted");

		return true;
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
			Float radius = cursor.getFloat(cursor.getColumnIndex("message"));
			Long expire = cursor.getLong(cursor.getColumnIndex("expire"))
					- System.currentTimeMillis();
			alert = new Alert(id, imagesrc, title, message, 0, 0,
					alertLocation, radius, "", expire);
		}
		return alert;

	}

	public void clearAll() {
		getReadableDatabase().rawQuery("delete from alerts", null);
	}

	public Cursor getNear(Double lng, Double lat) {
		fudge = Math.pow(Math.cos(Math.toRadians(lat)), 2);
		Long now = System.currentTimeMillis();
		String query = "SELECT _id, id, imagesrc, title, message, lng, lat, expire, ( "
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
				+ " order by distance asc";
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