package com.vrocketz.spotchu.activity;

import org.json.JSONArray;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.runnables.GetFollowers;
import com.vrocketz.spotchu.runnables.GetFollowing;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.UserListAdapter;

public class FollowFollowingActivity extends FragmentActivity {
	
	public static final Integer FOLLOWERS = 1;
	public static final Integer FOLLOWING = 2;
	public static final String REQUEST_TYPE = "request_type";
	private ListView mUserList;
	private UserListAdapter mAdapter;
	private Integer mUserId;
	private Integer mRequestType;
	private AnimatedGifImageView mGifLoader;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.user_list);
		mUserId = getIntent().getIntExtra(Constants.USER_ID, 0);
		mRequestType = getIntent().getIntExtra(REQUEST_TYPE, -1);
		mGifLoader = (AnimatedGifImageView)findViewById(R.id.gifLoader);
		mGifLoader.setAnimatedGif(R.raw.loader,	AnimatedGifImageView.TYPE.FIT_CENTER);
		mUserList = (ListView) findViewById(R.id.lstUsers);
		fetchDetails();
		overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
	}
	
	private void fetchDetails(){
		if (mRequestType == FOLLOWERS){
			fetchFollowersList();
		}else if (mRequestType == FOLLOWING){
			fetchFollowingList();
		}
	}
	
	private void fetchFollowersList(){
		new Thread(new GetFollowers(mHandler, mUserId)).start();
	}
	
	private void fetchFollowingList(){
		new Thread(new GetFollowing(mHandler, mUserId)).start();
	}
	
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
				case Constants.USER_FOLLOWERS_FETCHED:
					JSONArray result = (JSONArray) msg.obj;
					initFollowList(result);
					break;
				case Constants.USER_FOLLOWERS_FAILED:
				case Constants.NO_INTERNET:
					handleFailure(what);
					break;
			}
			mGifLoader.setVisibility(View.GONE);
		}
	};
	
	private void initFollowList(JSONArray users){
		mAdapter = new UserListAdapter(this, users, mRequestType);
		mUserList.setAdapter(mAdapter);
		mUserList.setVisibility(View.VISIBLE);
	}
	
	private void handleFailure(int what){
		switch(what){
		case Constants.USER_FOLLOWERS_FAILED:
			Toast.makeText(this, getResources().getString(R.string.operation_failed), Toast.LENGTH_LONG).show();
			break;
		case Constants.NO_INTERNET:
			Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
			break;
		}
		mGifLoader.setVisibility(View.GONE);
	}
}
