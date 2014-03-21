package com.lorenzbi.portalalert;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;

public class MainActivity extends DrawerActivity implements
LoaderManager.LoaderCallbacks<Cursor> {
	private static final int ADD_ID=Menu.FIRST + 1;
	private static final int DELETE_ID=Menu.FIRST + 3;
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
		 
		if (RegisterActivity.ringProgressDialog.isShowing()){
			RegisterActivity.ringProgressDialog.dismiss();
		}
		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		loader=
		        new SQLiteCursorLoader(this, db, "SELECT _ID, title, message "
		            + "FROM constants ORDER BY title", null);

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
	
}