package com.vrocketz.spotchu.views.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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
import com.vrocketz.spotchu.helper.Util;

public class MySpotsListAdapter extends BaseAdapter{
	
	private JSONArray spots;
	private LayoutInflater mLayoutInflater;
	private Context context;
	private Animation mAnim;
	
	public MySpotsListAdapter(Context c){
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		spots = new JSONArray();
		mAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
	}
	
	public MySpotsListAdapter(Context c, JSONArray spots){
		this.spots = spots;
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		mAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
	}
	
	public void setSpots(JSONArray spots){
		this.spots = spots;
	}

	@Override
	public int getCount() {
		return spots.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return spots.get(position);
		} catch (JSONException e) {
			if (Config.DEBUG)
				e.printStackTrace();
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position + 1;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		if (convertView == null){
			view = mLayoutInflater.inflate(R.layout.my_spots_list_item, parent, false);
			holder = new ViewHolder();
			holder.img = (ImageView)view.findViewById(R.id.imgSpot);
			holder.title = (TextView)view.findViewById(R.id.lblSpotTitle);
			holder.time = (TextView)view.findViewById(R.id.lblSpotTime);
			holder.hi5Count = (TextView) view.findViewById(R.id.lblHi5Count);
			holder.commentCount = (TextView) view.findViewById(R.id.lblCommentCount);
			holder.delete = (ImageButton) view.findViewById(R.id.btnDeleteSpot);
			view.setTag(holder);
		}else {
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}
		
		try {
			JSONObject spot = (JSONObject) spots.get(pos);
		    ImageAware imageAware = new ImageViewAware(holder.img, false);
		    ImageLoader.getInstance().displayImage(spot.getString("img"), imageAware);
			holder.title.setText(spot.getString("desc"));
			holder.time.setText(Util.getPrintableTimeFormat(spot.getLong("created_at")));
			holder.hi5Count.setText(spot.getString("no_of_likes"));
			holder.commentCount.setText(spot.getString("no_of_comments"));
			//setOnClickListner(holder.delete, pos);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
	/*private void setOnClickListner(ImageButton image, final int pos){
		image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(final View view) {
				 mAnim.setAnimationListener(new Animation.AnimationListener() {              
					 
	                @Override
			         public void onAnimationStart(Animation animation) {
			                   view.setHasTransientState(true);
			         }
			
			                @Override
			         public void onAnimationRepeat(Animation animation) {}
			
			        @Override
			        public void onAnimationEnd(Animation animation) {
			                   spots.remove(pos);
			                   view.setHasTransientState(false);
			       }
			     });
				 view.startAnimation(mAnim);
			}
		});
	}*/
	
	private class ViewHolder {
		public ImageView img;
		public TextView title, time, hi5Count, commentCount;
		public ImageButton delete;
	}


}
