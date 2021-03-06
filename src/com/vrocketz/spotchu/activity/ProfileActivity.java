package com.vrocketz.spotchu.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.runnables.Follow;
import com.vrocketz.spotchu.runnables.GetMySpots;
import com.vrocketz.spotchu.runnables.GetUserMeta;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.MySpotsListAdapter;

public class ProfileActivity extends FragmentActivity implements
		View.OnClickListener {

	private ImageView mUserPic, mNoInternet;
	private TextView mTxtUserName, mTxtFollowers, mTxtFollowing, mTxtLiveSpots,
			mLblFollowers, mLblFollowing, mTxtNoSpots;
	private Long mUserId;
	private String mImagePath;
	private String mUserName;
	private boolean mIsFollowed;
	private ArrayList<Spot> mSpots;
	private ListView mSpotList;
	private MySpotsListAdapter mAdapter;
	private AnimatedGifImageView mGifLoader;
	private Button mBtnFollow;
	private ProgressBar mProgressFollow;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.profile);
		Intent intent = getIntent();
		mNoInternet = (ImageView) findViewById(R.id.imgNoInternet);
		mTxtNoSpots = (TextView) findViewById(R.id.txtNoSpots);
		mUserPic = (ImageView) findViewById(R.id.imgUserDisplayPic);
		mTxtFollowers = (TextView) findViewById(R.id.lblFollowers);
		mTxtFollowers.setOnClickListener(this);
		mTxtFollowing = (TextView) findViewById(R.id.lblFollowing);
		mTxtFollowing.setOnClickListener(this);
		mLblFollowers = (TextView) findViewById(R.id.lblFollowersTitle);
		mLblFollowers.setOnClickListener(this);
		mLblFollowing = (TextView) findViewById(R.id.lblFollowingTitle);
		mLblFollowing.setOnClickListener(this);
		mTxtLiveSpots = (TextView) findViewById(R.id.lblOldSpots);
		mTxtUserName = (TextView) findViewById(R.id.lblUserName);
		mUserName = intent.getStringExtra(Constants.USER_NAME);
		mTxtUserName.setText(mUserName);

		ImageAware imageAwareUserPic = new ImageViewAware(mUserPic, false);
		mImagePath = intent.getStringExtra(Constants.USER_IMG_URL);
		ImageLoader.getInstance().displayImage(mImagePath, imageAwareUserPic);
		mUserPic.setOnClickListener(this);

		mUserId = intent.getLongExtra(Constants.USER_ID, 0);
		if (mUserId != 0) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Profile] UserID :" + mUserId);
			getUserMetaData();
			getUserSpots();
		}

		// Follow Button
		mBtnFollow = (Button) findViewById(R.id.btnFollow);
		mBtnFollow.setVisibility(View.GONE);
		mBtnFollow.setOnClickListener(this);

		mProgressFollow = (ProgressBar) findViewById(R.id.progressBarFollow);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int what = msg.what;
			switch (what) {
			case Constants.SPOTS_FETCHED:
				JSONArray spots = (JSONArray) msg.obj;
				try {
					if (spots.length() == 0) {
						mTxtNoSpots.setVisibility(View.VISIBLE);
						mTxtLiveSpots.setVisibility(View.GONE);
					} else {
						mSpots = SpotHelper.getFromJsonArray(spots);
						mAdapter.setSpots(mSpots);
						mSpotList.setVisibility(View.VISIBLE);
						mTxtLiveSpots.setVisibility(View.VISIBLE);
						mAdapter.notifyDataSetChanged();
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
			case Constants.USER_FOLLOWED:
				mIsFollowed = !mIsFollowed;
				updateFollowButton(mIsFollowed);
				break;
			case Constants.NO_INTERNET:
			case Constants.USER_META_FAILED:
			case Constants.SPOTS_FETCH_FAILED:
			case Constants.USER_FOLLOW_FAILED:
				handleFailure(what);
				break;
			}
		}
	};

	private void getUserSpots() {
		mGifLoader = (AnimatedGifImageView) findViewById(R.id.gifLoader);
		mGifLoader.setAnimatedGif(R.raw.loader,
				AnimatedGifImageView.TYPE.FIT_CENTER);
		mSpotList = (ListView) findViewById(R.id.lstMySpots);
		mAdapter = new MySpotsListAdapter(this);
		mAdapter.showDelete(false);
		mAdapter.showEdit(false);
		mSpotList.setAdapter(mAdapter);
		new Thread(new GetMySpots(mHandler, mUserId)).start();
	}

	private void getUserMetaData() {
		new Thread(new GetUserMeta(mHandler, mUserId)).start();
	}

	private void followUser() {
		new Thread(new Follow(mHandler, mUserId, mUserName)).start();
	}

	private void updateFollowButton(boolean isFollowed) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Profile] UpdateFollowButton : "
					+ isFollowed);
		mBtnFollow.setEnabled(true);
		mBtnFollow.setVisibility(View.VISIBLE);
		mProgressFollow.setVisibility(View.GONE);
		if (isFollowed) {
			mBtnFollow.setBackgroundResource(R.drawable.red_button);
			String text = "- " + getResources().getString(R.string.following);
			mBtnFollow.setText(text);
			mBtnFollow.setTextColor(getResources().getColor(R.color.white));
		} else {
			mBtnFollow
					.setBackgroundResource(R.drawable.white_button_red_border);
			String text = "+ " + getResources().getString(R.string.follow);
			mBtnFollow.setText(text);
			mBtnFollow.setTextColor(getResources()
					.getColor(R.color.theme_color));
		}
	}

	private void updateUserInfo(JSONObject meta) {
		try {
			mTxtFollowers.setText(meta.getString("followers"));
			mTxtFollowing.setText(meta.getString("following"));
			if (meta.getInt("selfFollowing") == 0) {
				mIsFollowed = false;
			} else {
				mIsFollowed = true;
			}
			updateFollowButton(mIsFollowed);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void handleFailure(int what) {
		switch (what) {
		case Constants.NO_INTERNET:
			mNoInternet.setVisibility(View.VISIBLE);
			break;
		case Constants.SPOTS_FETCH_FAILED:
			break;
		case Constants.USER_FOLLOW_FAILED:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Profile ] onClick : " + v.getId());
		if (v.getId() == R.id.btnFollow) {
			mBtnFollow.setEnabled(false);
			mBtnFollow
					.setBackgroundResource(R.drawable.white_button_grey_border);
			mBtnFollow.setTextColor(getResources().getColor(
					R.color.activity_background));
			mProgressFollow.setVisibility(View.VISIBLE);
			followUser();
		} else if (v.getId() == R.id.lblFollowers
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
		} else if (v.getId() == R.id.imgUserDisplayPic) {
			openFullScreenDP(mImagePath);
		}
	}

	private void openList(int requestType) {
		Intent intent = new Intent(this, FollowFollowingActivity.class);
		intent.putExtra(FollowFollowingActivity.REQUEST_TYPE, requestType);
		intent.putExtra(Constants.USER_ID, mUserId);
		startActivity(intent);
	}

	private void openFullScreenDP(String path) {
		Intent intent = new Intent(this, ProfilePicActivity.class);
		intent.putExtra(ProfilePicActivity.IMAGE_URL, path);
		startActivity(intent);
	}

}
