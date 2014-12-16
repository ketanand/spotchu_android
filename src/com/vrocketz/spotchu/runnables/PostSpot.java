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

public class PostSpot implements Runnable {

	private final String SERVER_URL = Constants.API_HOST + "spots";
	private final int IMG_WIDTH = 1200;
	private final int IMG_HEIGHT = 800;
	private String path;
	private Handler handler;
	private ArrayList<NameValuePair> nameValuePairs;
	
	public PostSpot(String path, Handler handler,ArrayList<NameValuePair> data){
		this.path = path;
		this.handler = handler;
		this.nameValuePairs = data;
	}
	
	@Override
	public void run() {
		Bitmap bitmap = getImageBitmap();
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Upload Image Started, Bitmap formed");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
		byte [] byteArr = stream.toByteArray();
        String image_str = Base64.encodeToString(byteArr, Base64.DEFAULT);
        if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Encoded image: " + image_str);
        nameValuePairs.add(new BasicNameValuePair("imagerawdata",image_str));
		
        try {
			HttpResponse response = Util.sendPost(SERVER_URL, nameValuePairs);
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Post Spot] response : " + res);
			if (res != null){
				JSONObject json = new JSONObject(res);
				if (!json.getBoolean("error")){
					Message msg = handler.obtainMessage(Constants.SPOT_POSTED);
					handler.sendMessage(msg);
				} else {
					Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED);
					handler.sendMessage(msg);
				}
			}	
		} catch (ClientProtocolException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED);
			handler.sendMessage(msg);
			e.printStackTrace();
		} catch (IOException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED);
			handler.sendMessage(msg);
			e.printStackTrace();
		} catch (JSONException e) {
			Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED);
			handler.sendMessage(msg);
			e.printStackTrace();
		}
	}
	
	/*
	 * Get Bitmap Image from saved image Uri
	 */
	private Bitmap getImageBitmap(){
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "image path for decoding:" + path);
		BitmapFactory.decodeFile(path, options);
		// Calculate inSampleSize, Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    options.inPreferredConfig = Bitmap.Config.RGB_565;
	    int inSampleSize = 1;
	 
	    if (height > IMG_HEIGHT) 
	    {
	        inSampleSize = Math.round((float)height / (float)IMG_HEIGHT);
	    }
	    int expectedWidth = width / inSampleSize;
	 
	    if (expectedWidth > IMG_WIDTH) 
	    {
	        //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
	        inSampleSize = Math.round((float)width / (float)IMG_WIDTH);
	    }
	 
	    options.inSampleSize = inSampleSize;
	 
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	 
	    return BitmapFactory.decodeFile(path, options);
	}
	
}
