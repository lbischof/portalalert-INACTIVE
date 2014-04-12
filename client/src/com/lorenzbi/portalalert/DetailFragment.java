package com.lorenzbi.portalalert;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.lorenzbi.portalalert.Alerts.Alert;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment {
	Alert alert;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_detail, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		TextView txtTitle = (TextView) getActivity().findViewById(R.id.title);
		TextView txtMessage = (TextView) getActivity().findViewById(
				R.id.message);
		ImageView imgView = (ImageView) getActivity().findViewById(R.id.image);
		String id = getArguments().getString("id");
		alert = dbHelper.getAlert(id);
		Picasso.with(getActivity()).load(alert.getImageSource()).fit()
				.centerCrop().into(imgView);
		Typeface typeFace = FontCache.get("Roboto-Light.ttf", getActivity());
		txtTitle.setTypeface(typeFace);
		txtMessage.setTypeface(typeFace);
		txtTitle.setText(alert.getTitle());
		txtMessage.setText(alert.getMessage());
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
	    super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.menu_detail, menu);
	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
        
        case R.id.action_done:
        	alertDone();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	public void alertDone(){
		String id = alert.getId();
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity().getApplicationContext());
		RequestParams params = new RequestParams();
		params.put("id", id);
		params.put("userid", prefs.getString("userid", ""));
		params.put("lng", alert.getLocation().getLng().toString());
		params.put("lat", alert.getLocation().getLat().toString());
		HttpManager.post("done", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				getFragmentManager().popBackStack(); 
				
				DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
				dbHelper.removeAlert(response, getActivity());
			}
		});
	}
}
