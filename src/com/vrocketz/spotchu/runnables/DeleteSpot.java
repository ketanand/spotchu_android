package com.vrocketz.spotchu.runnables;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.HandlerMessages;
import com.vrocketz.spotchu.helper.Util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DeleteSpot implements Runnable {
	
	private static String END_POINT = "spots/";
	private static String URL = Constants.API_HOST + END_POINT;
	private Integer mSpotId;
	private Handler mHandler;
	
	public DeleteSpot(Handler handler, Integer id){
		mSpotId= id;
		mHandler = handler;
	}

	@Override
	public void run() {
		StringBuilder url = new StringBuilder(URL);
		url.append(mSpotId);
		try {
			if (!Util.isInternetAvailable()){
				Message msg = mHandler.obtainMessage(Constants.NO_INTERNET);
				mHandler.sendMessage(msg);
			}else {
				HttpResponse response = Util.sendDelete(url.toString());
				String res = Util.convertResponseToString(response);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[DeleteComments] response : " + res);
				if (res != null){
					JSONObject json = new JSONObject(res);
					if (!json.getBoolean("error")){
						Message msg = mHandler.obtainMessage(Constants.COMMENT_POSTED, json);
						mHandler.sendMessage(msg);
					} else {
						Message msg = mHandler.obtainMessage(Constants.COMMENT_POST_FAILED);
						mHandler.sendMessage(msg);
					}
				}
			}
		}catch (IOException e) {
			Message msg = mHandler.obtainMessage(Constants.COMMENT_POST_FAILED);
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (JSONException e) {
			Message msg = mHandler.obtainMessage(Constants.COMMENT_POST_FAILED);
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} 
	}

}
