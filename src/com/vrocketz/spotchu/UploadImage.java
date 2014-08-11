package com.vrocketz.spotchu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class UploadImage implements Runnable {

	private final String SERVER_URL = "http://spotapi.vrocketz.com/upload";
	private final int IMG_WIDTH = 1200;
	private final int IMG_HEIGHT = 800;
	private String path;
	private Activity parent;
	
	public UploadImage(String path, Activity context){
		this.path = path;
		this.parent = context;
	}
	
	@Override
	public void run() {
		Bitmap bitmap = getImageBitmap();
		Log.d("spotu", "Upload Image Started, Bitmap formed");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
		byte [] byteArr = stream.toByteArray();
        String image_str = Base64.encodeToString(byteArr, Base64.DEFAULT);
        Log.d("Spotu", "Encoded image: " + image_str);
        ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("imagerawdata",image_str));
		
        try {
			HttpResponse response = Util.sendPost(SERVER_URL, nameValuePairs);
			String res = Util.convertResponseToString(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Get Bitmap Image from saved image Uri
	 */
	private Bitmap getImageBitmap(){
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    Log.d("spotu", "image path for decoding:" + path );
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
