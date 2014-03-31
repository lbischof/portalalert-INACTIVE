package com.lorenzbi.portalalert;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;

public class MainActivity extends DrawerActivity {
	GoogleMap map = null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            return;
        }
        if (savedInstanceState == null) {
        ListFragment listFragment = new ListFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, listFragment).commit();
    }
	}
	
	
}
