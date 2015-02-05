package com.vrocketz.spotchu.activity.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.MyCircleAdapter;

public class MyCircleFragment extends Fragment {
	
	 private RecyclerView mRecyclerView;
	 private RecyclerView.Adapter mAdapter;
	 private RecyclerView.LayoutManager mLayoutManager;
	 private ArrayList<Spot> mSpots;
	 
	 @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.my_circle, container, false);
		mRecyclerView = (RecyclerView)v.findViewById(R.id.myCircleRecyclerView);
		mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyCircleAdapter(mSpots);
        mRecyclerView.setAdapter(mAdapter);
        
        return v;
	}


}
