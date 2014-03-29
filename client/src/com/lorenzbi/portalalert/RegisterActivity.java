package com.lorenzbi.portalalert;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RegisterActivity extends Activity implements
ConnectionCallbacks, OnConnectionFailedListener, OnClickListener,GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	
	/* GCM VARIABLES */
	public static final String EXTRA_MESSAGE = "message";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    //private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private LocationClient mLocationclient;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "520119052038";
    static final String TAG = "PortalAlertGCM";
    
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String ingressUsername;
    String regid;
    String personName = "";
	String personEmail = "";
	String personId = "";
	String personPhotoUrl = "";
	Boolean isFrog = false;
	Double lat = null;
	Double lng = null;
	/* GOOGLE LOGIN VARIABLES */
	private GoogleApiClient mGoogleApiClient;
	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;
	public boolean waitForLocation = false;
	/* A flag indicating that a PendingIntent is in progress and prevents
	 * us from starting further intents.
	 */
	private boolean mIntentInProgress;
	public static ProgressDialog ringProgressDialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ringProgressDialog = ProgressDialog.show(RegisterActivity.this, "Please wait...", "Signing into Google Plus...", true);
		ringProgressDialog.setCancelable(true);
		setContentView(R.layout.activity_register);
		
		//TODO: Check if google play services is installed
		
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
		.addApi(Plus.API, null)
		.addScope(Plus.SCOPE_PLUS_LOGIN)
		.build();
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		
		try {
            // this.locatorService= new
            // Intent(FastMainActivity.this,LocatorService.class);
            // startService(this.locatorService);

			mIntentService = new Intent(this, LocationService.class);
	        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, 0);
	        
        } catch (Exception error) {
        }
	}
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sign_in_button
				&& !mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			resolveSignInError();
			ringProgressDialog = ProgressDialog.show(RegisterActivity.this, "Please wait...", "Signing into Google Plus...", true);
			ringProgressDialog.setCancelable(true);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		mGoogleApiClient.connect();
		int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            mLocationclient = new LocationClient(this, this, this);
            mLocationclient.connect();
            Log.i("onresume success", "success");
        } else {
            GooglePlayServicesUtil.getErrorDialog(resp, this, 2);
            Log.i("onresume fail", "fail");
        }
        
	}
	   @Override
	   protected void onPause() {
	    // TODO Auto-generated method stub
	    super.onPause();
	    if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	   }
	@Override
	protected void onStart() {
		super.onStart();
	}
	@Override
	protected void onStop() {
		super.onStop();

		
	}
	/* Track whether the sign-in button has been clicked so that we know to resolve
	 * all issues preventing sign-in without waiting.
	 */
	private boolean mSignInClicked;
	/* Store the connection result from onConnectionFailed callbacks so that we can
	 * resolve them when the user clicks sign-in.
	 */
	private ConnectionResult mConnectionResult;
	/* A helper method to resolve the current ConnectionResult error. */
	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try { 
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
			} catch (SendIntentException e) {
				// The intent was canceled before it was sent.  Return to the default
				// state and attempt to connect to get an updated ConnectionResult.
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		} 
	}

	public void onConnectionFailed(ConnectionResult result) {
        ringProgressDialog.dismiss();
		if (!mIntentInProgress) {
			// Store the ConnectionResult so that we can use it later when the user clicks
			// 'sign-in'.
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (mLocationclient != null && mLocationclient.isConnected() && mGoogleApiClient.isConnected()) {
			Location loc = mLocationclient.getLastLocation();
			final EditText usernameInput = (EditText)findViewById(R.id.username);
		ingressUsername = usernameInput.getText().toString(); //TODO do something with this
		mSignInClicked = false;
		try { 
				/*if ( loc.getTime() < (System.currentTimeMillis() - 60*60*60*5))
					Log.i(TAG, "smaller");*/
				lat = loc.getLatitude();
				lng = loc.getLongitude();
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                personName = currentPerson.getDisplayName();
                personPhotoUrl = currentPerson.getImage().getUrl();
                Log.i ("com.lorenzbi.portalalertResisterActivity", personPhotoUrl);
               // String personGooglePlusProfile = currentPerson.getUrl();
                personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
                personId = currentPerson.getId();
                registerGCM();
    			tryRegistering();
                
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		
		}
	}
	public void tryRegistering() {
		if (regid != null && !regid.isEmpty() && lat != null && personId != null && !personId.isEmpty()){
			sendRegistrationIdToBackend();
		} 
	}
	public void saveInfoToPrefs(){
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("regid", regid);
		editor.putString("name", personName);
		editor.putString("email", personEmail);
		editor.putString("username", ingressUsername);
		editor.putString("picture", personPhotoUrl);
		putDouble(editor, "lng", lng);
		putDouble(editor, "lat", lat);
		editor.putString("id", personId);
		editor.putBoolean("loggedIn", true);
		editor.commit();
	}
	Editor putDouble(final Editor edit, final String key, final double value) {
		   return edit.putLong(key, Double.doubleToRawLongBits(value));
		}

		double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
		return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
		}
	public void registerGCM() {
		context = getApplicationContext();

        // TODO: Check device for Play Services APK.
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            if (regid.isEmpty()) {
                registerInBackground();
            }
	}
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    String registrationId = prefs.getString("regid", "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	/**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {  
            	tryRegistering();
            }
        }.execute(null, null, null);
    }
    
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
    	//AsyncHttpClient client = new AsyncHttpClient();
    	RequestParams params = new RequestParams();
    	params.put("username", ingressUsername);
    	params.put("regid", regid);
    	params.put("name", personName );
    	params.put("email", personEmail);
    	params.put("userid", personId);
    	params.put("lng", lng.toString());
    	params.put("lat", lat.toString());

    	HttpManager.post("register", params, new AsyncHttpResponseHandler() {
    	    @Override
    	    public void onSuccess(String response) {
    	    	
    	    			try {
							JSONObject jsonObject = new JSONObject(response);
							
							if (jsonObject.has("error") && jsonObject.getString("error").equals("NOT_FROG")) {
								notFrog();
							} else if(jsonObject.has("error") && !jsonObject.isNull("error")) {
								Log.e("unknown error", jsonObject.getString("error"));
							} else {
								verifiedFrog(response);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	    }
    	    @Override
    	     public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
    	 {
    	    	ringProgressDialog.dismiss();
    	        Log.e("register failed", statusCode+ "");
    	        Toast.makeText(RegisterActivity.this, "Server not available.", Toast.LENGTH_LONG).show();
    	     }

			
    	});
    }
   protected void notFrog() {
		// TODO Auto-generated method stub
		ringProgressDialog.dismiss();
		Toast.makeText(this, "Not a verified Frog!", Toast.LENGTH_LONG).show();
	}
private void verifiedFrog(String currentFences) {
	Intent syncIntent = new Intent(RegisterActivity.this, SyncIntentService.class);
	syncIntent.putExtra("JSON", currentFences);
	startService(syncIntent);
	saveInfoToPrefs();
	ringProgressDialog.dismiss();
	Intent intent = new Intent(this, MainActivity.class);
	startActivity(intent);
	finish();
	}
    
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	public void updateLoc(Location loc) {
		lat = loc.getLatitude();
		lng = loc.getLongitude();
		
		tryRegistering();
		
	}
	
    

    // Required functions    
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
	
	public boolean isFrog(String email) {
		//TODO: Check if person is a valid Frog (maybe a serverside list of email adresses, don't know...) 
		return true;
	}
	
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();
	}

	public void onDisconnected() {
		
	}
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		}
	}
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}
	
}
