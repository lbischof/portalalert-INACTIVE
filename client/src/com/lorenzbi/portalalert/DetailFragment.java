package com.lorenzbi.portalalert;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jensdriller.libs.undobar.UndoBar;
import com.jensdriller.libs.undobar.UndoBar.Listener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.lorenzbi.portalalert.Alerts.Alert;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements Listener {
	Alert alert;
	Context context;
	DatabaseHelper dbHelper;
	String userid;
	Boolean alertNear = false;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_detail, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		context = getActivity();
		DatabaseHelper dbHelper = new DatabaseHelper(context);

		
		String id = getArguments().getString("id");
		if (dbHelper.getAlert(id) != null){
			alert = dbHelper.getAlert(id);
			alertNear = true;
			showDetails(alert);
		} else {
			RequestParams params = new RequestParams();
			params.put("id", id);
			HttpManager.post("getAlertById", params, new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(String json) {
					Log.d("response", json);
					Gson gson = new Gson();
					alert = gson.fromJson(json, Alert.class);
					//alert.getId();
					showDetails(alert);
				}
			});
		}
	}
	public void showDetails(Alert alert){
		TextView txtTitle = (TextView) getActivity().findViewById(R.id.title);
		TextView txtMessage = (TextView) getActivity().findViewById(
				R.id.message);
		ImageView imgView = (ImageView) getActivity().findViewById(R.id.image);
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
        case R.id.action_delete:
        	removeAlert();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	public void alertDone(){
		Integer type = alert.getType();
		switch(type){
		case 1: //upgraded
			upgradedAlert();
			break;
		case 2: //destroyed
			removeAlert();
			break;
		}
	}
	public void upgradedAlert(){
		Toast.makeText(getActivity(), "Danke fürs upgraden", Toast.LENGTH_SHORT).show();
	}
	public void removeAlert(){
		String id = alert.getId();
		String title = alert.getTitle();
		((MainActivity)getActivity()).setUpdateNeeded(true);
		if (alertNear){
		dbHelper = new DatabaseHelper(context);
		dbHelper.removeAlert(id);
		}
		getFragmentManager().popBackStack(); 
		final Bundle bundle = new Bundle();
		bundle.putString("id", id);
		new UndoBar.Builder(getActivity())//
		  .setMessage(title)
		  .setUndoToken(bundle)
		  .setListener(DetailFragment.this)//
		  .show();
	}
	@Override
	public void onUndo(Parcelable token) {
		if (token != null) {
			String id = ((Bundle) token).getString("id");
			if (alertNear)
				dbHelper.undoRemove(id);
			BusProvider.getInstance().post(new String("update"));
		}
	}

	@Override
	public void onHide() {
		Log.d("onhide","onhide");
		String id = alert.getId();
		RequestParams params = new RequestParams();
		params.put("id", id);
		params.put("userid", "");
		params.put("lng", alert.getLocation().getLng().toString());
		params.put("lat", alert.getLocation().getLat().toString());
		HttpManager.post("done", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				
			}
		});
	}
	@Subscribe
	public void onUpdateEvent(String msg) {
		Log.d("onupdateevent", "onupdateevent");
		((MainActivity)getActivity()).setUpdateNeeded(true);
	}
	public void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
	}
	public void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}
}
