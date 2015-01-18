package com.vrocketz.spotchu;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		String action = i.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
			context.startService(new Intent(context, SpotchuLocationService.class));
		}else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
			if(i.getExtras() != null) {
		        if(Util.isInternetAvailable()) {
		        	if(Config.DEBUG)
		        		Log.d(Constants.APP_NAME, "Internet is back. ");
		        	Intent intent = new Intent(context, SpotService.class);
		        	intent.putExtra(SpotService.PROCESS_PENDING, true);
		        	context.startService(intent);
		        } else if(i.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
		        	if(Config.DEBUG)
		        		Log.d(Constants.APP_NAME, "There's no network connectivity");
		        }
		   }
		}
	}

}
