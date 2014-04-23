package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class CreateFragment extends DialogFragment {
	public AutoCompleteTextView autoComplete;
	public EditText txtMessage; 
	public String data;
	public List<String> suggest;
	ArrayAdapter<String> aAdapter;
	public CreateFragment() {
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    LayoutInflater i = getActivity().getLayoutInflater();
	    View view = i.inflate(R.layout.fragment_create,null);
		autoComplete = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
		txtMessage = (EditText) view.findViewById(R.id.message);
		final AlertDialog dialog =  new  AlertDialog.Builder(getActivity())
		.setView(view)
	    .setTitle("Neuer Alert")
	    .setPositiveButton("OK", null)
	    .setNegativeButton("Cancel", null)
	    .create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

		    @Override
		    public void onShow(DialogInterface d) {

		        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		        b.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		                // TODO Do something
		            	if (!suggest.contains(autoComplete.getText().toString())){
		            		autoComplete.setError("Bitte wählen Sie ein Portal.");
		            	} else if (txtMessage.getText().toString().trim().length() == 0){
		            		txtMessage.setError("Bitte lassen sie die Nachricht nicht leer");
		            	}
		                //Dismiss once everything is OK.
		                //dialog.dismiss();
		            }
		        });
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
					autoComplete.setError("Bitte wählen Sie ein Portal.");
                }
            }
		});
		txtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
					txtMessage.setError(null);
                } else if (txtMessage.getText().toString().trim().length() == 0){
            		txtMessage.setError("Bitte lassen sie die Nachricht nicht leer");
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
				autoComplete.setError(null);
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
		txtMessage.addTextChangedListener(new TextWatcher(){
 
			public void afterTextChanged(Editable editable) {
				// TODO Auto-generated method stub
 
			}
 
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
 
			}
 
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				txtMessage.setError(null);
			}
	});
}
}
