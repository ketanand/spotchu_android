package com.vrocketz.spotchu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class Util {
	
	private static Context applicationContext;
	private static SharedPreferences mPref;
	
	public static void setApplicationContext(Context c){
		applicationContext = c;
	}
	
	public static Context getApplicationContext(){
		return applicationContext;
	}
	
	public static void setSharedPref(SharedPreferences pref){
		mPref = pref;
	}
	
	public static SharedPreferences getApplicationPrefrences(){
		return mPref;
	}
	
	public static File getSavePath(int type){
		// To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		/*File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), Constants.APP_NAME);*/
	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator +  Constants.APP_NAME);
	    if (Config.DEBUG)
	    	Log.d(Constants.APP_NAME, mediaStorageDir.getAbsolutePath());
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d(Constants.APP_NAME, "failed to create directory");
	            return null;
	        }
	    }
		if (type == Constants.IMAGE_TYPE_POST){
		    // Create a media file name
		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		    File mediaFile;
		    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
		        "IMG_"+ timeStamp + ".jpg");
		    if (Config.DEBUG)
		    	Log.d(Constants.APP_NAME, mediaFile.getAbsolutePath());
		    return mediaFile;
		}else if (type == Constants.IMAGE_TYPE_PROFILE){
			File profilePicture = new File(mediaStorageDir.getPath() + File.separator + "IMG_PROFILE");
			return profilePicture;
		}
		return null;
	}
	
	public static HttpResponse sendPost(String url, ArrayList<NameValuePair> postData) throws ClientProtocolException, IOException{
		HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
		httppost.setEntity(new UrlEncodedFormEntity(postData));
		HttpResponse response = httpclient.execute(httppost);
		return response;
	}
	
	public static String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException{
	   if (Config.DEBUG)
		   Log.d(Constants.APP_NAME, "Convert Response Started");
       String res = "";
       StringBuffer buffer = new StringBuffer();
       
       InputStream inputStream = response.getEntity().getContent();
       final int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
       int responseCode = response.getStatusLine().getStatusCode();
       if (responseCode != 200){
    	   if (Config.DEBUG)
    		   Log.d(Constants.APP_NAME, "Convert Response Started: Response Code:" + responseCode);
    	   return null;
       }
    
       //TODO : add content length in API
       if (false /*contentLength < 0*/){
       }
       else{
              byte[] data = new byte[512];
              int len = 0;
              try
              {
                  while (-1 != (len = inputStream.read(data)) )
                  {
                      buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..
                  }
              }
              catch (IOException e)
              {
                  e.printStackTrace();
              }
              try
              {
                  inputStream.close(); // closing the stream…..
              }
              catch (IOException e)
              {
                  e.printStackTrace();
              }
              res = buffer.toString();     // converting stringbuffer to string…..

       }
       return res;
  }
	
	public static boolean isInternetAvailable(){
		ConnectivityManager cm =
		        (ConnectivityManager)applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnected();
		return isConnected;
	}
	
	public static void setPref(String key, String val){
		Editor e = mPref.edit();
		e.putString(key, val);
		e.commit();
	}
	
	public static void setPrefInt(String key, int val){
		Editor e = mPref.edit();
		e.putInt(key, val);
		e.commit();
	}
	
	public static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	public static String getRegistrationId(){
		String regId = null;
		regId =  getApplicationPrefrences().getString(Constants.REGISTRATION_ID, null);
		if (regId == null){
			return regId;
		}
		int registeredVersion = getApplicationPrefrences().getInt(Constants.APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(getApplicationContext());
	    if (registeredVersion != currentVersion) {
	        Log.i(Constants.APP_NAME, "App version changed.");
	        return null;
	    }
		return regId;
	}
	
}
