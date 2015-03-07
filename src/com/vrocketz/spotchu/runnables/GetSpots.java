package com.vrocketz.spotchu.runnables;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetSpots implements Runnable {

	private static String END_POINT = "spots/";
	private static String URL = Constants.API_HOST + END_POINT;
	private static String END_POINT_MYCIRCLE = "followSpots/";
	private static String URL_MYCIRCLE = Constants.API_HOST + END_POINT_MYCIRCLE;
	private Handler mHandler;
	private Integer mLast;
	private Long mTimestamp;
	private StringBuilder urlBuilder;
	
	public GetSpots (Handler handler, Integer last, Long time){
		mHandler = handler;
		mLast = last;
		mTimestamp = time;
		urlBuilder = new StringBuilder(URL);
	}
	
	public GetSpots (Handler handler, Integer last, Long time, boolean myCircle){
		mHandler = handler;
		mLast = last;
		mTimestamp = time;
		urlBuilder = new StringBuilder(URL_MYCIRCLE);
	}
	
	@Override
	public void run() {
		urlBuilder.append(mTimestamp).append("/").append(mLast);
		try {
			if (!Util.isInternetAvailable()){
				Message msg = mHandler.obtainMessage(Constants.SPOTS_FETCH_FAILED);
				mHandler.sendMessage(msg);
			}else {
				HttpResponse response = Util.sendGet(urlBuilder.toString());
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
