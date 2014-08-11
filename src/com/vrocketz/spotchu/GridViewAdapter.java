package com.vrocketz.spotchu;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter{
	
	private Context mContext;

    public GridViewAdapter (Context c) {
        mContext = c;
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mThumbIds.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mThumbIds[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        ImageView picture;
        TextView name;

        if(v == null)
        {
           LayoutInflater inflater = LayoutInflater.from(Util.getApplicationContext());
           v = inflater.inflate(R.layout.home_item, parent, false);
           v.setTag(R.id.gridImage, v.findViewById(R.id.gridImage));
           v.setTag(R.id.gridImageText, v.findViewById(R.id.gridImageText));
        }

        picture = (ImageView)v.getTag(R.id.gridImage);
        name = (TextView)v.getTag(R.id.gridImageText);


        picture.setImageResource(mThumbIds[position]);
        name.setText("spotchu");

        return v;
	}
	
	// references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };

}
