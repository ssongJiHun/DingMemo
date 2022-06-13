package com.Ding.AppExample.Info;

import android.graphics.drawable.Drawable;

public class DrawerEntry {
	private String mTitle;
	private Drawable mDrawable;
	private int mColor;
	
	public DrawerEntry(String title, Drawable drawable){
		this.mTitle = title;
		this.mDrawable = drawable;	
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public Drawable getDrawable() {
		return mDrawable;
	}

	public void setDrawable(Drawable drawable) {
		this.mDrawable = drawable;
	}
	

}
