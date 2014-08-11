package com.vrocketz.spotchu;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Image;

public class LoginActivity extends Activity implements ConnectionCallbacks
					, OnConnectionFailedListener, OnClickListener{

	/* Request code used to invoke sign in user interactions. */
	  private static final int RC_SIGN_IN = 0;
	  /* Client used to interact with Google APIs. */
	  private GoogleApiClient mGoogleApiClient = null;
	  private GoogleCloudMessaging gcm;
	  
	  private boolean mSignInClicked;
	  private ConnectionResult mConnectionResult;

	  /* A flag indicating that a PendingIntent is in progress and prevents
	   * us from starting further intents.
	   */
	  private boolean mIntentInProgress;
	  private SharedPreferences mPref;
	  private ProgressDialog mDialog;
	  

	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mPref = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
	    Util.setApplicationContext(getApplicationContext());
	    if (!checkPlayServices()){
	    	Toast.makeText(this, R.string.app_incompatible, Toast.LENGTH_LONG).show();
	    	finish();
	    }
	    gcm = GoogleCloudMessaging.getInstance(this);
        String regid = Util.getRegistrationId();

        if (regid == null) {
            registerInBackground();
        }
	    if (/* mPref.getBoolean(Constants.USER_LOGGED_IN, false)*/ true){
	    	Log.d("spotchu", "User Logged in, taking to Spotchu Home.");
	    	startHomeActivity();
	    	finish();
	    }else {
		    setContentView(R.layout.main);
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		    	Log.d("spotchu", "On Version greated than GingerBread");
		    	mGoogleApiClient = new GoogleApiClient.Builder(this)
		        .addConnectionCallbacks(this)
		        .addOnConnectionFailedListener(this)
		        .addApi(Plus.API)
		        .addScope(Plus.SCOPE_PLUS_LOGIN)
		        .build();
		    	
		    	findViewById(R.id.sign_in_button).setOnClickListener(this);
		    }
	    }
	    
	  }
	  
	  private boolean checkPlayServices() {
		    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		    if (resultCode != ConnectionResult.SUCCESS) {
		        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
		            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
		                    9000).show();
		        } else {
		            Log.i(Constants.APP_NAME, "This device is not supported.");
		        }
		        return false;
		    }
		    return true;
		}
	  
	  private void startHomeActivity(){
		  Intent intent = new Intent(this, SpotuHome.class);
	      startActivity(intent);
	  }

	  protected void onStart() {
	    super.onStart();
	    if (mGoogleApiClient != null){
	    	mGoogleApiClient.connect();
	    }
	    
	  }
	  
	  @Override
	protected void onResume() {
		super.onResume();
		if (!checkPlayServices()){
	    	Toast.makeText(this, R.string.app_incompatible, Toast.LENGTH_LONG).show();
	    	finish();
	    }
	}

	  protected void onStop() {
	    super.onStop();
	    
	    if (mGoogleApiClient !=null && mGoogleApiClient.isConnected()) {
	      mGoogleApiClient.disconnect();
	    }
	  }
	  
	  protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		  Log.d("spotchu", "OnActivityResult, result code"+requestCode);
		  if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}  
		    mIntentInProgress = false;

		    if (!mGoogleApiClient.isConnecting()) {
		      mGoogleApiClient.connect();
		    }
		  }
	  }
	
	  private void resolveSignInError() {
		  Log.d("spotchu", "resolveSignInError");
		  if (mConnectionResult.hasResolution()) {
		    try {
		      mIntentInProgress = true;
		      mConnectionResult.startResolutionForResult(this, // your activity
                      RC_SIGN_IN);
		    } catch (SendIntentException e) {
		      // The intent was canceled before it was sent.  Return to the default
		      // state and attempt to connect to get an updated ConnectionResult.
		      mIntentInProgress = false;
		      mGoogleApiClient.connect();
		    }
		  }
		}
	  
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d("spotchu", "OnConnectionFailed");
		if (!mIntentInProgress) {
			Log.d("spotchu", "OnConnectionFailed: Intent not progressing");
			mConnectionResult = result;
			if (mSignInClicked) {
			      // The user has already clicked 'sign-in' so we attempt to resolve all
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
		if (!mPref.getBoolean(Constants.USER_LOGGED_IN, false)){
			Editor e = mPref.edit();
			String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
			e.putString(Constants.USER_EMAIL, email);
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
			    Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
			    String personName = currentPerson.getDisplayName();
			    Image personPhoto = currentPerson.getImage();
			    String personGooglePlusProfile = currentPerson.getUrl();
			    e.putString(Constants.USER_NAME, personName);
			    e.putString(Constants.GPLUS_PROFILE_URL, personGooglePlusProfile);
			    User user = new User(User.Type.GOOGLE, 
			    		email, personName, personPhoto.getUrl(), personGooglePlusProfile);
				new RegisterUser().execute(user);
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
		Log.d("Spotchu", "OnClick, view ID:" + v.getId());
		if (v.getId() == R.id.sign_in_button
			    && !mGoogleApiClient.isConnecting()) {
			if (Config.DEBUG)
			    Log.d(Constants.APP_NAME, "login button clicked");
			
			if (Util.isInternetAvailable()){
			    mSignInClicked = true;
			    resolveSignInError();
			}else {
				Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
			}
	   }
		
	}
	
	private void registerInBackground(){
		new RegisterAppToGCM().execute();
	}
	
	//AsyncTask To Push GCM Registration ID to Server
	private class RegisterAppToGCM extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                }
                String regid = gcm.register(Constants.SENDER_ID);
                if (Config.DEBUG)
                	Log.d(Constants.APP_NAME, "Device registered, registration ID=" + regid);

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                Api.sendRegistrationIdToBackend(regid);

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the regID - no need to register again.
                Util.setPref(Constants.REGISTRATION_ID, regid);
                Util.setPrefInt(Constants.APP_VERSION, Util.getAppVersion(getApplicationContext()));
            } catch (IOException ex) {
                // TODO: If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
			return null;
		}
		
	}
	
	private class RegisterUser extends AsyncTask<User, Void, String>{
		
		@Override
		protected void onPreExecute() {
			if (Config.DEBUG)
			    Log.d(Constants.APP_NAME, "Registered user: Pre Execute");
			super.onPreExecute();
			mDialog = new ProgressDialog(LoginActivity.this);
			mDialog.setTitle("Spotchu");
			mDialog.setMessage("Logging In..");
			mDialog.show();
		}
		
		@Override
		protected String doInBackground(User... params) {
			if (Config.DEBUG)
			    Log.d(Constants.APP_NAME, "Registered user: Do In BackGround");
			return Api.registerUser(params[0]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (Config.DEBUG)
			    Log.d(Constants.APP_NAME, "Registered user:" + result);
			mDialog.dismiss();
			if (result == null){
				Toast.makeText(getApplicationContext(), R.string.registration_failed, Toast.LENGTH_LONG).show();
			}else {
				Editor e = mPref.edit();
				e.putBoolean(Constants.USER_LOGGED_IN, true);
				e.commit();
				startHomeActivity();
				finish();
			}
		}
		
	}

}
