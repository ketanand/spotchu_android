package com.vrocketz.spotchu.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;

public class ViewSpot extends Activity{
	
	ProgressBar mProgressBar;
	Spot spot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen_spinner);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBarFetchSpot);
	}
	
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
			case Constants.SPOTS_FETCHED:
				try {
					spot = SpotHelper.getFromJson((JSONObject)msg.obj);
					initView();
				} catch (JSONException e) {
					e.printStackTrace();
					mProgressBar.setVisibility(View.GONE);
					//TODO : display some oops graphic
				}
				break;
			case Constants.SPOTS_FETCH_FAILED:
				mProgressBar.setVisibility(View.GONE);
				//TODO : display some oops graphic
			}
		}
	};
	
	private void initView(){
		mProgressBar.setVisibility(View.GONE);
		setContentView(R.layout.fullscreen_image);
	}

}
