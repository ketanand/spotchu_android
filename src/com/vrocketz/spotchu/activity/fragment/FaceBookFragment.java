package com.vrocketz.spotchu.activity.fragment;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AppEventsLogger;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.RegisterUser;
import com.vrocketz.spotchu.User;
import com.vrocketz.spotchu.activity.LoginActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

public class FaceBookFragment extends Fragment {

	private UiLifecycleHelper uiHelper;
	private boolean mRequestInProgress;
	private ProgressDialog mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// Initialize facebook login UI Helper

		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
		mRequestInProgress = false;
		// Initialize FB login button LoginButton authButton =
		// (LoginButton) findViewById(R.id.authButton);
		// authButton.setReadPermissions(Arrays.asList("public_profile",
		// "email"));

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fbbutton, container, false);

		LoginButton authButton = (LoginButton) view
				.findViewById(R.id.authButton);
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("public_profile", "email"));

		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "FB Session is present.");
			onSessionStateChange(session, session.getState(), null);
		} else {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "FB Session is null.");
		}

		uiHelper.onResume();
		AppEventsLogger.activateApp(getActivity());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Facebook Fragment] OnActivityResult, result code" + requestCode);
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
		// Logs 'app deactivate' App Event.
		AppEventsLogger.deactivateApp(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (state.isOpened()) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[FB Fragment] onSessionStateChanged : Logged in...");
			final SharedPreferences pref = Util.getGlobalPreferences();
			if (!pref.getBoolean(Constants.USER_LOGGED_IN, false)) {
				if (!mRequestInProgress) {
					Request request = Request.newMeRequest(session,
							new Request.GraphUserCallback() {

								@Override
								public void onCompleted(final GraphUser user,
										Response response) {
									if (Config.DEBUG)
										Log.d(Constants.APP_NAME,
											"user found: "
													+ user.getName()
													+ " , "
													+ (String) user
															.getProperty("email"));

									Bundle params = new Bundle();
									params.putBoolean("redirect", false);
									params.putString("height", "200");
									params.putString("type", "normal");
									params.putString("width", "200");
									/* make the API call */
									new Request(session, "/me/picture", params,
											HttpMethod.GET,
											new Request.Callback() {
												public void onCompleted(
														Response response) {
													if (Config.DEBUG)
														Log.d(Constants.APP_NAME,
															"pic response :"
																	+ response
																			.getRawResponse());
													GraphObject obj = response
															.getGraphObject();
													if (obj != null) {
														JSONObject json = obj
																.getInnerJSONObject();
														String url = null;
														try {
															url = json.getJSONObject("data")
																	.getString(
																			"url");
														} catch (JSONException e) {
															e.printStackTrace();
														}
														Editor e = pref.edit();
														String email = (String) user.getProperty("email");
														e.putString(Constants.USER_EMAIL, email);
														String personName = user.getName();
														e.putString(
																Constants.USER_NAME,
																personName);
														// e.putString(Constants.FB_PROFILE_URL,
														// url);
														e.putString(
																Constants.USER_TYPE,
																User.Type.FACEBOOK
																		.toString());
														User user = new User(
																User.Type.FACEBOOK,
																email,
																personName,
																url, null);
														new RegisterUser(
																Util.getAppVersion(getActivity()),
																getActivity(), mDialog)
																.execute(user);
														e.commit();
													}
													mRequestInProgress = false;
												}
											}).executeAsync();

								}
							});
					request.executeAsync();
					mRequestInProgress = true;
					mDialog = new ProgressDialog(getActivity());
					mDialog.setTitle(Constants.APP_NAME);
					mDialog.setMessage(getResources().getString(R.string.login_msg));
					mDialog.setCancelable(false);
					mDialog.show();
				}
			}
		} else if (state.isClosed()) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[FB Fragment] onSessionStateChanged :  Logged out...");
		} else {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[FB Fragment] onSessionStateChanged : " + state.name());
		}
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
}
