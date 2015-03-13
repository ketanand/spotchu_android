package com.vrocketz.spotchu;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.vrocketz.spotchu.activity.LoginActivity;
import com.vrocketz.spotchu.activity.ProfileActivity;
import com.vrocketz.spotchu.activity.Summary;
import com.vrocketz.spotchu.activity.ViewSpot;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class NotificationService extends IntentService{
	
	private static final int NOTIFICATION_SUMMARY_ID = 998;
	private static final int NOTIFICATION_ANNOUNCEMENT_ID = 997;
	private static final int NOTIFICATION_UPGRADE_ID = 997;
	public static final String GCM_MSG_TYPE = "type";
	public static final String GCM_MSG = "msg";
	public static final String GCM_TEXT = "text";
	public static final String SPOT_TITLE = "name";
	public static final String SPOT_ID = "spotId";
	public static final String SUMMARY_URL = "url";
	private static int new_spot_notify_no = 0;
	private static int new_comment_notify_no = 0;
	private static int new_follow_notify_no = 0;

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
		if (Config.DEBUG)
        	Log.d(Constants.APP_NAME, "GCM Send Notification: " + message);
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
		GCMMessageType type = GCMMessageType.getFromValue(gcm.getInt(GCM_MSG_TYPE));
		if (Config.DEBUG)
        	Log.d(Constants.APP_NAME, "GCM Send Notification Type: " + type.getValue());
		if (type == GCMMessageType.SUMMARY){
			Intent intent = new Intent(Util.getApp(), Summary.class);
			intent.putExtra("url", gcm.getJSONObject(GCM_MSG).getString(SUMMARY_URL));
	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		    stackBuilder.addParentStack(Summary.class);
		    stackBuilder.addNextIntent(intent);
		    PendingIntent pendingIntent = stackBuilder.getPendingIntent(NOTIFICATION_SUMMARY_ID,
							                 PendingIntent.FLAG_UPDATE_CURRENT
							             );
		    String text = gcm.getJSONObject(GCM_MSG).getString(GCM_TEXT);
		    NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setDefaults(Notification.DEFAULT_ALL)
	        .setAutoCancel(true)
	        .setSmallIcon(R.drawable.ic_launcher);
		    m_notificationBuilder.setContentIntent(pendingIntent);
		    mNM.notify(NOTIFICATION_SUMMARY_ID, m_notificationBuilder.build());
		}else if (type == GCMMessageType.NEW_SPOT 
				|| type == GCMMessageType.NEW_COMMENT 
				|| type == GCMMessageType.NEW_HI5){
			int spotId = gcm.getJSONObject(GCM_MSG).getInt(SPOT_ID);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Notification] type:" + type.toString() + ", id: " +  spotId);
			Intent intent = new Intent(Util.getApp(), ViewSpot.class);
			intent.putExtra(SPOT_ID, spotId);
	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		    stackBuilder.addParentStack(ViewSpot.class);
		    stackBuilder.addNextIntent(intent);
		    if (new_comment_notify_no < 20) {
		    	new_comment_notify_no = new_comment_notify_no + 1;
	        } else {
	        	new_comment_notify_no = 0;
	        }
		    PendingIntent pendingIntent = stackBuilder.getPendingIntent(new_comment_notify_no,
							                 PendingIntent.FLAG_UPDATE_CURRENT
							             );
		    String text = gcm.getJSONObject(GCM_MSG).getString(GCM_TEXT);
		    NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setDefaults(Notification.DEFAULT_ALL)
	        .setAutoCancel(true)
	        .setSmallIcon(R.drawable.ic_launcher);
		    
		    m_notificationBuilder.setContentIntent(pendingIntent);
		    mNM.notify(new_comment_notify_no, m_notificationBuilder.build());
		}else if (type == GCMMessageType.ANNOUNCEMENT){
			Intent intent = new Intent(Util.getApp(), LoginActivity.class);
		    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		    String text = gcm.getJSONObject(GCM_MSG).getString(GCM_TEXT);
		    NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setDefaults(Notification.DEFAULT_ALL)
	        .setAutoCancel(true)
	        .setSmallIcon(R.drawable.ic_launcher);
		    m_notificationBuilder.setContentIntent(pendingIntent);
		    mNM.notify(NOTIFICATION_ANNOUNCEMENT_ID, m_notificationBuilder.build());
		}else if (type == GCMMessageType.UPGRADE){
			/*
			 * Show Dialog for upgrade.
			 */
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.vrocketz.spotchu"));
			//intent.putExtra(Constants.UPGRADE_AVAILABLE, true);
		    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		    String text = gcm.getJSONObject(GCM_MSG).getString(GCM_TEXT);
		    NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setDefaults(Notification.DEFAULT_ALL)
	        .setAutoCancel(true)
	        .setSmallIcon(R.drawable.ic_launcher);
		    m_notificationBuilder.setContentIntent(pendingIntent);
		    mNM.notify(NOTIFICATION_UPGRADE_ID, m_notificationBuilder.build());
		}else if (type == GCMMessageType.FOLLOW){
			int userId = gcm.getJSONObject(GCM_MSG).getInt(Constants.USER_ID);
			Intent intent = new Intent(Util.getApp(), ProfileActivity.class);
			intent.putExtra(Constants.USER_ID, userId);
			intent.putExtra(Constants.USER_NAME, gcm.getJSONObject(GCM_MSG).getString(Constants.USER_NAME));
			intent.putExtra(Constants.USER_IMG_URL, gcm.getJSONObject(GCM_MSG).getString(Constants.USER_IMG_URL));
	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		    stackBuilder.addParentStack(ProfileActivity.class);
		    stackBuilder.addNextIntent(intent);
		    if (new_follow_notify_no < 10) {
		    	new_follow_notify_no = new_follow_notify_no + 1;
	        } else {
	        	new_follow_notify_no = 0;
	        }
		    PendingIntent pendingIntent = stackBuilder.getPendingIntent(new_follow_notify_no,
							                 PendingIntent.FLAG_UPDATE_CURRENT
							             );
		    String text = gcm.getJSONObject(GCM_MSG).getString(GCM_TEXT);
		    NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setDefaults(Notification.DEFAULT_ALL)
	        .setAutoCancel(true)
	        .setSmallIcon(R.drawable.ic_launcher);
		    
		    m_notificationBuilder.setContentIntent(pendingIntent);
		    mNM.notify(new_follow_notify_no, m_notificationBuilder.build());
		}
	}
}
