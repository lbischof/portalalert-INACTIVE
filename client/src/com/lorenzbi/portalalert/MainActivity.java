package com.lorenzbi.portalalert;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends DrawerActivity implements
		ConnectionCallbacks, OnConnectionFailedListener {
	LocationClient locationClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.layout_main);
		if (savedInstanceState == null) {
			ListFragment listFragment = new ListFragment();
			getFragmentManager().beginTransaction()
					.replace(R.id.content_frame, listFragment).commit();
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setNavigationBarTintEnabled(false);
		tintManager.setTintColor(Color.parseColor("#03dc03"));
		setupNavigationDrawer();
		locationClient = new LocationClient(this, this, this);

		// setContentView(R.layout.activity_main);

	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
        case R.id.action_search:
            Log.d("search menu","search menu");
            return true;
        case R.id.action_add:
        	Log.d("add menu","add menu");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	@Override
	protected void onResume() {
		super.onResume();
		locationClient.connect();
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationClient.disconnect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
	}

	public void sendLocationEvent(Location location) {
		Log.d("sendlocationevent", location.getLongitude() + "");
		BusProvider.getInstance().post(location);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Location location = locationClient.getLastLocation();
		if (location != null) {
			sendLocationEvent(location);
			/*
			 * Intent syncIntent = new Intent(this, SyncIntentService.class);
			 * syncIntent.putExtra("lng", location.getLongitude());
			 * syncIntent.putExtra("lat", location.getLatitude());
			 * startService(syncIntent);
			 */
			Toast.makeText(
					this,
					"Location: " + location.getLatitude() + ", "
							+ location.getLongitude(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

}
