package com.vrocketz.spotchu.runnables;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class GetFollowers implements Runnable {
	
	private static String END_POINT = "user/followers/";
	private static String URL = Constants.API_HOST + END_POINT;
	private Handler mHandler;
	private Long mUserId;
	
	public GetFollowers(Handler handler, Long userId){
		mHandler = handler;
		mUserId = userId;
	}

	@Override
	public void run() {
		StringBuilder url = new StringBuilder(URL);
		url.append(mUserId);
		try {
			HttpResponse response = Util.sendGet(url.toString());
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[User Followers] response : " + res);
			if (res != null){
				JSONObject json = new JSONObject(res);
				if (!json.getBoolean("error")){
					JSONArray data = json.getJSONArray("data");
					Message msg = mHandler.obtainMessage(Constants.USER_FOLLOWERS_FETCHED, data);
					mHandler.sendMessage(msg);
				} else {
					Message msg = mHandler.obtainMessage(Constants.USER_FOLLOWERS_FAILED);
					mHandler.sendMessage(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = mHandler.obtainMessage(Constants.USER_FOLLOWERS_FAILED);
			mHandler.sendMessage(msg);
		} 
	}

}
