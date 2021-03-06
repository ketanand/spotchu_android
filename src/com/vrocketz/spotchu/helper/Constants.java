package com.vrocketz.spotchu.helper;

public class Constants {
	
	public static final String SENDER_ID = "177608471580"; //project number in google developer console.
	public static final String PREF_FILE_NAME = "spotchu_preferences";
	public static final String USER_LOGGED_IN = "user_logged_in";
	public static final String USER_EMAIL = "user_email";
	public static final String USER_NAME = "user_name";
	public static final String USER_ID = "user_id";
	public static final String USER_IMG_URL = "user_img";
	public static final String GPLUS_PROFILE_URL = "user_gplus_profile_url";
	public static final String FB_PROFILE_URL = "user_fb_profile_url";
	public static final String USER_TYPE = "user_type";
	public static final String APP_NAME = "Spotchu";
	public static final String USER_KEY = "user_key";
	public static final String REGISTRATION_ID = "gcm_registration_id";
	public static final String APP_VERSION = "app_version";
	public static final String UPGRADE_AVAILABLE = "upgrade_available";
	
	public static final String API_HOST = "http://peesake.spotchu.com/v1/";
	
	public static final int IMAGE_TYPE_POST = 1;
	public static final int IMAGE_TYPE_PROFILE = 2;
	public static final int IMAGE_CACHE = 3;
	
	
	//Handler Messages
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
	public static final int USER_FOLLOWED = 15;
	public static final int USER_FOLLOW_FAILED = 16;
	public static final int USER_META_FETCHED = 17;
	public static final int USER_META_FAILED = 18;
	public static final int USERS_FETCHED = 19;
	public static final int USERS_FETCH_FAILED = 20;
	public static final int USER_FOLLOWERS_FETCHED = 21;
	public static final int USER_FOLLOWERS_FAILED = 22;
	public static final int COMMENT_DELETED = 23;
	public static final int COMMENT_DELETE_FAILED = 24;
	public static final int SPOT_HI5_LIST_FETCHED = 25;
	public static final int SPOT_HI5_LIST_FAILED = 26;
	
	//Intent data keys
	public static final String SPOT_ID = "spot_id";
	public static final String SPOT_IMAGES = "spot_images";
	public static final String SPOTS = "spots";
	
	public static final String SPOT_IMAGE_URI_KEY = "spot_image_uri_key";
	
	public static final int MAX_TITLE_SIZE = 50;
}
