package com.lorenzbi.portalalert;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;



public class DrawerActivity extends Activity {

	private String[] drawerListViewItems;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    protected LinearLayout fullLayout;
    protected FrameLayout actContent;

   
    public void setupNavigationDrawer() {
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	boolean loggedIn = prefs.getBoolean("loggedIn", false);
    	if (!loggedIn) {
    		Intent intent = new Intent(this, RegisterActivity.class);
    		startActivity(intent);
    		finish();
    	}
    	
        
        drawerListViewItems = getResources().getStringArray(R.array.nav_drawer_items);
        drawerListView = (ListView) findViewById(R.id.left_drawer);
 
        // Set the adapter for the list view
        drawerListView.setAdapter(new ArrayAdapter<String>(this,
        		android.R.layout.simple_list_item_1, drawerListViewItems));
 
        // App Icon 
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                );
 
        // Set actionBarDrawerToggle as the DrawerListener
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
 
        getActionBar().setDisplayHomeAsUpEnabled(true); 
 
        // just styling option add shadow the right edge of the drawer
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    drawerListView.setOnItemClickListener(new DrawerItemClickListener());
     
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
         actionBarDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
        	Fragment fragment = null;
            FragmentManager fragmentManager = getFragmentManager();
            switch(position) {
                case 0:
                    fragment = new MapsFragment();
                    break;
                case 1:
                    fragment = new ListFragment();
                    break;
                case 2:
                    
                    fragment = new PrefsFragment();
                    
                    break;
            }
            fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
            drawerLayout.closeDrawer(drawerListView);
 
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
 
         // call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns true
        // then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static class PrefsFragment extends PreferenceFragment {
    	 
        @Override
        public void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);

                    // Load the preferences from an XML resource
                    addPreferencesFromResource(R.layout.preferences);
        }
}
 
}
