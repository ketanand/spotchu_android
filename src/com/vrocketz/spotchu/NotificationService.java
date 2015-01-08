package com.vrocketz.spotchu;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.vrocketz.spotchu.activity.MainActivity;
import com.vrocketz.spotchu.activity.Summary;
import com.vrocketz.spotchu.activity.ViewSpot;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class NotificationService extends IntentService{
	
	private static final int NOTIFICATION_SUMMARY_ID = 998;
	private static final String MSG_TYPE = "type";
	private static final String MSG = "msg";
	private static final String SPOT_TITLE = "name";
	private int notify_no = 0;

	public NotificationService() {
        super(NotificationService.class.getName());
       
    }
	
	public NotificationService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		 Bundle extras = intent.getExtras();
	     GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
	     String messageType = gcm.getMessageType(intent);
		 if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
	            /*
	             * Filter messages based on message type. Since it is likely that GCM
	             * will be extended in the future with new message types, just ignore
	             * any message types you're not interested in, or that you don't
	             * recognize.
	             */
	            if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
	                //For now ignore
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_DELETED.equals(messageType)) {
	            	//For now ignore
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	            	if (Config.DEBUG)
	                	Log.d(Constants.APP_NAME, "GCM Received: " + extras.toString());
	                // Post notification of received message.
	                sendNotification(extras.getString("message"));
	            }
	        }
	        // Release the wake lock provided by the WakefulBroadcastReceiver.
	        GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String message){
		// create the notification
        JSONObject msg;
		try {
			msg = new JSONObject(message);
			sendNotificationFromMessage(msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void sendNotificationFromMessage(JSONObject gcm) throws JSONException{
		NotificationManager mNM = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		GCMMessageType type = GCMMessageType.getFromValue(gcm.getInt(MSG_TYPE));
		if (type == GCMMessageType.SUMMARY){
			Intent intent = new Intent(Util.getApp(), Summary.class);
			intent.putExtra("url", gcm.getString(MSG));
	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		    stackBuilder.addParentStack(MainActivity.class);
		    stackBuilder.addNextIntent(intent);
		    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
							                 PendingIntent.FLAG_UPDATE_CURRENT
							             );
		    String text = getResources().getString(R.string.your_day_today);
		    NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setSmallIcon(R.drawable.ic_launcher);
		    m_notificationBuilder.setContentIntent(pendingIntent);
		    mNM.notify(NOTIFICATION_SUMMARY_ID, m_notificationBuilder.build());
		}else if (type == GCMMessageType.NEW_SPOT){
			Intent intent = new Intent(Util.getApp(), ViewSpot.class);
	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		    stackBuilder.addParentStack(MainActivity.class);
		    stackBuilder.addNextIntent(intent);
		    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
							                 PendingIntent.FLAG_UPDATE_CURRENT
							             );
		    StringBuilder textBuilder = new StringBuilder();
		    textBuilder.append(getResources().getString(R.string.new_spot));
		    textBuilder.append(gcm.getJSONObject(MSG).getString(SPOT_TITLE)).append(".");
		    String text = textBuilder.toString();
		    NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setSmallIcon(R.drawable.ic_launcher);
		    m_notificationBuilder.setContentIntent(pendingIntent);
		    if (notify_no < 9) {
	            notify_no = notify_no + 1;
	        } else {
	            notify_no = 0;
	        }
		    mNM.notify(notify_no, m_notificationBuilder.build());
		}
	}
}
