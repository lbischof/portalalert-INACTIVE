package com.lorenzbi.portalalert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lorenzbi.portalalert.Alerts.Alert;
import com.lorenzbi.portalalert.Alerts.AlertLocation;

public class GcmIntentService extends IntentService {
	
	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				//sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				//sendNotification("Deleted messages on server: "+ extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				final SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				Integer counter = prefs.getInt("counter", 0);
				DatabaseHelper dbHelper = new DatabaseHelper(this);
				if (extras.getString("done") != null) {
					String id = extras.getString("done");
					dbHelper.removeAlert(id);
				} else {
					
					String id = extras.getString("_id");
					Log.d("id", id);
					String location = extras.getString("location");
					AlertLocation alertLocation = new AlertLocation();
					try {
						JSONObject jsonObject = new JSONObject(location);
						JSONArray jsonArray = jsonObject
								.getJSONArray("coordinates");
						alertLocation.setLng(jsonArray.getDouble(0));
						alertLocation.setLat(jsonArray.getDouble(1));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Float radius = Float.parseFloat("100");// extras.getInt("radius");
					String imagesrc = extras.getString("imagesrc");
					String title = extras.getString("title");
					String message = extras.getString("message");
					Long expire = Long.parseLong(extras.getString("expire"));// -
																				// System.currentTimeMillis();
					Alert alert = new Alert(id, imagesrc, title, message, 0, 0,
							alertLocation, radius, "", expire);

					if (dbHelper.addAlert(alert) && counter < 99) {
						dbHelper.createFence(alert);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt("counter", counter++);
						editor.commit();
					} else {
						Double lng = Double.parseDouble(prefs.getString("lng", ""));
						Double lat = Double.parseDouble(prefs.getString("lat", ""));
						Float largestRadius = prefs.getFloat("radius", 3000);
						if (distance(lat, lng, alert.getLocation().getLat(),alert.getLocation().getLng()) < largestRadius){
							Intent syncIntent = new Intent(this, SyncService.class);
							startService(syncIntent);
						}
						
					}
					Log.i("lorenz", "Received: " + extras.toString());
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	private double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

}