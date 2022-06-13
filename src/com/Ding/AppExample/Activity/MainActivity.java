package com.Ding.AppExample.Activity;

import java.sql.Date;
import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Ding.AppExample.R;
import com.Ding.AppExample.Adapter.DrawerAdapter;
import com.Ding.AppExample.Adapter.ScheduleAdapter;
import com.Ding.AppExample.Adapter.ScheduleAdapter.DeleteItemCallback;
import com.Ding.AppExample.Class.DBManager;
import com.Ding.AppExample.Class.SwipeTouchListener;
import com.Ding.AppExample.Info.DrawerEntry;
import com.Ding.AppExample.Info.ScheduleEntry;

public class MainActivity extends ActionBarActivity {
	private ListView mListView;
	private ArrayList<ScheduleEntry> mArrayList = new ArrayList<ScheduleEntry>();
	private ScheduleAdapter mAdapter;
	private SearchView mSearchView;
	private ActionBarDrawerToggle mToggle;
	private DBManager mDBManager;
	private LinearLayout HaveListView;
	private RelativeLayout NotListView;
	private ImageButton mFloatingButton;
	private ActionMode mActionMode;
	PopupWindow mPopupWindow;

	private int nRequestCode = 1; // ContentActivity
	private boolean mHasPressMode = false;
	private boolean mHasDrawerMode = false;
	private DrawerLayout mDrawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Toolbar Setting
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setElevation(25);
		getSupportActionBar().setTitle("모든 메모");
		
		// DrawerLayout Setting
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.app_name, R.string.app_name) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				mHasDrawerMode = false;
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				mHasDrawerMode = true;
			}

		};
		mDrawerLayout.setDrawerListener(mToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// view setting
		HaveListView = (LinearLayout) findViewById(R.id.HaveListView);
		NotListView = (RelativeLayout) findViewById(R.id.NotListView);
		
		ArrayList<DrawerEntry> mDrawerArray = new ArrayList<DrawerEntry>();
		mDrawerArray.add(new DrawerEntry("모든 노트", getResources().getDrawable(R.drawable.ic_description_white_24dp)));
		mDrawerArray.add(new DrawerEntry("메모 작성", getResources().getDrawable(R.drawable.ic_done_white_24dp)));
		mDrawerArray.add(new DrawerEntry("보관함", getResources().getDrawable(R.drawable.ic_done_white_24dp)));		
		mDrawerArray.add(new DrawerEntry("즐겨 찾기", getResources().getDrawable(R.drawable.ic_bookmark_white_24dp)));
		mDrawerArray.add(new DrawerEntry("환경 설정", getResources().getDrawable(R.drawable.ic_settings_white_24dp)));
		
		final DrawerAdapter mDrawerAdapter = new DrawerAdapter(this, R.layout.item_drawerview, mDrawerArray);
		ListView mDrawerListView = (ListView)findViewById(R.id.DrawerListView);
		
		mDrawerListView.addHeaderView(getLayoutInflater().inflate(R.layout.header_listview, null, false));
		mDrawerListView.setAdapter(mDrawerAdapter);
		 
		mDrawerListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mDrawerAdapter.setTouchPosition(position-1);
				mDrawerAdapter.notifyDataSetChanged();
			}
		});

		// Find ListView
		mListView = (ListView) findViewById(R.id.MianListView);

		// datebase manager
		mDBManager = new DBManager(MainActivity.this);
		

		// swape delet
		mAdapter = new ScheduleAdapter(this, R.layout.listview_row, mArrayList, mListView);
		mAdapter.setDeleteItemCallback(new DeleteItemCallback() {
			
			@Override
			public void onDismiss(int reverseSortedPosition) {
			//	mDBManager.removeData(mArrayList.get(reverseSortedPosition).getSSID());
				mArrayList.remove(mAdapter.getItem(reverseSortedPosition));
				mAdapter.notifyDataSetChanged();

//				if (mArrayList.size() == 0)
//					setViewVisible(false, true);
				
			}
		});
		mListView.setAdapter(mAdapter);

		// ListRefresh
		listRefresh();

		mListView.setOnItemClickListener(getItemClickListener);
		mListView.setOnItemLongClickListener(getItemLongClickListener);

		// Floating Button
		mFloatingButton = (ImageButton) findViewById(R.id.floatingButton);
		mFloatingButton.setOnClickListener(getFloatingListener);		 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		
		mFloatingButton.animate().translationY(0).setDuration(500).setListener(null);
		
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == nRequestCode) {// Content Activity
				if (intent.getBooleanExtra("CreateState", false) == true)
					listRefresh();
			}
		}
	}

	// 뒤로가기 버튼 오버라이드
	@Override
	public void onBackPressed() {
		if(!mToggle.isDrawerIndicatorEnabled()) {
			mSearchView.setQuery("", false);
			mSearchView.setIconified(true);
			mToggle.setDrawerIndicatorEnabled(true);
		} else if(mHasDrawerMode) {
			mDrawerLayout.closeDrawers();
		} else {
			super.onBackPressed();
		}
	}
	 
	// 메모가 없을때, 찾기 단어가 없을때 메인뷰를 숨기고 경고 뷰를 띄울수있다
	private void setViewVisible(boolean bHaveVisible, boolean bNotVisible ) {
		
		if (bHaveVisible && HaveListView.getVisibility() == View.INVISIBLE)
			HaveListView.setVisibility(View.VISIBLE);
		else if(!bHaveVisible && HaveListView.getVisibility() == View.VISIBLE)
			HaveListView.setVisibility(View.INVISIBLE);
		
		if(bNotVisible && NotListView.getVisibility() == View.INVISIBLE)
			NotListView.setVisibility(View.VISIBLE);
		else if(!bNotVisible && NotListView.getVisibility() == View.VISIBLE)
			NotListView.setVisibility(View.INVISIBLE);
	}

	// 리스트 새로 고침
	private void listRefresh() {
		mArrayList.clear();
		mArrayList.addAll(mDBManager.selectAll());
		
		for(int  i =0; i < 20; i++) 
			mArrayList.add(new ScheduleEntry(String.valueOf(i), String.valueOf(i), String.valueOf(i)));
		
		mAdapter.reSizeList(); 
		//mAdapter.notifyDataSetChanged();

		// 리스트 아무것도 없을시 설명
		if (mArrayList.size() == 0)
			setViewVisible(false, true);
		else
			setViewVisible(true, false);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		mSearchView.setQueryHint("검색어를 입력해주세요");
		mSearchView.setOnQueryTextListener(getQueryTextListener);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		if (null != searchManager) {
			mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		}
		
		mSearchView.setOnSearchClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mToggle.setDrawerIndicatorEnabled(false);
				
			}
		});
		

		mSearchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() { // 검색 닫기 버튼
				mToggle.setDrawerIndicatorEnabled(true);
				if (mArrayList.size() == 0) // 리스트 없을때
					setViewVisible(false, true); // 메모함 없습니다.
				else
					setViewVisible(true, false);
				return false;
			}
		});
		// 검색필드를 항상 표시하고싶을 경우false, 아이콘으로 보이고 싶을 경우 true
		mSearchView.setIconifiedByDefault(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (mToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case android.R.id.home:
			mSearchView.setQuery("", false);
			mSearchView.setIconified(true);
			mToggle.setDrawerIndicatorEnabled(true);
			break;
		case R.id.action_settings:
			break;

		default:
			return false;
		}

		return true;
	}
	
	private void startDeleteActionMode() {
		View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.actionbar_delete_mode, null);
		
		final CheckBox mCheckBox = (CheckBox)view.findViewById(R.id.CheckSelectAll);
		mCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAdapter.setAllChecked(mCheckBox.isChecked());
			}
		});
		
		mActionMode = startSupportActionMode(new ActionMode.Callback() {
			
			@Override
			public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode arg0) {
				mActionMode.finish();
				mAdapter.setHasCheckBox(false, mListView.getLastVisiblePosition());
				mAdapter.notifyDataSetChanged();
				mHasPressMode = false;
				
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode arg0, Menu arg1) {
				arg0.getMenuInflater().inflate(R.menu.select_delete_menu, arg1);
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
				if (arg1.getItemId() == R.id.select_delete) {
					final ArrayList<ScheduleEntry> clone = (ArrayList<ScheduleEntry>) mArrayList.clone();
					final ArrayList<Integer> mCheckedList = mAdapter.getChecked();
					if (mCheckedList.size() > 0) {
						for (int i = mCheckedList.size()-1; i >= 0; i--) {
							mDBManager.removeData(mArrayList.get((int)mCheckedList.get(i)).getSSID());
							mArrayList.remove((int)mCheckedList.get(i));
						}
						mAdapter.reSizeList();

						if(mPopupWindow != null)
							mPopupWindow.dismiss();
							
						
						View popupView = getLayoutInflater().inflate(R.layout.dismiss_undo_item, null);
						TextView mTextView = (TextView)popupView.findViewById(R.id.dismissSizeText);
						mTextView.setText("현재 "+ mCheckedList.size() +"개를 삭제하였습니다.");
						
						popupView.findViewById(R.id.undo).setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								mArrayList.clear();
								mArrayList.addAll(clone);							
								mAdapter.reSizeList(); 
								dismissPopUp();
								
							}
						});
						
						mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
						mPopupWindow.setAnimationStyle(-1);
						mPopupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 100);
						
						
					//	Toast.makeText(MainActivity.this, "선택한 항목들이 삭제되었습니다.",Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MainActivity.this, "삭제할 항목을 선택해 주세요.",	Toast.LENGTH_SHORT).show();
					}
				}
				return false;
			}
		});
		mActionMode.setCustomView(view);
	}

	private void startActivityAnimate(Intent intent) {
		startActivityForResult(intent, nRequestCode);
		overridePendingTransition(R.anim.start_enter, R.anim.start_exit);
	}

	private void dismissPopUp(){
		if(mPopupWindow == null) 
			return;
		
		mPopupWindow.dismiss();
		mPopupWindow = null;
	}
	// / Get Listener

	// 플룻팅 버튼 리스너
	private OnClickListener getFloatingListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mFloatingButton.animate().translationY(500).setDuration(500).setListener(new AnimatorListenerAdapter() {				
				@Override
				public void onAnimationEnd(Animator animation)  {
					Intent mIntent = new Intent(MainActivity.this,	ContentActivity.class);
					mIntent.putExtra("STATE", "Create");
					startActivityAnimate(mIntent);
				}
			});
		}
	};

	// ListView 클릭
	private AdapterView.OnItemClickListener getItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,	long l_position) {
			if(!mAdapter.getHasCheckBox() && !mHasPressMode) {// 체크박스 비활성화
				Intent mIntent = new Intent(MainActivity.this,ContentActivity.class);
				mIntent.putExtra("STATE", "Modify");
				mIntent.putExtra("SsidPosition", mArrayList.get(position).getSSID()); // 헤데 추가시 position -1
				startActivityAnimate(mIntent);
			} else if (mAdapter.getHasCheckBox() && mHasPressMode) { // 체크박스 활성화, 한번 눌렀때 떗을경우 비활성화하기 한단계전
				mHasPressMode = false;
			} else if (mAdapter.getHasCheckBox() && !mHasPressMode) { // 체크박스 비활성화 위해서
				mActionMode.finish();
				dismissPopUp();
				mAdapter.setHasCheckBox(false, mListView.getLastVisiblePosition());
				mAdapter.notifyDataSetChanged();
				mHasPressMode = false;
				
			}
		}
	};
	
	private AdapterView.OnItemLongClickListener getItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (!mAdapter.getHasCheckBox() && !mHasPressMode) {
				mAdapter.reSizeList();
				startDeleteActionMode();
				mHasPressMode = true;
				mAdapter.setHasCheckBox(true,mListView.getLastVisiblePosition());
				mAdapter.notifyDataSetChanged();
			}
			return false;
		}

	};

	// 검색 창 리스너
	private OnQueryTextListener getQueryTextListener = new OnQueryTextListener() {
		@Override
		public boolean onQueryTextSubmit(String query) {
			setViewVisible(true, false);
			mArrayList.clear();
			mArrayList.addAll(mDBManager.searchData(query));
			mAdapter.notifyDataSetChanged();

			// 키보드 설정
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchView.getWindowToken(),
					0);
			mSearchView.setQuery(query, false);
			return false;
		}

		@Override
		public boolean onQueryTextChange(String newText) { // collapseActionView 사용시 무조껀 호출
			
			ArrayList<ScheduleEntry> infos = mDBManager.searchData(newText);
			if (infos.size() > 0) { // 검색 대상이 있을시에만
				setViewVisible(true, false);
				mArrayList.clear();
				mArrayList.addAll(infos);
				mAdapter.notifyDataSetChanged();
			} else {
				setViewVisible(false, false);
			}
			return false;
		}
		
	};
}
