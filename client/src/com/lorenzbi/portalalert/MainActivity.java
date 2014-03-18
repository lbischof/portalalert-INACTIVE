package com.lorenzbi.portalalert;

import java.util.List;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.lorenzbi.portalalert.SQLiteAdapter.Alert;

public class MainActivity extends DrawerActivity {
    private SQLiteAdapter sqliteadapter;
    private ListView mListView;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.contentlist);
        RegisterActivity.ringProgressDialog.dismiss();
        sqliteadapter = new SQLiteAdapter(this);
        sqliteadapter.open();

		List values = sqliteadapter.getAllAlerts();

		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		 mListView.setAdapter(adapter);
		 Alert alert = sqliteadapter.addAlert("test");
	     //adapter.add(alert);
		 addAlert("test");
    }
    public void addAlert(String alertname) {

		ArrayAdapter adapter = (ArrayAdapter) mListView.getAdapter();

		
		Alert alert = sqliteadapter.addAlert(alertname);

		adapter.add(alert);

	}
    
    @Override
	protected void onResume() {
    	sqliteadapter.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		sqliteadapter.close();
		super.onPause();
	}
    
	 
	}