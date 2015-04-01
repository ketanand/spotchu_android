package com.vrocketz.spotchu.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.vrocketz.spotchu.runnables.UpdateSpot;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;

public class ViewSpot extends Activity implements OnClickListener {

	ProgressBar mProgressBar;
	Spot spot;
	ImageView imgDisplay, imgUserPic;
	TextView lblTitle, lblUserName, lblNoOfLikes, lblNoOfComments;
	EditText txtTitle;
	ImageButton btnLike, btnComment, btnShare, btnEdit;
	View background;
	Context context;
	private Long mCurrentUserId;
	private int mLikes;
	private boolean areControlsVisible;
	private boolean mEditMode;

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
			Log.d(Constants.APP_NAME, "[ViewSpot] action:" + action + ", uri: "
					+ uri);
		Long id = 0L;
		if (action != null && action.equals(Intent.ACTION_VIEW)) {
			List<String> path = uri.getPathSegments();
			if (path.size() == 3) {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[ViewSpot] path:" + path.get(2));
				id = Long.parseLong(path.get(2));
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				stackBuilder.addParentStack(ViewSpot.class);
			} else {
				Intent i = new Intent(this, Summary.class);
				i.putExtra(NotificationService.SUMMARY_URL, uri.toString());
				startActivity(i);
				finish();
			}

		} else {
			id = getIntent().getLongExtra(NotificationService.SPOT_ID, 0);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[ViewSpot] id:" + id);
		}
		new Thread(new GetSpotById(mHandler, id)).start();
		areControlsVisible = true;
		mCurrentUserId = Long.parseLong(Util.getGlobalPreferences().getString(
				Constants.USER_ID, "-1L"));
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
		imgDisplay.setOnClickListener(this);

		lblTitle = (TextView) this.findViewById(R.id.lblSpotTitle);
		lblTitle.setText(Util.boldHashTags(spot.getDesc()));
		lblUserName = (TextView) this.findViewById(R.id.lblUserName);
		final String userName = spot.getName();
		lblUserName.setText(userName);
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

					Long userId = spot.getUserId();
					String imgUrl = spot.getImageUrl();
					openProfilePage(userId, userName, imgUrl);

				}
			};
			imgUserPic.setOnClickListener(clickListener);
			lblUserName.setOnClickListener(clickListener);
		}
		lblNoOfComments = (TextView) findViewById(R.id.lblNoOfComment);
		final int comments = spot.getNoOfComments();
		lblNoOfComments.setText(comments + " "
				+ getResources().getString(R.string.comment_v));
		mLikes = spot.getNoOfLikes();
		lblNoOfLikes = (TextView) findViewById(R.id.lblNoOfLike);
		lblNoOfLikes.setText(mLikes + " "
				+ getResources().getString(R.string.hi5_v));
		lblNoOfLikes.setOnClickListener(this);
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
					mLikes++;
					lblNoOfLikes.setText(mLikes + " "
							+ getResources().getString(R.string.hi5_v));

				} else {
					img.setImageDrawable(getResources().getDrawable(
							R.drawable.hi5));
					mLikes--;
					lblNoOfLikes.setText(mLikes + " "
							+ getResources().getString(R.string.hi5_v));
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

		btnEdit = (ImageButton) findViewById(R.id.btnEditSpot);
		btnEdit.setOnClickListener(this);
		if (mCurrentUserId == spot.getUserId()) {
			btnEdit.setVisibility(View.VISIBLE);
		}
		txtTitle = (EditText) findViewById(R.id.txtSpotTitle);
		txtTitle.addTextChangedListener(mTitleTextWatcher);
		txtTitle.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME,
							"[PostSpotActivity] Done Button Clicked , action id "
									+ actionId);
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					doUpdate();
					mEditMode = !mEditMode;
					return true;
				}
				return false;
			}
		});
		background = findViewById(R.id.bottomBackground);
	}

	private boolean updateSpot() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		String desc = txtTitle.getText().toString();
		if (desc.length() == 0) {
			Toast.makeText(this,
					getResources().getString(R.string.required_desc),
					Toast.LENGTH_LONG).show();
			return false;
		}
		nameValuePairs.add(new BasicNameValuePair("desc", desc));
		nameValuePairs.add(new BasicNameValuePair("tags", Util
				.getTagsFromTitle(desc)));
		UpdateSpot instance = new UpdateSpot(spot.getId(), mHandler,
				nameValuePairs);
		Thread t = new Thread(instance);
		t.start();
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Update Spot Runnable called");
		return true;

	}

	private void openProfilePage(Long userId, String name, String userPic) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra(Constants.USER_NAME, name);
		intent.putExtra(Constants.USER_ID, userId);
		intent.putExtra(Constants.USER_IMG_URL, userPic);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.lblNoOfLike) {
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[View Spot Activity]Hi5 List val: "
						+ mLikes);
			if (mLikes > 0) {
				openList(FollowFollowingActivity.HI5);
			}
		} else if (v.getId() == R.id.imgDisplay) {
			if (areControlsVisible) {
				hideControls();
			} else {
				showControls();
			}
			areControlsVisible = !areControlsVisible;
		} else if (v.getId() == R.id.btnEditSpot) {
			if (!mEditMode) {
				txtTitle.setText(lblTitle.getText());
				lblTitle.setVisibility(View.GONE);
				txtTitle.setVisibility(View.VISIBLE);
				btnEdit.setImageResource(R.drawable.button_done);
				txtTitle.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(txtTitle, InputMethodManager.SHOW_IMPLICIT);
			} else {
				doUpdate();
			}
			mEditMode = !mEditMode;
		}
	}
	
	private void doUpdate(){
		btnEdit.setImageResource(R.drawable.button_edit);
		updateSpot();
		lblTitle.setVisibility(View.VISIBLE);
		txtTitle.setVisibility(View.GONE);
		lblTitle.setText(Util.boldHashTags(txtTitle.getText()
				.toString()));
		hideKeyboard();
	}

	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(txtTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void openList(int requestType) {
		Intent intent = new Intent(this, FollowFollowingActivity.class);
		intent.putExtra(FollowFollowingActivity.REQUEST_TYPE, requestType);
		intent.putExtra(Constants.USER_ID, spot.getId());
		startActivity(intent);
	}

	private void hideControls() {
		imgUserPic.setVisibility(View.GONE);
		lblTitle.setVisibility(View.GONE);
		lblUserName.setVisibility(View.GONE);
		lblNoOfLikes.setVisibility(View.GONE);
		lblNoOfComments.setVisibility(View.GONE);
		btnLike.setVisibility(View.GONE);
		btnComment.setVisibility(View.GONE);
		btnShare.setVisibility(View.GONE);
		btnEdit.setVisibility(View.GONE);
		background.setVisibility(View.GONE);
	}

	private void showControls() {
		imgUserPic.setVisibility(View.VISIBLE);
		lblTitle.setVisibility(View.VISIBLE);
		lblUserName.setVisibility(View.VISIBLE);
		lblNoOfLikes.setVisibility(View.VISIBLE);
		lblNoOfComments.setVisibility(View.VISIBLE);
		btnLike.setVisibility(View.VISIBLE);
		btnComment.setVisibility(View.VISIBLE);
		btnShare.setVisibility(View.VISIBLE);
		if (mCurrentUserId == spot.getUserId()) {
			btnEdit.setVisibility(View.VISIBLE);
		}
		background.setVisibility(View.VISIBLE);
	}

	private TextWatcher mTitleTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			String caption = s.toString();
			int lastHashIndex = caption.lastIndexOf("#");
			int end;
			if (lastHashIndex >= 0) {
				end = caption.indexOf(" ", lastHashIndex);
				if (end == -1) {
					end = s.length();
				}
				s.setSpan(new StyleSpan(Typeface.BOLD), lastHashIndex, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	};

}
