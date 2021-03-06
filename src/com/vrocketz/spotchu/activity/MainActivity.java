package com.vrocketz.spotchu.activity;

import java.io.File;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.User;
import com.vrocketz.spotchu.activity.fragment.ExploreGridFragment;
import com.vrocketz.spotchu.activity.fragment.MyCircleFragment;
import com.vrocketz.spotchu.activity.fragment.MySpotsFragment;
import com.vrocketz.spotchu.activity.fragment.TabListner;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.Logout;
import com.vrocketz.spotchu.spot.PendingSpotDao;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int SELECT_FILE_ACTIVITY_REQUEST_CODE = 101;
	private Uri fileUri;
	private String imageFilePath;
	/* Client used to interact with Google APIs. */
	private GoogleApiClient mGoogleApiClient = null;

	private ProgressDialog mDialog;
	private User.Type userType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[MainActivity] OnCreate Called.");
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		Tab tab = actionBar
				.newTab()
				.setText(R.string.explore)
				.setTabListener(
						new TabListner<ExploreGridFragment>(this, "explore",
								ExploreGridFragment.class));
		actionBar.addTab(tab);
		
		tab = actionBar
				.newTab()
				.setText(R.string.my_circle)
				.setTabListener(
						new TabListner<MyCircleFragment>(this, "my_circle",
								MyCircleFragment.class));
		actionBar.addTab(tab);

		tab = actionBar
				.newTab()
				.setText(R.string.profile)
				.setTabListener(
						new TabListner<MySpotsFragment>(this, "profile",
								MySpotsFragment.class));
		actionBar.addTab(tab);

		if (savedInstanceState != null) {
			imageFilePath = savedInstanceState.getString(
					Constants.SPOT_IMAGE_URI_KEY, null);
			if (imageFilePath != null) {
				fileUri = Uri.parse(imageFilePath);
			} else {
				fileUri = null;
			}
		}
		if (getIntent().getBooleanExtra("postSpot", false)) {
			actionBar.selectTab(tab);
		}
		userType = User.Type.valueOf(Util.getGlobalPreferences()
				.getString(Constants.USER_TYPE, "Google").toUpperCase());
		if (userType == User.Type.GOOGLE) {
			initGoogleAPIClient();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}

	}

	private void initGoogleAPIClient() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			if (Config.DEBUG)
				Log.d("spotchu", "On Version greated than GingerBread");
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).build();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME,
					"[MainActivity] OnConfigurationChanged Called.");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (imageFilePath != null) {
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
			Log.d(Constants.APP_NAME, "[Main] onClick , view :"
					+ v.getClass().getName() + ", view id : " + v.getId());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int what = msg.what;
			switch (what) {
			case Constants.NO_INTERNET:
				hideLogoutDialog();
				handleFailure(Constants.NO_INTERNET);
				break;
			case Constants.LOGOUT_FAILED:
				hideLogoutDialog();
				handleFailure(Constants.LOGOUT_FAILED);
				break;
			case Constants.LOGOUT_SUCCESSGFUL:
				Util.setPref(Constants.USER_LOGGED_IN, false);
				Util.setPref(Constants.USER_EMAIL, null);
				Util.setPref(Constants.USER_NAME, null);
				Util.setPref(Constants.USER_TYPE, null);
				clearUserDb();
				startLoginActivity();
				finish();
				break;
			}
		}

	};

	private void handleFailure(int type) {
		switch (type) {
		case Constants.LOGOUT_FAILED:
			Toast.makeText(this,
					getResources().getString(R.string.logout_failed),
					Toast.LENGTH_LONG).show();
			break;
		case Constants.NO_INTERNET:
			Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG)
					.show();
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.menu_item_camera:
			selectImage();
			return true;
		case R.id.menu_item_logout:
			displayLogoutDialog();
			clearUser();
			return true;
		case R.id.menu_item_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void displayLogoutDialog() {
		mDialog = new ProgressDialog(this);
		mDialog.setTitle(Constants.APP_NAME);
		mDialog.setMessage(getResources().getString(R.string.logout_msg));
		mDialog.setCancelable(false);
		mDialog.show();
	}

	private void hideLogoutDialog() {
		if (mDialog != null)
			mDialog.dismiss();
	}

	private void clearUser() {
		if (userType == User.Type.GOOGLE) {
			if (logoutFromGoogle()) {
				new Thread(new Logout(mHandler)).start();
			} else {
				hideLogoutDialog();
				handleFailure(Constants.LOGOUT_FAILED);
			}
		} else if (userType == User.Type.FACEBOOK) {
			if (logoutFromFacebook()) {
				new Thread(new Logout(mHandler)).start();
			} else {
				hideLogoutDialog();
				handleFailure(Constants.LOGOUT_FAILED);
			}
		} else {
			hideLogoutDialog();
			handleFailure(Constants.LOGOUT_FAILED);
		}
	}

	private boolean logoutFromFacebook() {
		Session session = Session.getActiveSession();
		if (session != null) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME,
						"[Main Activity] Logout from Facebook");
			session.closeAndClearTokenInformation();
			return true;
		}else {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME,
						"[Main Activity] Facebook Session is null");
	        session = new Session(this);
	        Session.setActiveSession(session);
	        session.closeAndClearTokenInformation();
	            //clear your preferences if saved
	        return true;
	    }

	}

	private void clearUserDb() {
		PendingSpotDao dao = new PendingSpotDao(this);
		dao.open();
		dao.deleteAllSpots();
		dao.close();
	}

	private boolean logoutFromGoogle() {
		if (mGoogleApiClient.isConnected()) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME,
						"[Main Activity] Signing Out of Google.");
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
			return true;
		} else {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME,
						"[Main Activity] Client Disconnected.");
			return false;
		}
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("logout", true);
		startActivity(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(this, PostSpotActivity.class);
			if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[MainActivity] OnActivtyResult Image Captured : "
									+ imageFilePath);
				intent.putExtra("PREVIEW_IMAGE", imageFilePath);
				startActivity(intent);

			} else if (requestCode == SELECT_FILE_ACTIVITY_REQUEST_CODE) {
				Uri selectedImageUri = data.getData();
				String imgPath = Util.getPathFromUri(selectedImageUri, MainActivity.this);
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[MainActivity] OnActivtyResult Image From Galery : "
									+ imgPath);
				intent.putExtra("PREVIEW_IMAGE", imgPath);
				startActivity(intent);
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			Toast.makeText(this, "User Cancelled", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Some Error Occured , Please try again",
					Toast.LENGTH_LONG).show();
		}
	}
	
	public void openCameraApp() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(); // create a file to save the image
		if (fileUri == null) {
			Log.d(Constants.APP_NAME, "file null");
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
															// name
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Start Camera: Image save path:"
					+ fileUri.getPath());
		// start the image capture Intent
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	public void openGaleryApp() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select File"),
				SELECT_FILE_ACTIVITY_REQUEST_CODE);
	}

	public void selectImage() {
		final CharSequence[] items = { "Take Photo", "Choose from Library",
				"Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Add Photo!");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Take Photo")) {
					openCameraApp();
				} else if (items[item].equals("Choose from Library")) {
					openGaleryApp();
				} else if (items[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME,
					"[MainActivity] getOutPutMediaFileUri Starts");
		File file = Util.getSavePath(Constants.IMAGE_TYPE_POST);
		imageFilePath = file.getAbsolutePath();
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME,
					"[MainActivity] getOutPutMediaFileUri File Path: "
							+ imageFilePath);
		return Uri.fromFile(file);
	}

}
