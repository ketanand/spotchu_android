package com.vrocketz.spotchu;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class SpotuHome extends FragmentActivity implements OnClickListener{
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	private String imageFilePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		 GridView gridview = (GridView) findViewById(R.id.gridview);
		    gridview.setAdapter(new GridViewAdapter(this));

		    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		            Toast.makeText(SpotuHome.this, "" + position, Toast.LENGTH_SHORT).show();
		        }
		    });
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_camera:
	            openCameraApp();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.home_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
		        if (resultCode == RESULT_OK) {
		            // Image captured and saved to fileUri specified in the Intent
		            //Toast.makeText(this, "Image saved to:\n" +
		                     //data.getData(), Toast.LENGTH_LONG).show();
		        	Intent intent = new Intent(this, PostReview.class);
		        	intent.putExtra("PREVIEW_IMAGE", imageFilePath);
		        	startActivity(intent);
		            Log.d("spotu", "Image Captured : " + fileUri.toString());
		        } else if (resultCode == RESULT_CANCELED) {
		        	 Toast.makeText(this, "User Cancelled", Toast.LENGTH_LONG).show();
		        } else {
		        	Toast.makeText(this, "Some Error Occured , Please try again", Toast.LENGTH_LONG).show();
		        }
		 }
	}
	
	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri(){
		  File file = Util.getSavePath(Constants.IMAGE_TYPE_POST);
		  imageFilePath = file.getAbsolutePath();
	      return Uri.fromFile(file);
	}

}
