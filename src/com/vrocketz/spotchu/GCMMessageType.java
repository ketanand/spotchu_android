package com.vrocketz.spotchu;

public enum GCMMessageType {
	
	SUMMARY(1),
	NEW_SPOT(2),
	NEW_COMMENT(3),
	NEW_HI5(4),
	ANNOUNCEMENT(5),
	UPGRADE(6);
	
	private final int value;
	
	GCMMessageType(int val){
		this.value = val;
	}
	
	public int getValue(){
		return value;
	}
	
	public static GCMMessageType getFromValue(int val){
		if (val == 1){
			return SUMMARY;
		}else if (val == 2){
			return NEW_SPOT;
		}else if (val == 3){
			return NEW_COMMENT;
		}else if (val == 4){
			return NEW_HI5;
		}else if (val == 5){
			return ANNOUNCEMENT;
		}else if (val == 6){
			return UPGRADE;
		}
		return null;
	}

}
