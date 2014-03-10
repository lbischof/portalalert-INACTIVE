package com.lorenzbi.portalalert;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ActivityUpdateService
    extends
        IntentService
{
    protected static String NAME = "ActivityUpdateService";

    // creates an IntentService to handle recognized activity notifications
    public ActivityUpdateService ()
    {
        this (NAME);
    }

    public ActivityUpdateService (String name)
    {
        super (name);
    }

    @Override
    protected void onHandleIntent (Intent intent)
    {
        if (ActivityRecognitionResult.hasResult (intent))
        {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult (intent);
            DetectedActivity activity = result.getMostProbableActivity ();
            
            Log.e ("com.lorenzbi.portalalert.LocationService", "received ActivityRecognitionResult");
            Toast.makeText (this, "ActivityRecognitionResult " + activity.toString (), Toast.LENGTH_LONG).show();
        }
    }
}
