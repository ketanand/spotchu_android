package com.vrocketz.spotchu.activity;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.PostComment;
import com.vrocketz.spotchu.runnables.GetComments;
import com.vrocketz.spotchu.spot.comment.Comment;
import com.vrocketz.spotchu.spot.comment.CommentHelper;
import com.vrocketz.spotchu.views.AnimatedGifImageView;
import com.vrocketz.spotchu.views.adapter.CommentsListAdapter;

public class CommentsActivity extends FragmentActivity implements OnClickListener {
	
	EditText mText;
	ListView mCommentsList;
	Button mLoadMore;
	AnimatedGifImageView mGifLoader;
	ProgressBar mPostProgressBar;
	Long mSpotId;
	private CommentsListAdapter mAdapter;
    private List<Comment> mComments;
    private Integer mFrom;
    private long mStartTime;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		mSpotId = getIntent().getLongExtra("spot", -1);
		if (mSpotId == -1){
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[Comments Activity] id : " + mSpotId);
			this.finish();
		}
		setContentView(R.layout.comments);
		getActionBar().hide();
		Button btnComment = (Button)findViewById(R.id.btnPostComment);
		btnComment.setOnClickListener(this);
		mGifLoader = (AnimatedGifImageView)findViewById(R.id.gifLoader);
		mGifLoader.setAnimatedGif(R.raw.loader,	AnimatedGifImageView.TYPE.FIT_CENTER);
		mPostProgressBar = (ProgressBar)findViewById(R.id.progressBarPostComment);
		mText = (EditText)findViewById(R.id.txtComment);
		mCommentsList = (ListView)findViewById(R.id.lstSpotComments);
		mLoadMore = (Button)findViewById(R.id.btnLoadMoreComments);
		mLoadMore.setOnClickListener(this);
		fetchCompleteComments();
		overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btnPostComment){
			String text = mText.getText().toString();
			if (text.length() > 0){
				hideSoftKeyboard();
				mText.setText("");
				new Thread(new PostComment(mSpotId, text, mHandler)).start();
				showPostCommentLoader();
			}else {
				Toast.makeText(this, getResources().getString(R.string.required_comment), Toast.LENGTH_LONG).show();
			}
			//TODO : add to adapter in posting state.
		}else if (view.getId() == R.id.btnLoadMoreComments){
			mLoadMore.setVisibility(View.GONE);
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
						onLoadMoreItems(CommentHelper.getFromJsonArray(result.getJSONArray("comments")));
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
		if (total > mComments.size()){
			mLoadMore.setVisibility(View.VISIBLE);
		}else {
			mLoadMore.setVisibility(View.GONE);
		}
	}
	
	private void handleFailure(){
		Toast.makeText(this, getResources().getString(R.string.operation_failed), Toast.LENGTH_LONG).show();
		mGifLoader.setVisibility(View.GONE);
	}
	
	private void onLoadMoreItems(List<Comment> newComments) {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[CommentsActivity] onLoadMoreItems.");
		if (mAdapter == null){
			mComments = newComments;
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[CommentsActivity] onLoadMoreItems. Adapter Null" +
						", data size:" + newComments.size());
			initListView();
		}else {
			mAdapter.addComments(newComments);
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[CommentsActivity] onLoadMoreItems. Adapter Notified" +
						", datasize:" + newComments.size() + ", Adapter size:" + mAdapter.getCount());
			// notify the adapter that we can update now
	        mAdapter.notifyDataSetChanged();
	        mCommentsList.setSelection(mAdapter.getCount() - mFrom - 1);
		}
		if (newComments.size() != 0){
			mFrom += newComments.size();
		}
    }
	
	private void initListView(){
		mAdapter = new CommentsListAdapter(this, mComments, mHandler);
		mCommentsList.setAdapter(mAdapter);
		mCommentsList.setVisibility(View.VISIBLE);
		mGifLoader.setVisibility(View.GONE);
		hidePostCommentLoader();
		mCommentsList.setSelection(mAdapter.getCount() - 1);
	}

}
