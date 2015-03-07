package com.vrocketz.spotchu.views.adapter;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.spot.Spot;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MyCircleAdapter extends RecyclerView.Adapter<MyCircleAdapter.ViewHolder>{
	
	private ArrayList<Spot> mSpots;
	public static final Integer MAX_TAG_COUNT = 2;
	
	public MyCircleAdapter(ArrayList<Spot> spots){
		mSpots = spots;
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
		public ImageView mSpotImg, mUserImg;
        public TextView mTxtHi5 , mTxtComment, mTxtTags;
        public ViewHolder(View v) {
            super(v);
            mSpotImg = (ImageView) v.findViewById(R.id.cardImage);
            mUserImg = (ImageView) v.findViewById(R.id.imgUser);
            mTxtHi5 = (TextView) v.findViewById(R.id.lblHi5Count);
            mTxtComment = (TextView) v.findViewById(R.id.lblCommentCount);
            mTxtTags = (TextView) v.findViewById(R.id.lblSpotTags);
        }
    }

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return mSpots.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Spot spot = mSpots.get(position);
		holder.mTxtComment.setText(String.valueOf(spot.getNoOfComments()));
		holder.mTxtHi5.setText(String.valueOf(spot.getNoOfLikes()));
		if (spot.getName().equalsIgnoreCase("anonymous")){
			holder.mUserImg.setImageResource(R.drawable.default_photo);
		}else {
			ImageAware imageAware = new ImageViewAware(holder.mUserImg, false);
			ImageLoader.getInstance().displayImage(spot.getImageUrl(), imageAware);
		}
		ImageAware imagePicAware = new ImageViewAware(holder.mSpotImg, false);
		ImageLoader.getInstance().displayImage(spot.getImg(), imagePicAware);
		String tags = Util.getTagsFromTitle(spot.getDesc());
		String[] tagsArr = tags.split(" ");
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < tagsArr.length && i < MAX_TAG_COUNT; i++){
			sb.append(tagsArr[i]).append(" ");
		}
		holder.mTxtTags.setText(Util.boldHashTags(sb.toString()));
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.my_circle_list_item, parent, false);
		return new ViewHolder(itemView);
	}
	
	public void addAll(ArrayList<Spot> spots){
		for(Spot spot : spots){
			mSpots.add(spot);
		}
	}
}
