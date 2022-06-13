package com.Ding.AppExample.Info;

public class ScheduleEntry {
	private String mContent;
	private String mColor;
	private String mDate;
	private int mSSID;
	
	// 추상적 생성용 생성시에는 프라이머리 키는 안써도 되기 떄문에
	public ScheduleEntry(String content, String color, String date){
		mContent = content;
		mColor = color;
		mDate = date;
	}
	
	// 불러올때 데이터 찾기 할때는 첫번째 변수로 프라이머리키 호출
	public ScheduleEntry(int ssid, String content, String color, String date){
		mSSID = ssid;
		mContent = content;
		mColor = color;
		mDate = date;
	}
	
	public void setContent(String content) {
		mContent = content;
	}
	
	public void setColor(String color) {
		mColor = color;
	}
	
	public void setDate(String date) {
		mDate = date;
	}
	
	public int getSSID(){
		return mSSID;
	}
	
	public String getContent(){
		return mContent;
	}
	
	public String getColor() {
		return mColor;
	}
	
	public String getDate() {
		return mDate;
	}
	

}
