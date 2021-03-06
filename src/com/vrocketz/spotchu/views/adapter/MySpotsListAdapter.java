package com.vrocketz.spotchu.views.adapter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.sax.StartElementListener;
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
import com.vrocketz.spotchu.NotificationService;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.PostSpotActivity;
import com.vrocketz.spotchu.activity.ViewSpot;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.DeleteSpot;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;

public class MySpotsListAdapter extends BaseAdapter{
	
	private ArrayList<Spot> mSpots;
	private LayoutInflater mLayoutInflater;
	private Context context;
	private Animation mAnim;
	private boolean mShowDelete;
	private boolean mShowEdit;
	
	public MySpotsListAdapter(Context c){
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		mSpots = new ArrayList<Spot>();
		mAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
		mShowDelete = true;
		mShowEdit = false;
	}
	
	public MySpotsListAdapter(Context c, ArrayList<Spot> spots){
		this.mSpots = spots;
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		mAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
		mShowDelete = true;
		mShowEdit = false;
	}
	
	public void setSpots(ArrayList<Spot> spots){
		this.mSpots = spots;
	}

	@Override
	public int getCount() {
		return mSpots.size();
	}
	
	public void showDelete(boolean val){
		mShowDelete = val;
	}
	
	public void showEdit(boolean val){
		mShowEdit = val;
	}

	@Override
	public Object getItem(int position) {
		return mSpots.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mSpots.get(position).getId();
	}
	
	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return true;//super.isEnabled(position);
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		if (convertView == null){
			view = mLayoutInflater.inflate(R.layout.my_spots_list_item, parent, false);
			holder = new ViewHolder();
			holder.img = (ImageButton)view.findViewById(R.id.imgSpot);
			holder.title = (TextView)view.findViewById(R.id.lblSpotTitle);
			holder.time = (TextView)view.findViewById(R.id.lblSpotTime);
			holder.hi5Count = (TextView) view.findViewById(R.id.lblHi5Count);
			holder.commentCount = (TextView) view.findViewById(R.id.lblCommentCount);
			holder.delete = (ImageButton) view.findViewById(R.id.btnDeleteSpot);
			holder.edit = (ImageButton) view.findViewById(R.id.btnEditSpot);
			view.setTag(holder);
		}else {
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}
		
			final Spot spot =  mSpots.get(pos);
		    ImageAware imageAware = new ImageViewAware(holder.img, false);
		    ImageLoader.getInstance().displayImage(spot.getImg(), imageAware);
		    String desc = spot.getDesc();
		    if (desc.length() > Constants.MAX_TITLE_SIZE){
		    	StringBuilder s = new StringBuilder(desc.subSequence(0, Constants.MAX_TITLE_SIZE));
		    	s.append("...");
		    	desc = s.toString();
		    }
			holder.title.setText(desc);
			holder.time.setText(Util.getPrintableTimeFormat(spot.getCreatedAt()));
			holder.hi5Count.setText(String.valueOf(spot.getNoOfLikes()));
			holder.commentCount.setText(String.valueOf(spot.getNoOfComments()));
			
			if (mShowDelete)
				setDeleteOnClickListner(holder.delete, view, pos, this);
			else 
				holder.delete.setVisibility(View.GONE);
			
			if (mShowEdit){
				holder.edit.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						Intent i = new Intent(context, PostSpotActivity.class);
						i.putExtra(PostSpotActivity.PREVIEW_IMAGE, spot.getImg());
						i.putExtra(SpotHelper.SPOT_ID, spot.getId());
						i.putExtra(SpotHelper.SPOT_DESC, spot.getDesc());
						i.putExtra(PostSpotActivity.UPDATE, true);
						context.startActivity(i);
					}
				});
			}else {
				holder.edit.setVisibility(View.GONE);
			}	
			
			holder.img.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME, "[Image Button Clicked] Spot ID:" + spot.getId());
					Intent intent = new Intent(Util.getApp(), ViewSpot.class);
					intent.putExtra(NotificationService.SPOT_ID, spot.getId());
					context.startActivity(intent);
				}
			});
		return view;
	}
	
	private void setDeleteOnClickListner(ImageButton image, final View animView, final int pos, final BaseAdapter adapter){
		image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(final View view) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
					mAnim.setAnimationListener(new Animation.AnimationListener() {
						
						@SuppressLint("NewApi") @Override
						public void onAnimationStart(Animation animation) {
							animView.setHasTransientState(true);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
							
						}
						
						@SuppressLint("NewApi") @Override
						public void onAnimationEnd(Animation animation) {
							if (Config.DEBUG)
								Log.d(Constants.APP_NAME, "[Delete Button Animation Ends] delete thread started.");
							new Thread(new DeleteSpot(null, mSpots.get(pos).getId())).start();
							mSpots.remove(pos);
				            adapter.notifyDataSetChanged();
				            animView.setHasTransientState(false);
						}
					});
				}else {
					Handler handle = new Handler();
				    handle.postDelayed(new Runnable() {

				        @Override
				        public void run() {
				            // TODO Auto-generated method stub
				        	new Thread(new DeleteSpot(null, mSpots.get(pos).getId())).start();
							mSpots.remove(pos);
				            adapter.notifyDataSetChanged();
				            mAnim.cancel();
				        }
				    }, 3000);
				}
			    animView.startAnimation(mAnim);
			}
		});
	}
	
	private class ViewHolder {
		public TextView title, time, hi5Count, commentCount;
		public ImageButton img, delete, edit;
	}


}
