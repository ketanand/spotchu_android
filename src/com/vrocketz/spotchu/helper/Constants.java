package com.vrocketz.spotchu.helper;

public class Constants {
	
	public static final String SENDER_ID = "177608471580"; //project number in google developer console.
	public static final String PREF_FILE_NAME = "spotchu_preferences";
	public static final String USER_LOGGED_IN = "user_logged_in";
	public static final String USER_EMAIL = "user_email";
	public static final String USER_NAME = "user_name";
	public static final String GPLUS_PROFILE_URL = "user_gplus_profile_url";
	public static final String APP_NAME = "Spotchu";
	public static final String USER_KEY = "user_key";
	public static final String REGISTRATION_ID = "gcm_registration_id";
	public static final String APP_VERSION = "app_version";
	public static final String UPGRADE_AVAILABLE = "upgrade_available";
	
	public static final String API_HOST = "http://peesake.spotchu.com/v1/";
	
	public static final int IMAGE_TYPE_POST = 1;
	public static final int IMAGE_TYPE_PROFILE = 2;
	public static final int IMAGE_CACHE = 3;
	
	public static final int ADDRESS_RESOLVED = 1;
	public static final int SPOT_POSTED = 2;
	public static final int SPOT_POST_FAILED = 3;
	public static final int SPOTS_FETCHED = 4;
	public static final int SPOTS_FETCH_FAILED = 5;
	public static final int COMMENTS_FETCHED = 6;
	public static final int COMMENTS_FETCH_FAILED = 7;
	public static final int COMMENT_POSTED = 8;
	public static final int NO_INTERNET = 9;
	public static final int COMMENT_POST_FAILED = 10;
	public static final int SPOT_DELETED = 11;
	public static final int SPOT_DELETE_FAILED = 12;
	public static final int LOGOUT_SUCCESSGFUL = 13;
	public static final int LOGOUT_FAILED = 14;
	
	//Intent data keys
	public static final String SPOT_ID = "spot_id";
	public static final String SPOT_IMAGES = "spot_images";
	public static final String SPOTS = "spots";
	
	public static final String SPOT_IMAGE_URI_KEY = "spot_image_uri_key";
	
	public static final int MAX_TITLE_SIZE = 50;
}
