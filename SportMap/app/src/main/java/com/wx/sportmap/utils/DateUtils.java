package com.wx.sportmap.utils;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lin on 2017/6/22.
 */

public class DateUtils {

	public static String getMinBySeconds(String seconds){
		if(TextUtils.isEmpty(seconds)){
			return seconds;
		}
		int intSeconds = Integer.parseInt(seconds);
		int getMin = intSeconds / 60;
		return String.valueOf(getMin);
	}
	public static String getMinBySecondsByInt(int seconds){
		int getSecond = seconds / 60;
		return String.valueOf(getSecond);
	}

	public static String getCurrentTime() {
		// 用于上传给服务器的格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = format.format(new Date());
		return strDate;
	}


	public static String getCurrentTime(long time) {
		// 用于上传给服务器的格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = format.format(time);
		return strDate;
	}

	public static String getMinutesByMills(long changeMillis) {
		int hour = (int) ((changeMillis / 1000 / 60) / 60);
		DateFormat formatter = new SimpleDateFormat("m");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(changeMillis);
		String wholeMin = String.valueOf( hour * 60 + Integer.parseInt(formatter.format(calendar.getTime())));
		return wholeMin;
	}

	public static String getReSyncItemStartTime(long startTime){
		SimpleDateFormat format = new SimpleDateFormat("M.dd HH:mm");
		String strDate = format.format(startTime);
		return strDate;
	}

	// yyyy-MM-dd HH:mm:ss --> yyyy-MM-dd
	public static String changeFormat4(String before) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat format = new SimpleDateFormat("M.d HH:mm");
		long millionSeconds = 1L;
		try {
			millionSeconds = sdf.parse(before).getTime();
			String result = format.format(millionSeconds);
			return result;
		} catch (ParseException e) {
			e.printStackTrace();
		}// 毫秒
		return null;
	}

	/**
	 * 得到现在时间与2017-01-01 00:00:00的秒数差
	 * （字节序是倒序，小端模式）
	 */
	public static byte[] judgeTimeDivide(){
		long diff = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d1 = df.parse("2017-01-01 00:00:00");
			diff = new Date().getTime() - d1.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(diff != 0){
			diff /= 1000;
		}
		byte bHighFront = (byte) (diff >> 24);
		byte bHighBehind = (byte) (diff >> 16);
		byte bLowFront = (byte) (diff >> 8);
		byte bLowBehind = (byte) (diff >> 0);
		byte[] timeBs = new byte[]{bLowBehind,bLowFront,bHighBehind,bHighFront};
		return timeBs;
	}


	public static String getCurrentTimeByTime() {
		// 用于上传给服务器的格式
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
		String strDate = format.format(new Date());
		return strDate;
	}

	public static String getNowTime() {
		// 用于上传给服务器的格式
		SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm");
		String strDate = format.format(new Date());
		return strDate;
	}

	public static String getCurrentTimeByDay() {
		// 用于判别是否为同一天的格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = format.format(new Date());
		return strDate;
	}

	/**
	 * 秒转换为时间
	 * @return 00:00:00
	 */
	public static String getNowTime(int time) {
		StringBuffer buffer = new StringBuffer();
		if (time == 0) {
			buffer.append("00:00:00");
		}else if (time < 60) {//1分钟内
			if (time <10) {
				buffer.append("00:00:").append("0"+time);
			}else {
				buffer.append("00:00:").append(time);
			}
		}else if (time < 3600 && time >= 60 ) {//1小时内
			buffer.append("00:");
			int second = time/60;
			if (second <10) {
				buffer.append("0"+second+":");
			}else {
				buffer.append(second+":");
			}
			int miao = time%60;
			if (miao <10) {
				buffer.append("0"+miao);
			}else {
				buffer.append(miao);
			}
		}else if (time < 3600*24 && time >= 3600) {//24小时内
			int hour = time/3600;
			if (hour <10) {
				buffer.append("0"+hour+":");
			}else {
				buffer.append(hour+":");
			}
			int second = (time-hour*3600)/60;
			if (second <10) {
				buffer.append("0"+second+":");
			}else {
				buffer.append(second+":");
			}
			int miao = (time-hour*3600)%60;
			if (miao <10) {
				buffer.append("0"+miao);
			}else {
				buffer.append(miao);
			}
		}else {
			buffer.append("时间过长");
		}
		return buffer.toString();
	}
	/**
	 * 两个时间之间的秒
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getSs(String date1, String date2) {
		if (date1 == null || date1.equals(""))
			return 0;
		if (date2 == null || date2.equals(""))
			return 0;
		// 转换为标准时间
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
		} catch (Exception e) {
		}
		long day = (date.getTime() - mydate.getTime()) / (1000);
		return day;
	}

	private static StringBuilder sb;

	public static String integerDateStr(int year, int month, int day){
		if(sb == null){
			sb = new StringBuilder();
		}
		sb.setLength(0);
		sb.append(String.valueOf(year));
		String monthStr = String.valueOf(month);
		String dayStr = String.valueOf(day);
		if(!TextUtils.isEmpty(monthStr) && monthStr.length() == 1){
			sb.append("0");
		}
		sb.append(String.valueOf(month));
		if(!TextUtils.isEmpty(dayStr) && monthStr.length() == 1){
			sb.append("0");
		}
		sb.append(dayStr);
		return sb.toString();
	}

}
