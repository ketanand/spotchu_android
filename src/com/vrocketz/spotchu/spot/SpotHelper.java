package com.vrocketz.spotchu.spot;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;


public class SpotHelper {
	
	public static final String SPOT_ID = "id";
	public static final String SPOT_USER_ID = "user_id";
	public static final String SPOT_TAG = "tag";
	public static final String SPOT_SCALE = "scale";
	public static final String SPOT_LAT = "location_lati";
	public static final String SPOT_LONG = "location_long";
	public static final String SPOT_CITY = "city";
	public static final String SPOT_LOCALITY = "locality";
	public static final String SPOT_IMG = "img";
	public static final String SPOT_URL = "url";
	public static final String SPOT_DESC = "desc";
	public static final String SPOT_CREATED_AT = "created_at";
	public static final String SPOT_NO_OF_COMMENTS = "no_of_comments";
	public static final String SPOT_NO_OF_LIKES = "no_of_likes";
	public static final String SPOT_MODIFIED_AT = "modified_at";
	public static final String SPOT_PROFILE_URL = "profile_url";
	public static final String SPOT_PROFILE_TYPE = "profile_type";
	public static final String SPOT_USERNAME = "name";
	public static final String SPOT_USERPIC = "image_url";
	public static final String SPOT_HI5_ID = "selfHi5Id";
	
	private final static int IMG_WIDTH = 1200;
	private final static int IMG_HEIGHT = 800;
	
	
	public static Spot getFromJson(JSONObject spotJson) throws JSONException{
		Spot spot = new Spot();
		spot.setId(spotJson.getInt(SPOT_ID));
		spot.setUserId(spotJson.getInt(SPOT_USER_ID));
		if (!spotJson.isNull(SPOT_TAG)){
			spot.setTag(spotJson.getString(SPOT_TAG));
		}
		if (!spotJson.isNull(SPOT_SCALE)){
			spot.setScale(spotJson.getInt(SPOT_SCALE));
		}
		if (!spotJson.isNull(SPOT_LAT)){
			spot.setLocationLati(spotJson.getString(SPOT_LAT));
		}
		if (!spotJson.isNull(SPOT_LONG)){
			spot.setLocationLong(spotJson.getString(SPOT_LONG));
		}
		if (!spotJson.isNull(SPOT_CITY)){
			spot.setCity(spotJson.getString(SPOT_CITY));
		}
		if (!spotJson.isNull(SPOT_LOCALITY)){
			spot.setLocality(spotJson.getString(SPOT_LOCALITY));
		}
		spot.setImg(spotJson.getString(SPOT_IMG));
		spot.setUrl(spotJson.getString(SPOT_URL));
		spot.setDesc(spotJson.getString(SPOT_DESC));
		spot.setCreatedAt(spotJson.getLong(SPOT_CREATED_AT));
		spot.setModifiedAt(spotJson.getLong(SPOT_MODIFIED_AT));
		spot.setName(spotJson.getString(SPOT_USERNAME));
		spot.setNoOfComments(spotJson.getInt(SPOT_NO_OF_COMMENTS));
		spot.setNoOfLikes(spotJson.getInt(SPOT_NO_OF_LIKES));
		if (!spotJson.isNull(SPOT_PROFILE_URL)){
			spot.setProfileUrl(spotJson.getString(SPOT_PROFILE_URL));
		}
		if (!spotJson.isNull(SPOT_PROFILE_TYPE)){
			spot.setProfileType(spotJson.getString(SPOT_PROFILE_TYPE));
		}
		spot.setImageUrl(spotJson.getString(SPOT_USERPIC));
		if (!spotJson.isNull(SPOT_HI5_ID)){
			spot.setSelfHi5Id(spotJson.getInt(SPOT_HI5_ID));
		}
		return spot;
	}
	
	public static ArrayList<Spot> getFromJsonArray(JSONArray arr) throws JSONException{
		ArrayList<Spot> list = new ArrayList<Spot>();
		for (int i = 0; i < arr.length(); i++){
			list.add(getFromJson(arr.getJSONObject(i)));
		}
		return list;
	}
	
	public static Intent getShareIntent(String url){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		String text = getShareText(url);
		if (text != null){
			sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		}else {
			sendIntent.putExtra(Intent.EXTRA_TEXT, Util.getApp().getResources().getString(R.string.spot_url_not_found_message));
		}
		sendIntent.setType("text/plain");
		return sendIntent;
	}
	
	private static String getShareText(String url){
		if (url == null){
			return null;
		}
		StringBuilder share = new StringBuilder();;
		share.append(Util.getApp().getResources().getString(R.string.spot_url_share_message)).append(" ").append(url);
		return share.toString();
	}
	
	/*
	 * Get Bitmap Image from saved image Uri
	 */
	public static Bitmap getImageBitmap(String path) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "image path for decoding:" + path);
		BitmapFactory.decodeFile(path, options);
		// Calculate inSampleSize, Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		int inSampleSize = 1;

		if (height > IMG_HEIGHT) {
			inSampleSize = Math.round((float) height / (float) IMG_HEIGHT);
		}
		int expectedWidth = width / inSampleSize;

		if (expectedWidth > IMG_WIDTH) {
			// if(Math.round((float)width / (float)reqWidth) > inSampleSize) //
			// If bigger SampSize..
			inSampleSize = Math.round((float) width / (float) IMG_WIDTH);
		}

		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}

}
