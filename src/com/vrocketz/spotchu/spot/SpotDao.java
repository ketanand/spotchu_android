package com.vrocketz.spotchu.spot;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SpotDao {
	  private SQLiteDatabase database;
	  private SpotSQLiteHelper dbHelper;
	  private String[] allColumns = { SpotSQLiteHelper.COLUMN_ID,
			  						  SpotSQLiteHelper.COLUMN_USERID,
			  						SpotSQLiteHelper.COLUMN_TAG,
			  						SpotSQLiteHelper.COLUMN_SCALE,
			  						SpotSQLiteHelper.COLUMN_LONG,
			  						SpotSQLiteHelper.COLUMN_LAT,
			  						SpotSQLiteHelper.COLUMN_CITY,
			  						SpotSQLiteHelper.COLUMN_LOCALITY,
			  						SpotSQLiteHelper.COLUMN_IMG,
			  						SpotSQLiteHelper.COLUMN_URL,
			  						SpotSQLiteHelper.COLUMN_DESC,
			  						SpotSQLiteHelper.COLUMN_CREATED_AT,
			  						SpotSQLiteHelper.COLUMN_MODIFIED_AT,
			  						SpotSQLiteHelper.COLUMN_NO_OF_LIKES,
			  						SpotSQLiteHelper.COLUMN_NO_OF_COMMENTS,
			  						SpotSQLiteHelper.COLUMN_USER_NAME,
			  						SpotSQLiteHelper.COLUMN_PROFILE_URL,
			  						SpotSQLiteHelper.COLUMN_PROFILE_TYPE,
			  						SpotSQLiteHelper.COLUMN_USER_IMG
	      							};

	  public SpotDao(Context context) {
	    dbHelper = new SpotSQLiteHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public Spot createSpot(JSONObject spot) throws JSONException {
	    ContentValues values = getValuesFromSpot(spot);
	    long insertId = database.insert(SpotSQLiteHelper.TABLE_SPOTS, null, values);
	    Cursor cursor = database.query(SpotSQLiteHelper.TABLE_SPOTS,
	        allColumns, SpotSQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Spot newSpot = cursorToSpot(cursor);
	    cursor.close();
	    return newSpot;
	  }
	  
	  public long createSpot(Spot spot) throws JSONException {
		    ContentValues values = getValuesFromSpot(spot);
		    long insertId = database.insert(SpotSQLiteHelper.TABLE_SPOTS, null, values);
		    return insertId;
	  }

	  public void deleteSpot(Spot spot) {
	    long id = spot.getId();
	    database.delete(SpotSQLiteHelper.TABLE_SPOTS, SpotSQLiteHelper.COLUMN_ID
	        + " = " + id, null);
	  }
	  
	  public boolean deleteAllSpots() {
		  int doneDelete = 0;
		  doneDelete = database.delete(SpotSQLiteHelper.TABLE_SPOTS, null , null);
		  return doneDelete > 0;
	  }

	  public List<Spot> getAllSpots() {
	    List<Spot> spots = new ArrayList<Spot>();

	    Cursor cursor = database.query(SpotSQLiteHelper.TABLE_SPOTS,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Spot spot = cursorToSpot(cursor);
	      spots.add(spot);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return spots;
	  }
	  
	  public Spot getSpotById(Integer id){
		  Cursor cursor = database.query(SpotSQLiteHelper.TABLE_SPOTS,
			        allColumns, SpotSQLiteHelper.COLUMN_ID + " = " + id, null,
			        null, null, null);
		  cursor.moveToFirst();
		  Spot spot = cursorToSpot(cursor);
		  cursor.close();
		  return spot;
	  }

	  private Spot cursorToSpot(Cursor cursor) {
	    Spot spot = new Spot();
	    spot.setId(cursor.getLong(0));
	    spot.setUserId(cursor.getInt(1));
	    spot.setTag(cursor.getString(2));
	    spot.setScale(cursor.getInt(3));
	    spot.setLocationLong(cursor.getString(4));
	    spot.setLocationLati(cursor.getString(5));
	    spot.setCity(cursor.getString(6));
	    spot.setLocality(cursor.getString(7));
	    spot.setImg(cursor.getString(8));
	    spot.setUrl(cursor.getString(9));
	    spot.setDesc(cursor.getString(10));
	    spot.setCreatedAt(cursor.getLong(11));
	    spot.setModifiedAt(cursor.getLong(12));
	    spot.setNoOfLikes(cursor.getInt(13));
	    spot.setNoOfComments(cursor.getInt(14));
	    spot.setName(cursor.getString(15));
	    spot.setProfileUrl(cursor.getString(16));
	    spot.setProfileType(cursor.getString(17));
	    spot.setImageUrl(cursor.getString(18));
	    return spot;
	  }
	  
	  private ContentValues getValuesFromSpot(JSONObject spot) throws JSONException{
		  ContentValues values = new ContentValues();
		  values.put(SpotSQLiteHelper.COLUMN_ID, spot.getInt(SpotHelper.SPOT_ID));
		  values.put(SpotSQLiteHelper.COLUMN_USERID, spot.getInt(SpotHelper.SPOT_USER_ID));
		  values.put(SpotSQLiteHelper.COLUMN_TAG, spot.getString(SpotHelper.SPOT_TAG));
		  values.put(SpotSQLiteHelper.COLUMN_SCALE, spot.getInt(SpotHelper.SPOT_SCALE));
		  values.put(SpotSQLiteHelper.COLUMN_LONG, spot.getString(SpotHelper.SPOT_LONG));
		  values.put(SpotSQLiteHelper.COLUMN_LAT, spot.getString(SpotHelper.SPOT_LAT));
		  values.put(SpotSQLiteHelper.COLUMN_CITY, spot.getString(SpotHelper.SPOT_CITY));
		  values.put(SpotSQLiteHelper.COLUMN_LOCALITY, spot.getString(SpotHelper.SPOT_LOCALITY));
		  values.put(SpotSQLiteHelper.COLUMN_IMG, spot.getString(SpotHelper.SPOT_IMG));
		  values.put(SpotSQLiteHelper.COLUMN_URL, spot.getString(SpotHelper.SPOT_URL));
		  values.put(SpotSQLiteHelper.COLUMN_DESC, spot.getString(SpotHelper.SPOT_DESC));
		  values.put(SpotSQLiteHelper.COLUMN_CREATED_AT, spot.getInt(SpotHelper.SPOT_CREATED_AT));
		  values.put(SpotSQLiteHelper.COLUMN_MODIFIED_AT, spot.getInt(SpotHelper.SPOT_MODIFIED_AT));
		  values.put(SpotSQLiteHelper.COLUMN_NO_OF_LIKES, spot.getInt(SpotHelper.SPOT_NO_OF_LIKES));
		  values.put(SpotSQLiteHelper.COLUMN_NO_OF_COMMENTS, spot.getInt(SpotHelper.SPOT_NO_OF_COMMENTS));
		  values.put(SpotSQLiteHelper.COLUMN_USER_NAME, spot.getString(SpotHelper.SPOT_USERNAME));
		  values.put(SpotSQLiteHelper.COLUMN_PROFILE_URL, spot.getString(SpotHelper.SPOT_PROFILE_URL));
		  values.put(SpotSQLiteHelper.COLUMN_PROFILE_TYPE, spot.getString(SpotHelper.SPOT_PROFILE_TYPE));
		  values.put(SpotSQLiteHelper.COLUMN_USER_IMG, spot.getString(SpotHelper.SPOT_USERPIC));
		  return values;
	  }
	  
	  private ContentValues getValuesFromSpot(Spot spot) throws JSONException{
		  ContentValues values = new ContentValues();
		  values.put(SpotSQLiteHelper.COLUMN_ID, spot.getId());
		  values.put(SpotSQLiteHelper.COLUMN_USERID, spot.getUserId());
		  values.put(SpotSQLiteHelper.COLUMN_TAG, spot.getTag());
		  values.put(SpotSQLiteHelper.COLUMN_SCALE, spot.getScale());
		  values.put(SpotSQLiteHelper.COLUMN_LONG, spot.getLocationLong());
		  values.put(SpotSQLiteHelper.COLUMN_LAT, spot.getLocationLati());
		  values.put(SpotSQLiteHelper.COLUMN_CITY, spot.getCity());
		  values.put(SpotSQLiteHelper.COLUMN_LOCALITY, spot.getLocality());
		  values.put(SpotSQLiteHelper.COLUMN_IMG, spot.getImg());
		  values.put(SpotSQLiteHelper.COLUMN_URL, spot.getUrl());
		  values.put(SpotSQLiteHelper.COLUMN_DESC, spot.getDesc());
		  values.put(SpotSQLiteHelper.COLUMN_CREATED_AT, spot.getCreatedAt());
		  values.put(SpotSQLiteHelper.COLUMN_MODIFIED_AT, spot.getModifiedAt());
		  values.put(SpotSQLiteHelper.COLUMN_NO_OF_LIKES, spot.getNoOfLikes());
		  values.put(SpotSQLiteHelper.COLUMN_NO_OF_COMMENTS, spot.getNoOfComments());
		  values.put(SpotSQLiteHelper.COLUMN_USER_NAME, spot.getName());
		  values.put(SpotSQLiteHelper.COLUMN_PROFILE_URL, spot.getProfileUrl());
		  values.put(SpotSQLiteHelper.COLUMN_PROFILE_TYPE, spot.getProfileType());
		  values.put(SpotSQLiteHelper.COLUMN_USER_IMG, spot.getImageUrl());
		  return values;
	  }
	  
	  @Override
	  protected void finalize() throws Throwable {
		  if (dbHelper != null){
			  dbHelper.close();
		  }
		  super.finalize();
	  }
}
