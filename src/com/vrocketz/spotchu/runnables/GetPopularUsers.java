package com.vrocketz.spotchu.runnables;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetPopularUsers implements Runnable{

	private static String END_POINT = "user/around";
	private static String URL = Constants.API_HOST + END_POINT;
	private Handler mHandler;
	
	public GetPopularUsers(Handler handler){
		mHandler = handler;
	}
	
	@Override
	public void run() {
		try {
			if (!Util.isInternetAvailable()){
				Message msg = mHandler.obtainMessage(Constants.NO_INTERNET);
				mHandler.sendMessage(msg);
			}else {
				HttpResponse response = Util.sendGet(URL);
				String res = Util.convertResponseToString(response);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[GetPopularUser] response : " + res);
				if (res != null){
					JSONObject json = new JSONObject(res);
					if (!json.getBoolean("error")){
						Message msg = mHandler.obtainMessage(Constants.USERS_FETCHED, json.getJSONArray("users"));
						mHandler.sendMessage(msg);
					} else {
						Message msg = mHandler.obtainMessage(Constants.USERS_FETCH_FAILED);
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
