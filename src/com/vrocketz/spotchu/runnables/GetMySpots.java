package com.vrocketz.spotchu.runnables;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class GetMySpots implements Runnable {

	private static String END_POINT_MYSPOTS = "spots";
	private static String URL_MYSPOTS = Constants.API_HOST + END_POINT_MYSPOTS;
	private static String END_POINT_SPOTS = "user/spots/";
	private static String URL_SPOTS = Constants.API_HOST + END_POINT_SPOTS;
	private Handler mHandler;
	private Long mUserId;
	private StringBuilder mUrl;
	
	public GetMySpots (Handler handler){
		mHandler = handler;
		mUserId = null;
		mUrl = new StringBuilder(URL_MYSPOTS);
	}
	
	public GetMySpots (Handler handler, Long userId){
		mHandler = handler;
		mUserId = userId;
		mUrl = new StringBuilder(URL_SPOTS);
		mUrl.append(mUserId);
	}
	
	@Override
	public void run() {
		try {
			if (!Util.isInternetAvailable()){
				Message msg = mHandler.obtainMessage(Constants.NO_INTERNET);
				mHandler.sendMessage(msg);
			}else {
				HttpResponse response = Util.sendGet(mUrl.toString());
				String res = Util.convertResponseToString(response);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[GetSpots] response : " + res);
				if (res != null){
					JSONObject json = new JSONObject(res);
					if (!json.getBoolean("error")){
						Message msg = mHandler.obtainMessage(Constants.SPOTS_FETCHED, json.getJSONArray("spots"));
						mHandler.sendMessage(msg);
					} else {
						Message msg = mHandler.obtainMessage(Constants.SPOTS_FETCH_FAILED);
						mHandler.sendMessage(msg);
					}
				}
			}
		}catch (IOException e) {
			Message msg = mHandler.obtainMessage(Constants.SPOTS_FETCH_FAILED);
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (JSONException e) {
			Message msg = mHandler.obtainMessage(Constants.SPOTS_FETCH_FAILED);
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} 
	}

}
