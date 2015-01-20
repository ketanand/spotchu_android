package com.vrocketz.spotchu.runnables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;

public class PostSpot implements Runnable {

	private final String SERVER_URL = Constants.API_HOST + "spots";
	private String path;
	private Handler handler;
	private ArrayList<NameValuePair> nameValuePairs;
	private Spot spot;

	public PostSpot(Spot spot, Handler handler, ArrayList<NameValuePair> data) {
		this.spot = spot;
		this.path = spot.getImg();
		this.handler = handler;
		this.nameValuePairs = data;
	}

	@Override
	public void run() {
		Bitmap bitmap = SpotHelper.getImageBitmap(this.path);
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Upload Image Started, Bitmap formed");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
		byte[] byteArr = stream.toByteArray();
		String image_str = Base64.encodeToString(byteArr, Base64.DEFAULT);
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Encoded image: " + image_str);
		nameValuePairs.add(new BasicNameValuePair("imagerawdata", image_str));
		nameValuePairs.add(new BasicNameValuePair("deviceCreatedAt", String.valueOf(spot.getCreatedAt())));

		try {
			if (Util.isInternetAvailable()) {

				HttpResponse response = Util.sendPost(SERVER_URL,
						nameValuePairs);
				String res = Util.convertResponseToString(response);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[Post Spot] response : " + res);
				if (res != null) {
					JSONObject json = new JSONObject(res);
					if (!json.getBoolean("error")) {
						Message msg = handler
								.obtainMessage(Constants.SPOT_POSTED, spot.getId());
						handler.sendMessage(msg);
					} else {
						Message msg = handler
								.obtainMessage(Constants.SPOT_POST_FAILED, spot);
						handler.sendMessage(msg);
					}
				}
			}else {
				Message msg = handler
						.obtainMessage(Constants.NO_INTERNET, spot.getId());
				handler.sendMessage(msg);
			}
		} catch (ClientProtocolException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED, spot);
			handler.sendMessage(msg);
			//TODO: add object in failed case, to be picked later.
			e.printStackTrace();
		} catch (IOException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED, spot);
			handler.sendMessage(msg);
			e.printStackTrace();
		} catch (JSONException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED, spot);
			handler.sendMessage(msg);
			e.printStackTrace();
		}
	}

	

}
