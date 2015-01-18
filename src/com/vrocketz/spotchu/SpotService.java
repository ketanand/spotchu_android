package com.vrocketz.spotchu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.spot.PendingSpotDao;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;

public class SpotService extends IntentService {
	
	public static String PROCESS_PENDING = "process_pending";
	public static String PROCESS_FAILED = "process_failed";
	private final String SERVER_URL = Constants.API_HOST + "spots";
	PendingSpotDao mPendingSpotDao;
	
	public SpotService() {
        super(SpotService.class.getName());
       
    }
	
	public SpotService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getBooleanExtra(PROCESS_PENDING, false)){
			mPendingSpotDao = new PendingSpotDao(this);
			mPendingSpotDao.open();
			List<Spot> spots = mPendingSpotDao.getALLPendingSpots();
			postSpots(spots);
			mPendingSpotDao.close();
		}else if (intent.getBooleanExtra(PROCESS_FAILED, false)){
			mPendingSpotDao = new PendingSpotDao(this);
			mPendingSpotDao.open();
			List<Spot> spots = mPendingSpotDao.getALLFailedSpots();
			postSpots(spots);
			mPendingSpotDao.close();
		}else {
			Log.e(Constants.APP_NAME, "Service Called with incorrent intent");
		}
	}
	
	private void postSpots(List<Spot> spots){
		for (Spot spot : spots){
			postToServer(spot);
		}
	}
	
	private void postToServer(Spot spot){
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("desc", spot.getDesc()));
		nameValuePairs.add(new BasicNameValuePair("tags", spot.getTag()));
		nameValuePairs.add(new BasicNameValuePair("goanonymous", String.valueOf(spot.getIsAnonymous())));
		if (spot.getLocationLati() != null){
			nameValuePairs.add(new BasicNameValuePair("locationLong", spot.getLocationLong()));
			nameValuePairs.add(new BasicNameValuePair("locationLati", spot.getLocationLati()));
		}

		Bitmap bitmap = SpotHelper.getImageBitmap(spot.getImg());
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Upload Image Started, Bitmap formed");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
		byte[] byteArr = stream.toByteArray();
		String image_str = Base64.encodeToString(byteArr, Base64.DEFAULT);
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Encoded image: " + image_str);
		nameValuePairs.add(new BasicNameValuePair("imagerawdata", image_str));

		try {
			if (Util.isInternetAvailable()) {

				HttpResponse response = Util.sendPost(SERVER_URL,
						nameValuePairs);
				String res = Util.convertResponseToString(response);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[Post Spot Service] response : " + res);
				if (res != null) {
					JSONObject json = new JSONObject(res);
					if (!json.getBoolean("error")) {
						//Remove from db.
						mPendingSpotDao.deleteSpotById(spot.getId());
					} else {
						//update status to Failed.
						spot.setStatus(Spot.Status.FAILED);
						mPendingSpotDao.UpdateSpot(spot);
					}
				}
			}else {
				//Again internet gone. try again later.
			}
		} catch (ClientProtocolException e) {
			//update status to Failed.
			spot.setStatus(Spot.Status.FAILED);
			mPendingSpotDao.UpdateSpot(spot);
			e.printStackTrace();
		} catch (IOException e) {
			//update status to Failed.
			spot.setStatus(Spot.Status.FAILED);
			mPendingSpotDao.UpdateSpot(spot);
			e.printStackTrace();
		} catch (JSONException e) {
			//update status to Failed.
			spot.setStatus(Spot.Status.FAILED);
			mPendingSpotDao.UpdateSpot(spot);
			e.printStackTrace();
		}
	
		
	}

}
