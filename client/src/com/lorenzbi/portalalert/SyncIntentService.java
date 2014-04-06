package com.lorenzbi.portalalert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.lorenzbi.portalalert.Alerts.Alert;

public class SyncIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	NotificationCompat.Builder builder;

	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS
			* DateUtils.HOUR_IN_MILLIS;

	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences = new ArrayList<Geofence>();

	private GeofenceRequester mGeofenceRequester = new GeofenceRequester(this);

	Double lng;
	Double lat;
	public SyncIntentService() {
		super("SyncIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String userid = prefs.getString("userid", "");
		lng = intent.getDoubleExtra("lng", 0);
		lat = intent.getDoubleExtra("lat", 0);

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://portalalert.lorenzz.ch:3000/sync");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("userid", userid));
		nameValuePairs.add(new BasicNameValuePair("lng", lng.toString()));
		nameValuePairs.add(new BasicNameValuePair("lat", lat.toString()));

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			HttpResponse response = httpclient.execute(httppost);
			String jsonResult = inputStreamToString(
					response.getEntity().getContent()).toString();

			if (jsonResult != null) {
				Log.d("http sync response", jsonResult);
				addToDb(jsonResult);
				// sendResultBroadcast(jsonResult);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StringBuilder inputStreamToString(InputStream is) {
		String rLine = "";
		StringBuilder answer = new StringBuilder();

		InputStreamReader isr = new InputStreamReader(is);

		BufferedReader rd = new BufferedReader(isr);

		try {
			while ((rLine = rd.readLine()) != null) {
				answer.append(rLine);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return answer;
	}

	public void addToDb(String json) {
		Gson gson = new Gson();
		Alerts root = gson.fromJson(json, Alerts.class);
		List<Alert> alerts = root.getAlerts();
		Integer counter = 0;
		Double lng2 = null;
		Double lat2 = null;
		if (!alerts.isEmpty()) {
			DatabaseHelper dbHelper = new DatabaseHelper(this);
			dbHelper.clearAll();
			for (Alert a : alerts) {
				dbHelper.addAlert(a);
				if (counter < 99) {
					lng2 = a.getLocation().getLng();
					lat2 = a.getLocation().getLat();
					 SimpleGeofence mGeofence = new SimpleGeofence( a.getId(),
							 lat2, lng2, a.getRadius(), //Set the expiration time 
							 GEOFENCE_EXPIRATION_IN_MILLISECONDS,
							 Geofence.GEOFENCE_TRANSITION_ENTER |
							 Geofence.GEOFENCE_TRANSITION_EXIT);
							 mCurrentGeofences.add(mGeofence.toGeofence());
					counter++;
				} 
			}
			Float radius = (float) distance(lat, lng, lat2, lng2);
			Log.d("radius lastgeofenc",radius.toString());
			SimpleGeofence mGeofence = new SimpleGeofence( "SYNC",
					 lat, lng, radius, //Set the expiration time 
					 GEOFENCE_EXPIRATION_IN_MILLISECONDS,
					 Geofence.GEOFENCE_TRANSITION_ENTER |
					 Geofence.GEOFENCE_TRANSITION_EXIT);
					 mCurrentGeofences.add(mGeofence.toGeofence());

				mGeofenceRequester.addGeofences(mCurrentGeofences);
			
		}
	}


	private double distance(double lat1, double lon1, double lat2, double lon2) {
	      double theta = lon1 - lon2;
	      double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	      dist = Math.acos(dist);
	      dist = rad2deg(dist);
	      dist = dist * 60 * 1.1515;
	      dist = dist * 1.609344;
	      return (dist);
	    }

	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    /*::  This function converts decimal degrees to radians             :*/
	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    private double deg2rad(double deg) {
	      return (deg * Math.PI / 180.0);
	    }

	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    /*::  This function converts radians to decimal degrees             :*/
	    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	    private double rad2deg(double rad) {
	      return (rad * 180.0 / Math.PI);
	    }
	
	
}
