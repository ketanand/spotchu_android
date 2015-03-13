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

public class Comment implements Runnable{
	
	Long spotId;
	String text;
	private Handler mHandler;
	private static String END_POINT = "spots/comments";
	private static String URL = Constants.API_HOST + END_POINT;
	
	public Comment(Long id, String text, Handler handler){
		spotId = id;
		this.text = text;
		mHandler = handler;
	}

	@Override
	public void run() {
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("spot_id", String.valueOf(spotId)));
		postData.add(new BasicNameValuePair("comments", text));
		postData.add(new BasicNameValuePair("userName", Util.getGlobalPreferences().getString(Constants.USER_NAME, "")));
		try {
			HttpResponse response = Util.sendPost(URL, postData);
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Comment] response : " + res);
			JSONObject json = new JSONObject(res);
			if (!json.getBoolean("error")){
				Message msg = mHandler.obtainMessage(Constants.COMMENT_POSTED, json);
				mHandler.sendMessage(msg);
			} else {
				Message msg = mHandler.obtainMessage(Constants.COMMENT_POST_FAILED);
				mHandler.sendMessage(msg);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
