package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.lorenzbi.portalalert.Alerts.Alert;

public class SyncService extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	public static final int NOTIFICATION_ID = 1;
	NotificationCompat.Builder builder;

	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS
			* DateUtils.HOUR_IN_MILLIS;

	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences = new ArrayList<Geofence>();
	private GeofenceRemover mGeofenceRemover = new GeofenceRemover(this);
	private GeofenceRequester mGeofenceRequester = new GeofenceRequester(this);

	Double lng;
	Double lat;
	LocationClient locationclient;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester
				.getRequestPendingIntent());

		locationclient = new LocationClient(this, this, this);
		locationclient.connect();
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		return null;
	}

	public void addToDb(String json) {
		Gson gson = new Gson();
		Alerts root = gson.fromJson(json, Alerts.class);
		List<Alert> alerts = root.getAlerts();
		Integer counter = 0;
		Float radius;
		Double lng2 = null;
		Double lat2 = null;
		if (!alerts.isEmpty()) {
			DatabaseHelper dbHelper = new DatabaseHelper(this);
			dbHelper.clearAll();
			for (Alert a : alerts) {
				dbHelper.addAlert(a);
				if (counter < 100) {
					lng2 = a.getLocation().getLng();
					lat2 = a.getLocation().getLat();
					Long expire = a.getExpire() - System.currentTimeMillis();
					SimpleGeofence mGeofence = new SimpleGeofence(a.getId(),
							lat2, lng2, a.getRadius(), // Set the expiration
														// time
							expire, Geofence.GEOFENCE_TRANSITION_ENTER
									| Geofence.GEOFENCE_TRANSITION_EXIT);
					mCurrentGeofences.add(mGeofence.toGeofence());
					counter++;
				}
			}
			// if (counter == 99) {
			radius = (float) distance(lat, lng, lat2, lng2) * 1000;
			// } else {
			// radius = (float) 3000;
			// }
			Log.d("radius lastgeofenc", radius.toString());
			SimpleGeofence mGeofence = new SimpleGeofence("SYNC", lat, lng,
					radius, // Set the expiration time
					Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_EXIT);
			mCurrentGeofences.add(mGeofence.toGeofence());
			mGeofenceRequester.addGeofences(mCurrentGeofences);
			final SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("counter", counter);
			editor.commit();
		}
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

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String userid = prefs.getString("userid", "");
		Log.d("userid", userid);
		Location location = locationclient.getLastLocation();
		if (location != null) {
			Log.d("location lng", location.getLongitude() + "");
			lng = location.getLongitude();
			lat = location.getLatitude();

			RequestParams params = new RequestParams();
			params.put("userid", userid);
			params.put("lng", lng.toString());
			params.put("lat", lat.toString());

			HttpManager.post("sync", params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					Log.d("resonse", response);
					addToDb(response);
				}
			});
			stopSelf();
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

}
