package com.vrocketz.spotchu;

import java.io.File;

public class DownloadFileRunnable implements Runnable {
	
	private String url;
	private File saveAt;
	
	public DownloadFileRunnable(String url, File saveAt){
		this.url = url;
		this.saveAt = saveAt;
	}

	@Override
	public void run() {
		
	}

}
