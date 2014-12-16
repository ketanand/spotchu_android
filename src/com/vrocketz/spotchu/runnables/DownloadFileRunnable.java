package com.vrocketz.spotchu.runnables;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.util.Log;

import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;

public class DownloadFileRunnable implements Runnable {
	
	private String url;
	private File saveAt;
	
	public DownloadFileRunnable(String url, File saveAt){
		this.url = url;
		this.saveAt = saveAt;
	}

	@Override
	public void run() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Download File] url : " + this.url);
		try {
			 URL url = new URL(this.url);
			 InputStream in = new BufferedInputStream(url.openStream());
			 ByteArrayOutputStream out = new ByteArrayOutputStream();
			 byte[] buf = new byte[1024];
			 int n = 0;
			 while (-1!=(n=in.read(buf)))
			 {
			    out.write(buf, 0, n);
			 }
			 out.close();
			 in.close();
			 byte[] response = out.toByteArray();
			 
			 FileOutputStream fos = new FileOutputStream(this.saveAt);
			 fos.write(response);
			 fos.close();
			 if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[Download File] File Downloaded : " + response.length + " bytes downloaded.");
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
