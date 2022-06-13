package com.Ding.AppExample.Adapter;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.Ding.AppExample.R;
import com.Ding.AppExample.Class.ScheduleUndoView;
import com.Ding.AppExample.Class.SwipeTouchListener;
import com.Ding.AppExample.Info.ScheduleEntry;
import com.nineoldandroids.view.ViewHelper;

public class ScheduleAdapter extends ArrayAdapter<ScheduleEntry> {
	private ArrayList<ScheduleEntry> mArrayList = null;
	private int mItemView = 0; // 아이템 뷰 id
	private LayoutInflater mLayoutInflater = null;
	private boolean mCheckVisble = false;
	private int mLastVisibleCount = 0; // 현재 화면에 나타나는 리스트 숫자
	private int mVisibleCount = 0; // 카운터중인 보여지는 숫자
	private boolean[] isCheckedConfrim;
	private ListView mListView;
	private DeleteItemCallback mDeleteItemCallback;
	private ScheduleUndoView mCurrentRemovedView;
	private long mCurrentRemovedId;
	private Map<View, Animator> mActiveAnimators = new ConcurrentHashMap<View, Animator>();

	public ScheduleAdapter(Context context, int resource,
			ArrayList<ScheduleEntry> arrays, ListView listview) {
		super(context, resource, arrays);

		mArrayList = arrays;
		mItemView = resource;
		mListView = listview;
		mCurrentRemovedId = -1;

		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.isCheckedConfrim = new boolean[mArrayList.size()];

		SwipeTouchListener getTouchListener = new SwipeTouchListener(mListView,
				new SwipeTouchListener.DismissCallbacks() {

					@Override
					public void onViewSwiped(View dismissView,
							int dismissPosition) {
						ScheduleUndoView mScheduleUndoView = (ScheduleUndoView) dismissView;
						if (mScheduleUndoView.isContentDisplayed()) {
							restoreViewPosition(mScheduleUndoView);
							mScheduleUndoView.displayUndo();
							removePreviousContextualUndoIfPresent();

							mCurrentRemovedView = mScheduleUndoView;
							mCurrentRemovedId = mScheduleUndoView.getItemId();
						} else {
							if (mCurrentRemovedView != null) {
								performDismiss();
							}
						}
					}
				});

		mListView.setOnTouchListener(getTouchListener);
		mListView.setOnScrollListener(getTouchListener.makeScrollListener());
		mListView.setRecyclerListener(new RecyclerListener() {

			@Override
			public void onMovedToScrapHeap(View view) {
				Animator animator = mActiveAnimators.get(view);
				if (animator != null) {
					animator.cancel();
				}
			}
		});
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ScheduleUndoView mScheduleUndoView = (ScheduleUndoView) convertView;
		if (mScheduleUndoView == null) {
			mScheduleUndoView = new ScheduleUndoView(parent.getContext(),R.layout.undo_row);
			final ScheduleUndoView scheduleUndoView = mScheduleUndoView;

			mScheduleUndoView.findViewById(R.id.undo_row_undobutton)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							clearCurrentRemovedView();
							scheduleUndoView.displayContentView();
							ViewHelper.setTranslationX(scheduleUndoView,scheduleUndoView.getWidth());
							animate(scheduleUndoView).translationX(0).setDuration(100).setListener(null);
						}
					});
		}
		mScheduleUndoView.updateContentView(mLayoutInflater.inflate(mItemView, parent, false));

		final ViewHolder holder;
		if (convertView == null) {
			//mScheduleUndoView.updateContentView(mLayoutInflater.inflate(mItemView, parent, false));
			holder = new ViewHolder();

			holder.mTextContent = (TextView) mScheduleUndoView
					.findViewById(R.id.text_content);
			holder.mTextDay = (TextView) mScheduleUndoView
					.findViewById(R.id.text_day);
			holder.mCheckBox = (CheckBox) mScheduleUndoView
					.findViewById(R.id.list_checkbox);

			mScheduleUndoView.setTag(holder);
		} else {
			holder = (ViewHolder) mScheduleUndoView.getTag();
		}
		ScheduleEntry mScheduleEntry = mArrayList.get(position);

		holder.mTextContent.setPaintFlags(holder.mTextContent.getPaintFlags()
				| Paint.FAKE_BOLD_TEXT_FLAG);
		holder.mTextContent.setText(mScheduleEntry.getContent());
		holder.mTextDay.setText(mScheduleEntry.getDate());
		holder.mCheckBox.setChecked(isCheckedConfrim[position]);

		holder.mCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isCheckedConfrim[position] = !isCheckedConfrim[position];
			}
		});

		long itemId = getItemId(position);
		Log.d(String.valueOf(itemId), "asfasfasfa");
		if (itemId == mCurrentRemovedId) {
			mScheduleUndoView.displayUndo();
			mCurrentRemovedView = mScheduleUndoView;
		} else {
			mScheduleUndoView.displayContentView();
		}
		mScheduleUndoView.setItemId(position);

		// Scroll 할때 마다 호출
		checkBoxAnimate(holder);
		
		return mScheduleUndoView;
	}

	private void removePreviousContextualUndoIfPresent() {
		if (mCurrentRemovedView != null) {
			performDismiss();
		}
	}

	private void restoreViewPosition(View view) {
		setAlpha(view, 1f);
		setTranslationX(view, 0);
	}

	private void clearCurrentRemovedView() {
		mCurrentRemovedView = null;
		mCurrentRemovedId = -1;
	}

	private void performDismiss() {

		final View mDismissView = mCurrentRemovedView;
		ValueAnimator animator = ValueAnimator.ofInt(mDismissView.getHeight(),
				1).setDuration(1000);
		final ViewGroup.LayoutParams lp = mDismissView.getLayoutParams();
		final int originalHeight = mDismissView.getHeight();

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mActiveAnimators.remove(mDismissView);
				restoreViewPosition(mDismissView);

				ViewGroup.LayoutParams lp;
				lp = mDismissView.getLayoutParams();
				lp.height = originalHeight;
				mDismissView.setLayoutParams(lp);
				deleteCurrentItem();
			}

			private void deleteCurrentItem() {
				ScheduleUndoView mScheduleUndoView = (ScheduleUndoView) mDismissView;
				int position = mListView.getPositionForView(mScheduleUndoView);
				mDeleteItemCallback.onDismiss(position);
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {

				lp.height = (Integer) valueAnimator.getAnimatedValue();
				mDismissView.setLayoutParams(lp);

			}
		});

		animator.start();
		mActiveAnimators.put(mDismissView, animator);
		clearCurrentRemovedView();
	}

	public interface DeleteItemCallback {
		public void onDismiss(int reverseSortedPosition);
	}

	public void setDeleteItemCallback(DeleteItemCallback deleteItemCallback) {
		mDeleteItemCallback = deleteItemCallback;
	}

	private void checkBoxAnimate(final ViewHolder holder) {
		// CheckMode on
		if (mCheckVisble) {
			// 현재 보이는 리스트 갯수만 애니메이션 처리
			if (mVisibleCount != mLastVisibleCount + 1) {// 현재 리스트 카운터
				// 현재 보이는 뷰이기 때문에 애니메이션 처리
				holder.mTextContent.animate().translationX(80).setDuration(200)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								holder.mCheckBox.setVisibility(View.VISIBLE);
							}
						});
				mVisibleCount++;
			} else if (holder.mCheckBox.getVisibility() == View.INVISIBLE) { // 현재
																				// 리스트에
																				// 보이지
																				// 않는
																				// 뷰
				holder.mCheckBox.setVisibility(View.VISIBLE);
				holder.mTextContent.setTranslationX(80);
			}

		} else { // CheckMode off
			if (mVisibleCount != mLastVisibleCount + 1) {
				holder.mTextContent.animate().translationX(0).setDuration(200)
						.setListener(new AnimatorListenerAdapter() {

							@Override
							public void onAnimationStart(Animator animation) {
								holder.mCheckBox.setVisibility(View.INVISIBLE);
							}
						});
				mVisibleCount++;
			} else if (holder.mCheckBox.getVisibility() == View.VISIBLE) {
				// 현재 리스트에 보이지 않는뷰
				holder.mCheckBox.setVisibility(View.INVISIBLE);
				holder.mTextContent.setTranslationX(0);
			}
		}
	}

	// 체크박스 1차 배열 사이즈 재설정 ? getView 에서 Position 이 않맞기 때문에
	public void reSizeList() {
		this.isCheckedConfrim = new boolean[mArrayList.size()];
		notifyDataSetChanged();
	}

	public void setAllChecked(boolean ischeked) {
		int tempSize = isCheckedConfrim.length;
		for (int a = 0; a < tempSize; a++) {
			isCheckedConfrim[a] = ischeked;
		}

		notifyDataSetChanged();
	}

	public void setChecked(int position, boolean checked) {
		isCheckedConfrim[position] = checked;
	}

	public ArrayList<Integer> getChecked() {
		int tempSize = isCheckedConfrim.length;
		ArrayList<Integer> mArrayList = new ArrayList<Integer>();
		for (int b = 0; b < tempSize; b++) {
			if (isCheckedConfrim[b]) {
				mArrayList.add(b);
			}
		}
		return mArrayList;
	}

	public int getCheckedCount() {
		int tempSize = isCheckedConfrim.length;
		int CheckedCount = 0;
		for (int num = 0; num < tempSize; num++) {
			if (isCheckedConfrim[num]) {
				CheckedCount++;
			}
		}
		return CheckedCount;
	}

	public void setHasCheckBox(boolean BVisible, int nVisible) {
		mCheckVisble = BVisible;
		mVisibleCount = 0;
		mLastVisibleCount = nVisible;
	}

	public boolean getHasCheckBox() {
		return mCheckVisble;
	}

	static class ViewHolder {
		TextView mTextContent;
		TextView mTextDay;
		CheckBox mCheckBox;
	}
}
