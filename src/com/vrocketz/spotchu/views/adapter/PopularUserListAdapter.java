package com.vrocketz.spotchu.views.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.runnables.Follow;

public class PopularUserListAdapter extends BaseAdapter{
	
	private JSONArray mUsers;
	private LayoutInflater mLayoutInflater;
	private Context context;
	
	public PopularUserListAdapter(Context c, JSONArray users){
		mUsers = users;
		context = c;
		mLayoutInflater = LayoutInflater.from(c);
		for (int i = 0; i < mUsers.length(); i++){
			JSONObject user;
			try {
				user = mUsers.getJSONObject(i);
				user.put("followed", false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getCount() {
		return mUsers.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return mUsers.get(position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		final ViewHolder holder;
		if (convertView == null){
			view = mLayoutInflater.inflate(R.layout.popular_user_list_item, parent, false);
			holder = new ViewHolder();
			holder.userDP = (ImageView)view.findViewById(R.id.imgUserProfilePic);
			holder.userName = (TextView)view.findViewById(R.id.lblUserName);
			holder.btnFollow = (Button)view.findViewById(R.id.btnFollow);
			holder.progressFollow = (ProgressBar) view.findViewById(R.id.progressBarFollow);
			view.setTag(holder);
		}else {
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}
		try {
			final JSONObject user = (JSONObject) mUsers.get(position);
		    ImageAware imageAware = new ImageViewAware(holder.userDP, false);
		    ImageLoader.getInstance().displayImage(user.getString("userImg"), imageAware);
		    final String name = user.getString("userName");
			holder.userName.setText(name);
			if (user.getBoolean("followed")){
				holder.btnFollow.setBackgroundResource(R.drawable.red_button);
				String text = "- " + context.getResources().getString(R.string.following);
				holder.btnFollow.setText(text);
				holder.btnFollow.setTextColor(context.getResources().getColor(R.color.white));
			}else {
				holder.btnFollow.setBackgroundResource(R.drawable.white_button_red_border);
				String text = "+ " + context.getResources().getString(R.string.follow);
				holder.btnFollow.setText(text);
				holder.btnFollow.setTextColor(context.getResources().getColor(R.color.theme_color));
			}
			holder.progressFollow.setVisibility(View.GONE);
			holder.btnFollow.setEnabled(true);
			holder.btnFollow.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					v.setEnabled(false);
					v.setBackgroundResource(R.drawable.white_button_grey_border);
					holder.btnFollow.setTextColor(context.getResources().getColor(R.color.activity_background));
					holder.progressFollow.setVisibility(View.VISIBLE);
					try {
						followUser(user.getLong("userId"), name);
						user.put("followed", !user.getBoolean("followed"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
	private void followUser(Long id, String name){
		new Thread(new Follow(mHandler, id, name)).start();
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
				case Constants.USER_FOLLOWED:
					notifyDataSetChanged();
					break;
				case Constants.NO_INTERNET:	
				case Constants.USER_FOLLOW_FAILED:
					break;
			}
		}
	};
	
	private class ViewHolder {
		public ImageView userDP;
		public TextView userName;
		public Button btnFollow;
		public ProgressBar progressFollow;
	}

}
