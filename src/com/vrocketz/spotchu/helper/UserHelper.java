package com.vrocketz.spotchu.helper;

import android.content.Intent;

import com.vrocketz.spotchu.activity.LoginActivity;

public class UserHelper {
	
	public static void clearUser(){
		Util.setPref(Constants.USER_LOGGED_IN, false);
		Util.setPref(Constants.USER_EMAIL, null);
		Util.setPref(Constants.USER_NAME, null);
		logoutFromGoogle();
		startLoginActivity();
	}
	
	private static void logoutFromGoogle(){
		/*if (mGoogleApiClient.isConnected()) {
		      Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		      mGoogleApiClient.disconnect();
		      mGoogleApiClient.connect();
		}*/
	}
	
	private static void startLoginActivity() {
		Intent intent = new Intent(Util.getApp(), LoginActivity.class);
		Util.getApp().startActivity(intent);
	}

}
