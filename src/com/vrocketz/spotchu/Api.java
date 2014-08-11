package com.vrocketz.spotchu;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Api {
	
	public static String registerUser(User user){
		String url = "http://spotapi.vrocketz.com/v1/login";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("name",user.getName()));
        nameValuePairs.add(new BasicNameValuePair("email",user.getEmail()));
        nameValuePairs.add(new BasicNameValuePair("image_url",user.getImageUrl()));
        nameValuePairs.add(new BasicNameValuePair("profile_url",user.getProfileUrl()));
        try {
			String res = Util.convertResponseToString((Util.sendPost(url, nameValuePairs)));
			if (res != null){
				JSONObject json = new JSONObject(res);
				if (!json.getBoolean("error")){
					Util.setPref(Constants.USER_KEY, json.getString("key"));
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
	
	public static void sendRegistrationIdToBackend(String regId){
		//TODO : Add Registration URL.
		String url = "";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("registration_id",regId));
        try {
			Util.sendPost(url, nameValuePairs);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
