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
	private Long mSpotId;
	private Handler mHandler;
	
	public DeleteSpot(Handler handler, Long id){
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
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[DeleteSpots] Internet not available ");
			}else {
				HttpResponse response = Util.sendDelete(url.toString());
				String res = Util.convertResponseToString(response);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[DeleteSpots] response : " + res);
				/*if (res != null){
					JSONObject json = new JSONObject(res);
					if (!json.getBoolean("error")){
						Message msg = mHandler.obtainMessage(Constants.SPOT_DELETED, json);
						mHandler.sendMessage(msg);
					} else {
						Message msg = mHandler.obtainMessage(Constants.SPOT_DELETE_FAILED);
						mHandler.sendMessage(msg);
					}
				}*/
			}
		}catch (IOException e) {
			/*Message msg = mHandler.obtainMessage(Constants.SPOT_DELETE_FAILED);
			mHandler.sendMessage(msg);*/
			e.printStackTrace();
		} 
	}

}
