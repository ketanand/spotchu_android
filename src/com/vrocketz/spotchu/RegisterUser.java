package com.vrocketz.spotchu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.vrocketz.spotchu.activity.MainActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class RegisterUser extends AsyncTask<User, Void, String> {

	int appVer;
	Activity activity;
	ProgressDialog mDialog;
	
	public RegisterUser(int ver, Activity c, ProgressDialog dialog){
		appVer = ver;
		activity = c;
		mDialog = dialog;
	}
	
	@Override
	protected void onPreExecute() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Registered user: Pre Execute");
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(User... params) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Registered user: Do In BackGround");
		return Api.registerUser(params[0], appVer);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Registered user:" + result);
		if (mDialog != null && mDialog.isShowing())
			mDialog.dismiss();
		if (result == null) {
			Toast.makeText(activity,
					R.string.registration_failed, Toast.LENGTH_LONG).show();
		} else {
			Util.setPref(Constants.USER_LOGGED_IN, true);
			startMainActivity();
			activity.finish();
		}
	}
	
	private void startMainActivity() {
		Intent intent = new Intent(activity, MainActivity.class);
		activity.startActivity(intent);
	}

}
