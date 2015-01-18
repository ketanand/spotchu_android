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

public class PendingSpotDao {
	private SQLiteDatabase database;
	private SpotSQLiteHelper dbHelper;

	private String[] allColumns = { SpotSQLiteHelper.COLUMN_ID,
			SpotSQLiteHelper.COLUMN_TAG, SpotSQLiteHelper.COLUMN_LONG,
			SpotSQLiteHelper.COLUMN_LAT, SpotSQLiteHelper.COLUMN_IMG,
			SpotSQLiteHelper.COLUMN_DESC, SpotSQLiteHelper.COLUMN_GO_ANONYMOUS,
			SpotSQLiteHelper.COLUMN_CREATED_AT,
			SpotSQLiteHelper.COLUMN_SPOT_STATUS };

	public PendingSpotDao(Context context) {
		dbHelper = new SpotSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long createSpot(Spot spot) {
		ContentValues values = getValuesFromSpot(spot);
		long insertId = database.insert(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
				null, values);
		return insertId;
	}

	public void deleteSpot(Spot spot) {
		long id = spot.getId();
		database.delete(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
				SpotSQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public void deleteSpotById(Integer id) {
		database.delete(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
				SpotSQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public boolean deleteAllSpots() {
		int doneDelete = 0;
		doneDelete = database.delete(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
				null, null);
		return doneDelete > 0;
	}

	public void UpdateSpot(Spot spot) {
		ContentValues values = getValuesFromSpot(spot);
		long id = spot.getId();
		database.update(SpotSQLiteHelper.TABLE_PENDING_SPOTS, values,
				SpotSQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public List<Spot> getAllSpots() {
		List<Spot> spots = new ArrayList<Spot>();

		Cursor cursor = database.query(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
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
	
	public Cursor getAllSpotsCursor(){
		Cursor cursor = database.query(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
				allColumns, null, null, null, null, null);
		if (cursor != null){
			cursor.moveToFirst();
		}
		return cursor;
	}

	public List<Spot> getALLPendingSpots() {
		List<Spot> spots = new ArrayList<Spot>();

		Cursor cursor = database.query(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
				allColumns, SpotSQLiteHelper.COLUMN_SPOT_STATUS + " = "
						+ Spot.Status.PENDING.getValue(), null, null, null,
				null);

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
	
	public List<Spot> getALLFailedSpots() {
		List<Spot> spots = new ArrayList<Spot>();

		Cursor cursor = database.query(SpotSQLiteHelper.TABLE_PENDING_SPOTS,
				allColumns, SpotSQLiteHelper.COLUMN_SPOT_STATUS + " = "
						+ Spot.Status.FAILED.getValue(), null, null, null,
				null);

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

	public Spot getSpotById(Integer id) {
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
		spot.setId(cursor.getInt(0));
		spot.setTag(cursor.getString(1));
		spot.setLocationLong(cursor.getString(2));
		spot.setLocationLati(cursor.getString(3));
		spot.setImg(cursor.getString(4));
		spot.setDesc(cursor.getString(5));
		spot.setIsAnonymous(Boolean.parseBoolean(cursor.getString(6)));
		spot.setCreatedAt(cursor.getLong(7));
		spot.setStatus(Spot.Status.getFromValue(cursor.getInt(8)));
		return spot;
	}

	private ContentValues getValuesFromSpot(Spot spot) {
		ContentValues values = new ContentValues();
		values.put(SpotSQLiteHelper.COLUMN_ID, spot.getId());
		values.put(SpotSQLiteHelper.COLUMN_TAG, spot.getTag());
		values.put(SpotSQLiteHelper.COLUMN_LONG, spot.getLocationLong());
		values.put(SpotSQLiteHelper.COLUMN_LAT, spot.getLocationLati());
		values.put(SpotSQLiteHelper.COLUMN_IMG, spot.getImg());
		values.put(SpotSQLiteHelper.COLUMN_DESC, spot.getDesc());
		values.put(SpotSQLiteHelper.COLUMN_GO_ANONYMOUS,
				String.valueOf(spot.getIsAnonymous()));
		values.put(SpotSQLiteHelper.COLUMN_CREATED_AT, spot.getCreatedAt());
		values.put(SpotSQLiteHelper.COLUMN_SPOT_STATUS, spot.getStatus()
				.getValue());
		return values;
	}

	@Override
	protected void finalize() throws Throwable {
		if (dbHelper != null) {
			dbHelper.close();
		}
		super.finalize();
	}

}
