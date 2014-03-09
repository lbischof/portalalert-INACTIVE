package com.lorenzbi.portalalert;

import java.util.ArrayList;

import com.lorenzbi.portalalert.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;

public class MainActivity
    extends
        Activity
    implements
        ConnectionCallbacks,
        OnConnectionFailedListener
{
    // private request codes for the sender
    public int ACTIVITY_NOTIFICATION_REQUEST_CODE = 423467;
    public int LOCATION_NOTIFICATION_REQUEST_CODE = 987651;
    public int FENCE_NOTIFICATION_REQUEST_CODE = 543212;
    // activity detection wake up interval time in milliseconds
    public int DETECTION_INTERVAL = 30000;
    // fastest location update interval time in milliseconds
    public int LOCATION_UPDATE_MINIMUM_INTERVAL = 5000;
    // normal location update interval time in milliseconds
    public int LOCATION_UPDATE_NOMINAL_INTERVAL = 30000;

    protected ActivityRecognitionClient _ActivityRecognitionClient;
    protected LocationClient _LocationClient;
    protected PendingIntent _ActivityUpdateCallbackIntent;
    protected PendingIntent _LocationCallbackIntent;

	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        _ActivityRecognitionClient = new ActivityRecognitionClient (this, this, this);
        _LocationClient = new LocationClient (this, this, this);
    }

	@Override
	protected void onStart ()
	{
	    // connect to Google Play services
	    // either use:
	    // _ActivityRecognitionClient.connect ();
	    // or use:
	    _LocationClient.connect ();
	    // but not both
	}

	@Override
	protected void onStop ()
	{
	    // disconnect from Google Play services
        // either use:
        // _ActivityRecognitionClient.disconnect ();
        // or use:
        _LocationClient.disconnect ();
        // but not both
	}

	@Override
	protected void onPause ()
	{
	    // Activities should strongly consider removing all location request when entering the background (for example at onPause()), or at least swap the request to a larger interval and lower quality.
	    if (_LocationClient.isConnected () && (null != _LocationCallbackIntent))
	        _LocationClient.removeLocationUpdates (_LocationCallbackIntent);
	}
	
	@Override
	protected void onResume ()
	{
        if (_LocationClient.isConnected () && (null != _LocationCallbackIntent))
            _LocationClient.requestLocationUpdates (getLocationRequest (), _LocationCallbackIntent);
	}
	
    //
    // ConnectionCallbacks interface
	//

    @Override
    public void onConnected (Bundle arg0)
    {
        Intent intent;
        PendingIntent pending;
        
        intent = new Intent (this, ActivityUpdateService.class);
        _ActivityUpdateCallbackIntent = PendingIntent.getService (this, ACTIVITY_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        _ActivityRecognitionClient.requestActivityUpdates (DETECTION_INTERVAL, _ActivityUpdateCallbackIntent);
        
        intent = new Intent (this, LocationService.class);
        _LocationCallbackIntent = PendingIntent.getService (this, LOCATION_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Location updates are sent with a key of KEY_LOCATION_CHANGED and a Location value on the intent.
        _LocationClient.requestLocationUpdates (getLocationRequest (), _LocationCallbackIntent);
        
        // TODO: Figure out how to fetch the portal information from the server and fill in this list
        ArrayList<Geofence> fences = new ArrayList<Geofence> ();
        // TODO: Figure out what activity should get these notifications, i.e. BorderGuardActivity
        intent = new Intent (this, MainActivity.class);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        pending = PendingIntent.getActivity (this, FENCE_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        _LocationClient.addGeofences (fences, pending,
            new LocationClient.OnAddGeofencesResultListener ()
            {
    
                /**
                 * Handle the asynchronous result from addGeofences().
                 * @param status the status code of the addGeofences operation
                 * @param ids the list of geofence request IDs
                 */
                @Override
                public void onAddGeofencesResult (int status, String[] ids)
                {
                    // check the status code
                    switch (status)
                    {
                        case LocationStatusCodes.SUCCESS:
                            break;
                        case LocationStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                            // TODO: reduce the radius we're asking for and try again
                            break;
                        case LocationStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                            // shouldn't happen, we should only be using one intent here
                            break;
                        case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE:
                            // TODO: message to user to turn on location access in settings > location access and retry 
                            break;
                        case LocationStatusCodes.ERROR:
                            // oops, 
                            break;
                    }
                    
                }
                
            });
/*
        Sets alerts to be notified when the device enters or exits one of the specified geofences. If an existing geofence with the same request ID is already registered, the old geofence is replaced by the new one, and the new pendingIntent is used to generate intents for alerts.

        onAddGeofencesResult(int, String[]) is called when geofences are successfully added or failed to be added. Refer to onAddGeofencesResult(int, String[]) for possible errors when adding geofences.

        When a geofence transition (for example, entering or exiting) matches one of the transition filter (see setTransitionTypes(int)) in the given geofence list, an intent is generated using the given pending intent. You can call getGeofenceTransition(Intent) to get the transition type of this alert intent and call getTriggeringGeofences(Intent) to get the geofences that triggered this intent.

        In case network location provider is disabled by the user, the geofence service will stop updating, all registered geofences will be removed and an intent is generated by the provided pending intent. In this case, hasError(Intent) returns true and getErrorCode(Intent) returns GEOFENCE_NOT_AVAILABLE.
        */ 
    }

    @Override
    public void onDisconnected ()
    {
        _ActivityRecognitionClient.removeActivityUpdates (_ActivityUpdateCallbackIntent);
    }

    //
    // OnConnectionFailedListener interface
    //

    @Override
    public void onConnectionFailed (ConnectionResult arg0)
    {
        Log.e ("com.lorenzbi.portalalert.MainActivity", "connection to ActivityRecognitionClient failed");
        Toast.makeText (this, "Unable to connect to the ActivityRecognitionClient", Toast.LENGTH_LONG).show();
    }
    
    //
    // implementation details
    //
    
    // gets a location request based on heuristics and user settings
    protected LocationRequest getLocationRequest ()
    {
        LocationRequest ret;

        // TODO: use user settings
        ret =  LocationRequest.create ();
        ret.setFastestInterval (LOCATION_UPDATE_MINIMUM_INTERVAL);
        ret.setInterval (LOCATION_UPDATE_NOMINAL_INTERVAL);
        ret.setPriority (LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        
        return (ret);
    }
}
