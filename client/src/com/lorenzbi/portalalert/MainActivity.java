package com.lorenzbi.portalalert;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;

public class MainActivity extends DrawerActivity implements
LoaderManager.LoaderCallbacks<Cursor> {
	private DatabaseHelper db=null;
	private SimpleCursorAdapter adapter=null;
	public SQLiteCursorLoader loader=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
		
		 db=new DatabaseHelper(this);
		 adapter=
			        new SimpleCursorAdapter(this, R.layout.row, null, new String[] {
			            DatabaseHelper.TITLE, DatabaseHelper.MESSAGE }, new int[] {
			            R.id.title, R.id.message }, 0);
		 
		 ListView lv=(ListView)findViewById(R.id.contentlist);

		    lv.setAdapter(adapter);
		    registerForContextMenu(lv);
		    getLoaderManager().initLoader(0, null, this);
		 
		/*if (RegisterActivity.ringProgressDialog.isShowing()){
			RegisterActivity.ringProgressDialog.dismiss();
		}*/
		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		loader=
		        new SQLiteCursorLoader(this, db, "SELECT _id, id, title, message "
		            + "FROM alerts ORDER BY title", null);

		    return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		this.loader=(SQLiteCursorLoader)loader;
	    adapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	    adapter.changeCursor(null);
	}
	private BroadcastReceiver notifyDataSetChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // intent can contain anydata
            Log.d("sohail","onReceive called");
            adapter.notifyDataSetChanged();

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
}