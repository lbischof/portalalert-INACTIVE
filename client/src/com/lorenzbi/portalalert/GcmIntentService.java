package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.Geofence;
import com.lorenzbi.portalalert.Alerts.Alert;
import com.lorenzbi.portalalert.Alerts.AlertLocation;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;


	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences = new ArrayList<Geofence>();
	// Store the list of geofences to remove
	private List<String> mGeofenceIdsToRemove = new ArrayList<String>();
	// Add geofences handler
	private GeofenceRequester mGeofenceRequester = new GeofenceRequester(this);
	// Remove geofences handler
	private GeofenceRemover mGeofenceRemover = new GeofenceRemover(this);

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
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				DatabaseHelper dbHelper = new DatabaseHelper(this);
				if (extras.getString("done") != null) {
					String id = extras.getString("done");
					dbHelper.removeAlert(id, this);
				} else {
					final SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					Integer counter = prefs.getInt("counter", 0);
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
						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt("counter", counter++);
						editor.commit();
						createGeofence(alert);
						Log.d("createdgeofence", counter.toString());
					} else {
						Intent syncIntent = new Intent(this, SyncService.class);
						startService(syncIntent);
					}

					Log.i("lorenz",
							"Completed work @ " + SystemClock.elapsedRealtime());
					// Post notification of received message.
					sendNotification("Received: " + extras.toString());
					Log.i("lorenz", "Received: " + extras.toString());
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	public void createGeofence(Alert alert) {

		/*
		 * Record the request as an ADD. If a connection error occurs, the app
		 * can automatically restart the add request if Google Play services can
		 * fix the error
		 */
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
			Toast.makeText(this,
					R.string.add_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}
	}

	public void removeGeofences(String id) {
		
		// Create a List of 1 Geofence with the ID "2" and store it in the
		// global list
		mGeofenceIdsToRemove = Collections.singletonList(id);

		/*
		 * Check for Google Play services. Do this after setting the request
		 * type. If connecting to Google Play services fails, onActivityResult
		 * is eventually called, and it needs to know what type of request was
		 * in progress.
		 */

		// Try to remove the geofence
		try {
			mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

			// Catch errors with the provided geofence IDs
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this,
					R.string.remove_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ListFragment.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_drawer)
				.setContentTitle("GCM Notification")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}