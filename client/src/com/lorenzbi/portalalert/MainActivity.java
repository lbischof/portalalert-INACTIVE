package com.lorenzbi.portalalert;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends DrawerActivity implements
LoaderManager.LoaderCallbacks<Cursor> {
	Double lat = null;
	Double lng = null;
	Double fudge = null;
	private DatabaseHelper db=null;
	private ListAdapter adapter=null;
	public SQLiteCursorLoader loader=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
	    // enable status bar tint
	    tintManager.setStatusBarTintEnabled(true);
	    // enable navigation bar tint
	    tintManager.setNavigationBarTintEnabled(true);
		tintManager.setTintColor(Color.parseColor("#03dc03"));
		super.onCreate(savedInstanceState);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                .penaltyLog()
                .build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
		        .detectLeakedClosableObjects()
		        .penaltyLog()
		        .penaltyDeath()
		        .build());
		

		setContentView(R.layout.activity_main);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		lng = getDouble(prefs, "lng", 0);
		lat = getDouble(prefs, "lat", 0);
		 db=new DatabaseHelper(this);
		 getLoaderManager().initLoader(0, null, this);
	}
	double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
		return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
		}
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		loader= new SQLiteCursorLoader(this, db, "SELECT _id, id, imagesrc, title, message, lng, lat, ( " + lat + " - lat) * ( " + lat +"- lat) + ( " + lng + "- lng) * ( " + lng + "- lng) * " + fudge + " as distance "	+ " from alerts "+ " order by distance asc", null);

		    return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		this.loader=(SQLiteCursorLoader)loader;
	   	adapter = new ListAdapter (getApplicationContext(), cursor, 0);
		ListView lv=(ListView)findViewById(R.id.contentlist);
	    lv.setAdapter(adapter);
	    registerForContextMenu(lv);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	    Cursor cursor = adapter.swapCursor(null);
	    cursor.close();
	}
	private BroadcastReceiver notifyDataSetChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // intent can contain anydata
        	DatabaseHelper dbhelper = new DatabaseHelper(MainActivity.this);
        	Cursor cursor = dbhelper.getAll();
        	Cursor oldcursor = adapter.swapCursor(cursor);
        	oldcursor.close();
            Log.d("sohail","onReceive called");
            //adapter.notifyDataSetChanged();
            //getLoaderManager().restartLoader(0, null, MainActivity.this);


        }
    };
    @Override
    protected void onResume() {
    	super.onResume();
    	LocalBroadcastManager.getInstance(this).registerReceiver(notifyDataSetChanged, new IntentFilter("notifyDatasetChanged"));
    }
    @Override 
    protected void onPause(){
    	super.onPause();
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(notifyDataSetChanged);
    }
    @Override
    public void onDestroy() {
      super.onDestroy();

      db.close();
    }
}