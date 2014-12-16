package com.vrocketz.spotchu.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ExploreGridImageItemView extends ImageView {

	public ExploreGridImageItemView(Context context) {
		super(context);
	}
	
	public ExploreGridImageItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ExploreGridImageItemView(Context context, AttributeSet attrs, int defStyle)
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
