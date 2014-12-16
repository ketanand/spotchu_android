package com.vrocketz.spotchu.activity.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.FullScreenSpotActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.GetSpots;
import com.vrocketz.spotchu.views.adapter.ExploreGridViewAdapter;

public class ExploreGridFragment extends Fragment implements AbsListView.OnScrollListener, AbsListView.OnItemClickListener{
	
	private ArrayList<String> mData;
	public static final String SAVED_DATA_KEY = "saved_data";
	public static final String SAVED_SPOTS_KEY = "saved_spots";
	public static final String SAVED_PAGE_NUMBER = "saved_page_number";
	public static final String SAVED_START_TIME = "saved_start_time";
    private StaggeredGridView mGridView;
    private boolean mHasRequestedMore;
    private ExploreGridViewAdapter mAdapter;
    private JSONArray mSpots;
    private Integer mFrom;
    private long mStartTime;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.explore, container, false);
		mGridView = (StaggeredGridView) v.findViewById(R.id.gridview);
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] onCreateView ");
		if (mAdapter != null){
	    	mGridView.setAdapter(mAdapter);
	    }else if (savedInstanceState != null) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[ExploreGridFragment] onCreateView savedInstanceState");
			mData = savedInstanceState.getStringArrayList(SAVED_DATA_KEY);
			mFrom = savedInstanceState.getInt(SAVED_PAGE_NUMBER);
			try {
				String spotsJson = savedInstanceState.getString(SAVED_SPOTS_KEY);
				if (spotsJson != null){
					mSpots = new JSONArray(spotsJson);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mStartTime = savedInstanceState.getLong(SAVED_START_TIME);
			if (mData != null){
				initGridView(mData);
			}else {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[ExploreGridFragment] onCreateView mData null");
				showStartOverlay();
				mFrom = 0;
		    	mStartTime = Util.getTimeInMilliseconds();
				getNextPage();
			}
	    }else {
	    	showStartOverlay();
	    	mFrom = 0;
	    	mStartTime = Util.getTimeInMilliseconds();
	    	getNextPage();
	    }
	    mGridView.setOnScrollListener(this);
	    mGridView.setOnItemClickListener(this);
		return v;
	}
	
	private void showStartOverlay(){
		//TODO : create OverLay.
	}
	
	private void showLoadMore(){
		//TODO : create loader
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	private void initGridView(ArrayList<String> images){
		mData = images;
		mAdapter = new ExploreGridViewAdapter(getActivity(), android.R.layout.simple_list_item_1, mData);
		mAdapter.setSpots(mSpots);
		mGridView.setAdapter(mAdapter);
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] initGridView , Adapter Size: " + mAdapter.getCount());
	}
	
	@Override
	public void onSaveInstanceState(final Bundle outState) {
		 if (mSpots != null){
		     outState.putStringArrayList(SAVED_DATA_KEY, mData);
		     outState.putString(SAVED_SPOTS_KEY, mSpots.toString());
		     outState.putLong(SAVED_START_TIME, mStartTime);
		     outState.putInt(SAVED_PAGE_NUMBER, mFrom);
		 }
		 super.onSaveInstanceState(outState);
	}
	

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Toast.makeText(getActivity(), "[ExploreGridFragment] Item Clicked: " + position, Toast.LENGTH_SHORT).show();
		Intent i = new Intent(getActivity(), FullScreenSpotActivity.class);
        i.putExtra(Constants.SPOT_ID, position);
        i.putExtra(Constants.SPOTS, mSpots.toString());
        getActivity().startActivity(i);
	}

	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
		/*if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] onScroll firstVisibleItem:" + firstVisibleItem +
                " visibleItemCount:" + visibleItemCount +
                " totalItemCount:" + totalItemCount);*/
		// our handling
		if (!mHasRequestedMore) {
			int lastInScreen = firstVisibleItem + visibleItemCount;
			if (lastInScreen >= totalItemCount) {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[ExploreGridFragment] onScroll lastInScreen - so load more");
			    mHasRequestedMore = true;
			    getNextPage();
			}
		}
	}
	
	private void onLoadMoreItems(JSONArray newSpots) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] onLoadMoreItems.");
		if (mAdapter == null){
			mSpots = newSpots;
			mData = new ArrayList<String>();
			for (int i = 0; i < newSpots.length(); i++){
				try {
					JSONObject spot = newSpots.getJSONObject(i);
					mData.add(spot.getString("img"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[ExploreGridFragment] onLoadMoreItems. Adapter Null" +
						", data size:" + newSpots.length());
			initGridView(mData);
		}else {
			for (int i = 0; i < newSpots.length(); i++){
				try {
					JSONObject spot = newSpots.getJSONObject(i);
					//mAdapter.add(spot.getString("img"));
					mData.add(spot.getString("img"));
					mSpots.put(spot);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[ExploreGridFragment] onLoadMoreItems. Adapter Notified" +
						", datasize:" + newSpots.length() + ", Adapter size:" + mAdapter.getCount());
			// notify the adapter that we can update now
			mAdapter.setSpots(mSpots);
	        mAdapter.notifyDataSetChanged();
		}
		if (newSpots.length() != 0){
			mFrom += newSpots.length();
	        mHasRequestedMore = false;
		}
    }
	
	private void getNextPage(){
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] getNext From : " + mFrom);
		new Thread(new GetSpots(mHandler, mFrom, mStartTime)).start();
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		//Log.d(Constants.APP_NAME, "[ExploreGridFragment] onScrollStateChanged:" + scrollState);
	}
	
	private Handler mHandler = new Handler(){
			public void handleMessage(Message msg){
				final int what = msg.what;
				switch(what){
					case Constants.SPOTS_FETCHED:
						onLoadMoreItems((JSONArray) msg.obj);
						break;
						
					case Constants.SPOTS_FETCH_FAILED:
						handleFailure();
						break;
				}
			}
			
	};
	
	private void handleFailure(){
		
	}

}
