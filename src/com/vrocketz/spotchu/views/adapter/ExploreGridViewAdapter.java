package com.vrocketz.spotchu.views.adapter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.spot.Spot;

public class ExploreGridViewAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Spot> mSpots;
	private final LayoutInflater mLayoutInflater;
	private final Random mRandom;
	private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

	public ExploreGridViewAdapter(Context context, int textViewResourceId,
			ArrayList<Spot> objects) {
		this.mLayoutInflater = LayoutInflater.from(context);
		this.mRandom = new Random();
		mSpots = objects;
		this.context = context;
	}

	@Override
	public Spot getItem(int position) {
		return mSpots.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		DynamicHeightImageView picture;
		ImageView userImg, imgHi5;
		TextView name, commentCount, hi5Count;

		if (v == null) {
			v = mLayoutInflater.inflate(R.layout.explore_grid_item, parent,
					false);
			v.setTag(R.id.gridImage, v.findViewById(R.id.gridImage));
			// v.setTag(R.id.gridImageText, v.findViewById(R.id.gridImageText));
			v.setTag(R.id.lblCommentCount, v.findViewById(R.id.lblCommentCount));
			v.setTag(R.id.lblHi5Count, v.findViewById(R.id.lblHi5Count));
			v.setTag(R.id.imgUser, v.findViewById(R.id.imgUser));

			// v.setTag(R.id.gridImageCorner,
			// v.findViewById(R.id.gridImageCorner));
		}
		// v.setTag(R.id.imgHi5, v.findViewById(R.id.imgHi5));
		picture = (DynamicHeightImageView) v.getTag(R.id.gridImage);
		// name = (TextView)v.getTag(R.id.gridImageText);
		commentCount = (TextView) v.getTag(R.id.lblCommentCount);
		hi5Count = (TextView) v.getTag(R.id.lblHi5Count);
		userImg = (ImageView) v.getTag(R.id.imgUser);
		imgHi5 = (ImageView) v.getTag(R.id.imgHi5);

		// name.setText(mSpots.getJSONObject(position).getString("name"));
		Spot spot = getItem(position);
		commentCount.setText(String.valueOf(spot.getNoOfComments()));
		hi5Count.setText(String.valueOf(spot.getNoOfLikes()));
		if (spot.getName().equalsIgnoreCase("anonymous")){
			userImg.setImageResource(R.drawable.default_photo);
		}else {
			ImageAware imageAware = new ImageViewAware(userImg, false);
			ImageLoader.getInstance().displayImage(spot.getImageUrl(), imageAware);
		}
		double positionHeight = getPositionRatio(position);
		picture.setHeightRatio(positionHeight);
		ImageAware imagePicAware = new ImageViewAware(picture, false);
		ImageLoader.getInstance().displayImage(spot.getImg(), imagePicAware);
		/*
		 * if (!spot.isNull("selfHi5Id")){
		 * imgHi5.setImageResource(R.drawable.hi5_trans_red); }
		 */

		return v;
	}

	private double getPositionRatio(final int position) {
		double ratio = sPositionHeightRatios.get(position, 0.0);
		// if not yet done generate and stash the columns height
		// in our real world scenario this will be determined by
		// some match based on the known height and width of the image
		// and maybe a helpful way to get the column height!
		if (ratio == 0) {
			ratio = getRandomHeightRatio();
			sPositionHeightRatios.append(position, ratio);
		}
		return ratio;
	}

	private double getRandomHeightRatio() {
		return (mRandom.nextDouble() / 2.0) + 1.0; // height will be 1.0 - 1.5
													// the width
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mSpots.size();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public void addAll(ArrayList<Spot> list){
		mSpots.addAll(list);
	}

}
