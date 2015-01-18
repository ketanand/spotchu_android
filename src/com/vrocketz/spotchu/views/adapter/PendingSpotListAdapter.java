package com.vrocketz.spotchu.views.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.NotificationService;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.activity.ViewSpot;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.DeleteSpot;
import com.vrocketz.spotchu.spot.PendingSpotDao;
import com.vrocketz.spotchu.spot.Spot;

public class PendingSpotListAdapter extends BaseAdapter {
	
	private ArrayList<Spot> mSpots;
	private LayoutInflater mLayoutInflater;
	private Context context;
	private Animation mAnim;
	
	public PendingSpotListAdapter(Context c){
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		mSpots = new ArrayList<Spot>();
		mAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
	}
	
	public PendingSpotListAdapter(Context c, ArrayList<Spot> spots){
		this.mSpots = spots;
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		mAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
	}
	
	public void setSpots(ArrayList<Spot> spots){
		this.mSpots = spots;
	}

	@Override
	public int getCount() {
		return mSpots.size();
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
			view = mLayoutInflater.inflate(R.layout.pending_spots_list_item, parent, false);
			holder = new ViewHolder();
			holder.img = (ImageButton)view.findViewById(R.id.imgPendingSpot);
			holder.title = (TextView)view.findViewById(R.id.lblPendingSpotTitle);
			holder.time = (TextView)view.findViewById(R.id.lblPendingSpotTime);
			holder.delete = (ImageButton) view.findViewById(R.id.btnDeletePendingSpot);
			view.setTag(holder);
		}else {
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}
		
			final Spot spot =  mSpots.get(pos);
		    holder.img.setImageURI(Uri.parse(spot.getImg()));
		    String desc = spot.getDesc();
		    if (desc.length() > Constants.MAX_TITLE_SIZE){
		    	StringBuilder s = new StringBuilder(desc.subSequence(0, Constants.MAX_TITLE_SIZE));
		    	s.append("...");
		    	desc = s.toString();
		    }
			holder.title.setText(Util.boldHashTags(desc));
			holder.time.setText(Util.getPrintableTimeFormat(spot.getCreatedAt()));
			setDeleteOnClickListner(holder.delete, view, pos, this);
			/*holder.img.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME, "[Image Button Clicked] Spot ID:" + spot.getId());
					Intent intent = new Intent(Util.getApp(), ViewSpot.class);
					intent.putExtra(NotificationService.SPOT_ID, spot.getId());
					context.startActivity(intent);
				}
			});*/
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
							PendingSpotDao dao = new PendingSpotDao(context);
				        	dao.open();
				        	dao.deleteSpotById(mSpots.get(pos).getId());
				        	dao.close();
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
				        	PendingSpotDao dao = new PendingSpotDao(context);
				        	dao.open();
				        	dao.deleteSpotById(mSpots.get(pos).getId());
				        	dao.close();
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
		public TextView title, time;
		public ImageButton img, delete;
	}


}
