package com.vrocketz.spotchu;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.runnables.LocationBeacon;

public class SpotchuLocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	
	//private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	LocationClient mLocationClient;
	Location mCurrentLocation;
	LocationRequest mLocationRequest;
	PendingIntent mPendingIntent;
	Map<String, Object> mAddress;
	private final IBinder mBinder = new LocalBinder();
	private static final long INTERVAL = 60 * 60 * 1000; 

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationClient = new LocationClient(this, this, this);
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(INTERVAL);
		mLocationRequest.setFastestInterval(INTERVAL);
		Intent intent = new Intent(this,SpotchuLocationService.class);
		mPendingIntent = PendingIntent.getService(this, 1, intent, 0);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!mLocationClient.isConnected()){
			mLocationClient.connect();
		}
		return START_STICKY;
	}
	
	public class LocalBinder extends Binder {
        public SpotchuLocationService getService() {
                return SpotchuLocationService.this;
        }
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
	    }
		mLocationClient.disconnect();
		super.onDestroy();
	}
	
    	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
        	//TODO : enter code for resolution
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            //showErrorDialog(connectionResult.getErrorCode());
        }
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[LocationService] OnConnected");
		mCurrentLocation = mLocationClient.getLastLocation();
		if (mCurrentLocation != null){
			if (Geocoder.isPresent()){
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[LocationService] OnConnected Geocoder Present, Get Address.");
				new GetAddressTask(this).execute(mCurrentLocation);
			}else {
				new Thread(new LocationBeacon(mCurrentLocation, null)).start();
			}
		}
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[LocationService] OnLocationChanged.");
		mCurrentLocation = location;
		if (Geocoder.isPresent()){
			new GetAddressTask(this).execute(mCurrentLocation);
		}else {
			new Thread(new LocationBeacon(mCurrentLocation, null)).start();
		}
	}

	public Location getStoredLocation(){
		return mCurrentLocation;
	}
	
	public Location getCurrentLocation(){
		mCurrentLocation = mLocationClient.getLastLocation();
		Boolean present = Geocoder.isPresent();
		if (mCurrentLocation != null && present != null && present){
			new GetAddressTask(this).execute(mCurrentLocation);
		}
		return mCurrentLocation;
	}
	
	public Map<String, Object> getLocationData(){
		return mAddress;
	}
	
	private class GetAddressTask extends AsyncTask<Location, Void, Map<String, Object>> {
		Context mContext;
		public GetAddressTask(Context context) {
		    super();
		    mContext = context;
		}
	
		@Override
		protected Map<String, Object> doInBackground(Location... params) {
		    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
		    // Create a list to contain the result address
		    List<Address> addresses = null;
		    try {
		        addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(),
		                mCurrentLocation.getLongitude(), 1);
		    } catch (IOException e1) {
		    	e1.printStackTrace();
		    	//Send Beacon even if address resolution fails.
		    	new Thread(new LocationBeacon(mCurrentLocation, mAddress)).start();
		    	return null;
		    } catch (IllegalArgumentException e2) {
			    e2.printStackTrace();
			    new Thread(new LocationBeacon(mCurrentLocation, mAddress)).start();
			    return null;
		    }
		    // If the reverse geocode returned an address
		    if (addresses != null && addresses.size() > 0) {
		        // Get the first address
		        Address address = addresses.get(0);
		        if (Config.DEBUG)
		        	Log.d(Constants.APP_NAME, "[GeoCoder] Address : " + address.getSubLocality());
		        Map<String, Object> addressMap = new HashMap<String, Object>();
		        addressMap.put("street", address.getSubLocality());
		        addressMap.put("city", address.getLocality());
		        addressMap.put("country", address.getCountryName());
		        addressMap.put("lat", address.getLatitude());
		        addressMap.put("long", address.getLongitude());
		        		
		        return addressMap;
		    } else {
		        return null;
		    }
		}
		
		@Override
        protected void onPostExecute(Map<String, Object> address) {
            mAddress = address;
            //Send a Beacon
            new Thread(new LocationBeacon(mCurrentLocation, mAddress)).start();
        }
	}

}
