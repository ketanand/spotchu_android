package com.vrocketz.spotchu.views.adapter;

import java.util.ArrayList;

import com.vrocketz.spotchu.spot.Spot;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyCircleAdapter extends RecyclerView.Adapter<MyCircleAdapter.ViewHolder>{
	
	private ArrayList<Spot> mSpots;
	
	public MyCircleAdapter(ArrayList<Spot> spots){
		mSpots = spots;
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// TODO Auto-generated method stub
		return null;
	}
}
