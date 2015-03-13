package com.vrocketz.spotchu.activity;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
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
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.GetSpotById;
import com.vrocketz.spotchu.runnables.Like;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;

public class ViewSpot extends Activity {

	ProgressBar mProgressBar;
	Spot spot;
	ImageView imgDisplay, imgUserPic;
	TextView lblTitle, lblUserName, lblNoOfLikes, lblNoOfComments;
	ImageButton btnLike, btnComment, btnShare;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen_spinner);
		context = this;
		mProgressBar = (ProgressBar) findViewById(R.id.progressBarFetchSpot);
		mProgressBar.setVisibility(View.VISIBLE);
		Intent intent = getIntent();
		String action = intent.getAction();
		Uri uri = intent.getData();
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ViewSpot] action:" + action + ", uri: " + uri);
		int id = 0;
		if (action != null && action.equals(Intent.ACTION_VIEW)){
			List<String> path = uri.getPathSegments();
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[ViewSpot] path:" + path.get(2));
			if (path.size() > 1) {
				id = Integer.parseInt(path.get(2));
			}
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		    stackBuilder.addParentStack(ViewSpot.class);
		}else {
			id = getIntent().getIntExtra(NotificationService.SPOT_ID, 0);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[ViewSpot] id:" + id);
		}	
		new Thread(new GetSpotById(mHandler, id)).start();
	}

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int what = msg.what;
			switch (what) {
			case Constants.SPOTS_FETCHED:
				try {
					spot = SpotHelper.getFromJson((JSONObject) msg.obj);
					initView();
				} catch (JSONException e) {
					e.printStackTrace();
					mProgressBar.setVisibility(View.GONE);
					// TODO : display some oops graphic
				}
				break;
			case Constants.SPOTS_FETCH_FAILED:
				mProgressBar.setVisibility(View.GONE);
				// TODO : display some oops graphic
			}
		}
	};

	private void initView() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[ViewSpot] InitView");
		mProgressBar.setVisibility(View.GONE);
		setContentView(R.layout.fullscreen_image);
		imgDisplay = (ImageView) this.findViewById(R.id.imgDisplay);
		ImageAware imageAware = new ImageViewAware(imgDisplay, false);
		ImageLoader.getInstance().displayImage(spot.getImg(), imageAware);

		lblTitle = (TextView) this.findViewById(R.id.lblSpotTitle);
		lblTitle.setText(Util.boldHashTags(spot.getDesc()));
		lblUserName = (TextView) this.findViewById(R.id.lblUserName);
		final String userName = spot.getName();
		lblUserName.setText(userName);
		// Init User Images.
		imgUserPic = (ImageView) this.findViewById(R.id.imgUserPic);
		// Init User Images.
		imgUserPic = (ImageView) this.findViewById(R.id.imgUserPic);
		if (userName.equalsIgnoreCase("anonymous")) {
			imgUserPic.setImageResource(R.drawable.default_photo);
		} else {
			ImageAware imageAwareUserPic = new ImageViewAware(imgUserPic, false);
			ImageLoader.getInstance().displayImage(spot.getImageUrl(),
					imageAwareUserPic);
			View.OnClickListener clickListener = new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					
					Integer userId = spot.getUserId(); 
					String imgUrl = spot.getImageUrl();
					openProfilePage(userId, userName, imgUrl);
					
				}
			};
			imgUserPic.setOnClickListener(clickListener);
			lblUserName.setOnClickListener(clickListener);
		}
		btnLike = (ImageButton) this.findViewById(R.id.btnHi5Spot);
		boolean hi5ed = false;
		if (spot.getSelfHi5Id() != null) {
			btnLike.setImageDrawable(getResources().getDrawable(
					R.drawable.hi5_trans_red));
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
				// TODO : toggle drawable.
				ImageButton img = (ImageButton) v;
				clicked = !clicked;
				if (clicked) {
					img.setImageDrawable(getResources().getDrawable(
							R.drawable.hi5_trans_red));
				} else {
					img.setImageDrawable(getResources().getDrawable(
							R.drawable.hi5));
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
				Intent shareIntent = SpotHelper.getShareIntent(spot.getUrl());
				context.startActivity(Intent.createChooser(shareIntent, context
						.getResources().getString(R.string.share_via)));
			}
		});

		lblNoOfComments = (TextView) findViewById(R.id.lblNoOfComment);
		int comments = spot.getNoOfComments();
		lblNoOfComments.setText(comments + " "
				+ getResources().getString(R.string.comment_v));
		int likes = spot.getNoOfLikes();
		lblNoOfLikes = (TextView) findViewById(R.id.lblNoOfLike);
		lblNoOfLikes.setText(likes + " "
				+ getResources().getString(R.string.hi5_v));
	}
	
	private void openProfilePage(Integer userId, String name, String userPic){
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra(Constants.USER_NAME, name);
		intent.putExtra(Constants.USER_ID, userId);
		intent.putExtra(Constants.USER_IMG_URL, userPic);
		startActivity(intent);
	}
	

}
