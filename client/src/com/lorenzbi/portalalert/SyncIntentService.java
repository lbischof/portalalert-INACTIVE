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
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    String userid = prefs.getString("userid", "");
    	Double lng = intent.getDoubleExtra("lng", Double.parseDouble("0.0"));
    	Double lat = intent.getDoubleExtra("lat", Double.parseDouble("0.0"));
    	DatabaseHelper db = new DatabaseHelper(getBaseContext());
    	
    	HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(
                "http://portalalert.lorenzz.ch:3000/sync");
    	
    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
    	nameValuePairs.add(new BasicNameValuePair("userid", userid));  
    	nameValuePairs.add(new BasicNameValuePair("lng",lng.toString()));  
    	nameValuePairs.add(new BasicNameValuePair("lat",lat.toString()));  
   	
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
                //sendResultBroadcast(jsonResult);
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

    
    	
    	
    	
    	
    	//Log.d("sync ids",db.getNearIds(lng, lat));
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
    //}
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
