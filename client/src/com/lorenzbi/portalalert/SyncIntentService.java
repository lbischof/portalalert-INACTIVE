package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;

public class SyncIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;

    
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;


    // Store a list of geofences to add
    List<Geofence> mCurrentGeofences = new ArrayList<Geofence>();
   
    private GeofenceRequester mGeofenceRequester = new GeofenceRequester(this);
    
 
    public SyncIntentService() {
        super("SyncIntentService");
    }
 
    @Override
    protected void onHandleIntent(Intent intent) {
        String json = intent.getStringExtra("JSON");
        createGeofences(json);
    }
    public void createGeofences(String json) {
    	
    	Gson gson = new Gson();
    	Alerts alerts = gson.fromJson(json, Alerts.class);
    	for(Alert a: alerts.alerts){
    		Log.i("one alert", gson.toJson(a));
    	}
    	Log.e("alertlist", gson.toJson(alerts));
    	//Log.i("portalalert object", object.toString());
        /*
         * Record the request as an ADD. If a connection error occurs,
         * the app can automatically restart the add request if Google Play services
         * can fix the error
         */
        /*SimpleGeofence mGeofence = new SimpleGeofence(
            "id",
            Double.parseDouble("lat"),
            Double.parseDouble("lng"),
            Float.parseFloat("radius"),
            // Set the expiration time
            GEOFENCE_EXPIRATION_IN_MILLISECONDS,
            Geofence.GEOFENCE_TRANSITION_ENTER |
            Geofence.GEOFENCE_TRANSITION_EXIT);
        
        mCurrentGeofences.add(mGeofence.toGeofence());

        // Start the request. Fail if there's already a request in progress
        try {
            // Try to add geofences
            mGeofenceRequester.addGeofences(mCurrentGeofences);
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.add_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();
        }*/
    }
}
