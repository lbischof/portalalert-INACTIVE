package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteAdapter {
	// Database fields
		private DataBaseWrapper dbHelper;
		private String[] ALERT_TABLE_COLUMNS = { DataBaseWrapper.ALERT_ID, DataBaseWrapper.ALERT_TITLE, DataBaseWrapper.ALERT_MESSAGE };
		private SQLiteDatabase database;

		public SQLiteAdapter(Context context) {
			dbHelper = new DataBaseWrapper(context);
		}

		public void open() throws SQLException {
			database = dbHelper.getWritableDatabase();
		}

		public void close() {
			dbHelper.close();
		}

		public Alert addAlert(String title) {

			ContentValues values = new ContentValues();

			values.put(DataBaseWrapper.ALERT_TITLE, title);
			values.put(DataBaseWrapper.ALERT_MESSAGE, "");
			long studId = database.insert(DataBaseWrapper.ALERTS, null, values);

			// now that the alert is created return it ...
			Cursor cursor = database.query(DataBaseWrapper.ALERTS,
					ALERT_TABLE_COLUMNS, DataBaseWrapper.ALERT_ID + " = "
							+ studId, null, null, null, null);

			cursor.moveToFirst();

			Alert newComment = parseAlert(cursor);
			cursor.close();
			return newComment;
		}

		public void deleteAlert(Alert comment) {
			long id = comment.getId();
			System.out.println("Comment deleted with id: " + id);
			database.delete(DataBaseWrapper.ALERTS, DataBaseWrapper.ALERT_ID
					+ " = " + id, null);
		}

		public List getAllAlerts() {
			List alerts = new ArrayList();

			Cursor cursor = database.query(DataBaseWrapper.ALERTS,
					ALERT_TABLE_COLUMNS, null, null, null, null, null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Alert alert = parseAlert(cursor);
				alerts.add(alert);
				cursor.moveToNext();
			}

			cursor.close();
			return alerts;
		}

		private Alert parseAlert(Cursor cursor) {
			Alert alert = new Alert();
			alert.setId(cursor.getInt(0));
			alert.setTitle(cursor.getString(1));
			alert.setMessage(cursor.getString(2));
			return alert;
		}
	

	public class DataBaseWrapper extends SQLiteOpenHelper {

		public static final String ALERTS = "Alerts";
		public static final String ALERT_ID = "_id";
		public static final String ALERT_TITLE = "_title";
		public static final String ALERT_MESSAGE = "_message";
		
		private static final String DATABASE_NAME = "Alerts.db";
		private static final int DATABASE_VERSION = 1;

		// creation SQLite statement
		private static final String DATABASE_CREATE = "create table " + ALERTS
				+ "(" + ALERT_ID + " integer primary key autoincrement, "
				+ ALERT_TITLE + " text not null, "+ ALERT_MESSAGE + " text);";

		public DataBaseWrapper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// you should do some logging in here
			// ..

			db.execSQL("DROP TABLE IF EXISTS " + ALERTS);
			onCreate(db);
		}

	}
	public class Alert {

		private int id;
		private String title;
		private String message;

		public long getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getTitle() {
			return this.title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		public String getMessage() {
			return this.message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		@Override
		public String toString() {
			return title;
		}
	}
	}