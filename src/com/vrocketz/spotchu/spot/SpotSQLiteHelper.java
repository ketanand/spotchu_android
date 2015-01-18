package com.vrocketz.spotchu.spot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SpotSQLiteHelper extends SQLiteOpenHelper {

	//Tables
	public static final String TABLE_SPOTS = "spots";
	public static final String TABLE_PENDING_SPOTS = "pending_spots";
	
	//Columms 
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USERID = "user_id";
	public static final String COLUMN_TAG = "tag";
	public static final String COLUMN_SCALE = "scale";
	public static final String COLUMN_LONG = "location_long";
	public static final String COLUMN_LAT = "location_lati";
	public static final String COLUMN_CITY = "city";
	public static final String COLUMN_LOCALITY = "locality";
	public static final String COLUMN_IMG = "img";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_DESC = "desc";
	public static final String COLUMN_CREATED_AT = "created_at";
	public static final String COLUMN_MODIFIED_AT = "modified_at";
	public static final String COLUMN_NO_OF_LIKES = "no_of_likes";
	public static final String COLUMN_NO_OF_COMMENTS = "no_of_comments";
	public static final String COLUMN_USER_NAME = "user_name";
	public static final String COLUMN_PROFILE_URL = "user_profile_url";
	public static final String COLUMN_PROFILE_TYPE = "user_profile_type";
	public static final String COLUMN_USER_IMG = "user_img";
	public static final String COLUMN_GO_ANONYMOUS = "goanonymous";
	public static final String COLUMN_SPOT_STATUS = "status";
	

	private static final String DATABASE_NAME = "spotchu.db";
	private static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String CREATE_TABLE_SPOT = "create table "
	      + TABLE_SPOTS + "(" 
		  			+ COLUMN_ID + " integer primary key, " 
		  			+ COLUMN_USERID + " integer not null, " 
		  			+ COLUMN_TAG + " text, "
		  			+ COLUMN_SCALE + " integer, "
		  			+ COLUMN_LONG + " text, "
		  			+ COLUMN_LAT + " text, "
		  			+ COLUMN_CITY + " text, "
		  			+ COLUMN_LOCALITY + " text, "
		  			+ COLUMN_IMG + " text, "
		  			+ COLUMN_URL + " text, "
		  			+ COLUMN_DESC + " text, "
		  			+ COLUMN_CREATED_AT + " integer, "
		  			+ COLUMN_MODIFIED_AT + " integer, "
		  			+ COLUMN_NO_OF_LIKES + " integer, "
		  			+ COLUMN_NO_OF_COMMENTS + " integer, "
		  			+ COLUMN_USER_NAME + " text, "
		  			+ COLUMN_PROFILE_URL + " text, "
		  			+ COLUMN_PROFILE_TYPE + " text, "
		  			+ COLUMN_USER_IMG + " text "
		  			+ ");";
	
	private static final String CREATE_TABLE_PENDING_SPOT = "create table "
		      + TABLE_PENDING_SPOTS + "(" 
			  			+ COLUMN_ID + " integer primary key autoincrement, " 
			  			+ COLUMN_TAG + " text, "
			  			+ COLUMN_LONG + " text, "
			  			+ COLUMN_LAT + " text, "
			  			+ COLUMN_IMG + " text, "
			  			+ COLUMN_DESC + " text, "
			  			+ COLUMN_GO_ANONYMOUS + " text, "
			  			+ COLUMN_CREATED_AT + " integer, "
			  			+ COLUMN_SPOT_STATUS + " integer "
			  			+ ");";
	
	

	
	public SpotSQLiteHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_SPOT);
		database.execSQL(CREATE_TABLE_PENDING_SPOT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SpotSQLiteHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOTS);
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_SPOTS);
		    onCreate(db);

	}

}
