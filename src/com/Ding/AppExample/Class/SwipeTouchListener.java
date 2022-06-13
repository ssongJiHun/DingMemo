package com.Ding.AppExample.Class;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class SwipeTouchListener implements View.OnTouchListener{
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;

	// Fixed properties
	private ListView mListView;
	private DismissCallbacks mCallback;
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

	// Transient properties
	private float mDownX;
	private float mDownY;
	private boolean mSwiping;
	private int mSwipingSlop;
	private VelocityTracker mVelocityTracker;
	private int mDownPosition;
	private View mDownView;
	private boolean mPaused;

	/**
	* The callback interface used by {@link SwipeDismissListViewTouchListener} to inform its client
	* about a successful dismissal of one or more list item positions.
	*/
	public interface DismissCallbacks {

		void onViewSwiped(View dismissView, int dismissPosition);
	}

	/**
	* Constructs a new swipe-to-dismiss touch listener for the given list view.
	*
	* @param listView  The list view whose items should be dismissable.
	* @param callbacks The callback to trigger when the user has indicated that she would like to
	*                  dismiss one or more list items.
	*/
	public SwipeTouchListener(ListView listView, DismissCallbacks callbacks) {
		ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = listView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
		mListView = listView;
		mCallback = callbacks;
	}

	/**
	* Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
	*
	* @param enabled Whether or not to watch for gestures.
	*/
	public void setEnabled(boolean enabled) {
		mPaused = !enabled;
	}

	/**
	* Returns an {@link AbsListView.OnScrollListener} to be added to the {@link
	* ListView} using {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}.
	* If a scroll listener is already assigned, the caller should still pass scroll changes through
	* to this listener. This will ensure that this {@link SwipeDismissListViewTouchListener} is
	* paused during list view scrolling.</p>
	*
	* @see SwipeDismissListViewTouchListener
	*/
	public AbsListView.OnScrollListener makeScrollListener() {
		return new AbsListView.OnScrollListener() {
			@Override
				public void onScrollStateChanged(AbsListView absListView, int scrollState) {
					setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
				}

			@Override
				public void onScroll(AbsListView absListView, int i, int i1, int i2) {
				}
		};
	}

	@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (mViewWidth < 2) {
				mViewWidth = mListView.getWidth();
			}

			switch (motionEvent.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: {
											  if (mPaused) {
												  return false;
											  }

											  // TODO: ensure this is a finger, and set a flag

											  // Find the child view that was touched (perform a hit test)
											  Rect rect = new Rect();
											  int childCount = mListView.getChildCount();
											  int[] listViewCoords = new int[2];
											  mListView.getLocationOnScreen(listViewCoords);
											  int x = (int)motionEvent.getRawX() - listViewCoords[0];
											  int y = (int)motionEvent.getRawY() - listViewCoords[1];
											  View child;
											  for (int i = 0; i < childCount; i++) {
												  child = mListView.getChildAt(i);
												  child.getHitRect(rect);
												  if (rect.contains(x, y)) {
													  mDownView = child;
													  break;
												  }
											  }

											  if (mDownView != null) {
												  mDownX = motionEvent.getRawX();
												  mDownY = motionEvent.getRawY();
												  mDownPosition = mListView.getPositionForView(mDownView); // 헤더 추가시 -1 
												  mVelocityTracker = VelocityTracker.obtain();
												  mVelocityTracker.addMovement(motionEvent);
											  }
											  // view.onTouchEvent(motionEvent);
											  return false;
			}

			case MotionEvent.ACTION_CANCEL: {
												if (mVelocityTracker == null) {
													break;
												}

												if (mDownView != null && mSwiping) {
													// cancel
													mDownView.animate()
														.translationX(0)
														.alpha(1)
														.setDuration(mAnimationTime)
														.setListener(null);
												}
												mVelocityTracker.recycle();
												mVelocityTracker = null;
												mDownX = 0;
												mDownY = 0;
												mDownView = null;
												mDownPosition = ListView.INVALID_POSITION;
												mSwiping = false;
												break;
			}

			case MotionEvent.ACTION_UP: {
											if (mVelocityTracker == null) {
												break;
											}

											float deltaX = motionEvent.getRawX() - mDownX;
											mVelocityTracker.addMovement(motionEvent);
											mVelocityTracker.computeCurrentVelocity(1000);
											float velocityX = mVelocityTracker.getXVelocity();
											float absVelocityX = Math.abs(velocityX);
											float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
											boolean dismiss = false;
											boolean dismissRight = false;
											if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
												dismiss = true;
												dismissRight = deltaX > 0;
											}
											else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
												&& absVelocityY < absVelocityX && mSwiping) {
												// dismiss only if flinging in the same direction as dragging
												dismiss = (velocityX < 0) == (deltaX < 0);
												dismissRight = mVelocityTracker.getXVelocity() > 0;
											}
											if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
												// dismiss
												final View downView = mDownView; // mDownView gets null'd before animation ends
												final int downPosition = mDownPosition;
												mDownView.animate()
													.translationX(dismissRight ? mViewWidth : -mViewWidth)
													.alpha(0)
													.setDuration(mAnimationTime)
													.setListener(new AnimatorListenerAdapter(){
													@Override
													public void onAnimationEnd(Animator animation) {
														mCallback.onViewSwiped(downView, downPosition);
														// performDismiss(downView, downPosition);
													}
												});
											}
											else {
												// cancel
												mDownView.animate()
													.translationX(0)
													.alpha(1)
													.setDuration(mAnimationTime)
													.setListener(null);
											}
											mVelocityTracker.recycle();
											mVelocityTracker = null;
											mDownX = 0;
											mDownY = 0;
											mDownView = null;
											mDownPosition = ListView.INVALID_POSITION;
											mSwiping = false;
											break;
			}

			case MotionEvent.ACTION_MOVE: {
											  if (mVelocityTracker == null || mPaused) {
												  break;
											  }

											  mVelocityTracker.addMovement(motionEvent);
											  float deltaX = motionEvent.getRawX() - mDownX;
											  float deltaY = motionEvent.getRawY() - mDownY;
											  if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
												  mSwiping = true;
												  mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
												  mListView.requestDisallowInterceptTouchEvent(true);

												  // Cancel ListView's touch (un-highlighting the item)
												  MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
												  cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
													  (motionEvent.getActionIndex()
													  << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
												  mListView.onTouchEvent(cancelEvent);
												  cancelEvent.recycle();
											  }

											  if (mSwiping) {
												  mDownView.setTranslationX(deltaX - mSwipingSlop);
												  mDownView.setAlpha(Math.max(0f, Math.min(1f,
													  1f - 2f * Math.abs(deltaX) / mViewWidth)));
												  return true;
											  }
											  break;
			}
			}
			return false;
		}


}
