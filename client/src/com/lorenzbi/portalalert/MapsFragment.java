package com.lorenzbi.portalalert;

import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.lorenzbi.portalalert.Alerts.Alert;


public class MapsFragment extends Fragment {

	private GoogleMap mMap;
	MapView mapView;
    private Bundle mBundle;

	private HashMap<String, Marker> courseMarkers = new HashMap<String, Marker>();
	private HashMap<Marker, String> markerIds = new HashMap<Marker, String>();

	List<Alert> alerts;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_map, container, false);
		// Gets the MapView from the XML layout and creates it

		MapsInitializer.initialize(getActivity());
		mapView = (MapView) v.findViewById(R.id.map);
        mapView.onCreate(mBundle);
		setupMapIfNeeded(v);

		// Updates the location and zoom of the MapView

		return v;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mBundle = savedInstanceState;
	}
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
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
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    private void setupMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();
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
		HttpManager.post("everything", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String json) {
				Gson gson = new Gson();
				Alerts root = gson.fromJson(json, Alerts.class);
				alerts = root.getAlerts();
	            addItemsToMap(alerts);
			}
		});
		mMap.setOnCameraChangeListener(getCameraChangeListener());
		
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	        @Override
	        public void onInfoWindowClick(Marker marker) {
	        	String id = markerIds.get(marker);
	        	Log.d("id",id);
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
	public MarkerOptions getMarkerForAlert(Alert alert){
		MarkerOptions marker = new MarkerOptions().position(new LatLng(alert.getLocation().getLat(), alert.getLocation().getLng())).title(
				alert.getTitle()).snippet(alert.getMessage()).alpha(0);
		return marker;
	}
	public OnCameraChangeListener getCameraChangeListener()
	{
	    return new OnCameraChangeListener() 
	    {
	        @Override
	        public void onCameraChange(CameraPosition position) 
	        {
	        	if (alerts != null)
	            addItemsToMap(alerts);
	        }
	    };
	}
	private void addItemsToMap(List<Alert> items)
	{
	    if(this.mMap != null)
	    {
	        //This is the current user-viewable region of the map
	        LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;
	 
	        //Loop through all the items that are available to be placed on the map
	        for(Alert item : items) 
	        {
	 
	            //If the item is within the the bounds of the screen
	            if(bounds.contains(new LatLng(item.getLocation().getLat(), item.getLocation().getLng())))
	            {
	                //If the item isn't already being displayed
	                if(!courseMarkers.containsKey(item.getId()))
	                {
	                    //Add the Marker to the Map and keep track of it with the HashMap
	                    //getMarkerForItem just returns a MarkerOptions object
	                	Marker marker = this.mMap.addMarker(getMarkerForAlert(item));
	                    this.courseMarkers.put(item.getId(), marker);
	                    this.markerIds.put(marker, item.getId());
	                    animateMarker(marker, true);
	                }
	            }
	 
	            //If the marker is off screen
	            else
	            {
	                //If the course was previously on screen
	                if(courseMarkers.containsKey(item.getId()))
	                {
	                    //1. Remove the Marker from the GoogleMap
	                    courseMarkers.get(item.getId()).remove();
	                 
	                    //2. Remove the reference to the Marker from the HashMap
	                    courseMarkers.remove(item.getId());
	                }
	            }
	        }
	    }
	}
	public static void animateMarker(final Marker marker, final boolean visible) {
    	boolean isVisible = (marker.getAlpha() == 1);
		// check if marker already has the desired state.
		if (isVisible != visible) {
			ObjectAnimator markerFade;
			// set if marker should fade in ou out
			if (visible) {
				markerFade = ObjectAnimator.ofFloat(marker, "alpha", 0, 1);
			} else {
				markerFade = ObjectAnimator.ofFloat(marker, "alpha", 1, 0);
			}
			// set animation time
			markerFade.setDuration(500);
			// setVisible() is necessary to make the marker clickable or not
			markerFade.addListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
					if (visible)
						marker.setVisible(true);
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					if (!visible)
						marker.setVisible(false);
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			// start the animation
			markerFade.start();
		}
	}
}