package com.vrocketz.spotchu.views.adapter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
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
import com.vrocketz.spotchu.runnables.DeleteSpot;
import com.vrocketz.spotchu.spot.Spot;

public class MySpotsListAdapter extends BaseAdapter{
	
	private ArrayList<Spot> mSpots;
	private LayoutInflater mLayoutInflater;
	private Context context;
	private Animation mAnim;
	
	public MySpotsListAdapter(Context c){
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		mSpots = new ArrayList<Spot>();
		mAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
	}
	
	public MySpotsListAdapter(Context c, ArrayList<Spot> spots){
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
		
			Spot spot =  mSpots.get(pos);
		    ImageAware imageAware = new ImageViewAware(holder.img, false);
		    ImageLoader.getInstance().displayImage(spot.getImg(), imageAware);
			holder.title.setText(spot.getDesc());
			holder.time.setText(Util.getPrintableTimeFormat(spot.getCreatedAt()));
			holder.hi5Count.setText(String.valueOf(spot.getNoOfLikes()));
			holder.commentCount.setText(String.valueOf(spot.getNoOfComments()));
			setOnClickListner(holder.delete, view, pos, this);
		
		return view;
	}
	
	private void setOnClickListner(ImageButton image, final View animView, final int pos, final BaseAdapter adapter){
		image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(final View view) {
			    animView.startAnimation(mAnim);
			    Handler handle = new Handler();
			    handle.postDelayed(new Runnable() {

			        @Override
			        public void run() {
			            // TODO Auto-generated method stub
			        	new Thread(new DeleteSpot(null, mSpots.get(pos).getId()));
						mSpots.remove(pos);
			            adapter.notifyDataSetChanged();
			            mAnim.cancel();
			        }
			    }, 1000);
			}
		});
	}
	
	private class ViewHolder {
		public ImageView img;
		public TextView title, time, hi5Count, commentCount;
		public ImageButton delete;
	}


}
