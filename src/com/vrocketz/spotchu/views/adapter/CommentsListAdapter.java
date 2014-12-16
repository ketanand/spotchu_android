package com.vrocketz.spotchu.views.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentsListAdapter extends BaseAdapter {

	JSONArray mComments;
	LayoutInflater mLayoutInflater;
	Context mContext;
	
	public CommentsListAdapter(Context c ,JSONArray comments){
		mComments = comments;
		mContext = c;
		mLayoutInflater = LayoutInflater.from(c);
	}
	
	public void addComments(JSONArray comments){
		for (int i = 0; i < comments.length(); i++){
			try {
				JSONObject comment = comments.getJSONObject(i);
				mComments.put(comment);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int getCount() {
		return mComments.length();
	}

	@Override
	public Object getItem(int pos) {
		try {
			return mComments.get(pos);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int pos) {
		return pos + 1;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		if (convertView == null){
			view = mLayoutInflater.inflate(R.layout.comments_list_item, parent, false);
			holder = new ViewHolder();
			holder.img = (ImageView)view.findViewById(R.id.imgUserProfilePic);
			holder.userName = (TextView)view.findViewById(R.id.lblUserName);
			holder.comment = (TextView)view.findViewById(R.id.lblComment);
			holder.time = (TextView)view.findViewById(R.id.lblCommentTime);
			view.setTag(holder);
		}else {
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}
		
		try {
			JSONObject comment = (JSONObject) mComments.get(pos);
		    ImageAware imageAware = new ImageViewAware(holder.img, false);
		    ImageLoader.getInstance().displayImage(comment.getString("user_dp"), imageAware);
			holder.userName.setText(comment.getString("user_name"));
			holder.comment.setText(comment.getString("comment"));
			holder.time.setText(Util.getPrintableTimeFormat(comment.getLong("created_at")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
	private class ViewHolder {
		public ImageView img;
		public TextView userName, time, comment;
	}

}
