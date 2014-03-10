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
        // TODO: Use the coarse location information from the location service to inform the server what
        // area we are in (and what portals to send us), and monitor the location service
        // for being out of a large radius - and hence when to fetch a new set of portals
    }

}
