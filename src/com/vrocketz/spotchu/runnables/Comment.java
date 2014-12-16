package com.vrocketz.spotchu.runnables;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

import android.os.Handler;
import android.util.Log;

public class Comment implements Runnable{
	
	int spotId;
	String text;
	private Handler mHandler;
	private static String END_POINT = "spots/comments";
	private static String URL = Constants.API_HOST + END_POINT;
	
	public Comment(int id, String text, Handler handler){
		spotId = id;
		this.text = text;
		mHandler = handler;
	}

	@Override
	public void run() {
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("spot_id", String.valueOf(spotId)));
		postData.add(new BasicNameValuePair("comments", text));
		try {
			HttpResponse response = Util.sendPost(URL, postData);
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Comment] response : " + res);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
