package com.vrocketz.spotchu.views.adapter;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.CommentsActivity;
import com.vrocketz.spotchu.activity.FollowFollowingActivity;
import com.vrocketz.spotchu.activity.ProfileActivity;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.Like;
import com.vrocketz.spotchu.runnables.UpdateSpot;
import com.vrocketz.spotchu.spot.SpotHelper;
import com.vrocketz.spotchu.views.SpotchuViewPager;
import com.vrocketz.spotchu.views.TouchImageView;

public class FullScreenSpotAdapter extends PagerAdapter implements TouchImageView.OnZoomChangeListener{

	private Activity activity;
	private Handler mHandler;
	private JSONArray mSpots;
	private SpotchuViewPager mPager;
	private Long mCurrentUserId;

	public FullScreenSpotAdapter(Activity activity, Handler handler,
			JSONArray spots, SpotchuViewPager pager) {
		this.activity = activity;
		this.mHandler = handler;
		this.mSpots = spots;
		this.mPager = pager;
		mCurrentUserId = Long.parseLong(Util.getGlobalPreferences().getString(
				Constants.USER_ID, "-1"));
	}

	@Override
	public int getCount() {
		return mSpots.length();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		// TODO Auto-generated method stub
		return view == ((RelativeLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		TouchImageView imgDisplay;
		final ImageView imgUserPic;
		final TextView lblTitle, lblUserName;
		final TextView lblNoOfLikes;
		final TextView lblNoOfComments;
		final EditText txtTitle;
		final ImageButton btnLike, btnComment, btnShare, btnEdit;
		final View background;
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.fullscreen_image,
				container, false);
		try {
			final JSONObject spot = mSpots.getJSONObject(position);
			final Long spotId = spot.getLong("id");
			final Long userId = spot.getLong("user_id");
			// Init main Image
			imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
			ImageAware imageAware = new ImageViewAware(imgDisplay, false);
			ImageLoader.getInstance().displayImage(
					spot.getString("img").replace(".jpeg", "_or.jpeg"),
					imageAware);
			imgDisplay.setOnZoomChangedListener(this);
			
			lblTitle = (TextView) viewLayout.findViewById(R.id.lblSpotTitle);
			lblTitle.setText(Util.boldHashTags(spot.getString("desc")));
			lblUserName = (TextView) viewLayout.findViewById(R.id.lblUserName);
			final String userName = spot.getString("name");
			lblUserName.setText(userName);
			
			btnEdit = (ImageButton) viewLayout.findViewById(R.id.btnEditSpot);
			txtTitle = (EditText) viewLayout.findViewById(R.id.txtSpotTitle);
			
			background = viewLayout.findViewById(R.id.bottomBackground);
			
			// Init User Images.
			imgUserPic = (ImageView) viewLayout.findViewById(R.id.imgUserPic);
			if (userName.equalsIgnoreCase("anonymous")) {
				imgUserPic.setImageResource(R.drawable.default_photo);
			} else {
				ImageAware imageAwareUserPic = new ImageViewAware(imgUserPic,
						false);
				ImageLoader.getInstance().displayImage(
						spot.getString("image_url"), imageAwareUserPic);
				View.OnClickListener clickListener = new View.OnClickListener() {
					
					@Override
					public void onClick(View view) {
						
						Long userId = null; 
						String imgUrl = null;
						try {
							userId = spot.getLong("user_id");
							imgUrl = spot.getString("image_url");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						openProfilePage(userId, userName, imgUrl);
						
					}
				};
				imgUserPic.setOnClickListener(clickListener);
				lblUserName.setOnClickListener(clickListener);
			}
			
			lblNoOfComments = (TextView) viewLayout
					.findViewById(R.id.lblNoOfComment);
			int comments = spot.getInt("no_of_comments");
			lblNoOfComments.setText(comments + " "
					+ activity.getResources().getString(R.string.comment_v));
			final int likes = spot.getInt("no_of_likes");
			lblNoOfLikes = (TextView) viewLayout.findViewById(R.id.lblNoOfLike);
			lblNoOfLikes.setText(likes + " "
					+ activity.getResources().getString(R.string.hi5_v));
			
			/*
			 * Open list of users who have liked this spot 
			 * on click of hi5 count.
			 */
			lblNoOfLikes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME, "[ViewPager ]Hi5 List val: "
								+ likes);
					if (likes > 0) {
						openList(FollowFollowingActivity.HI5, spotId);
					}
				}
			});
			
			//Init hi5 Button
			btnLike = (ImageButton) viewLayout.findViewById(R.id.btnHi5Spot);
			boolean hi5ed = false;
			if (!spot.isNull("selfHi5Id")) {
				btnLike.setImageDrawable(activity.getResources().getDrawable(
						R.drawable.hi5_trans_red));
				hi5ed = true;
			}
			
			final boolean hi5edFinal = hi5ed;
			
			btnComment = (ImageButton) viewLayout
					.findViewById(R.id.btnCommentSpot);
			
			btnShare = (ImageButton) viewLayout.findViewById(R.id.btnShareSpot);
			
			// like button click event
			btnLike.setOnClickListener(new View.OnClickListener() {

				private boolean clicked = hi5edFinal;
				private int like = likes;

				@Override
				public void onClick(View v) {
					
					new Thread(new Like(spotId, mHandler)).start();
					// TODO : toggle drawable.
					ImageButton img = (ImageButton) v;
					clicked = !clicked;
					if (clicked) {
						img.setImageDrawable(activity.getResources()
								.getDrawable(R.drawable.hi5_trans_red));
						like++;
						lblNoOfLikes.setText(like + " "
								+ activity.getResources().getString(R.string.hi5_v));
					} else {
						like--;
						img.setImageDrawable(activity.getResources()
								.getDrawable(R.drawable.hi5));
						lblNoOfLikes.setText(like + " "
								+ activity.getResources().getString(R.string.hi5_v));
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
					Intent shareIntent;
					try {
						shareIntent = SpotHelper.getShareIntent(spot
								.getString("url"));
						activity.startActivity(Intent.createChooser(
								shareIntent,
								activity.getResources().getString(
										R.string.share_via)));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			
			/**
			 * On click show and hide rest of controls
			 */
			imgDisplay.setOnClickListener(new OnClickListener() {
				private boolean visible = true;
				@Override
				public void onClick(View arg0) {
					if (visible){
						hideControls();
					}else {
						showControls();
					}
					visible = !visible;
				}
				
				private void hideControls(){
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
				
				private void showControls(){
					imgUserPic.setVisibility(View.VISIBLE);
					lblTitle.setVisibility(View.VISIBLE);
					lblUserName.setVisibility(View.VISIBLE);
					lblNoOfLikes.setVisibility(View.VISIBLE);
					lblNoOfComments.setVisibility(View.VISIBLE);
					btnLike.setVisibility(View.VISIBLE); 
					btnComment.setVisibility(View.VISIBLE); 
					btnShare.setVisibility(View.VISIBLE);
					if (mCurrentUserId == userId) {
						btnEdit.setVisibility(View.VISIBLE);
					}
					background.setVisibility(View.VISIBLE);
				}
			});
			
			/**
			 * Defining Listener class here so that view items are accessible.
			 * @author sa
			 *
			 */
			class Listener implements EditText.OnEditorActionListener, View.OnClickListener {
				private boolean mEditMode = false;
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
				
				@Override
				public void onClick(View v) {
					if (!mEditMode) {
						txtTitle.setText(lblTitle.getText());
						lblTitle.setVisibility(View.GONE);
						txtTitle.setVisibility(View.VISIBLE);
						btnEdit.setImageResource(R.drawable.button_done);
						txtTitle.requestFocus();
						InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(txtTitle, InputMethodManager.SHOW_IMPLICIT);
					} else {
						doUpdate();
					}
					mEditMode = !mEditMode;
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
					InputMethodManager inputManager = (InputMethodManager) activity
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(txtTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
				
				private boolean updateSpot() {
					ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					String desc = txtTitle.getText().toString();
					if (desc.length() == 0) {
						Toast.makeText(activity,
								activity.getResources().getString(R.string.required_desc),
								Toast.LENGTH_LONG).show();
						return false;
					}
					nameValuePairs.add(new BasicNameValuePair("desc", desc));
					nameValuePairs.add(new BasicNameValuePair("tags", Util
							.getTagsFromTitle(desc)));
					UpdateSpot instance = new UpdateSpot(spotId, mHandler,
							nameValuePairs);
					Thread t = new Thread(instance);
					t.start();
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME, "Update Spot Runnable called");
					return true;

				}
			}
			
			btnEdit.setOnClickListener(new Listener());
			if (mCurrentUserId == userId) {
				btnEdit.setVisibility(View.VISIBLE);
			}
			txtTitle.addTextChangedListener(mTitleTextWatcher);
			txtTitle.setOnEditorActionListener(new Listener());

		} catch (JSONException e) {
			Log.d(Constants.APP_NAME,
					"[FullScreenSpotAdapter] spot id not found");
			e.printStackTrace();
		}
		((ViewPager) container).addView(viewLayout);
		return viewLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);
	}
	
	private void openProfilePage(Long userId, String name, String userPic){
		Intent intent = new Intent(activity, ProfileActivity.class);
		intent.putExtra(Constants.USER_NAME, name);
		intent.putExtra(Constants.USER_ID, userId);
		intent.putExtra(Constants.USER_IMG_URL, userPic);
		activity.startActivity(intent);
	}

	@Override
	public void onStateChanged(boolean isZoomed) {
		if (mPager != null)
			mPager.setPagingEnabled(!isZoomed);
	}
	
	private void openList(int requestType, Long id){
		Intent intent = new Intent(activity, FollowFollowingActivity.class);
		intent.putExtra(FollowFollowingActivity.REQUEST_TYPE, requestType);
		intent.putExtra(Constants.USER_ID, id);
		activity.startActivity(intent);
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
