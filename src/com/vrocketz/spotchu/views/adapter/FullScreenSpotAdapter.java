package com.vrocketz.spotchu.views.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.CommentsActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.runnables.Like;

public class FullScreenSpotAdapter extends PagerAdapter {
	
	private Activity activity;
	private Handler mHandler;
	private JSONArray mSpots;
	
	public FullScreenSpotAdapter(Activity activity, Handler handler, JSONArray spots){
		this.activity = activity;
		this.mHandler = handler;
		this.mSpots = spots;
	}

	@Override
	public int getCount() {
		return mSpots.length();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		// TODO Auto-generated method stub
		return view == ((RelativeLayout)object);
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final int pos = position;
		ImageView imgDisplay, imgUserPic;
		TextView lblTitle, lblUserName;
		ImageButton btnLike, btnComment, btnShare;
		LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.fullscreen_image, container,
                false);
		try {
			JSONObject spot = mSpots.getJSONObject(position);
			final int spotId = spot.getInt("id");
			//Init main Image
			imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
		    ImageAware imageAware = new ImageViewAware(imgDisplay, false);
	        ImageLoader.getInstance().displayImage(spot.getString("img")
	        		.replace("_sm", ""), imageAware);
	        
			lblTitle = (TextView) viewLayout.findViewById(R.id.lblSpotTitle);
			lblTitle.setText(spot.getString("desc"));
			lblUserName = (TextView) viewLayout.findViewById(R.id.lblUserName);
			lblUserName.setText(spot.getString("name"));
			//Init User Images.
			imgUserPic = (ImageView) viewLayout.findViewById(R.id.imgUserPic);
			ImageAware imageAwareUserPic = new ImageViewAware(imgUserPic, false);
			ImageLoader.getInstance().displayImage(spot.getString("image_url")
					, imageAwareUserPic);
			btnLike = (ImageButton) viewLayout.findViewById(R.id.btnHi5Spot);
			boolean hi5ed = false;
			if (!spot.isNull("selfHi5Id")){
				btnLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.hi5_trans_red));
				hi5ed = true;
			}
			final boolean hi5edFinal = hi5ed;
		    btnComment = (ImageButton) viewLayout.findViewById(R.id.btnCommentSpot);
		    btnShare = (ImageButton) viewLayout.findViewById(R.id.btnShareSpot);
		    // like button click event
	        btnLike.setOnClickListener(new View.OnClickListener() { 
	        	
	        	private boolean clicked = hi5edFinal;
	            @Override
	            public void onClick(View v) {
	            	new Thread(new Like(spotId, mHandler)).start();
	            	//TODO : toggle drawable.
	            	ImageButton img = (ImageButton)v;
	            	clicked = !clicked;
	            	if (clicked){
	            		img.setImageDrawable(activity.getResources().getDrawable(R.drawable.hi5_trans_red));
	            	}else {
	            		img.setImageDrawable(activity.getResources().getDrawable(R.drawable.hi5));
	            	}
	            }
	            
	        });
	        btnComment.setOnClickListener(new View.OnClickListener() {   
	        	
	            @Override
	            public void onClick(View v) {
	                Intent i = new Intent(activity, CommentsActivity.class);
	                i.putExtra("spot", spotId);
	                activity.startActivity(i);
	            }
	        });
	        btnShare.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent shareIntent = getShareIntent(pos);
					activity.startActivity(Intent.createChooser(shareIntent,
								activity.getResources().getString(R.string.share_via)));
				}
			});
	        
		} catch (JSONException e) {
			Log.d(Constants.APP_NAME, "[FullScreenSpotAdapter] spot id not found");
			e.printStackTrace();
		}
		((ViewPager) container).addView(viewLayout);
		return viewLayout;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);
	}
	
	private Intent getShareIntent(int position){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		String text = getShareText(position);
		if (text != null){
			sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		}else {
			sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.spot_url_not_found_message));
		}
		sendIntent.setType("text/plain");
		return sendIntent;
	}
	
	private String getShareText(int position){
		StringBuilder share = new StringBuilder();;
		try {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[FullScreenSpotActivity] position: " + 1);
			String url = mSpots.getJSONObject(position).getString("url");
			share.append(activity.getResources().getString(R.string.spot_url_share_message)).append(url);
		}catch(JSONException e){
			Log.d(Constants.APP_NAME, "[FullScreenSpotActivity] getShareText : " + e.getMessage());
		}
		return share.toString();
	}
	

}
