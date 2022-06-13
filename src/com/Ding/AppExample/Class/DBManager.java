package com.Ding.AppExample.Class;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.Ding.AppExample.Info.ScheduleEntry;

public class DBManager {

	// DB관련 상수 선언
	private static final String dbName = "ScheduleEntry.db";
	private static final String tableName = "ScheduleEntry";
	public static final int dbVersion = 1;

	// DB관련 객체 선언
	private OpenHelper opener; // DB opener
	private SQLiteDatabase db; // DB controller

	// 부가적인 객체들
	private Context context;

	// 생성자
	public DBManager(Context context) {
		this.context = context;
		this.opener = new OpenHelper(context, dbName, null, dbVersion);
		db = opener.getWritableDatabase();
	}

	// Opener of DB and Table
	private class OpenHelper extends SQLiteOpenHelper {

		public OpenHelper(Context context, String name, CursorFactory factory,
		int version) {
			super(context, name, null, version);
			// TODO Auto-generated constructor stub
		}

		// 생성된 DB가 없을 경우에 한번만 호출됨
		@Override
			public void onCreate(SQLiteDatabase arg0) {
				// String dropSql = "drop table if exists " + tableName;
				// db.execSQL(dropSql);

				String createSql = "create table " + tableName + " ("
					+ "id integer primary key autoincrement, "
					+ "Content text, "
					+ "Color text, "
					+ "Date text)";
				arg0.execSQL(createSql);
			}

		@Override
			public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
			}
	}

	// 데이터 추가
	public void insertData(ScheduleEntry info) {
		ContentValues mContentValues = new ContentValues();
		mContentValues.put("Content", info.getContent());
		mContentValues.put("Color", info.getColor());
		mContentValues.put("Date", info.getDate());
		db.insert(tableName, null, mContentValues);
	}

	// 데이터 검색
	public ArrayList<ScheduleEntry> searchData(String data) {
		// SELECT 필드1 FROM 테이블 WHERE 필드2 LIKE '%단어%';
		String sql = "select * from " + tableName + " where Content like '%" + data + "%'";
		Cursor results = db.rawQuery(sql, null);


		ArrayList<ScheduleEntry> infos = new ArrayList<ScheduleEntry>();

		if (results.moveToFirst()) {
			while (!results.isAfterLast()) {
				ScheduleEntry info = new ScheduleEntry(results.getInt(0), results.getString(1), results.getString(2), results.getString(3));
				infos.add(info);
				results.moveToNext();
			}
		}
		results.close();
		return infos;
	}

	// 데이터 갱신
	public void updateData(ScheduleEntry info, int index) {
		ContentValues mContentValues = new ContentValues();
		mContentValues.put("Content", info.getContent());
		mContentValues.put("Color", info.getColor());
		mContentValues.put("Date", info.getDate());

		db.update(tableName, mContentValues, "id = ?", new String[]{String.valueOf(index)});
	}

	// 데이터 삭제
	public void removeData(int index) {
		String sql = "delete from " + tableName + " where id = " + index + ";";
		db.execSQL(sql);
	}

	public ScheduleEntry selectData(int index) {
		String sql = "select * from " + tableName + " where id = " + index
			+ ";";
		Cursor result = db.rawQuery(sql, null);

		// result(Cursor 객체)가 비어 있으면 false 리턴  1부터 get 0은 프라이머리 키
		if (result.moveToFirst()) {
			ScheduleEntry info = new ScheduleEntry(result.getInt(0), result.getString(1), result.getString(2), result.getString(3));
			result.close();
			return info;
		}
		result.close();
		return null;
	}

	// 데이터 전체 검색
	public ArrayList<ScheduleEntry> selectAll() {
		String sql = "select * from " + tableName + ";";
		Cursor results = db.rawQuery(sql, null);

		results.moveToFirst();
		ArrayList<ScheduleEntry> infos = new ArrayList<ScheduleEntry>();

		while (!results.isAfterLast()) {
			ScheduleEntry info = new ScheduleEntry(results.getInt(0), results.getString(1), results.getString(2), results.getString(3));
			infos.add(info);
			results.moveToNext();
		}
		results.close();
		return infos;
	}

}
