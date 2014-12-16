package com.vrocketz.spotchu.runnables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.location.Location;
import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class LocationBeacon implements Runnable{
	
	Location mLocation;
	Map<String, Object> mAddress;
	private static String END_POINT = "user/location";
	private static String URL = Constants.API_HOST + END_POINT;

	public LocationBeacon(Location loc, Map<String, Object> addr){
		mLocation = loc;
		mAddress = addr;
	}
	@Override
	public void run() {
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("locationLong", String.valueOf(mLocation.getLongitude())));
		postData.add(new BasicNameValuePair("locationLati", String.valueOf(mLocation.getLatitude())));
		if (mAddress != null){
			postData.add(new BasicNameValuePair("city", (String) mAddress.get("city")));
			postData.add(new BasicNameValuePair("locality", (String) mAddress.get("street")));
		}
		
		try {
			HttpResponse response = Util.sendPost(URL, postData);
			String res = Util.convertResponseToString(response);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[LocationBeacon] response : " + res);
			/*if (res != null){
				JSONObject json = new JSONObject(res);
				if (!json.getBoolean("error")){
					Message msg = handler.obtainMessage(Constants.SPOT_POSTED);
					handler.sendMessage(msg);
				} else {
					Message msg = handler.obtainMessage(Constants.SPOT_POST_FAILED);
					handler.sendMessage(msg);
				}
			}	*/
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
