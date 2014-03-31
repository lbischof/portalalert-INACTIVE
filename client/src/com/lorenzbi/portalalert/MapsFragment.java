package com.lorenzbi.portalalert;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapsFragment extends Fragment {
	
		static final LatLng HAMBURG = new LatLng(53.558, 9.927);
        static final LatLng KIEL = new LatLng(53.551, 9.993);
        private GoogleMap map;
        private static View view;

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
	  Log.d("oncreateview", "oncreateview");
	  try {
      view = (RelativeLayout) inflater.inflate(R.layout.fragment_map, container, false);
	  } catch(Exception e) {
		  
	  }
	  
	      map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapview))
	          .getMap();
	      map.setMyLocationEnabled(true);
	      
	Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
	              .title("Hamburg"));
	          Marker kiel = map.addMarker(new MarkerOptions()
	              .position(KIEL)
	              .title("Kiel")
	              .snippet("Kiel is cool")
	              .icon(BitmapDescriptorFactory
	                  .fromResource(R.drawable.ic_launcher)));

	          // Move the camera instantly to hamburg with a zoom of 15.
	          map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));

	          // Zoom in, animating the camera.
	          map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
	  
      return view;
  }
 
}
