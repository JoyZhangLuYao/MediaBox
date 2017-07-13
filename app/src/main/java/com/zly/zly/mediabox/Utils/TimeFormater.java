package com.zly.zly.mediabox.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeFormater {
	public static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.US);
	public static final SimpleDateFormat yyyyMMddHHmm = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm", Locale.US);
	public static final SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.US);

	public static String formatYMD(long time) {
		return formatYMD(new Date(time));
	}

	public static String formatYMD(Date date) {
		return yyyyMMdd.format(date);
	}

	public static String formatYMD(Calendar calendar) {
		return formatYMD(calendar.getTime());
	}

	public static String formatYMDHM(long time) {
		return formatYMDHM(new Date(time));
	}

	public static String formatYMDHM(Date date) {
		return yyyyMMddHHmm.format(date);
	}

	public static String formatYMDHM(Calendar calendar) {
		return formatYMDHM(calendar.getTime());
	}

	public static String formatYMDHMS(long time) {
		return formatYMDHMS(new Date(time));
	}

	public static String formatYMDHMS(Date date) {
		return yyyyMMddHHmmss.format(date);
	}

	public static String formatYMDHMS(Calendar calendar) {
		return formatYMDHMS(calendar.getTime());
	}
}
