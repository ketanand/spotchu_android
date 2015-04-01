package com.vrocketz.spotchu.runnables;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.spot.Spot;

public class UpdateSpot implements Runnable {
	
	private final String SERVER_URL = Constants.API_HOST + "spots/update/";
	private Handler handler;
	private ArrayList<NameValuePair> nameValuePairs;
	//private Spot pendingSpot;
	private Long spotId;

	public UpdateSpot(Long spotId, Handler handler, ArrayList<NameValuePair> data) {
		//this.pendingSpot = pendingSpot;
		this.spotId = spotId;
		this.handler = handler;
		this.nameValuePairs = data;
	}

	@Override
	public void run() {
		try {
			//nameValuePairs.add(new BasicNameValuePair("deviceCreatedAt", String.valueOf(pendingSpot.getCreatedAt())));
			if (Util.isInternetAvailable()) {
				StringBuilder url = new StringBuilder(SERVER_URL);
				url.append(spotId);
				HttpResponse response = Util.sendPost(url.toString(),
						nameValuePairs);
				String res = Util.convertResponseToString(response);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[Update Spot] response : " + res);
				if (res != null) {
					JSONObject json = new JSONObject(res);
					if (!json.getBoolean("error")) {
						Message msg = handler
								.obtainMessage(Constants.SPOT_POSTED, spotId);
						handler.sendMessage(msg);
					} else {
						Message msg = handler
								.obtainMessage(Constants.SPOT_POST_FAILED, spotId);
						handler.sendMessage(msg);
					}
				}
			}else {
				Message msg = handler
						.obtainMessage(Constants.NO_INTERNET, spotId);
				handler.sendMessage(msg);
			}
		} catch (ClientProtocolException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED, spotId);
			handler.sendMessage(msg);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Update Spot] ClientProtocolException : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED, spotId);
			handler.sendMessage(msg);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Update Spot] IOException : " + e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED, spotId);
			handler.sendMessage(msg);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Update Spot] JSONException : " + e.getMessage());
			e.printStackTrace();
		}
	}

}
