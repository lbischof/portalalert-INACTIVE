package com.lorenzbi.portalalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.SensorManager;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "db";
	static final String TITLE = "title";
	static final String MESSAGE = "message";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE constants (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, message TEXT);");

		ContentValues cv = new ContentValues();

		cv.put(TITLE, "Gravity, Death Star I");
		cv.put(MESSAGE, SensorManager.GRAVITY_DEATH_STAR_I+"");
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Earth");
		cv.put(MESSAGE, SensorManager.GRAVITY_EARTH);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Jupiter");
		cv.put(MESSAGE, SensorManager.GRAVITY_JUPITER);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Mars");
		cv.put(MESSAGE, SensorManager.GRAVITY_MARS);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Mercury");
		cv.put(MESSAGE, SensorManager.GRAVITY_MERCURY);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Moon");
		cv.put(MESSAGE, SensorManager.GRAVITY_MOON);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Neptune");
		cv.put(MESSAGE, SensorManager.GRAVITY_NEPTUNE);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Pluto");
		cv.put(MESSAGE, SensorManager.GRAVITY_PLUTO);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Saturn");
		cv.put(MESSAGE, SensorManager.GRAVITY_SATURN);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Sun");
		cv.put(MESSAGE, SensorManager.GRAVITY_SUN);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, The Island");
		cv.put(MESSAGE, SensorManager.GRAVITY_THE_ISLAND);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Uranus");
		cv.put(MESSAGE, SensorManager.GRAVITY_URANUS);
		db.insert("constants", TITLE, cv);

		cv.put(TITLE, "Gravity, Venus");
		cv.put(MESSAGE, SensorManager.GRAVITY_VENUS);
		db.insert("constants", TITLE, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("Constants",
				"Upgrading database, which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS constants");
		onCreate(db);
	}
}