package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class CreateFragment extends DialogFragment {
	public AutoCompleteTextView autoComplete;
	public String data;
	public List<String> suggest;
	ArrayAdapter<String> aAdapter;
	AlertDialog dialog;
	public CreateFragment() {
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder b=  new  AlertDialog.Builder(getActivity())
	    .setTitle("Neuer Alert")
	    .setPositiveButton("OK",
	        new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                // do something...
	            }
	        }
	    )
	    .setNegativeButton("Cancel",
	        new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                dialog.dismiss();
	            }
	        }
	    );

	    LayoutInflater i = getActivity().getLayoutInflater();

	    View view = i.inflate(R.layout.fragment_create,null);
		autoComplete = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        b.setView(view);
        dialog = b.create();
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        return dialog;
	}
	
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		suggest = new ArrayList<String>();
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		autoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
					autoComplete.setError(null);
                } else if (!suggest.contains(autoComplete.getText().toString())){
					autoComplete.setError("Not a Portal");
                }
            }
		});
		autoComplete.addTextChangedListener(new TextWatcher(){
 
			public void afterTextChanged(Editable editable) {
				// TODO Auto-generated method stub
 
			}
 
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
 
			}
 
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				final String title = s.toString();
				if (!suggest.contains(title)){
            		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            	} else {
            		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            	}
				
				RequestParams params = new RequestParams();
				Location lastLocation = ((MainActivity)getActivity()).getLastLocation();
				params.put("lng", lastLocation.getLongitude()+"");
				params.put("lat", lastLocation.getLatitude()+"");
				params.put("title", title);
				if (title.length() > 1){
				HttpManager.post("search", params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String json) {
						
						try {
							suggest = new ArrayList<String>();
							JSONArray array = new JSONArray(json);
							for (int i=0; i<array.length(); i++)
								suggest.add(array.getString(i));
							aAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),R.layout.dropdown_row,suggest);
							autoComplete.setAdapter(aAdapter);
							
							
							aAdapter.notifyDataSetChanged();
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						

						Log.d("onTextChanged onSuccess", json);
					}
				});
			}
			}
        });
	}
}
