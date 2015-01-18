package com.vrocketz.spotchu.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.FullScreenSpotActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.GetMySpots;
import com.vrocketz.spotchu.spot.PendingSpotDao;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;
import com.vrocketz.spotchu.spot.SpotSQLiteHelper;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.MySpotsListAdapter;
import com.vrocketz.spotchu.views.adapter.PendingSpotListAdapter;

public class MySpotsFragment extends Fragment{
	
	private ImageView mDisplayPic;
	private TextView mUserId, mlblPendingSpots;
	private JSONArray spots;
	private ListView mSpotList;
	private MySpotsListAdapter adapter;
	private PendingSpotDao mPendingSpotDao;
	private ListView mPendingSpotList;
	private PendingSpotListAdapter pendingAdapter;
	private AnimatedGifImageView mGifLoader;
	private ImageView mImgNoInternet;
	
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
		if(Config.DEBUG)
			Log.d(Constants.APP_NAME, "[My Spots] DP uri:" + uri.toString());
		mDisplayPic.setImageURI(uri);
		mUserId = (TextView)v.findViewById(R.id.lblUserId);
		mUserId.setText(pref.getString(Constants.USER_NAME, ""));
		//TODO : show spinner.
		mSpotList = (ListView) v.findViewById(R.id.lstMySpots);
		mGifLoader = (AnimatedGifImageView) v.findViewById(R.id.gifLoader);
		mGifLoader.setAnimatedGif(R.raw.loader,	AnimatedGifImageView.TYPE.FIT_CENTER);
		mImgNoInternet = (ImageView) v.findViewById(R.id.imgNoInternet);
		if (adapter == null){
			adapter = new MySpotsListAdapter(getActivity());
		}else {
			mGifLoader.setVisibility(View.GONE);
			mSpotList.setVisibility(View.VISIBLE);
		}
		mSpotList.setAdapter(adapter);
		
		new Thread(new GetMySpots(mHandler)).start();
		//Pending listview
		initPendingSpotList(v);
		return v;
	}
	
	private void initPendingSpotList(View v){
		mPendingSpotDao = new PendingSpotDao(getActivity());
		mPendingSpotDao.open();
		ArrayList<Spot> spots = (ArrayList<Spot>) mPendingSpotDao.getAllSpots();
		if (spots.size() > 0){
			 
			 
			  mPendingSpotList = (ListView) v.findViewById(R.id.lstPendingSpots);
			  pendingAdapter = new PendingSpotListAdapter(getActivity(), spots);
			  mPendingSpotList.setAdapter(pendingAdapter);
			  mlblPendingSpots = (TextView) v.findViewById(R.id.lblPendingSpots);
			  
			  mPendingSpotList.setVisibility(View.VISIBLE);
			  mlblPendingSpots.setVisibility(View.VISIBLE);
		}
		
	}
	
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
			case Constants.SPOTS_FETCHED:
				spots = (JSONArray) msg.obj;
				try {
					adapter.setSpots(SpotHelper.getFromJsonArray(spots));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mGifLoader.setVisibility(View.GONE);
				mSpotList.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				break;
			case Constants.SPOTS_FETCH_FAILED:
				mGifLoader.setVisibility(View.GONE);
				Toast.makeText(getActivity(), getResources().getString(R.string.spot_fetch_failed), Toast.LENGTH_LONG).show();
			case Constants.NO_INTERNET:
				mImgNoInternet.setVisibility(View.VISIBLE);
				mGifLoader.setVisibility(View.GONE);
				
			}
		}
	};

}
