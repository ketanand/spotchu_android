package com.vrocketz.spotchu;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null) {
			if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
				context.startService(new Intent(context,
						SpotchuLocationService.class));
				Util.scheduleServiceCheckBroadCast(context);
			} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (intent.getExtras() != null) {
					if (Util.isInternetAvailable()) {
						if (Config.DEBUG)
							Log.d(Constants.APP_NAME, "Internet is back. ");
						Intent i = new Intent(context, SpotService.class);
						i.putExtra(SpotService.PROCESS_PENDING, true);
						context.startService(intent);
					} else if (intent.getBooleanExtra(
							ConnectivityManager.EXTRA_NO_CONNECTIVITY,
							Boolean.FALSE)) {
						if (Config.DEBUG)
							Log.d(Constants.APP_NAME,
									"There's no network connectivity");
					}
				}
			}
		} else {
			// Default Behavior is to start location service.
			context.startService(new Intent(context,
					SpotchuLocationService.class));
		}
	}

}
