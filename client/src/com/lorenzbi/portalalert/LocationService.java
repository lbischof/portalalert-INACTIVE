package com.lorenzbi.portalalert;

import android.app.IntentService;
import android.content.Intent;

public class LocationService
    extends
        IntentService
{
    protected static String NAME = "LocationService";

    // creates an IntentService to handle location notifications
    public LocationService ()
    {
        this (NAME);
    }

    public LocationService (String name)
    {
        super (name);
    }

    @Override
    protected void onHandleIntent (Intent intent)
    {
        // TODO Auto-generated method stub
        
    }

}
