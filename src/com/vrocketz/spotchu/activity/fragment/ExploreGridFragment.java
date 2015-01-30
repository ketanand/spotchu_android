package com.vrocketz.spotchu.activity.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.StaggeredGridView;
import android.support.v4.widget.StaggeredGridView.OnItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshStaggeredGridView;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.FullScreenSpotActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.GetSpots;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.ExploreGridViewAdapter;

public class ExploreGridFragment extends Fragment implements
		AbsListView.OnScrollListener, OnItemClickListener {

	public static final String SAVED_DATA_KEY = "saved_data";
	public static final String SAVED_SPOTS_KEY = "saved_spots";
	public static final String SAVED_PAGE_NUMBER = "saved_page_number";
	public static final String SAVED_START_TIME = "saved_start_time";
	private PullToRefreshStaggeredGridView mGridView;
	//private ProgressBar mProgressBar;
	private AnimatedGifImageView mLoaderGif;
	private ImageView mNoInternet;
	private boolean mHasRequestedMore;
	private ExploreGridViewAdapter mAdapter;
	private JSONArray mSpotsJson;
	private ArrayList<Spot> mSpots;
	private Integer mFrom;
	private long mStartTime;
	
	/*private StaggeredGridView.OnLoadMoreListener loadMoreListener = new StaggeredGridView.OnLoadMoreListener() {

	    @Override
	    public boolean onLoadMore() {
	        //loading.setVisibility(View.VISIBLE);
	        // load more data from internet (not in the UI thread)
	    	if (Config.DEBUG)
	    		Log.d(Constants.APP_NAME, "[Staggered Grid View] on Load more");
	    	getNextPage();
	        return true; // true if you have more data to load, false you dont have more data to load 
	    }
	};*/
	
	/*private void doneLoading() {
	    mGridView.getRefreshableView().loadMoreCompleated();
	    //loading.setVisibility(View.GONE);
	}*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.explore, container, false);
		//if (mGridView != null){
		//mProgressBar  = (ProgressBar)v.findViewById(R.id.progressBarFetchSpot);
		mLoaderGif = (AnimatedGifImageView) v.findViewById(R.id.gifLoader);
		mLoaderGif.setAnimatedGif(R.raw.loader,	AnimatedGifImageView.TYPE.FIT_CENTER);
		mNoInternet = (ImageView)v.findViewById(R.id.imgNoInternet);
			mGridView = (PullToRefreshStaggeredGridView) v
					.findViewById(R.id.exploreGridView);
			mGridView
					.setOnRefreshListener(new OnRefreshListener<StaggeredGridView>() {
						@Override
						public void onRefresh(
								PullToRefreshBase<StaggeredGridView> refreshView) {
							if (Config.DEBUG)
								Log.d(Constants.APP_NAME,
										"[ExploreGridFragment] gridview Refreshed. ");
							refershSpots();
						}
					});
		//}
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] onCreateView ");
		if (mAdapter != null) {
			mGridView.getRefreshableView().setAdapter(mAdapter);
		} else {
			showStartOverlay();
			mFrom = 0;
			mStartTime = Util.getTimeInMilliseconds();
			getNextPage();
		}
		mGridView.getRefreshableView().setOnScrollListener(this);
		mGridView.getRefreshableView().setOnItemClickListener(this);
		//mGridView.getRefreshableView().setOnLoadMoreListener(loadMoreListener);
		return v;
	}

	private void showStartOverlay() {
		// TODO : create OverLay.
		mLoaderGif.setVisibility(View.VISIBLE);
		mGridView.setVisibility(View.GONE);
	}
	
	private void hideOverlay(){
		mLoaderGif.setVisibility(View.GONE);
		mGridView.setVisibility(View.VISIBLE);
	}

	private void showLoadMore() {
		// TODO : create loader
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initGridView(ArrayList<Spot> spots) {
		mAdapter = new ExploreGridViewAdapter(getActivity(),
				android.R.layout.simple_list_item_1, spots);
		mAdapter.setNotifyOnChange(false);
		mGridView.getRefreshableView().setAdapter(mAdapter);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		/*
		 * if (mSpots != null){ outState.putStringArrayList(SAVED_DATA_KEY,
		 * mData); outState.putString(SAVED_SPOTS_KEY, mSpots.toString());
		 * outState.putLong(SAVED_START_TIME, mStartTime);
		 * outState.putInt(SAVED_PAGE_NUMBER, mFrom); }
		 */
		super.onSaveInstanceState(outState);
	}

	/*
	 * @Override public void onItemClick(AdapterView<?> adapterView, View view,
	 * int position, long id) { Toast.makeText(getActivity(),
	 * "[ExploreGridFragment] Item Clicked: " + position,
	 * Toast.LENGTH_SHORT).show(); Intent i = new Intent(getActivity(),
	 * FullScreenSpotActivity.class); i.putExtra(Constants.SPOT_ID, position);
	 * i.putExtra(Constants.SPOTS, mSpots.toString());
	 * getActivity().startActivity(i); }
	 */

	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {
		if (!mHasRequestedMore) {
			int lastInScreen = firstVisibleItem + visibleItemCount + 4;//TODO: test this number
			if (lastInScreen >= totalItemCount) {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[ExploreGridFragment] onScroll lastInScreen - so load more");
				mHasRequestedMore = true;
				getNextPage();
			}
		}
	}

	private void onLoadMoreItems(JSONArray newSpots) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] onLoadMoreItems.");
		if (mAdapter == null) {
			try {
				mSpots = SpotHelper.getFromJsonArray(newSpots);
				mSpotsJson = newSpots;
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[ExploreGridFragment] onLoadMoreItems. Adapter Null"
									+ ", data size:" + newSpots.length());
				if (Config.DEBUG)
					if (mSpots == null)
					Log.d(Constants.APP_NAME,
							"[ExploreGridFragment] onLoadMoreItems. mSpots Null");
				initGridView(mSpots);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[ExploreGridFragment] onLoadMoreItems. Adapter Notified"
									+ ", datasize:" + newSpots.length()
									+ ", Adapter size:" + mAdapter.getCount());
				// notify the adapter that we can update now
				for (int i = 0; i < newSpots.length(); i++) {
					mSpotsJson.put(newSpots.get(i));
				}
				ArrayList<Spot> spotList = SpotHelper
						.getFromJsonArray(newSpots);
				//mSpots.addAll(spotList);
				mAdapter.addAll(spotList);
				mAdapter.notifyDataSetChanged();
				//doneLoading();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (newSpots.length() != 0) {
			mFrom += newSpots.length();
			mHasRequestedMore = false;
		}
	}

	private void getNextPage() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ExploreGridFragment] getNext From : "
					+ mFrom);
		new Thread(new GetSpots(mHandler, mFrom, mStartTime)).start();
	}

	private void refershSpots() {
		mFrom = 0;
		mStartTime = Util.getTimeInMilliseconds();
		mAdapter = null;
		getNextPage();
	}

	@Override
	public void onScrollStateChanged(final AbsListView view,
			final int scrollState) {

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int what = msg.what;
			switch (what) {
			case Constants.SPOTS_FETCHED:
				onLoadMoreItems((JSONArray) msg.obj);
				mGridView.onRefreshComplete();
				hideOverlay();
				break;
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

	@Override
	public void onItemClick(StaggeredGridView parent, View view, int position,
			long id) {
		Intent i = new Intent(getActivity(), FullScreenSpotActivity.class);
		i.putExtra(Constants.SPOT_ID, position);
		i.putExtra(Constants.SPOTS, mSpotsJson.toString());
		getActivity().startActivity(i);

	}

}
