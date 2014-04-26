package com.lorenzbi.portalalert;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.util.Log;

public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	SharedPreferences prefs;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.preferences);
		prefs = PreferenceManager.getDefaultSharedPreferences (getActivity());
		setRingtoneSummary();
	}
	
	public void onStart() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	
	public void onOnstop() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		switch(key){
		case "ringtone":
			setRingtoneSummary();
			break;
		}
		
	}
	public void setRingtoneSummary(){
		Log.d("default", Settings.System.DEFAULT_NOTIFICATION_URI.toString());
		Uri ringtoneUri = Uri.parse(prefs.getString("ringtone", Settings.System.DEFAULT_NOTIFICATION_URI.toString()));
		if (ringtoneUri != null){
			Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
			String name = ringtone.getTitle(getActivity());
			RingtonePreference ringtonePref = (RingtonePreference) findPreference("ringtone");
			ringtonePref.setSummary(name);
		}
	}
	
}
