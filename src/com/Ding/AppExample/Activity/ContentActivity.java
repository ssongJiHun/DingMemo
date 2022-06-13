package com.Ding.AppExample.Activity;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Ding.AppExample.R;
import com.Ding.AppExample.Class.DBManager;
import com.Ding.AppExample.Info.ScheduleEntry;

public class ContentActivity extends ActionBarActivity{
	private Toolbar mToolbar;
	private EditText mEditText;
	private DBManager mDBManager;
	private int nSsidCode = 0;
	private boolean bModify = false;

	@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_memo);
			// Toolbar Setting
			mToolbar = (Toolbar)findViewById(R.id.toolbar);
			setSupportActionBar(mToolbar);

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setElevation(25);
			getSupportActionBar().setTitle("메모하기");

			mDBManager = new DBManager(ContentActivity.this);

			// getEditText
			mEditText = (EditText)findViewById(R.id.editText1);


			// 데이터 받아오기 수정, 새로 쓰기
			Intent mIntent = getIntent();
			if (mIntent.getStringExtra("STATE").equals("Modify")){
				bModify = true;
				nSsidCode = mIntent.getIntExtra("SsidPosition", -1);
				mEditText.setText(mDBManager.selectData(nSsidCode).getContent());
			}
			else { // 새로 쓰기 일 경우에만 키보드를 보이게한다.
				// 키보드 설정
				new Handler().postDelayed(new Runnable(){
					@Override
					public void run() {
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
					}
				}, 100);
			}



			// Date setting

			TextView mDateText = (TextView)findViewById(R.id.textDate);
			SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN);
			String DateYear = CurDateFormat.format(new Date(System.currentTimeMillis())).toString();
			String DateTime = CurDateFormat.getTimeInstance(2, Locale.KOREAN).format(new java.util.Date()).toString();
			mDateText.setText(DateYear + " " + DateTime);
		}

	private void ContentSave() {
		Intent mIntent = getIntent();
		mIntent.putExtra("CreateState", true);
		setResult(Activity.RESULT_OK, mIntent);

		SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy.MM.dd");
		ScheduleEntry mScheduleEntry = new ScheduleEntry(mEditText.getText().toString(), "#301b92", CurDateFormat.format(new Date(System.currentTimeMillis())));
		// 데이터 파일 
		if (!bModify)
			mDBManager.insertData(mScheduleEntry);
		else
			mDBManager.updateData(mScheduleEntry, nSsidCode);
		Toast.makeText(ContentActivity.this, "저장 완료", Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.content_menu, menu);
			return true;
		}

	@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.action_save:
				if (mEditText.getText().length() > 0)
					ContentSave();
				else
					Toast.makeText(ContentActivity.this, "입력해주세요", Toast.LENGTH_SHORT).show();

				break;

			default:
				return false;
			}

			return true;
		}

	@Override
		public void finish() {
			super.finish();
			overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
		}
}
