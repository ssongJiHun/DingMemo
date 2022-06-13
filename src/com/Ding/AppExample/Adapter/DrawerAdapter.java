package com.Ding.AppExample.Adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.Ding.AppExample.R;
import com.Ding.AppExample.Info.DrawerEntry;

public class DrawerAdapter extends ArrayAdapter<DrawerEntry> {
	private ArrayList<DrawerEntry> mArrayList = null;
	private LayoutInflater mLayoutInflater = null;
	private int mResource;
	private int nTouchPosition = 0;

	public DrawerAdapter(Context context, int resource,
		ArrayList<DrawerEntry> arrays) {
		super(context, resource, arrays);

		mArrayList = arrays;
		mResource = resource;

		mLayoutInflater = (LayoutInflater)context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			if (convertView == null) {
				convertView = mLayoutInflater.inflate(mResource, parent, false);

				holder = new ViewHolder();
				holder.mImageView = (ImageView)convertView
					.findViewById(R.id.drawer_item_iconview);
				holder.mTextTitle = (TextView)convertView
					.findViewById(R.id.drawer_item_titletext);

				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder)convertView.getTag();
			}
			DrawerEntry mDrawerEntry = mArrayList.get(position);

			int color = (nTouchPosition == position) ? Color.RED : Color.BLACK;

			holder.mImageView.setImageDrawable(mDrawerEntry.getDrawable());
			holder.mImageView.setColorFilter(color);

			//holder.mTextTitle.setPaintFlags(holder.mTextTitle.getPaintFlags()	| Paint.FAKE_BOLD_TEXT_FLAG);
			holder.mTextTitle.setTextColor(color);
			holder.mTextTitle.setText(mDrawerEntry.getTitle());
			return convertView;
		}

	public void setTouchPosition(int position) {
		this.nTouchPosition = position;
	}

	static class ViewHolder {
		ImageView mImageView;
		TextView mTextTitle;
	}
}
