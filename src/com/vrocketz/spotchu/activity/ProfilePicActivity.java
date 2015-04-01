package com.vrocketz.spotchu.activity;

import java.util.regex.Pattern;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.User;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

public class ProfilePicActivity extends FragmentActivity{
	
	public static final String IMAGE_URL = "image_url";
	private ImageView mProfilePic;
	private String mImagePath;
	private User.Type userType;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fullscreen_profile_pic);
		
		//Hide navigation controls.
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		
		mProfilePic = (ImageView) findViewById(R.id.imgDisplay);
		mImagePath = getIntent().getStringExtra(IMAGE_URL);
		//userType = User.Type.valueOf(Util.getGlobalPreferences()
		//		.getString(Constants.USER_TYPE, "Google").toUpperCase());
		updateGoogleImagePath(mImagePath);
		ImageAware imageAware = new ImageViewAware(mProfilePic, false);
	    ImageLoader.getInstance().displayImage(mImagePath, imageAware);
	    
	    ActionBar ab = getActionBar();
		ab.setDisplayShowTitleEnabled(true);
		ab.setDisplayUseLogoEnabled(false);
		ab.setIcon(R.drawable.ic_launcher);
		ab.setTitle("Profile Pic");
	}
	
	private void updateGoogleImagePath(String path){
		mImagePath = path.replaceFirst(Pattern.quote("?sz=50"), "?sz=500");
	}

}
