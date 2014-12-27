package com.vrocketz.spotchu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;

public class GcmBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		//TODO : Handle intent in an intent service
		if (!extras.isEmpty()){
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[GCM Received] " + extras.toString());
			
		}else {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[GCM Received] extras empty.");
		}
	}

}
