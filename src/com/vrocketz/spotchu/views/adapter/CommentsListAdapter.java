package com.vrocketz.spotchu.views.adapter;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.DeleteComment;
import com.vrocketz.spotchu.spot.comment.Comment;

public class CommentsListAdapter extends BaseAdapter {

	private List<Comment> mComments;
	private LayoutInflater mLayoutInflater;
	private Context mContext;
	private Long mCurrentUserId;
	private Handler mHandler;
	private Animation mAnim;

	public CommentsListAdapter(Context c, List<Comment> comments,
			Handler handler) {
		mComments = comments;
		mContext = c;
		if (c != null) {
			mLayoutInflater = LayoutInflater.from(c);
			mCurrentUserId = Long.parseLong(Util.getGlobalPreferences()
					.getString(Constants.USER_ID, "-1L"));
			mHandler = handler;
			mAnim = AnimationUtils.loadAnimation(c, R.anim.fade_out);
		}
	}

	public void addComments(List<Comment> comments) {
		mComments.addAll(comments);
	}

	@Override
	public int getCount() {
		return mComments.size();
	}

	@Override
	public Object getItem(int pos) {
		int position = getCount() - 1 - pos;
		return mComments.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos + 1;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		final View view;
		ViewHolder holder;
		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.comments_list_item, parent,
					false);
			holder = new ViewHolder();
			holder.img = (ImageView) view.findViewById(R.id.imgUserProfilePic);
			holder.userName = (TextView) view.findViewById(R.id.lblUserName);
			holder.comment = (TextView) view.findViewById(R.id.lblComment);
			holder.time = (TextView) view.findViewById(R.id.lblCommentTime);
			holder.delete = (ImageButton) view
					.findViewById(R.id.btnDeleteComment);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		final int position = getCount() - 1 - pos;
		Comment comment = mComments.get(position);
		ImageAware imageAware = new ImageViewAware(holder.img, false);
		ImageLoader.getInstance().displayImage(comment.getUserDp(), imageAware);
		holder.userName.setText(comment.getUserName());
		holder.comment.setText(comment.getComment());
		holder.time
				.setText(Util.getPrintableTimeFormat(comment.getCreatedAt()));
		Long userId = comment.getUserId();
		if (mCurrentUserId == userId) {
			final Long id = comment.getId();
			holder.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View deleteButton) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						mAnim.setAnimationListener(new Animation.AnimationListener() {

							@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
							@Override
							public void onAnimationStart(Animation animation) {
								view.setHasTransientState(true);
							}

							@Override
							public void onAnimationRepeat(Animation animation) {

							}

							@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
							@Override
							public void onAnimationEnd(Animation animation) {
								if (Config.DEBUG)
									Log.d(Constants.APP_NAME,
											"[Comment Delete Button Animation Ends] delete thread started.");
								new Thread(new DeleteComment(id, mHandler))
										.start();
								mComments.remove(position);
								notifyDataSetChanged();
								view.setHasTransientState(false);
							}
						});
					} else {
						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								new Thread(new DeleteComment(id, mHandler))
										.start();
								mComments.remove(position);
								notifyDataSetChanged();
								mAnim.cancel();
							}
						}, 3000);
					}
					view.startAnimation(mAnim);
				}
			});
		} else {
			holder.delete.setVisibility(View.GONE);
		}

		return view;
	}

	private class ViewHolder {
		public ImageView img;
		public ImageButton delete;
		public TextView userName, time, comment;
	}

}
