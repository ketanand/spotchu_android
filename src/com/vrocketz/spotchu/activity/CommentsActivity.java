package com.vrocketz.spotchu.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.Comment;
import com.vrocketz.spotchu.runnables.GetComments;
import com.vrocketz.spotchu.views.adapter.CommentsListAdapter;

public class CommentsActivity extends FragmentActivity implements OnClickListener {
	
	EditText mText;
	ListView mCommentsList;
	ImageButton mLoadMore;
	ProgressBar mProgressBar;
	ProgressBar mPostProgressBar;
	int mSpotId;
	private CommentsListAdapter mAdapter;
    private JSONArray mComments;
    private Integer mFrom;
    private long mStartTime;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		mSpotId = getIntent().getIntExtra("spot", -1);
		if (mSpotId == -1){
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Comments Activity] id : " + mSpotId);
			this.finish();
		}
		setContentView(R.layout.comments);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Button btnComment = (Button)findViewById(R.id.btnPostComment);
		btnComment.setOnClickListener(this);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBarFetchComment);
		mPostProgressBar = (ProgressBar)findViewById(R.id.progressBarPostComment);
		mText = (EditText)findViewById(R.id.txtComment);
		mCommentsList = (ListView)findViewById(R.id.lstSpotComments);
		mLoadMore = (ImageButton)findViewById(R.id.btnLoadMoreComments);
		fetchCompleteComments();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btnPostComment){
			String text = mText.getText().toString();
			if (text.length() > 0){
				hideSoftKeyboard();
				mText.setText("");
				new Thread(new Comment(mSpotId, text, mHandler)).start();
				showPostCommentLoader();
			}else {
				Toast.makeText(this, getResources().getString(R.string.required_comment), Toast.LENGTH_LONG).show();
			}
			//TODO : add to adapter in posting state.
		}else if (view.getId() == R.id.btnLoadMoreComments){
			new Thread(new GetComments(mHandler, mSpotId, mFrom, mStartTime)).start();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        //NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
				case Constants.COMMENT_POSTED:
					mAdapter = null;
					fetchCompleteComments();
					break;
				case Constants.COMMENT_POST_FAILED:
					hidePostCommentLoader();
					handleFailure();
					break;
				case Constants.COMMENTS_FETCHED:
					JSONObject result = (JSONObject) msg.obj;
					try {
						onLoadMoreItems(result.getJSONArray("comments"));
						toggleLoadMoreButton(result.getInt("total"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				case Constants.COMMENTS_FETCH_FAILED:
					handleFailure();
			}
		}
	};
	
	public void hideSoftKeyboard() {
	    if(getCurrentFocus()!=null) {
	        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	    }
	}
	
	private void fetchCompleteComments(){
		mFrom = 0;
    	mStartTime = Util.getTimeInMilliseconds();
		new Thread(new GetComments(mHandler, mSpotId, mFrom, mStartTime)).start();
	}
	
	private void showPostCommentLoader(){
		mPostProgressBar.setVisibility(View.VISIBLE);
	}
	
	private void hidePostCommentLoader(){
		mPostProgressBar.setVisibility(View.GONE);
	}
	
	private void toggleLoadMoreButton(int total){
		if (total > mComments.length()){
			mLoadMore.setVisibility(View.VISIBLE);
		}else {
			mLoadMore.setVisibility(View.GONE);
		}
	}
	
	private void handleFailure(){
		Toast.makeText(this, getResources().getString(R.string.operation_failed), Toast.LENGTH_LONG).show(); 
	}
	
	private void onLoadMoreItems(JSONArray newComments) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[CommentsActivity] onLoadMoreItems.");
		if (mAdapter == null){
			mComments = newComments;
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[CommentsActivity] onLoadMoreItems. Adapter Null" +
						", data size:" + newComments.length());
			initListView();
		}else {
			mAdapter.addComments(newComments);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[CommentsActivity] onLoadMoreItems. Adapter Notified" +
						", datasize:" + newComments.length() + ", Adapter size:" + mAdapter.getCount());
			// notify the adapter that we can update now
	        mAdapter.notifyDataSetChanged();
		}
		if (newComments.length() != 0){
			mFrom += mComments.length();
		}
    }
	
	private void initListView(){
		mAdapter = new CommentsListAdapter(this, mComments);
		mCommentsList.setAdapter(mAdapter);
		mCommentsList.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		hidePostCommentLoader();
	}

}
