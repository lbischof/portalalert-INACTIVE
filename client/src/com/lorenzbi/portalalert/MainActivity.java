package com.lorenzbi.portalalert;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

public class MainActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	    // enable status bar tint
	    tintManager.setStatusBarTintEnabled(true);
	    // enable navigation bar tint
	    tintManager.setNavigationBarTintEnabled(true);
		tintManager.setTintColor(Color.parseColor("#03dc03"));
        if (savedInstanceState != null) {
            return;
        }
        ListFragment listFragment = new ListFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, listFragment).commit();
    }
}
