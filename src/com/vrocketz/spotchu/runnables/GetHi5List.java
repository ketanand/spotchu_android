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

public class GetHi5List implements Runnable {
	private static String END_POINT = "spots/hi5/list/";
	private static String URL = Constants.API_HOST + END_POINT;
	private Handler mHandler;
	private Long mSpotId;

	public GetHi5List(Handler handler, Long spotId) {
		mHandler = handler;
		mSpotId = spotId;
	}

	@Override
	public void run() {
		StringBuilder url = new StringBuilder(URL);
		url.append(mSpotId);
		try {
			HttpResponse response = Util.sendGet(url.toString());
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Hi5 List] response : " + res);
			if (res != null) {
				JSONObject json = new JSONObject(res);
				if (!json.getBoolean("error")) {
					JSONArray data = json.getJSONArray("data");
					Message msg = mHandler.obtainMessage(
							Constants.SPOT_HI5_LIST_FETCHED, data);
					mHandler.sendMessage(msg);
				} else {
					Message msg = mHandler
							.obtainMessage(Constants.SPOT_HI5_LIST_FAILED);
					mHandler.sendMessage(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = mHandler
					.obtainMessage(Constants.SPOT_HI5_LIST_FAILED);
			mHandler.sendMessage(msg);
		}
	}
}
