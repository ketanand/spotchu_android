package com.vrocketz.spotchu.activity.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.FollowFollowingActivity;
import com.vrocketz.spotchu.activity.MainActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.GetMySpots;
import com.vrocketz.spotchu.runnables.GetUserMeta;
import com.vrocketz.spotchu.spot.PendingSpotDao;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.MySpotsListAdapter;
import com.vrocketz.spotchu.views.adapter.PendingSpotListAdapter;

public class MySpotsFragment extends Fragment implements OnClickListener {

	private ImageView mDisplayPic;
	private TextView mUserId, mlblPendingSpots, mTxtFollowers, mTxtFollowing,
			mLblFollowers, mLblFollowing;
	private JSONArray spots;
	private ListView mSpotList;
	private MySpotsListAdapter adapter;
	private PendingSpotDao mPendingSpotDao;
	private ListView mPendingSpotList;
	private PendingSpotListAdapter pendingAdapter;
	private AnimatedGifImageView mGifLoader;
	private ImageView mImgNoInternet;
	private Button mGetStarted;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[MySpotsFragment] OnCreateView Called.");
		View v = inflater.inflate(R.layout.my_spots, container, false);
		mDisplayPic = (ImageView) v.findViewById(R.id.imgUserDisplayPic);
		SharedPreferences pref = Util.getGlobalPreferences();
		Uri uri = Uri.fromFile(Util.getSavePath(Constants.IMAGE_TYPE_PROFILE));
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[My Spots] DP uri:" + uri.toString());
		mDisplayPic.setImageURI(uri);
		mUserId = (TextView) v.findViewById(R.id.lblUserId);
		mUserId.setText(pref.getString(Constants.USER_NAME, ""));
		mTxtFollowers = (TextView) v.findViewById(R.id.lblFollowers);
		mTxtFollowers.setOnClickListener(this);
		mTxtFollowing = (TextView) v.findViewById(R.id.lblFollowing);
		mTxtFollowing.setOnClickListener(this);
		mLblFollowers = (TextView) v.findViewById(R.id.lblFollowersTitle);
		mLblFollowers.setOnClickListener(this);
		mLblFollowing = (TextView) v.findViewById(R.id.lblFollowingTitle);
		mLblFollowing.setOnClickListener(this);
		// TODO : show spinner.
		mSpotList = (ListView) v.findViewById(R.id.lstMySpots);
		mGifLoader = (AnimatedGifImageView) v.findViewById(R.id.gifLoader);
		mGifLoader.setAnimatedGif(R.raw.loader,
				AnimatedGifImageView.TYPE.FIT_CENTER);
		mGetStarted = (Button) v.findViewById(R.id.btnGetStarted);
		mGetStarted.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity a = ((MainActivity) getActivity());
				a.selectImage();
			}
		});
		mImgNoInternet = (ImageView) v.findViewById(R.id.imgNoInternet);
		if (adapter == null) {
			adapter = new MySpotsListAdapter(getActivity());
		} else {
			mGifLoader.setVisibility(View.GONE);
			mSpotList.setVisibility(View.VISIBLE);
		}
		mSpotList.setAdapter(adapter);

		new Thread(new GetMySpots(mHandler)).start();
		new Thread(new GetUserMeta(mHandler, 0L)).start();
		// Pending listview
		initPendingSpotList(v);
		return v;
	}

	private void initPendingSpotList(View v) {
		mPendingSpotDao = new PendingSpotDao(getActivity());
		mPendingSpotDao.open();
		ArrayList<Spot> spots = (ArrayList<Spot>) mPendingSpotDao.getAllSpots();
		if (spots.size() > 0) {

			mPendingSpotList = (ListView) v.findViewById(R.id.lstPendingSpots);
			pendingAdapter = new PendingSpotListAdapter(getActivity(), spots);
			mPendingSpotList.setAdapter(pendingAdapter);
			mlblPendingSpots = (TextView) v.findViewById(R.id.lblPendingSpots);

			mPendingSpotList.setVisibility(View.VISIBLE);
			mlblPendingSpots.setVisibility(View.VISIBLE);
		}
		mPendingSpotDao.close();
	}

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int what = msg.what;
			switch (what) {
			case Constants.SPOTS_FETCHED:
				spots = (JSONArray) msg.obj;
				try {
					if (spots.length() == 0) {
						mGetStarted.setVisibility(View.VISIBLE);
					} else {
						adapter.setSpots(SpotHelper.getFromJsonArray(spots));
						mSpotList.setVisibility(View.VISIBLE);
						adapter.notifyDataSetChanged();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mGifLoader.setVisibility(View.GONE);
				break;
			case Constants.USER_META_FETCHED:
				JSONObject meta = (JSONObject) msg.obj;
				updateUserInfo(meta);
				break;
			case Constants.SPOTS_FETCH_FAILED:
				mGifLoader.setVisibility(View.GONE);
				Toast.makeText(getActivity(),
						getResources().getString(R.string.spot_fetch_failed),
						Toast.LENGTH_LONG).show();
			case Constants.NO_INTERNET:
				mImgNoInternet.setVisibility(View.VISIBLE);
				mGifLoader.setVisibility(View.GONE);

			}
		}
	};

	private void updateUserInfo(JSONObject meta) {
		try {
			mTxtFollowers.setText(meta.getString("followers"));
			mTxtFollowing.setText(meta.getString("following"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.lblFollowers
				|| v.getId() == R.id.lblFollowersTitle) {
			String value = (String) mTxtFollowers.getText();
			if (value != null) {
				try {
					int val = Integer.parseInt(value);
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME,
								"[Profile Activity]Followers val: " + val);
					if (val > 0) {
						openList(FollowFollowingActivity.FOLLOWERS);
					}
				} catch (NumberFormatException e) {
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME,
								"[Profile Activity]Followers numberformatexception: "
										+ value);
				}
			}
		} else if (v.getId() == R.id.lblFollowing
				|| v.getId() == R.id.lblFollowingTitle) {
			String value = (String) mTxtFollowing.getText();
			if (value != null) {
				try {
					int val = Integer.parseInt(value);
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME,
								"[Profile Activity]Following val: " + val);
					if (val > 0) {
						openList(FollowFollowingActivity.FOLLOWING);
					}
				} catch (NumberFormatException e) {
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME,
								"[Profile Activity]Following numberformatexception: "
										+ value);
				}
			}
		}

	}
	
	private void openList(int requestType){
		Intent intent = new Intent(getActivity(), FollowFollowingActivity.class);
		intent.putExtra(FollowFollowingActivity.REQUEST_TYPE, requestType);
		long id = Long.parseLong(Util.getGlobalPreferences().getString(Constants.USER_ID, "-1"));
		intent.putExtra(Constants.USER_ID, id);
		startActivity(intent);
	}

}
