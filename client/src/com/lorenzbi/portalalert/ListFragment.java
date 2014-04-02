package com.lorenzbi.portalalert;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.squareup.otto.Subscribe;

public class ListFragment extends Fragment implements
LoaderManager.LoaderCallbacks<Cursor> {
	Double lat = null;
	Double lng = null;
	Double fudge = null;
	private DatabaseHelper db=null;
	private ListAdapter adapter=null;
	public SQLiteCursorLoader loader=null;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }
		
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		/*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                .penaltyLog()
                .build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
		        .detectLeakedClosableObjects()
		        .penaltyLog()
		        .penaltyDeath()
		        .build());*/
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		lng = getDouble(prefs, "lng", 0);
		lat = getDouble(prefs, "lat", 0);
		 db=new DatabaseHelper(getActivity().getBaseContext());
		 getLoaderManager().initLoader(0, null, this);
		 MainActivity.getEventBus().register(this);
	}
	@Subscribe
    public void onUpdateEvent(String msg) {
		Log.d("onupdateevent","onupdateevent");
		Cursor cursor = db.getAll();
    	Cursor oldcursor = adapter.swapCursor(cursor);
    	oldcursor.close();
    }
	double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
		return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
		}
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		loader= new SQLiteCursorLoader(getActivity().getBaseContext(), db, "SELECT _id, id, imagesrc, title, message, lng, lat, ( " + lat + " - lat) * ( " + lat +"- lat) + ( " + lng + "- lng) * ( " + lng + "- lng) * " + fudge + " as distance "	+ " from alerts "+ " order by distance asc", null);

		    return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		this.loader=(SQLiteCursorLoader)loader;
	   	adapter = new ListAdapter (getActivity().getApplicationContext(), cursor, 0);
		ListView lv=(ListView) getActivity().findViewById(R.id.contentlist);
	    lv.setAdapter(adapter);
	    registerForContextMenu(lv);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	    Cursor cursor = adapter.swapCursor(null);
	    cursor.close();
	}
	
    @Override
    public void onDestroy() {
      super.onDestroy();
      loader.reset();
      db.close();
    }
}