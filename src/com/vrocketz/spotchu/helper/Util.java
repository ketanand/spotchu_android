package com.vrocketz.spotchu.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;

import com.vrocketz.spotchu.SpotchuApp;

public class Util {
	
	private static Context applicationContext;
	private static SpotchuApp mApp;
	
	public static void setApp(SpotchuApp app){
		mApp = app;
	}
	
	public static Application getApp(){
		return mApp;
	}
	
	/*public static void setApplicationContext(Context c){
		applicationContext = c;
	}
	
	public static Context getApplicationContext(){
		return applicationContext;
	}*/
	
	public static SharedPreferences getGlobalPreferences(){
		return mApp.getGlobalPreferences();
	}
	
	public static File getSavePath(int type){
		// To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		/*File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), Constants.APP_NAME);*/
	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator +  Constants.APP_NAME);
	    if (Config.DEBUG)
	    	Log.d(Constants.APP_NAME, "[Util getSavePath] media storage dir:  " + mediaStorageDir.getAbsolutePath());
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
		    	Log.d(Constants.APP_NAME, "[Util getSavePath] mediaFile for POST : " + mediaFile.getAbsolutePath());
		    return mediaFile;
		}else if (type == Constants.IMAGE_TYPE_PROFILE){
			File profilePicture = new File(mediaStorageDir.getPath() + File.separator + "IMG_PROFILE");
			if (Config.DEBUG)
		    	Log.d(Constants.APP_NAME, "[Util getSavePath] mediaFile Profile : " + profilePicture.getAbsolutePath());
			return profilePicture;
		}else if (type == Constants.IMAGE_CACHE){
			File cacheDir = new File(mediaStorageDir.getPath() + File.separator);
			if (Config.DEBUG)
		    	Log.d(Constants.APP_NAME, "[Util getSavePath] Cache Dir : " + cacheDir.getAbsolutePath());
			return cacheDir;
		}
		if (Config.DEBUG)
	    	Log.d(Constants.APP_NAME, "[Util getSavePath] Invalid Type.");
		return null;
	}
	
	public static HttpResponse sendPost(String url, ArrayList<NameValuePair> postData) throws ClientProtocolException, IOException{
		if (Config.DEBUG)
        	Log.d(Constants.APP_NAME, "[HTTP POST], url=" + url);
		HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        String regId = Util.getRegistrationId();
        if (regId != null){
        	httppost.addHeader("Authorization", regId);
        }
		httppost.setEntity(new UrlEncodedFormEntity(postData));
		HttpResponse response = httpclient.execute(httppost);
		return response;
	}
	
	public static HttpResponse sendGet(String url) throws ClientProtocolException, IOException{
		if (Config.DEBUG)
        	Log.d(Constants.APP_NAME, "[HTTP GET], url=" + url);
		HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        String regId = Util.getRegistrationId();
        if (regId != null){
        	httpget.addHeader("Authorization", regId);
        }
		HttpResponse response = httpclient.execute(httpget);
		return response;
	}
	
	public static HttpResponse sendDelete(String url) throws ClientProtocolException, IOException{
		if (Config.DEBUG)
        	Log.d(Constants.APP_NAME, "[HTTP DELETE], url=" + url);
		HttpClient httpclient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(url);
        String regId = Util.getRegistrationId();
        if (regId != null){
        	httpDelete.addHeader("Authorization", regId);
        }
		HttpResponse response = httpclient.execute(httpDelete);
		return response;
	}
	public static long getTimeInMilliseconds(){
		Calendar c = Calendar.getInstance();
		return c.getTimeInMillis();
	}
	
	public static String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException{
	   if (Config.DEBUG)
		   Log.d(Constants.APP_NAME, "Convert Response Started");
       String res = "";
       StringBuffer buffer = new StringBuffer();
       
       InputStream inputStream = response.getEntity().getContent();
       final int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
       int responseCode = response.getStatusLine().getStatusCode();
       //TODO resolve response code issue.
       /*if (responseCode != 200){
    	   if (Config.DEBUG)
    		   Log.d(Constants.APP_NAME, "Convert Response Started: Response Code:" + responseCode);
    	   return null;
       }*/
    
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
		        (ConnectivityManager)getApp().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnected();
		return isConnected;
	}
	
	public static void setPref(String key, String val){
		Editor e = getGlobalPreferences().edit();
		e.putString(key, val);
		e.commit();
	}
	
	public static void setPref(String key, int val){
		Editor e = getGlobalPreferences().edit();
		e.putInt(key, val);
		e.commit();
	}
	
	public static void setPref(String key, boolean val){
		Editor e = getGlobalPreferences().edit();
		e.putBoolean(key, val);
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
		regId =  getGlobalPreferences().getString(Constants.REGISTRATION_ID, null);
		return regId;
	}
	
	public static boolean wasAppUpgraded(){
		int registeredVersion = getGlobalPreferences().getInt(Constants.APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(getApp());
	    if (registeredVersion != currentVersion) {
	        Log.i(Constants.APP_NAME, "App version changed.");
	        return true;
	    }
	    return false;
	}
	
	public static int getOsApiVersion(){
		return android.os.Build.VERSION.SDK_INT;
	}
	
	public static String getPrintableTimeFormat(Long seconds){
		return DateFormat.format("dd-MMM-yy hh:mm", seconds * 1000).toString();
	}
	
	public static void saveBytesToFile(byte[] bytes , String path) throws IOException{
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Utils]saveBytesToFile length : " + bytes.length);
		File file = new File(path);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();
	}
	
	public static String getTagsFromTitle(String title){
		StringBuilder tags = new StringBuilder();
		boolean found = false;
		for (int i = 0; i < title.length(); i++){
			char c = title.charAt(i);
			if (c == '#'){
				found = true;
				if (tags.length() > 0){
					tags.append(" ");
				}
			}else if (c == ' '){
				found = false;
			}else if (found){
				tags.append(c);
			}
			
		}
		
		if (tags.length() > 0){
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Utils]getTagsFromTitle Tags : " + tags.toString());
			return tags.toString();
		}else {
			return "";
		}
	}
	
	public static Spanned boldHashTags(String s){
		StringBuilder output = new StringBuilder();
		String[] words = s.split(" ");
		for (String word : words){
			char c = word.charAt(0);
			String sourceString;
			if (c == '#'){
				sourceString = " <b>" + word + "</b> "; 
			}else {
				sourceString = word;
			}
			output.append(sourceString);
		}
		return Html.fromHtml(output.toString().trim());
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Util Class Destroyed");
		super.finalize();
	}
	
}
