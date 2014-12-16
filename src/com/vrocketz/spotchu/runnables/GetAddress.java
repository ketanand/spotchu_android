package com.vrocketz.spotchu.runnables;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.vrocketz.spotchu.helper.Constants;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;

public class GetAddress implements Runnable {
	
	private Context mContext;
	private Handler mHandler;
	private Location mCurrentLocation;
	
	public GetAddress(Context c, Handler handler, Location location){
		mContext = c;
		mHandler = handler;
		mCurrentLocation = location;
	}

	@Override
	public void run() {
		Boolean present = Geocoder.isPresent();
		if (present != null && present ){
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
		    // Create a list to contain the result address
		    List<Address> addresses = null;
		    try {
		        /*
		         * Return 1 address.
		         */
		        addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(),
		                mCurrentLocation.getLongitude(), 1);
		    } catch (IOException e1) {
		    	//Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
		    	e1.printStackTrace();
		    } catch (IllegalArgumentException e2) {
			    //Log.e("LocationSampleActivity", errorString);
			    e2.printStackTrace();
		    }
		    // If the reverse geocode returned an address
		    if (addresses != null && addresses.size() > 0) {
		        // Get the first address
		        Address address = addresses.get(0);
		        Map<String, Object> addressMap = new HashMap<String, Object>();
		        addressMap.put("Street", address.getMaxAddressLineIndex() > 0 ?
	                    address.getAddressLine(0) : "");
		        addressMap.put("city", address.getLocality());
		        addressMap.put("country", address.getCountryName());
		        addressMap.put("lat", address.getLatitude());
		        addressMap.put("long", address.getLongitude());
		        Message msg = mHandler.obtainMessage(Constants.ADDRESS_RESOLVED, addressMap);	
		        mHandler.sendMessage(msg);
		    } 
		}
	}

}
