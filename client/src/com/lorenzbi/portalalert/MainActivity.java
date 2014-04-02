package com.lorenzbi.portalalert;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class MainActivity extends DrawerActivity {
	GoogleMap map = null;
	private static Bus mEventBus;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
        ListFragment listFragment = new ListFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, listFragment).commit();
        }
        mEventBus = new Bus(ThreadEnforcer.ANY);
	}
	public static Bus getEventBus() {
        return mEventBus;
    }
	
	
}
