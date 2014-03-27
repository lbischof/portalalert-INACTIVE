package com.lorenzbi.portalalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.lorenzbi.portalalert.Alerts.Alert;
import com.lorenzbi.portalalert.Alerts.AlertLocation;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "db";
	static final String ID = "id";
	static final String TITLE = "title";
	static final String MESSAGE = "message";
	static final String USERID = "userid";
	static final String TYPE = "type";
	static final String URGENCY = "urgency";
	static final String LATITUDE = "lat";
	static final String LONGITUDE = "lng";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE alerts (_id INTEGER PRIMARY KEY AUTOINCREMENT, id TEXT UNIQUE ON CONFLICT REPLACE, title TEXT, message TEXT, userid TEXT, type INTEGER, urgency INTEGER, lat REAL, lng REAL, time INTEGER)");

		/*ContentValues cv = new ContentValues();
		cv.put(TITLE, "Gravity, Venus");
		cv.put(MESSAGE, SensorManager.GRAVITY_VENUS);
		db.insert("alerts", TITLE, cv);*/
	}
	public boolean addAlert(Alert alert){
		if(TextUtils.isEmpty(alert.getTitle())){
            return false;
        }
        ContentValues row = new ContentValues();
        row.put(ID, alert.getId());
        row.put(TITLE, alert.getTitle());
        row.put(MESSAGE, alert.getMessage());

        SQLiteDatabase database = getWritableDatabase();
        database.insert("alerts", null, row);
        database.close();

        //Log.i("portalalert Lorenz", String.format("(%s) %s inserted", title, message));
        return true;
    }
	public Alert getAlert(String id){
		Alert alert = null;
        Cursor cursor = getReadableDatabase().rawQuery("select * from alerts where id = ?", new String[] { id });
        if (cursor.moveToFirst()){
        		String title = cursor.getString(cursor.getColumnIndex("title"));
        		String message = cursor.getString(cursor.getColumnIndex("message"));
        		AlertLocation alertLocation = new AlertLocation();
        		Log.i("getlng portalalert" , cursor.getDouble(cursor.getColumnIndex("lng")) + "");

        		alertLocation.setLng(cursor.getDouble(cursor.getColumnIndex("lng")));
        		alertLocation.setLat(cursor.getDouble(cursor.getColumnIndex("lat")));
        		Float radius = cursor.getFloat(cursor.getColumnIndex("message"));


        	   	alert = new Alert(id, title, message, 0, 0, alertLocation, radius, "", 0);
        	}
		return alert;
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("Constants",
				"Upgrading database, which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS alerts");
		onCreate(db);
	}
}