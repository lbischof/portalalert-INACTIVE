package com.lorenzbi.portalalert;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.squareup.otto.Subscribe;

public class ListFragment extends Fragment implements
LoaderManager.LoaderCallbacks<Cursor> {
	static Double lat = null;
	static Double lng = null;
	Double fudge = null;
	private DatabaseHelper db=null;
	private ListAdapter adapter=null;
	public SQLiteCursorLoader loader=null;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }
		
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		db=new DatabaseHelper(getActivity().getBaseContext());
		if (lat != null && lng != null){ //if no location wait for locationevent
			 getLoaderManager().initLoader(0, null, this);
		}
	}
	@Subscribe
	public void onLocationEvent(Location location){
		lng = location.getLongitude();
		lat = location.getLatitude();
		getLoaderManager().initLoader(0, null, this);
	}
	@Subscribe
    public void onUpdateEvent(String msg) {
		Log.d("onupdateevent", "onupdateevent");
		if (adapter != null) {
		Cursor cursor = db.getNear(lng,lat);
    	adapter.swapCursor(cursor);
		}
    }
	double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
		return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
		}
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		fudge = Math.pow(Math.cos(Math.toRadians(lat)),2);
		Long now = System.currentTimeMillis();
		Log.d("now",now.toString());
		loader= new SQLiteCursorLoader(getActivity().getBaseContext(), db, "SELECT _id, id, imagesrc, title, message, lng, lat, expire, ( " + lat + " - lat) * ( " + lat +"- lat) + ( " + lng + "- lng) * ( " + lng + "- lng) * " + fudge + " as distance "	+ " from alerts  WHERE expire >= "+now+ " order by distance asc", null);

		    return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		this.loader=(SQLiteCursorLoader)loader;
	   	adapter = new ListAdapter (getActivity().getApplicationContext(), cursor, 0);
		ListView lv=(ListView) getActivity().findViewById(R.id.contentlist);
	    lv.setAdapter(adapter);
	    registerForContextMenu(lv);
		
		Log.d("onloadfinished register", "onloadfinished register");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	    adapter.swapCursor(null);
	   	}
	public void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
		//Log.d("register onresume", "register onresume");
	}
	public void onPause(){
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}
	
    @Override
    public void onDestroy() {
      super.onDestroy();
      if (loader != null)
      loader.reset();
      db.close();
    }
}