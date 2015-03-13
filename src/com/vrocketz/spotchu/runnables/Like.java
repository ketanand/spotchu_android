package com.vrocketz.spotchu.runnables;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class Like implements Runnable{
	
	Long spotId;
	private Handler mHandler;
	private static String END_POINT = "spots/hi5";
	private static String URL = Constants.API_HOST + END_POINT;
	
	public Like (Long id, Handler handler){
		spotId = id;
		mHandler = handler;
	}

	@Override
	public void run() {
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("spot_id", String.valueOf(spotId)));
		postData.add(new BasicNameValuePair("userName", Util.getGlobalPreferences().getString(Constants.USER_NAME, "")));
		try {
			HttpResponse response = Util.sendPost(URL, postData);
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Like] response : " + res);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
