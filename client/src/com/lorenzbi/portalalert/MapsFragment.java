package com.lorenzbi.portalalert;

import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsFragment extends Fragment {

	private GoogleMap mMap;
	private static View view;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.fragment_map, container, false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
		}
		setUpMapIfNeeded();
		return view;
	}
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
		    @Override
		    public void onCameraChange(CameraPosition cameraPosition) {
		        // Make a web call for the locations
		    	LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
		        Log.d("camerachange", bounds.northeast.toString());
		}
		});
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	        @Override
	        public void onInfoWindowClick(Marker marker) {
	        	DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
	        	String id = dbHelper.getId(marker.getTitle());

	    		FragmentManager fragmentManager = getFragmentManager();
	    		Fragment fragment = new DetailFragment();
	    		Bundle bundle = new Bundle();
	    		bundle.putString("id", id);
	    		fragment.setArguments(bundle);
	    		fragmentManager.beginTransaction().setCustomAnimations(R.animator.fragment_slide_left_enter,
	                    R.animator.fragment_slide_left_exit,
	                    R.animator.fragment_slide_right_enter,
	                    R.animator.fragment_slide_right_exit).addToBackStack(null)
	    				.replace(R.id.content_frame, fragment).commit();
	        }
	    });
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
	    super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.menu_map, menu);
	}
	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.mapview)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}
	
	private void setUpMap() {
		Location lastLocation = ((MainActivity)getActivity()).getLastLocation();
		CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(
	            new CameraPosition.Builder().target(new LatLng(lastLocation.getLatitude(),
	                    lastLocation.getLongitude())).zoom(13).build());
	    mMap.moveCamera(myLoc);
		mMap.setMyLocationEnabled(true);
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		//mMap.setOnMyLocationChangeListener(this);
		Cursor cursor = dbHelper.getNear(lastLocation.getLongitude(), lastLocation.getLatitude());
		while (cursor.moveToNext()) {
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String message = cursor.getString(cursor.getColumnIndex("message"));
			Double lng = cursor.getDouble(cursor.getColumnIndex("lng"));
			Double lat = cursor.getDouble(cursor.getColumnIndex("lat"));

			mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(
					title).snippet(message));
		}
		
	}
	
}