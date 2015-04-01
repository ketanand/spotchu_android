package com.vrocketz.spotchu.spot.comment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentHelper {
	
	public static final String ID = "id";
	public static final String SPOT_ID = "spot_id";
	public static final String USER_ID = "user_id";
	public static final String COMMENT = "comment";
	public static final String CREATED_AT = "created_at";
	public static final String USER_NAME = "user_name";
	public static final String USER_DP = "user_dp";
	
	public static Comment getFromJson(JSONObject commentJson) throws JSONException{
		Comment comment = new Comment();
		comment.setId(commentJson.getLong(ID));
		comment.setSpotId(commentJson.getLong(SPOT_ID));
		comment.setUserId(commentJson.getLong(USER_ID));
		comment.setComment(commentJson.getString(COMMENT));
		comment.setCreatedAt(commentJson.getLong(CREATED_AT));
		comment.setUserName(commentJson.getString(USER_NAME));
		comment.setUserDp(commentJson.getString(USER_DP));
		return comment;
	}
	
	public static ArrayList<Comment> getFromJsonArray(JSONArray arr) throws JSONException{
		ArrayList<Comment> list = new ArrayList<Comment>();
		for (int i = 0; i < arr.length(); i++){
			list.add(getFromJson(arr.getJSONObject(i)));
		}
		return list;
	}
}
