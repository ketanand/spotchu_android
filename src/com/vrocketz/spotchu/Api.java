package com.vrocketz.spotchu;

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

import android.util.Log;

public class Api {
	
	public static String registerUser(User user, int appVer){
		String url = Constants.API_HOST + "login";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("name",user.getName()));
        nameValuePairs.add(new BasicNameValuePair("email",user.getEmail()));
        nameValuePairs.add(new BasicNameValuePair("image_url",user.getImageUrl()));
        nameValuePairs.add(new BasicNameValuePair("profile_url",user.getProfileUrl()));
        nameValuePairs.add(new BasicNameValuePair("profile_type", user.getType().toString()));
        nameValuePairs.add(new BasicNameValuePair("regId", Util.getRegistrationId()));
        nameValuePairs.add(new BasicNameValuePair("app_version", String.valueOf(appVer)));
        try {
			String res = Util.convertResponseToString((Util.sendPost(url, nameValuePairs)));
			if (res != null){
				JSONObject json = new JSONObject(res);
				if (!json.getBoolean("error")){
					//Util.setPref(Constants.USER_KEY, json.getString("key"));
					return user.getEmail();
				}else {
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME, json.getString("message"));
				}
			}
		} catch (IllegalStateException e) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public static void sendRegistrationIdToBackend(String regId,int appVersion, String oldRegId){
		//TODO : Add Registration URL.
		String url = Constants.API_HOST + "register";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("registration_id", regId));
        nameValuePairs.add(new BasicNameValuePair("app_version", String.valueOf(appVersion)));
        if (oldRegId != null)
        	nameValuePairs.add(new BasicNameValuePair("old_registration_id", oldRegId));
        try {
			HttpResponse response = Util.sendPost(url, nameValuePairs);
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
            	Log.d(Constants.APP_NAME, "[SendRegistrationIdToBackend], response=" + res);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean logoutUser(){
		String url = Constants.API_HOST + "logout";
		boolean ret = true;
		try {
			Util.sendGet(url);
		} catch (ClientProtocolException e) {
			ret = false;
			e.printStackTrace();
		} catch (IOException e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}

}
