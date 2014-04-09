package com.lorenzbi.portalalert;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lorenzbi.portalalert.Alerts.Alert;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_detail, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		TextView txtTitle = (TextView) getActivity().findViewById(R.id.title);
		TextView txtMessage = (TextView) getActivity().findViewById(
				R.id.message);
		ImageView imgView = (ImageView) getActivity().findViewById(R.id.image);
		String id = getArguments().getString("id");
		Alert alert = dbHelper.getAlert(id);
		Picasso.with(getActivity()).load(alert.getImageSource()).fit()
				.centerCrop().into(imgView);
		Typeface typeFace = FontCache.get("Roboto-Light.ttf", getActivity());
		txtTitle.setTypeface(typeFace);
		txtMessage.setTypeface(typeFace);
		txtTitle.setText(alert.getTitle());
		txtMessage.setText(alert.getMessage());
	}
}
