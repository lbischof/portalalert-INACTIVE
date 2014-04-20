package com.lorenzbi.portalalert;

import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends DrawerActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, OnBackStackChangedListener{
	LocationClient locationClient;
	private Boolean updateNeeded = false;
	private Location lastLocation;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.layout_main);		
		Log.d("action"," "+getIntent().getAction());
		if (getIntent().getAction() != null && getIntent().getAction().contains("detail")){
			String id = getIntent().getAction().replace("detail", "");
			DetailFragment detailFragment = new DetailFragment();
			Bundle bundle = new Bundle();
			bundle.putString("id", id);
			detailFragment.setArguments(bundle);
			getFragmentManager().beginTransaction().replace(R.id.content_frame, detailFragment).commit();
		} else if (savedInstanceState == null) {
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
		  getFragmentManager().addOnBackStackChangedListener(this);

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
        case android.R.id.home: 
			getFragmentManager().popBackStack(); 
        	return super.onOptionsItemSelected(item);
        case R.id.action_add:
            CreateFragment fragment= new CreateFragment();
            fragment.show(getFragmentManager(), "fragment_edit_name");
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
		lastLocation = locationClient.getLastLocation();

		if (lastLocation != null) {
			sendLocationEvent(lastLocation);
			
		}
	}
	public Location getLastLocation(){
		return lastLocation;
	}
	public Boolean getUpdateNeeded(){
		return this.updateNeeded;
	}
	public void setUpdateNeeded(Boolean updateNeeded){
		this.updateNeeded = updateNeeded;
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}
	@Override
	public void onBackStackChanged() {
		int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
		  if(backStackEntryCount > 0){
				actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
		  }else{
				actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
		  }
	}
	

}
