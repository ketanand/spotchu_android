package com.vrocketz.spotchu.activity;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.views.adapter.FullScreenSpotAdapter;

public class FullScreenSpotActivity extends Activity implements OnPageChangeListener{

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	private String imageFilePath;
	private PagerAdapter mPagerAdapter;
	private ViewPager mPager;
	private JSONArray mSpots;

	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.spot_fullscreen);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// get intent data
		
		Intent i = getIntent();
		
		int position = i.getExtras().getInt(Constants.SPOT_ID);
		try {
			mSpots = new JSONArray(i.getStringExtra(Constants.SPOTS));
		} catch (JSONException e) {
			// TODO Handle and return to caller.
			e.printStackTrace();
		}
	
		mPagerAdapter = new FullScreenSpotAdapter(this, mHandler, mSpots);
	
		mPager = (ViewPager) findViewById(R.id.pager);
	
		mPager.setAdapter(mPagerAdapter);
	
		mPager.setCurrentItem(position);
		mPager.setOnPageChangeListener(this);
	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	// Respond to the action bar's Up/Home button
	    	case android.R.id.home:
	    		NavUtils.navigateUpFromSameTask(this);
	    		return true;
	    	case R.id.menu_item_camera:
	    		openCameraApp();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}
	
	private static final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
				
			}
		}
	};
	
	public Handler getHandler(){
		return mHandler;
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[FullScreenSpotActivity] onPageSelected: " + position);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
		        if (resultCode == Activity.RESULT_OK) {
		        	Intent intent = new Intent(this, PostSpotActivity.class);
		        	intent.putExtra("PREVIEW_IMAGE", imageFilePath);
		        	startActivity(intent);
		            Log.d("spotu", "Image Captured : " + fileUri.toString());
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
	    	Log.d("Spotu", "file null");
	    }
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	    Log.d("spotu", fileUri.getPath());
	    // start the image capture Intent
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(){
		  File file = Util.getSavePath(Constants.IMAGE_TYPE_POST);
		  imageFilePath = file.getAbsolutePath();
	      return Uri.fromFile(file);
	}

}


