package com.lorenzbi.portalalert;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class MainActivity extends DrawerActivity {
	GoogleMap map = null;
	private static Bus mEventBus;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.layout_main);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	    // enable status bar tint
	    tintManager.setStatusBarTintEnabled(true);
	    // enable navigation bar tint
	    tintManager.setNavigationBarTintEnabled(true);
		tintManager.setTintColor(Color.parseColor("#03dc03"));
		setupNavigationDrawer();
        //setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
        ListFragment listFragment = new ListFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, listFragment).commit();
        }
        mEventBus = new Bus(ThreadEnforcer.ANY);
	}
	public static Bus getEventBus() {
        return mEventBus;
    }
	
	
}
