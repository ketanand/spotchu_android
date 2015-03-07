package com.vrocketz.spotchu.activity.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.RecyclerListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.FullScreenSpotActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.GetPopularUsers;
import com.vrocketz.spotchu.runnables.GetSpots;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.ExploreGridViewAdapter;
import com.vrocketz.spotchu.views.adapter.MyCircleAdapter;
import com.vrocketz.spotchu.views.adapter.PopularUserListAdapter;

public class MyCircleFragment extends Fragment {

	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private ListView mPopularUsersList;
	private PopularUserListAdapter mPopularUsersAdapter;
	private LinearLayout mPopularUserLayout;
	private Button mBtnDone;
	private ArrayList<Spot> mSpots;
	private JSONArray mSpotsJson;
	private Integer mFrom;
	private long mStartTime;
	private AnimatedGifImageView mLoaderGif;
	private ImageView mNoInternet;
	private boolean mHasRequestedMore;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.my_circle, container, false);
		
		mLoaderGif = (AnimatedGifImageView) v.findViewById(R.id.gifLoader);
		mLoaderGif.setAnimatedGif(R.raw.loader,	AnimatedGifImageView.TYPE.FIT_CENTER);
		mNoInternet = (ImageView)v.findViewById(R.id.imgNoInternet);
		mRecyclerView = (RecyclerView) v
				.findViewById(R.id.myCircleRecyclerView);
		mRecyclerView.setHasFixedSize(true);

		// use a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		((LinearLayoutManager) mLayoutManager)
				.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mLayoutManager);

		if (mAdapter != null) {
			mRecyclerView.setAdapter(mAdapter);
			if (mSpots.size() == 0){
				mFrom = 0;
				mStartTime = Util.getTimeInMilliseconds();
				showStartOverlay();
				getNextPage();
			}
		} else {
			mFrom = 0;
			mStartTime = Util.getTimeInMilliseconds();
			showStartOverlay();
			getNextPage();
		}
		mRecyclerView.setOnScrollListener(mScrollListener);
		mRecyclerView.addOnItemTouchListener(
			    new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
			      @Override public void onItemClick(View view, int position) {
			    	Intent i = new Intent(getActivity(), FullScreenSpotActivity.class);
			  		i.putExtra(Constants.SPOT_ID, position);
			  		i.putExtra(Constants.SPOTS, mSpotsJson.toString());
			  		getActivity().startActivity(i);
			      }
			    })
			);
		
		//Init Popular User layout references.
		mPopularUserLayout = (LinearLayout) v.findViewById(R.id.layoutPopularUsers);
		mPopularUsersList = (ListView) v.findViewById(R.id.lstPopularUsers);
		mBtnDone = (Button) v.findViewById(R.id.btnDone);
		return v;
	}
	
	RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
	    @Override
	    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

	            int visibleItemCount = mLayoutManager.getChildCount();
	            int totalItemCount = mLayoutManager.getItemCount();
	            int firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
	            
	            if (!mHasRequestedMore) {
	    			int lastInScreen = firstVisibleItem + visibleItemCount + 4;//TODO: test this number
	    			if (lastInScreen >= totalItemCount) {
	    				if (Config.DEBUG)
	    					Log.d(Constants.APP_NAME,
	    							"[MyCircleFragment] onScroll lastInScreen - so load more");
	    				mHasRequestedMore = true;
	    				getNextPage();
	    			}
	    		}
	     }
	};

	private void showStartOverlay() {
		// TODO : create OverLay.
		mLoaderGif.setVisibility(View.VISIBLE);
		mRecyclerView.setVisibility(View.GONE);
	}

	private void hideOverlay() {
		mLoaderGif.setVisibility(View.GONE);
		mRecyclerView.setVisibility(View.VISIBLE);
	}
	
	private void getNextPage() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] getNext From : "
					+ mFrom);
		new Thread(new GetSpots(mHandler, mFrom, mStartTime, true)).start();
	}
	
	private void refershSpots() {
		mFrom = 0;
		mStartTime = Util.getTimeInMilliseconds();
		mAdapter = null;
		getNextPage();
	}
	
	private void onLoadMoreItems(JSONArray newSpots) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[MyCircleFragment] onLoadMoreItems.");
		if (mAdapter == null) {
			try {
				mSpots = SpotHelper.getFromJsonArray(newSpots);
				mSpotsJson = newSpots;
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[MyCircleFragment] onLoadMoreItems. Adapter Null"
									+ ", data size:" + newSpots.length());
				if (Config.DEBUG)
					if (mSpots == null)
					Log.d(Constants.APP_NAME,
							"[MyCircleFragment] onLoadMoreItems. mSpots Null");
				if (newSpots.length() == 0){
					//Show popular users to follow.
					new Thread(new GetPopularUsers(mHandler)).start();
				}else {
					initRecyclerView(mSpots);
				}	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[MyCircleFragment] onLoadMoreItems. Adapter Notified"
									+ ", datasize:" + newSpots.length()
									+ ", Adapter size:" + mAdapter.getItemCount());
				// notify the adapter that we can update now
				for (int i = 0; i < newSpots.length(); i++) {
					mSpotsJson.put(newSpots.get(i));
				}
				ArrayList<Spot> spotList = SpotHelper
						.getFromJsonArray(newSpots);
				//mSpots.addAll(spotList);
				((MyCircleAdapter) mAdapter).addAll(spotList);
				mAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (newSpots.length() != 0) {
			mFrom += newSpots.length();
			mHasRequestedMore = false;
		}
	}
	
	private void initRecyclerView(ArrayList<Spot> spots) {
		mAdapter = new MyCircleAdapter(mSpots);
		mRecyclerView.setAdapter(mAdapter);
	}
	
	private void initPopularUserLayout(JSONArray users){
		
		mPopularUsersAdapter = new PopularUserListAdapter(getActivity(), users);
		mPopularUsersList.setAdapter(mPopularUsersAdapter);
		mPopularUserLayout.setVisibility(View.VISIBLE);
		mLoaderGif.setVisibility(View.GONE);
		mBtnDone.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				mPopularUserLayout.setVisibility(View.GONE);
				showStartOverlay();
				refershSpots();
			}
			
		});
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int what = msg.what;
			switch (what) {
			case Constants.SPOTS_FETCHED:
				onLoadMoreItems((JSONArray) msg.obj);
				hideOverlay();
				break;
			case Constants.USERS_FETCHED:
				initPopularUserLayout((JSONArray) msg.obj);
				break;
			case Constants.USERS_FETCH_FAILED:	
			case Constants.SPOTS_FETCH_FAILED:
				handleFailure();
				break;
			}
		}

	};

	private void handleFailure() {
		hideOverlay();
		mNoInternet.setVisibility(View.VISIBLE);
	}

}
