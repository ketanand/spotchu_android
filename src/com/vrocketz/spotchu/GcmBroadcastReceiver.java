package com.vrocketz.spotchu;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[GCM Received]" );
		// Explicitly specify that IntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                NotificationService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
	}
	
}
