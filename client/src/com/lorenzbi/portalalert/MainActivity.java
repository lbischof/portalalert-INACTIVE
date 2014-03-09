package com.lorenzbi.portalalert;

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
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

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
        
        intent = new Intent (this, ActivityUpdateService.class);
        _ActivityUpdateCallbackIntent = PendingIntent.getService (this, ACTIVITY_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        _ActivityRecognitionClient.requestActivityUpdates (DETECTION_INTERVAL, _ActivityUpdateCallbackIntent);
        
        intent = new Intent (this, LocationService.class);
        _LocationCallbackIntent = PendingIntent.getService (this, LOCATION_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Location updates are sent with a key of KEY_LOCATION_CHANGED and a Location value on the intent.
        _LocationClient.requestLocationUpdates (getLocationRequest (), _LocationCallbackIntent);
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
