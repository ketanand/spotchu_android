package com.vrocketz.spotchu.views.adapter;


import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;

public class ExploreGridViewAdapter extends ArrayAdapter<String>{
	
	private final LayoutInflater mLayoutInflater;
	private final Random mRandom;
	private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();
	private JSONArray mSpots;
	
    public ExploreGridViewAdapter (Context context, int textViewResourceId,
            ArrayList<String> objects) {
    	super(context, textViewResourceId, objects);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mRandom = new Random();
    }
	
	public void setSpots(JSONArray spots){
		mSpots = spots;
	}
    
    @Override
    public String getItem(int position) {
    	String item = super.getItem(position);
    	//Log.d(Constants.APP_NAME, "[ExploreGridAdapter] pos :" + position + ", uri: " + item);
    	return item;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		DynamicHeightImageView picture;
		ImageView userImg, imgHi5;
        TextView name, commentCount, hi5Count;

        if(v == null)
        {
           v = mLayoutInflater.inflate(R.layout.explore_grid_item, parent, false);
           v.setTag(R.id.gridImage, v.findViewById(R.id.gridImage));
           //v.setTag(R.id.gridImageText, v.findViewById(R.id.gridImageText));
           v.setTag(R.id.lblCommentCount, v.findViewById(R.id.lblCommentCount));
           v.setTag(R.id.lblHi5Count, v.findViewById(R.id.lblHi5Count));
           v.setTag(R.id.imgUser, v.findViewById(R.id.imgUser));
          
           //v.setTag(R.id.gridImageCorner, v.findViewById(R.id.gridImageCorner));
        }
        //v.setTag(R.id.imgHi5, v.findViewById(R.id.imgHi5));
        picture = (DynamicHeightImageView)v.getTag(R.id.gridImage);
        //name = (TextView)v.getTag(R.id.gridImageText);
        commentCount = (TextView)v.getTag(R.id.lblCommentCount);
        hi5Count = (TextView)v.getTag(R.id.lblHi5Count);
        userImg = (ImageView)v.getTag(R.id.imgUser);
        imgHi5 = (ImageView)v.getTag(R.id.imgHi5);

        try {
			//name.setText(mSpots.getJSONObject(position).getString("name"));
        	JSONObject spot = mSpots.getJSONObject(position);
			commentCount.setText(spot.getString("no_of_comments"));
			hi5Count.setText(spot.getString("no_of_likes"));
			ImageAware imageAware = new ImageViewAware(userImg, false);
			ImageLoader.getInstance().displayImage(spot.getString("image_url"), imageAware);
			/*if (!spot.isNull("selfHi5Id")){
				imgHi5.setImageResource(R.drawable.hi5_trans_red);
			}*/
        } catch (JSONException e) {
			e.printStackTrace();
		}
        double positionHeight = getPositionRatio(position);
        
        picture.setHeightRatio(positionHeight);
        ImageAware imageAware = new ImageViewAware(picture, false);
        ImageLoader.getInstance().displayImage(getItem(position), imageAware);
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
	
}
