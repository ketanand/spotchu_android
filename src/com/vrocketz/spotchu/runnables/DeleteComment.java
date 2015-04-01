package com.vrocketz.spotchu.runnables;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class DeleteComment implements Runnable{

	private Long mCommentId;
	private Handler mHandler;
	private static String END_POINT = "spots/comments/";
	private static String URL = Constants.API_HOST + END_POINT;
	
	public DeleteComment(Long id, Handler handler){
		mCommentId = id;
		mHandler = handler;
	}
	
	@Override
	public void run() {
		try {
			StringBuilder url = new StringBuilder(URL);
			url.append(mCommentId);
			HttpResponse response = Util.sendDelete(url.toString());
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[DeleteComment] response : " + res);
			JSONObject json = new JSONObject(res);
			if (!json.getBoolean("error")){
				Message msg = mHandler.obtainMessage(Constants.COMMENT_DELETED, json);
				mHandler.sendMessage(msg);
			} else {
				Message msg = mHandler.obtainMessage(Constants.COMMENT_DELETE_FAILED);
				mHandler.sendMessage(msg);
			}
		} catch (ClientProtocolException e) {
			Message msg = mHandler.obtainMessage(Constants.COMMENT_DELETE_FAILED);
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (IOException e) {
			Message msg = mHandler.obtainMessage(Constants.COMMENT_DELETE_FAILED);
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (JSONException e) {
			Message msg = mHandler.obtainMessage(Constants.COMMENT_DELETE_FAILED);
			mHandler.sendMessage(msg);
			e.printStackTrace();
		}
	}

}
