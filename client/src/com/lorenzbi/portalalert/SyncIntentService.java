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
import com.lorenzbi.portalalert.Alerts.Alert;

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
    	//get exras (lng,lat)
    	
    	
    	//get nearest 3km alert ids from db and send them to server
    	//receive all new and deleted info
    	//update database
    	
    	//TODO: need to figure out if i should reregister 99 geofences each time the app syncs or not
    	//check if distance less than 3km last updated {
    	//create missing geofences and delete old ones
    	// } else if further than 3km last updated {
    	//delete all geofences and register the 99 nearest
    	
    	
    	
    	
        //String json = intent.getStringExtra("JSON");
        //createGeofences(json);
    }
    public void createGeofences(String json) {
    	Log.d("json",json);
    	Gson gson = new Gson();
    	Alerts root = gson.fromJson(json, Alerts.class);
    	List<Alert> alerts = root.getAlerts();
    	if (!alerts.isEmpty()){
    	for(Alert a: alerts){
    		SimpleGeofence mGeofence = new SimpleGeofence(
    	            a.getId(),
    	            a.getLocation().getLat(),
    	            a.getLocation().getLng(),
    	            a.getRadius(),
    	            // Set the expiration time
    	            GEOFENCE_EXPIRATION_IN_MILLISECONDS,
    	            Geofence.GEOFENCE_TRANSITION_ENTER |
    	            Geofence.GEOFENCE_TRANSITION_EXIT);
    			DatabaseHelper dbHelper = new DatabaseHelper(this);
				dbHelper.addAlert(a);
    	        mCurrentGeofences.add(mGeofence.toGeofence());
    	}
        // Start the request. Fail if there's already a request in progress
        try {
            // Try to add geofences
            mGeofenceRequester.addGeofences(mCurrentGeofences);
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
           /* Toast.makeText(this, R.string.add_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();*/
        }
    	}
    }
}
