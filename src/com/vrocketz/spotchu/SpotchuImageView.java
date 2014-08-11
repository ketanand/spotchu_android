package com.vrocketz.spotchu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SpotchuImageView extends ImageView {

	public SpotchuImageView(Context context) {
		super(context);
	}
	
	public SpotchuImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SpotchuImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }

}
