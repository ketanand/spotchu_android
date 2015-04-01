package com.vrocketz.spotchu.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.vrocketz.spotchu.NotificationService;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;

public class Summary extends Activity{
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary);
		WebView browser = (WebView) findViewById(R.id.webview);
		String url = getIntent().getStringExtra(NotificationService.SUMMARY_URL);
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Summary] url:" + url);
		browser.loadUrl(url);
	}
}
