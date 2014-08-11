package com.vrocketz.spotchu;

import java.io.File;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class PostReview extends FragmentActivity implements OnClickListener {
	
	private String imageFilePath;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.post_review);
		Button ok = (Button)findViewById(R.id.btnOk);
		ok.setOnClickListener(this);
		Button cancel = (Button)findViewById(R.id.btnCancel);
		cancel.setOnClickListener(this);
		imageFilePath = getIntent().getStringExtra("PREVIEW_IMAGE");
		ImageView img = (ImageView)findViewById(R.id.imgPic);
		Uri uri = Uri.fromFile(new File(imageFilePath));
		img.setImageURI(uri);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btnOk){
			UploadImage instance = new UploadImage(imageFilePath, this);
			Thread t = new Thread(instance);
			t.start();
			Log.d("spotu", "upload thread called");
		}else if (v.getId() == R.id.btnCancel){
			
		}
	}

}
