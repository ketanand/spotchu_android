package com.vrocketz.spotchu.activity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.vrocketz.spotchu.R;
import com.vrocketz.spotchu.SpotchuLocationService;
import com.vrocketz.spotchu.helper.Config;
import com.vrocketz.spotchu.helper.Constants;
import com.vrocketz.spotchu.helper.Util;
import com.vrocketz.spotchu.runnables.GetAddress;
import com.vrocketz.spotchu.runnables.PostSpot;
import com.vrocketz.spotchu.runnables.UpdateSpot;
import com.vrocketz.spotchu.spot.PendingSpotDao;
import com.vrocketz.spotchu.spot.Spot;
import com.vrocketz.spotchu.spot.SpotHelper;

public class PostSpotActivity extends FragmentActivity implements OnClickListener {
	
	public static final String UPDATE = "isUpdate";
	public static final String PREVIEW_IMAGE = "PREVIEW_IMAGE";
	private String imageFilePath;
	private String imageUrl;
	private Uri mImageUri;
	private SpotchuLocationService mLocationService;
	private boolean mIsBound;
	private Location mLocation;
	private Map<String, Object> mAddress;
	private EditText mTitle;
	private ImageView mImageView;
	private Button mButton;
	private NotificationManager mNM;
	private static int NOTIFICATION_ID = 999;
	private Spot mSpot;
	private CheckBox mCheck;
	
	private boolean isUpdate;
	private Long spotId;
	
	
	//Image Effects Variables
	/*private GLSurfaceView mEffectView;
    private int[] mTextures = new int[2];
    private EffectContext mEffectContext;
    private Effect mEffect;
    private TextureRenderer mTexRenderer = new TextureRenderer();
    private int mImageWidth;
    private int mImageHeight;
    private boolean mInitialized = false;
    int mCurrentEffect;
    private Bitmap mImage;
    private Bitmap mImageWithEffect;
    private ImageFilters mImageFilters;

    private void setCurrentEffect(int effect) {
        mCurrentEffect = effect;
    }*/
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		//Create location client
		setContentView(R.layout.post_review);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();
	    
		//Set listeners for buttons
		mButton = (Button)findViewById(R.id.btnOk);
		mButton.setOnClickListener(this);
		mCheck = (CheckBox)findViewById(R.id.chkAnonymous);
		/*ImageButton noEffect = (ImageButton)findViewById(R.id.btnNoEffect);
		noEffect.setOnClickListener(this);
		ImageButton effectDocumentary = (ImageButton)findViewById(R.id.btnEffectDocumentary);
		effectDocumentary.setOnClickListener(this);
		ImageButton effectGrain = (ImageButton)findViewById(R.id.btnEffectGrain);
		effectGrain.setOnClickListener(this);
		ImageButton effectGreyscale = (ImageButton)findViewById(R.id.btnEffectGreyscale);
		effectGreyscale.setOnClickListener(this);
		ImageButton effectSepia = (ImageButton)findViewById(R.id.btnEffectSepia);
		effectSepia.setOnClickListener(this);*/
		
		mTitle = (EditText)findViewById(R.id.txtCaption);
		mTitle.addTextChangedListener(mTitleTextWatcher);
		mTitle.setImeActionLabel("Share", EditorInfo.IME_ACTION_DONE);
		mTitle.setOnEditorActionListener(new EditText.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
	                    KeyEvent event) {
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME, "[PostSpotActivity] Done Button Clicked , action id " + actionId);
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						mSpot = createPendingSpot();
						if (mSpot != null){
							//if (!isUpdate)
								postSpot();
							//else 
							//	updateSpot();
						}else {
							if (Config.DEBUG)
								Log.d(Constants.APP_NAME, "[CreateSpot] NULL.");
							//TODO : tell user;
						}
	                    return true;
	                }
	                return false;
				}
	        });
		
		//Bind to location Service
		doBindService();
		
		//Initialize Image View
		mImageView = (ImageView)findViewById(R.id.imgPic);
		if (isUpdate = getIntent().getBooleanExtra(UPDATE, false)){
			//TODO integrate update spot API
			initUpdateView(intent);
		}else {
			if (Intent.ACTION_SEND.equals(action) && type != null) {
		        if (type.startsWith("image/")) {
		        	mImageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		            if (mImageUri != null) {
		            	mImageView.setImageURI(mImageUri);
		            	if (mImageUri.toString().contains("content://"))
		            		imageFilePath = Util.getPathFromUri(mImageUri, this);
		            	else {
		            		imageFilePath = mImageUri.getPath();
		            	}	
		            }
		        }
		    }else {
				imageFilePath = intent.getStringExtra(PREVIEW_IMAGE);
				mImageUri = Uri.fromFile(new File(imageFilePath));
				mImageView.setImageURI(mImageUri);
		    }
		}
		/*imageFilePath = getIntent().getStringExtra("PREVIEW_IMAGE");
		mImage = BitmapFactory.decodeFile(imageFilePath);
		
		mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCurrentEffect = R.id.btnNoEffect;*/
		
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	}
	
	private void initUpdateView(Intent intent){
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Update Spot] init start.");
		String imageUrl = intent.getStringExtra(PREVIEW_IMAGE);
		imageFilePath = imageUrl;
		mImageUri = Uri.parse(imageUrl);
		ImageAware imageAware = new ImageViewAware(mImageView, false);
	    ImageLoader.getInstance().displayImage(imageFilePath, imageAware);
		spotId = intent.getLongExtra(SpotHelper.SPOT_ID, 0);
		mTitle.setText(intent.getStringExtra(SpotHelper.SPOT_DESC));
		mCheck.setVisibility(View.GONE);
		isUpdate = true;
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[Update Spot] spot url: " + imageUrl +  ", spotId:" +spotId);
	}
	
	private TextWatcher mTitleTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			String caption = s.toString();
			int lastHashIndex = caption.lastIndexOf("#");
			int end;
			if (lastHashIndex >= 0){
				end = caption.indexOf(" ", lastHashIndex);
				if (end == -1){
					end = s.length();
				}
				s.setSpan(new StyleSpan(Typeface.BOLD), lastHashIndex, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}
	
	private boolean postSpot(){
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
		/*String desc = mTitle.getText().toString();
		if (desc.length() == 0){
			Toast.makeText(this, getResources().getString(R.string.required_desc), Toast.LENGTH_LONG).show();
			return false;
		}*/
		nameValuePairs.add(new BasicNameValuePair("desc", mSpot.getDesc()));
		nameValuePairs.add(new BasicNameValuePair("tags", mSpot.getTag()));
		nameValuePairs.add(new BasicNameValuePair("goanonymous", String.valueOf(mSpot.getIsAnonymous())));
		if (mLocation != null){
			nameValuePairs.add(new BasicNameValuePair("locationLong", mSpot.getLocationLong()));
			nameValuePairs.add(new BasicNameValuePair("locationLati", mSpot.getLocationLati()));
		}/*if (mCurrentEffect != R.id.btnNoEffect){
			//TODO : figure out a way to save the modified image
			try {
				//saveBitmap();
				saveModifiedImage();
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(imageFilePath);
				mImageWithEffect.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			    fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		PostSpot instance = new PostSpot(mSpot, mHandler, nameValuePairs);
		Thread t = new Thread(instance);
		t.start();
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Post Spot Runnable called");
		
		showNotification();
		finish();
		return true;
	}
	
	/*private void updateSpot(){
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("desc", mSpot.getDesc()));
		nameValuePairs.add(new BasicNameValuePair("tags", mSpot.getTag()));
		UpdateSpot instance = new UpdateSpot(mSpot,spotId, mHandler, nameValuePairs);
		Thread t = new Thread(instance);
		t.start();
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "Update Spot Runnable called");
		
		showNotification();
		finish();
	}*/
	
	private Spot createPendingSpot(){
		PendingSpotDao dao = new PendingSpotDao(this);
		dao.open();
		Spot spot = new Spot();
		String desc = mTitle.getText().toString();
		if (desc.length() == 0){
			Toast.makeText(this, getResources().getString(R.string.required_desc), Toast.LENGTH_LONG).show();
			return null;
		}
		spot.setDesc(desc);
		spot.setTag( Util.getTagsFromTitle(mTitle.getText().toString()));
		spot.setIsAnonymous(mCheck.isChecked());
		if (mLocation != null){
			spot.setLocationLong(String.valueOf(mLocation.getLongitude()));
			spot.setLocationLati(String.valueOf(mLocation.getLatitude()));
		}
		spot.setImg(imageFilePath);
		spot.setImgUri(mImageUri);
		spot.setCreatedAt(Util.getTimeInMilliseconds() / 1000);
		spot.setStatus(Spot.Status.PENDING);
		long id;
		try{
			id = dao.createSpot(spot);
			spot.setId(id);
		} finally{
			if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[PostSpot] Pending Spot Size: " + dao.getAllSpots().size());
			dao.close();
		}
		return spot;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnOk:
			mSpot = createPendingSpot();
			if (mSpot != null){
				//if (isUpdate)
					//updateSpot();
				//else 
					postSpot();
			}else {
				if (Config.DEBUG)
					Log.d(Constants.APP_NAME, "[CreateSpot] NULL.");
				//TODO : tell user;
			}
			break;
		/*case R.id.btnNoEffect:
			mImageView.setImageBitmap(mImage);
			setCurrentEffect(v.getId());
			break;
		case R.id.btnEffectDocumentary:
		case R.id.btnEffectGrain:
		case R.id.btnEffectGreyscale:
			mImageWithEffect = mImageFilters.applyGreyscaleEffect(mImage);
			mImageView.setImageBitmap(mImageWithEffect);
			setCurrentEffect(v.getId());
			break;
		case R.id.btnEffectSepia:	
			mImageWithEffect = mImageFilters.applySepiaToningEffect(mImage, 150,.7, 0.3, 0.12);
			mImageView.setImageBitmap(mImageWithEffect);
			setCurrentEffect(v.getId());
	        mEffectView.requestRender();
	        break;*/
	    default:
	    	if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[PostSpotActivity] No View Found" + v.getId());
		}
	}
	
	private ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			mLocationService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mLocationService = ((SpotchuLocationService.LocalBinder)service).getService();
			mLocation = mLocationService.getCurrentLocation();
			if (mLocation != null){
				new Thread(new GetAddress(getApplicationContext(), mHandler, mLocation)).start();
			}
		}
	};
	
	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(this, 
	            SpotchuLocationService.class), connection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(connection);
	        mIsBound = false;
	    }
	}
	
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			final int what = msg.what;
			switch(what){
				case Constants.ADDRESS_RESOLVED:
					mAddress = (HashMap<String, Object>) msg.obj;
					displayAddressInfo();
					break;
					
				case Constants.SPOT_POSTED:
					updateNotification(R.string.spot_posted);
					Long id = (Long) msg.obj;
					PendingSpotDao dao = new PendingSpotDao(Util.getApp());
					dao.open();
					dao.deleteSpotById(id);
					if (Config.DEBUG)
						Log.d(Constants.APP_NAME, "[PostSpot] Pending Spot Size After post: " + dao.getAllSpots().size());
					dao.close();
					finish();
					break;
					
				case Constants.SPOT_POST_FAILED:
					updateNotification(R.string.spot_post_failed);
					Spot spot = (Spot) msg.obj;
					if (spot != null){
						PendingSpotDao spotDao = new PendingSpotDao(Util.getApp());
						spotDao.open();
						spot.setStatus(Spot.Status.FAILED);
						spotDao.UpdateSpot(spot);
						spotDao.close();
					}
					finish();
					break;
				case Constants.NO_INTERNET:
					updateNotification(R.string.spot_post_failed_no_internet);
					finish();
					break;
			}
		}
	};
	
	private void displayAddressInfo(){
		//TODO : present city info to user.
	}
	
	private void showNotification(){
		// create the notification
        
        String text = (String) getText(R.string.spot_being_uploaded);
       

        // create the pending intent and add to the notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("tab", 2);// Open My Spots when notificaition is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
	     stackBuilder.addParentStack(MainActivity.class);
	     stackBuilder.addNextIntent(intent);
	     PendingIntent pendingIntent =
	             stackBuilder.getPendingIntent(
	                 0,
	                 PendingIntent.FLAG_UPDATE_CURRENT
	             );*/
        
        NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setOngoing(true);
	    m_notificationBuilder.setContentIntent(pendingIntent);
	    m_notificationBuilder.setProgress(0, 0, true);
        // send the notification
        mNM.notify(NOTIFICATION_ID, m_notificationBuilder.build());
	}
	
	private void updateNotification(int resID){
		String text = (String) getText(resID);
	       

        // create the pending intent and add to the notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("postSpot", true);// Open My Spots when notificaition is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
	     // Adds the back stack for the Intent (but not the Intent itself)
	     stackBuilder.addParentStack(MainActivity.class);
	     // Adds the Intent that starts the Activity to the top of the stack
	     stackBuilder.addNextIntent(intent);
	     PendingIntent pendingIntent =
	             stackBuilder.getPendingIntent(
	                 0,
	                 PendingIntent.FLAG_UPDATE_CURRENT
	             );*/
        
        NotificationCompat.Builder m_notificationBuilder = new NotificationCompat.Builder(this)
	        .setContentTitle(getText(R.string.app_name))
	        .setContentText(text)
	        .setTicker(text)
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setAutoCancel(true)
	        .setOngoing(false);
	    m_notificationBuilder.setContentIntent(pendingIntent);
	    m_notificationBuilder.setProgress(0, 0, false);
        // send the notification
        mNM.notify(NOTIFICATION_ID, m_notificationBuilder.build());
	}
	
	public Handler getHandler(){
		return mHandler;
	}
	
	/*private void saveBitmap() 
    {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(mImageWidth * mImageHeight * 4).order(ByteOrder.nativeOrder());
        GLES20.glReadPixels(0, 0, mImageWidth, mImageHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(pixelBuffer);

        try 
        {
            FileOutputStream fos = new FileOutputStream(imageFilePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) { e.printStackTrace();  }

    }
	private void saveModifiedImage() throws IOException{
		int width = mImageWidth;
	    int height = mImageHeight;


	    int size = width * height;
	    ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
	    buf.order(ByteOrder.nativeOrder());
	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	    GLES20.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buf);

	    int data[] = new int[size];
	    buf.asIntBuffer().get(data);
	    buf = null;
	    Bitmap createdBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    createdBitmap.setPixels(data, size-width, -width, 0, 0, width, height);
	    data = null;

	    short sdata[] = new short[size];
	    ShortBuffer sbuf = ShortBuffer.wrap(sdata);
	    createdBitmap.copyPixelsToBuffer(sbuf);
	    for (int i = 0; i < size; ++i) {
	        //BGR-565 to RGB-565
	        short v = sdata[i];
	        sdata[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
	    }

	    sbuf.rewind();
	    createdBitmap.copyPixelsFromBuffer(sbuf);
	    
	    //Save Image
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    createdBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	    byte[] byteArray = stream.toByteArray();
	    Util.saveBytesToFile(byteArray, imageFilePath);
	}

	 private void loadTextures() {
		 if (Config.DEBUG)
				Log.d(Constants.APP_NAME, "[PostSpotActivity] loadTextures starts");
	        // Generate textures
	        GLES20.glGenTextures(2, mTextures, 0);

	        // Load input bitmap
	        mImageWidth = mImage.getWidth();
	        mImageHeight = mImage.getHeight();
	        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);

	        // Upload to texture
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mImage, 0);

	        // Set texture parameters
	        GLToolbox.initTexParams();
	}
	 
	private void initEffect() {
		if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[PostSpotActivity] initEffect starts");
        EffectFactory effectFactory = mEffectContext.getFactory();
        if (mEffect != null) {
            mEffect.release();
        }
        *//**
         * Initialize the correct effect based on the selected menu/action item
         *//*
        switch (mCurrentEffect) {

            case R.id.btnNoEffect:
                break;

            case R.id.btnEffectDocumentary:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DOCUMENTARY);
                break;

            case R.id.btnEffectGrain:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAIN);
                mEffect.setParameter("strength", 1.0f);
                break;

            case R.id.btnEffectGreyscale:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAYSCALE);
                break;

            case R.id.btnEffectSepia:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SEPIA);
                break;
            default:
                break;

        }
    }

    private void applyEffect() {
    	if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[PostSpotActivity] applyEffect starts");
        mEffect.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
    }

    private void renderResult() {
    	if (Config.DEBUG)
			Log.d(Constants.APP_NAME, "[PostSpotActivity] renderResult starts");
        if (mCurrentEffect != R.id.btnNoEffect) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1]);
        }
        else {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            loadTextures();
            mInitialized = true;
        }
        if (mCurrentEffect != R.id.btnNoEffect) {
            //if an effect is chosen initialize it and apply it to the texture
            initEffect();
            applyEffect();
        }
        renderResult();
        mImageWithEffect = createBitmapFromGLSurface(0, 0, mImageWidth, mImageHeight, gl);
    } 

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }
    
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }*/


}
