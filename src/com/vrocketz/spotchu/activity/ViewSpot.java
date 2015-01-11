package com.vrocketz.spotchu.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.NotificationService;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.runnables.GetSpotById;
import com.vrocketz.spotchu.runnables.Like;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;

public class ViewSpot extends Activity{
	
	ProgressBar mProgressBar;
	Spot spot;
	ImageView imgDisplay, imgUserPic;
	TextView lblTitle, lblUserName;
	ImageButton btnLike, btnComment, btnShare;
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen_spinner);
		context = this;
		mProgressBar = (ProgressBar) findViewById(R.id.progressBarFetchSpot);
		mProgressBar.setVisibility(View.VISIBLE);
		int id = getIntent().getIntExtra(NotificationService.SPOT_ID, 0);
		new Thread(new GetSpotById(mHandler, id)).start();
	}
	
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
			case Constants.SPOTS_FETCHED:
				try {
					spot = SpotHelper.getFromJson((JSONObject)msg.obj);
					initView();
				} catch (JSONException e) {
					e.printStackTrace();
					mProgressBar.setVisibility(View.GONE);
					//TODO : display some oops graphic
				}
				break;
			case Constants.SPOTS_FETCH_FAILED:
				mProgressBar.setVisibility(View.GONE);
				//TODO : display some oops graphic
			}
		}
	};
	
	private void initView(){
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ViewSpot] InitView");
		mProgressBar.setVisibility(View.GONE);
		setContentView(R.layout.fullscreen_image);
		imgDisplay = (ImageView) this.findViewById(R.id.imgDisplay);
	    ImageAware imageAware = new ImageViewAware(imgDisplay, false);
        ImageLoader.getInstance().displayImage(spot.getImg().replace("_sm", ""), imageAware);
        
		lblTitle = (TextView) this.findViewById(R.id.lblSpotTitle);
		lblTitle.setText(spot.getDesc());
		lblUserName = (TextView) this.findViewById(R.id.lblUserName);
		lblUserName.setText(spot.getName());
		//Init User Images.
		imgUserPic = (ImageView) this.findViewById(R.id.imgUserPic);
		ImageAware imageAwareUserPic = new ImageViewAware(imgUserPic, false);
		ImageLoader.getInstance().displayImage(spot.getImageUrl()
				, imageAwareUserPic);
		btnLike = (ImageButton) this.findViewById(R.id.btnHi5Spot);
		boolean hi5ed = false;
		if (spot.getSelfHi5Id() != null){
			btnLike.setImageDrawable(getResources().getDrawable(R.drawable.hi5_trans_red));
			hi5ed = true;
		}
		final boolean hi5edFinal = hi5ed;
	    btnComment = (ImageButton) this.findViewById(R.id.btnCommentSpot);
	    btnShare = (ImageButton) this.findViewById(R.id.btnShareSpot);
	    // like button click event
        btnLike.setOnClickListener(new View.OnClickListener() { 
        	
        	private boolean clicked = hi5edFinal;
            @Override
            public void onClick(View v) {
            	new Thread(new Like(spot.getId(), mHandler)).start();
            	//TODO : toggle drawable.
            	ImageButton img = (ImageButton)v;
            	clicked = !clicked;
            	if (clicked){
            		img.setImageDrawable(getResources().getDrawable(R.drawable.hi5_trans_red));
            	}else {
            		img.setImageDrawable(getResources().getDrawable(R.drawable.hi5));
            	}
            }
            
        });
        btnComment.setOnClickListener(new View.OnClickListener() {   
        	
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CommentsActivity.class);
                i.putExtra("spot", spot.getId());
                context.startActivity(i);
            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent shareIntent = getShareIntent();
				context.startActivity(Intent.createChooser(shareIntent,
							context.getResources().getString(R.string.share_via)));
			}
		});
	}
	
	private Intent getShareIntent(){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		String text = getShareText();
		if (text != null){
			sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		}else {
			sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.spot_url_not_found_message));
		}
		sendIntent.setType("text/plain");
		return sendIntent;
	}
	
	private String getShareText(){
		StringBuilder share = new StringBuilder();;
		String url = spot.getUrl(); 
		share.append(getResources().getString(R.string.spot_url_share_message)).append(url);
		return share.toString();
	}

}
