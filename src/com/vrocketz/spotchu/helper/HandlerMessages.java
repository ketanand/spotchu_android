package com.vrocketz.spotchu.helper;

public enum HandlerMessages {
	
	SPOTS_FETCHED(1),
	SPOTS_FETCH_FAILED(2),
	COMMENTS_FETCHED(3),
	COMMENTS_FETCH_FAILED(4);
	
	int value;
	HandlerMessages(int val){
		value = val;
	}
	
	public int getValue(){
		return value;
	}
}
