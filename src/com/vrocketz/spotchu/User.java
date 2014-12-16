package com.vrocketz.spotchu;

import java.io.File;

import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.DownloadFileRunnable;

public class User {
	public enum Type {
		GOOGLE("Google"), FACEBOOK("Facebook");
		
		private String val;
		
		private Type(String val){
			this.val = val;
		}
		
		public String toString(){
			return this.val;
		}
	}
	private String email;
	private String name;
	private String imageUrl;
	private Type type; // 1 - google, 2- facebook
	private String profileUrl;
	
	public User(User.Type type,String email,String name,String imageUrl,String profileUrl){
		this.type = type;
		this.email = email;
		this.name = name;
		this.imageUrl = imageUrl;
		this.profileUrl = profileUrl;
		saveProfileImage();
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	private void saveProfileImage(){
		File saveAt = Util.getSavePath(Constants.IMAGE_TYPE_PROFILE);
		Thread t = new Thread(new DownloadFileRunnable(imageUrl, saveAt));
		t.start();
	}
}
