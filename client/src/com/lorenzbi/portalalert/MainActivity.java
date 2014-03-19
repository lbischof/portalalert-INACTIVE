package com.lorenzbi.portalalert;

import java.util.List;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends DrawerActivity {
    private SQLiteAdapter sqliteadapter;
    private static ListView mListView;
    public static List listAlerts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.contentlist);
        if (RegisterActivity.ringProgressDialog.isShowing()){
        	RegisterActivity.ringProgressDialog.dismiss();
        }
        sqliteadapter = new SQLiteAdapter(this);
        sqliteadapter.open();

		listAlerts = sqliteadapter.getAllAlerts();

		ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.textrow, listAlerts);
		mListView.setAdapter(adapter);
    }
    /*public void addAlert(String alertname) {

		ArrayAdapter adapter = (ArrayAdapter) mListView.getAdapter();

		
		Alert alert = sqliteadapter.addAlert(alertname);

		adapter.add(alert);

	}*/
    public void refreshList(){
    	ArrayAdapter adapter = (ArrayAdapter) mListView.getAdapter();
    	adapter.notifyDataSetChanged();
    }
    @Override
	protected void onResume() {
    	refreshList();
    	sqliteadapter.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		sqliteadapter.close();
		super.onPause();
	}
    
	 
	}