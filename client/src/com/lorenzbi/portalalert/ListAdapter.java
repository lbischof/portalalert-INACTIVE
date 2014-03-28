package com.lorenzbi.portalalert;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lorenzbi.portalalert.Alerts.Alert;
import com.lorenzbi.portalalert.Alerts.AlertLocation;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ListAdapter extends CursorAdapter {
	private LayoutInflater mInflator;
	protected ImageLoader imageLoader;

	Context context;

	ListAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
		mInflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = ImageLoader.getInstance();
	}

	static class ViewHolder {
		ImageView imageView;
		TextView txtTitle, txtMessage;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (null != cursor) {
			// Cursor cursor =
			// getReadableDatabase().rawQuery("select * from alerts",null);
			ImageView imageView = (ImageView) view.findViewById(R.id.image);
			TextView txtTitle = (TextView) view.findViewById(R.id.title);
			TextView txtMessage = (TextView) view.findViewById(R.id.message);

			
			
			String id = cursor.getString(cursor.getColumnIndex("id"));
			String imagesrc = cursor.getString(cursor
					.getColumnIndex("imagesrc"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String message = cursor.getString(cursor.getColumnIndex("message"));
			AlertLocation alertLocation = new AlertLocation();
			Log.i("getlng portalalert",
					cursor.getDouble(cursor.getColumnIndex("lng")) + "");

			alertLocation
					.setLng(cursor.getDouble(cursor.getColumnIndex("lng")));
			alertLocation
					.setLat(cursor.getDouble(cursor.getColumnIndex("lat")));
			Float radius = cursor.getFloat(cursor.getColumnIndex("message"));

			imageLoader.displayImage(imagesrc, imageView);
			txtTitle.setText(title);
			txtMessage.setText(message);
		}
		// Set the Menu Image
		/*
		 * ImageView menuImage=(ImageView)arg0.findViewById(R.id.iv_ContactImg);
		 * menuImage.setImageResource(R.drawable.ic_launcher);
		 * 
		 * //Set the Name TextView
		 * heading=(TextView)arg0.findViewById(R.id.tv_ContactName);
		 * heading.setText(name); //Set Availability TextView
		 * randomText=(TextView)arg0.findViewById(R.id.tv_ContactNumber);
		 * randomText.setText("Not Available");
		 */

	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		final View customListView = mInflator.inflate(R.layout.row, null);
		return customListView;
	}
}
