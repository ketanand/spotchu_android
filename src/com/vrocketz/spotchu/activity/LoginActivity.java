package com.vrocketz.spotchu.activity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Image;
import com.vrocketz.spotchu.Api;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.SpotchuLocationService;
import com.vrocketz.spotchu.User;
import com.vrocketz.spotchu.activity.fragment.FaceBookFragment;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class LoginActivity extends FragmentActivity implements ConnectionCallbacks,
		OnConnectionFailedListener, OnClickListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private ViewFlipper mViewFlipper;
	private GestureDetector detector;
	private Context mContext;

	private FaceBookFragment fbFragment;
	
	/* Client used to interact with Google APIs. */
	private GoogleApiClient mGoogleApiClient = null;
	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;
	private GoogleCloudMessaging gcm;

	private boolean mSignInClicked;
	private ConnectionResult mConnectionResult;

	/*
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;
	private SharedPreferences mPref;
	private ProgressDialog mDialog;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mPref = Util.getGlobalPreferences();
		
		//Facebook key hash log starts
		 /*try {
		        PackageInfo info = getPackageManager().getPackageInfo(
		                "com.vrocketz.spotchu", 
		                PackageManager.GET_SIGNATURES);
		        for (Signature signature : info.signatures) {
		            MessageDigest md = MessageDigest.getInstance("SHA");
		            md.update(signature.toByteArray());
		            Log.d(Constants.APP_NAME, "KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
		            }
		    } catch (NameNotFoundException e) {

		    } catch (NoSuchAlgorithmException e) {

		    }*/
		//Facebook key hash log ends 
		 
		if (!checkPlayServices()) {
			Toast.makeText(this, R.string.app_incompatible, Toast.LENGTH_LONG)
					.show();
			finish();
		}
		gcm = GoogleCloudMessaging.getInstance(this);
		String regid = Util.getRegistrationId();

		if (regid == null) {
			registerInBackground(false);
		}else if (Util.wasAppUpgraded()){
			registerInBackground(true);
		}
		// Start location service.
		Intent intent = new Intent(this, SpotchuLocationService.class);
		startService(intent);
		Util.scheduleServiceCheckBroadCast(this);
		if (mPref.getBoolean(Constants.USER_LOGGED_IN, false)) {
			Log.d(Constants.APP_NAME, "User Logged in, taking to Spotchu Home.");
			startMainActivity();
			finish();
		} else {
			setContentView(R.layout.login);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "On Version greated than GingerBread");
				mGoogleApiClient = new GoogleApiClient.Builder(this)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this).addApi(Plus.API)
						.addScope(Plus.SCOPE_PLUS_LOGIN).build();

				SignInButton button = (SignInButton) findViewById(R.id.sign_in_button);
				button.setOnClickListener(this);
				button.setSize(SignInButton.SIZE_WIDE);
			}

			/*if (savedInstanceState == null) {
		        // Add the fragment on initial activity setup
		        fbFragment = new FaceBookFragment();
		        getSupportFragmentManager()
		        .beginTransaction()
		        .add(android.R.id.content, fbFragment)
		        .commit();
		    } else {
		        // Or set the fragment from restored state info
		    	fbFragment = (FaceBookFragment) getSupportFragmentManager()
		        .findFragmentById(android.R.id.content);
		    }*/

			// Initialize Slider
			initSlider();
		}

	}

	private void initSlider() {
		// detector = new GestureDetector(this, new SwipeGestureDetector());
		/*
		 * mViewFlipper = (ViewFlipper) this.findViewById(R.id.viewFlipper);
		 * 
		 * mViewFlipper.setOnTouchListener(new View.OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(final View view, final MotionEvent
		 * event) { detector.onTouchEvent(event); return true; } });
		 * 
		 * mViewFlipper.setAutoStart(true); mViewFlipper.setFlipInterval(3000);
		 * mViewFlipper.startFlipping();
		 */
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000)
						.show();
			} else {
				Log.i(Constants.APP_NAME, "This device is not supported.");
			}
			return false;
		}
		return true;
	}

	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!checkPlayServices()) {
			Toast.makeText(this, R.string.app_incompatible, Toast.LENGTH_LONG)
					.show();
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	protected void onStop() {
		super.onStop();

		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Login Activity] OnActivityResult, result code" + requestCode);
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}
			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		} else {
			super.onActivityResult(requestCode, responseCode, intent);
		}
	}

	/**
	 * Resolve google plus sing in error
	 */
	private void resolveSignInError() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "resolveSignInError");
		if (mConnectionResult != null) {
			if (mConnectionResult.hasResolution()) {
				try {
					mIntentInProgress = true;
					mConnectionResult
							.startResolutionForResult(this, RC_SIGN_IN);
					// Show dialog as soon as login button is clicked.
					mDialog = new ProgressDialog(LoginActivity.this);
					mDialog.setTitle(Constants.APP_NAME);
					mDialog.setMessage(getResources().getString(R.string.login_msg));
					mDialog.setCancelable(false);
					mDialog.show();
				} catch (SendIntentException e) {
					mIntentInProgress = false;
					mGoogleApiClient.connect();
					if (mDialog != null){
						if (mDialog.isShowing())
						mDialog.dismiss();
					}
				}
			}
		} else {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "resolveSignInError connection result is null.");
			Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "OnConnectionFailed");
		if (!mIntentInProgress) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "OnConnectionFailed: Intent not progressing");
			mConnectionResult = result;
			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "OnConnected");
		mSignInClicked = false;
		if (!mPref.getBoolean(Constants.USER_LOGGED_IN, false)) {
			Editor e = mPref.edit();
			String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
			e.putString(Constants.USER_EMAIL, email);
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(mGoogleApiClient);
				String personName = currentPerson.getDisplayName();
				Image personPhoto = currentPerson.getImage();
				String personGooglePlusProfile = currentPerson.getUrl();
				e.putString(Constants.USER_NAME, personName);
				e.putString(Constants.GPLUS_PROFILE_URL,
						personGooglePlusProfile);
				e.putString(Constants.USER_TYPE, User.Type.GOOGLE.toString());
				User user = new User(User.Type.GOOGLE, email, personName,
						personPhoto.getUrl(), personGooglePlusProfile);
				new RegisterUser(Util.getAppVersion(this)).execute(user);
			}
			e.commit();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onClick(View v) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "OnClick, view ID:" + v.getId());
		if (v.getId() == R.id.sign_in_button
				&& !mGoogleApiClient.isConnecting()) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "login button clicked");

			if (Util.isInternetAvailable()) {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "Internet Available resolve SignInError called.");
				mSignInClicked = true;
				resolveSignInError();
			} else {
				Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	private void registerInBackground(boolean upgraded) {
		new RegisterAppToGCM(upgraded).execute();
	}

	class SwipeGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_left));
					mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_left));
					mViewFlipper.stopFlipping();
					mViewFlipper.showNext();
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_right));
					mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_right));
					mViewFlipper.stopFlipping();
					mViewFlipper.showPrevious();
					return true;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return false;
		}
	}

	// AsyncTask To Push GCM Registration ID to Server
	private class RegisterAppToGCM extends AsyncTask<Void, Void, Void> {
		
		private boolean upgraded;
		
		public RegisterAppToGCM(){
			upgraded = false;
		}
		
		public RegisterAppToGCM(boolean upgraded){
			this.upgraded = upgraded;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging
							.getInstance(getApplicationContext());
				}
				String regid = gcm.register(Constants.SENDER_ID);
				String oldRegId = null;
				if (upgraded){
					oldRegId = Util.getGlobalPreferences().getString(Constants.REGISTRATION_ID, null);
				}
				int appVer = Util.getAppVersion(getApplicationContext());
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"Device registered, registration ID=" + regid);
				
				Api.sendRegistrationIdToBackend(regid, appVer, oldRegId);

				Util.setPref(Constants.REGISTRATION_ID, regid);
				Util.setPref(Constants.APP_VERSION,	appVer);
			} catch (IOException ex) {

			}
			return null;
		}

	}

	private class RegisterUser extends AsyncTask<User, Void, String> {

		int appVer;
		
		public RegisterUser(int ver){
			appVer = ver;
			
		}
		
		@Override
		protected void onPreExecute() {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "Registered user: Pre Execute");
			super.onPreExecute();
			/*
			 * mDialog = new ProgressDialog(LoginActivity.this);
			 * mDialog.setTitle("Spotchu"); mDialog.setMessage("Logging In..");
			 * mDialog.show();
			 */
		}

		@Override
		protected String doInBackground(User... params) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "Registered user: Do In BackGround");
			return Api.registerUser(params[0], appVer);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "Registered user:" + result);
			if (mDialog != null)
				mDialog.dismiss();
			if (result == null) {
				Toast.makeText(getApplicationContext(),
						R.string.registration_failed, Toast.LENGTH_LONG).show();
			} else {
				Util.setPref(Constants.USER_LOGGED_IN, true);
				startMainActivity();
				finish();
			}
		}

	}

}
