package com.vrocketz.spotchu.activity;

import java.io.File;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.fragment.ExploreGridFragment;
import com.vrocketz.spotchu.activity.fragment.MySpotsFragment;
import com.vrocketz.spotchu.activity.fragment.TabListner;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.UserHelper;
import com.vrocketz.spotchu.helper.Util;

public class MainActivity extends FragmentActivity implements OnClickListener{
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	private String imageFilePath;
	/* Client used to interact with Google APIs. */
	private GoogleApiClient mGoogleApiClient = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[MainActivity] OnCreate Called.");
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);

	    Tab tab = actionBar.newTab()
	                       .setText(R.string.explore)
	                       .setTabListener(new TabListner<ExploreGridFragment>(
	                               this, "explore", ExploreGridFragment.class));
	    actionBar.addTab(tab);

	    tab = actionBar.newTab()
	                   .setText(R.string.my_spots)
	                   .setTabListener(new TabListner<MySpotsFragment>(
	                           this, "my_spots", MySpotsFragment.class));
	    actionBar.addTab(tab);
	    if (savedInstanceState != null){
		    imageFilePath = savedInstanceState.getString(Constants.SPOT_IMAGE_URI_KEY, null);
		    if (imageFilePath != null){
		    	fileUri = Uri.parse(imageFilePath);
		    }else {
		    	fileUri = null;
		    }
	    }
	    if (getIntent().getBooleanExtra("postSpot", false)){
	    	actionBar.selectTab(tab);
	    }
	    initGoogleAPIClient();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}

	}
	
	private void initGoogleAPIClient(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			if (Config.DEBUG)
				Log.d("spotchu", "On Version greated than GingerBread");
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(Plus.API)
					.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[MainActivity] OnConfigurationChanged Called.");
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (imageFilePath != null){
			outState.putString(Constants.SPOT_IMAGE_URI_KEY, imageFilePath);
		}
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Main] onClick , view :" + v.getClass().getName()
					 + ", view id : " + v.getId());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.main_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	    	case R.id.menu_item_camera:
	    		openCameraApp();
	    		return true;
	    	case R.id.menu_item_logout:
	    		clearUser();
	    		finish();
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void clearUser(){
		Util.setPref(Constants.USER_LOGGED_IN, false);
		Util.setPref(Constants.USER_EMAIL, null);
		Util.setPref(Constants.USER_NAME, null);
		logoutFromGoogle();
		startLoginActivity();
	}
	
	private void logoutFromGoogle(){
		if (mGoogleApiClient.isConnected()) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Main Activity] Signing Out of Google.");
		      Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		      mGoogleApiClient.disconnect();
		      mGoogleApiClient.connect();
		}else {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Main Activity] Client Disconnected.");
		}
	}
	
	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("logout", true);
		startActivity(intent);
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
		        if (resultCode == Activity.RESULT_OK) {
		        	Intent intent = new Intent(this, PostSpotActivity.class);
		        	if (Config.DEBUG)
		        		Log.d(Constants.APP_NAME, "[MainActivity] OnActivtyResult Image Captured : " + imageFilePath);
		        	intent.putExtra("PREVIEW_IMAGE", imageFilePath);
		        	startActivity(intent);
		        } else if (resultCode == Activity.RESULT_CANCELED) {
		        	 Toast.makeText(this, "User Cancelled", Toast.LENGTH_LONG).show();
		        } else {
		        	Toast.makeText(this, "Some Error Occured , Please try again", Toast.LENGTH_LONG).show();
		        }
		 }
	}
	
	private void openCameraApp(){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
	    fileUri = getOutputMediaFileUri(); // create a file to save the image
	    if (fileUri == null){
	    	Log.d(Constants.APP_NAME, "file null");
	    }
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	    if (Config.DEBUG)
	    	Log.d(Constants.APP_NAME, "Start Camera: Image save path:" + fileUri.getPath());
	    // start the image capture Intent
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(){
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[MainActivity] getOutPutMediaFileUri Starts");
		  File file = Util.getSavePath(Constants.IMAGE_TYPE_POST);
		  imageFilePath = file.getAbsolutePath();
		  if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[MainActivity] getOutPutMediaFileUri File Path: " + imageFilePath);  
	      return Uri.fromFile(file);
	}
	
	
}
