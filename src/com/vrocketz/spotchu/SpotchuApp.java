package com.vrocketz.spotchu;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;

import android.app.Application;
import android.content.SharedPreferences;

public class SpotchuApp extends Application {
	
	SharedPreferences mPref;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mPref = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
		Util.setApp(this);
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
												.showImageOnLoading(R.drawable.ic_launcher)
												//.delayBeforeLoading(1000)
												.cacheInMemory(true)
												.cacheOnDisk(true)
												.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
											.threadPriority(Thread.NORM_PRIORITY - 2)
											.denyCacheImageMultipleSizesInMemory()
											
											.tasksProcessingOrder(QueueProcessingType.LIFO)
											.memoryCache(new LruMemoryCache(10 * 1024 * 1024))
									        .memoryCacheSize(10 * 1024 * 1024)
									        .memoryCacheSizePercentage(13) // default
									        .diskCache(new UnlimitedDiscCache(Util.getSavePath(Constants.IMAGE_CACHE))) // default
									        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
											.diskCacheSize(50 * 1024 * 1024) // 50 Mb
									        .diskCacheFileCount(100)
											.defaultDisplayImageOptions(defaultOptions)
											.writeDebugLogs() // Remove for release app
											.build();
		
        ImageLoader.getInstance().init(config);
	}
	
	public SharedPreferences getGlobalPreferences(){
		if (mPref == null){
			mPref = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
		}
		return mPref;
	}

}
