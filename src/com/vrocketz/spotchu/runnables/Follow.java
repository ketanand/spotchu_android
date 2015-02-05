package com.vrocketz.spotchu.runnables;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Follow implements Runnable {
	
	private static String END_POINT = "user/follow";
	private static String URL = Constants.API_HOST + END_POINT;
	
	private Integer mUserId;
	private String mUserName;
	private Handler mHandler;
	
	public Follow(Handler handler, Integer id, String name){
		mHandler = handler;
		mUserId = id;
		mUserName = name;
	}

	@Override
	public void run() {
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("userId", String.valueOf(mUserId)));
		postData.add(new BasicNameValuePair("userName", mUserName));
		try {
			HttpResponse response = Util.sendPost(URL, postData);
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Follow] response : " + res);
			if (res != null){
				JSONObject json = new JSONObject(res);
				if (!json.getBoolean("error")){
					Message msg = mHandler.obtainMessage(Constants.USER_FOLLOWED);
					mHandler.sendMessage(msg);
				} else {
					Message msg = mHandler.obtainMessage(Constants.USER_FOLLOW_FAILED);
					mHandler.sendMessage(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = mHandler.obtainMessage(Constants.USER_FOLLOW_FAILED);
			mHandler.sendMessage(msg);
		} 
	}

}
