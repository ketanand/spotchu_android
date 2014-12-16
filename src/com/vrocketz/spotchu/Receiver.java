package com.vrocketz.spotchu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		context.startService(new Intent(context, SpotchuLocationService.class));;
	}

}
