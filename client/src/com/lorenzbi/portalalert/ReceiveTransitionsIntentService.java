package com.lorenzbi.portalalert;

import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.lorenzbi.portalalert.Alerts.Alert;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that
 * triggered the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {

	/**
	 * Sets an identifier for this class' background thread
	 */
	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}

	/**
	 * Handles incoming intents
	 * 
	 * @param intent
	 *            The Intent sent by Location Services. This Intent is provided
	 *            to Location Services (inside a PendingIntent) when you call
	 *            addGeofences()
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("transition onhandleintent", "onhandleintent");
		// Create a local broadcast Intent
		Intent broadcastIntent = new Intent();
		DatabaseHelper dbHelper = new DatabaseHelper(this);

		// Give it the category for all intents sent by the Intent Service
		broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		List<Geofence> geofences = LocationClient
				.getTriggeringGeofences(intent);
		String[] geofenceIds = new String[geofences.size()];
		for (int index = 0; index < geofences.size(); index++) {
			geofenceIds[index] = geofences.get(index).getRequestId();
		}
		// First check for errors
		if (LocationClient.hasError(intent)) {

			// Get the error code
			int errorCode = LocationClient.getErrorCode(intent);

			// Get the error message
			String errorMessage = LocationServiceErrorMessages.getErrorString(
					this, errorCode);

			// Log the error
			Log.e(GeofenceUtils.APPTAG,
					getString(R.string.geofence_transition_error_detail,
							errorMessage));

			// Set the action and error message for the broadcast intent
			broadcastIntent
					.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
					.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

			// Broadcast the error *locally* to other components in this app
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					broadcastIntent);

			// If there's no error, get the transition type and create a
			// notification
		} else {

			// Get the type of transition (entry or exit)
			int transition = LocationClient.getGeofenceTransition(intent);

			// Test that a valid transition was reported
			if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

				// Post a notification
				
				String ids = TextUtils.join(
						GeofenceUtils.GEOFENCE_ID_DELIMITER, geofenceIds);
				//String transitionType = getTransitionString(transition);
				if (ids == "SYNC") {
					Log.d("Geofence Sync", "SYNC");
					Intent syncIntent = new Intent(this, SyncService.class);
					startService(syncIntent);
				} else {
					for (String id : geofenceIds) {
						Alert alert = dbHelper.getAlert(id);
						if (alert != null) {
							sendNotification(alert);
							Log.d("sendnotify", "sendnotify");
						}
					}

					// Log the transition type and a message
					Log.d(GeofenceUtils.APPTAG + "transition",
							getString(
									R.string.geofence_transition_notification_title,
									transition, ids));
					Log.d(GeofenceUtils.APPTAG + "transition",
							getString(R.string.geofence_transition_notification_text));
				}
			} else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
				for (String id : geofenceIds) {
					Alert alert = dbHelper.getAlert(id);
					if (alert != null) {
						removeNotification(alert);
						Log.d("removenotify", "removenotify");
					}
				}
				
			} else {
				// Always log as an error
				Log.e(GeofenceUtils.APPTAG,
						getString(R.string.geofence_transition_invalid_type,
								transition));
			}
		}
	}

	/**
	 * Posts a notification in the notification bar when a transition is
	 * detected. If the user clicks the notification, control goes to the main
	 * Activity.
	 * 
	 * @param transitionType
	 *            The type of transition that occurred.
	 * 
	 */
	private void sendNotification(Alert alert) {
		// Create an explicit content Intent that starts the main Activity
				// Get an instance of the Notification manager
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Intent intent = new Intent(this, MainActivity.class);
				intent.setAction("detail"+alert.getId());
				PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        Notification notification = builder.setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.ic_drawer).setTicker("Portal Alert").setWhen(0)
                .setAutoCancel(true).setContentTitle(alert.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(alert.getMessage()))
                .setContentText(alert.getMessage()).build();

		// Issue the notification
		String lng = alert.getLocation().getLng().toString();
		String lat = alert.getLocation().getLat().toString();
		Integer notifyId = Integer
				.parseInt(lng.substring(Math.max(lng.length() - 4, 0))
						+ lat.substring(Math.max(lat.length() - 4, 0)).replace(
								".", ""));

		mNotificationManager.notify(notifyId, notification);
	}
	public void removeNotification(Alert alert){
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		String lng = alert.getLocation().getLng().toString();
		String lat = alert.getLocation().getLat().toString();
		Integer notifyId = Integer
				.parseInt(lng.substring(Math.max(lng.length() - 4, 0))
						+ lat.substring(Math.max(lat.length() - 4, 0)).replace(
								".", ""));
		mNotificationManager.cancel(notifyId);
	}
	/**
	 * Maps geofence transition types to their human-readable equivalents.
	 * 
	 * @param transitionType
	 *            A transition type constant defined in Geofence
	 * @return A String indicating the type of transition
	 */
	private String getTransitionString(int transitionType) {
		switch (transitionType) {

		case Geofence.GEOFENCE_TRANSITION_ENTER:
			return getString(R.string.geofence_transition_entered);

		case Geofence.GEOFENCE_TRANSITION_EXIT:
			return getString(R.string.geofence_transition_exited);

		default:
			return getString(R.string.geofence_transition_unknown);
		}
	}
}
