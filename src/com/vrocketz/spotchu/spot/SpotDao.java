package com.vrocketz.spotchu.spot;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SpotDao {
	  private SQLiteDatabase database;
	  private SpotSQLiteHelper dbHelper;
	  private String[] allColumns = { SpotSQLiteHelper.COLUMN_ID,
	      SpotSQLiteHelper.COLUMN_COMMENT };

	  public SpotDao(Context context) {
	    dbHelper = new SpotSQLiteHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public Spot createComment(String comment) {
	    ContentValues values = new ContentValues();
	    values.put(SpotSQLiteHelper.COLUMN_COMMENT, comment);
	    long insertId = database.insert(SpotSQLiteHelper.TABLE_SPOTS, null,
	        values);
	    Cursor cursor = database.query(SpotSQLiteHelper.TABLE_SPOTS,
	        allColumns, SpotSQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    Spot newSpot = cursorToComment(cursor);
	    cursor.close();
	    return newSpot;
	  }

	  public void deleteComment(Spot spot) {
	    long id = spot.getId();
	    System.out.println("Comment deleted with id: " + id);
	    database.delete(SpotSQLiteHelper.TABLE_SPOTS, SpotSQLiteHelper.COLUMN_ID
	        + " = " + id, null);
	  }

	  public List<Spot> getAllSpots() {
	    List<Spot> comments = new ArrayList<Spot>();

	    Cursor cursor = database.query(SpotSQLiteHelper.TABLE_SPOTS,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Spot spot = cursorToComment(cursor);
	      comments.add(spot);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return comments;
	  }

	  private Spot cursorToComment(Cursor cursor) {
	    Spot spot = new Spot();
	    spot.setId(cursor.getInt(0));
	    //spot.setComment(cursor.getString(1));
	    return spot;
	  }
}
