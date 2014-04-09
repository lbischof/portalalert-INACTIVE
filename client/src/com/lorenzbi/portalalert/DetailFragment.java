package com.lorenzbi.portalalert;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lorenzbi.portalalert.Alerts.Alert;

public class DetailFragment extends Fragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_detail, container, false);
	}
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		TextView txtTitle = (TextView) getActivity().findViewById(R.id.title);
		TextView txtMessage = (TextView) getActivity().findViewById(R.id.message);
		String id = getArguments().getString("id");
		Alert alert = dbHelper.getAlert(id);
		txtTitle.setText(alert.getTitle());
		txtMessage.setText(alert.getMessage());
	}
}
